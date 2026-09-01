-- Runs before Hibernate's ddl-auto schema update (spring.sql.init.mode=always).
-- Migrates the schema to the final model (Office belongs to Zone, code-based).
-- Statement separator is "@@" (see application.yaml) to allow DO blocks.
-- Idempotent; safe on a fresh database.
@@
-- A zone no longer references an office; an office now belongs to a zone.
-- Must be dropped before the offices cleanup below because zones still reference
-- the placeholder offices.
ALTER TABLE zones DROP COLUMN IF EXISTS office_id;
@@
-- Legacy placeholder offices from the previous model are removed before Hibernate
-- adds the new NOT NULL zone_id/office_code columns. Guarded by the presence of the
-- legacy office_type column so this only runs during the first migration.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'offices' AND column_name = 'office_type') THEN
        UPDATE users SET office_id = NULL WHERE office_id IS NOT NULL;
        DELETE FROM offices
        WHERE office_type IN ('ZANZIBAR', 'ZONE')
           OR name IN ('Zanzibar Office', 'Zone Office', 'Office Room 101', 'Room 101');
    END IF;
END $$;
@@
-- Old Office fields (no longer part of the model: offices are identified by code).
ALTER TABLE offices DROP COLUMN IF EXISTS name;
@@
ALTER TABLE offices DROP COLUMN IF EXISTS room_number;
@@
ALTER TABLE offices DROP COLUMN IF EXISTS office_type;
@@
ALTER TABLE offices DROP COLUMN IF EXISTS description;
@@
-- Status columns required by the new model (added before Hibernate so the NOT NULL
-- columns can be created safely on non-empty tables).
ALTER TABLE zones ADD COLUMN IF NOT EXISTS status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE';
@@
ALTER TABLE offices ADD COLUMN IF NOT EXISTS status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE';
@@
-- Legacy "Department"-era schema artefacts.
ALTER TABLE users DROP COLUMN IF EXISTS department_id;@@
ALTER TABLE units DROP COLUMN IF EXISTS department_id;
@@
DROP TABLE IF EXISTS departments;
@@
DROP SEQUENCE IF EXISTS departments_id_seq;
@@
-- Zone/Office no longer belong to a User (they moved to the Asset location).
-- ddl-auto:update cannot drop columns, so they are removed here explicitly.
DROP INDEX IF EXISTS idx_users_zone_id;
@@
DROP INDEX IF EXISTS idx_users_office_id;
@@
ALTER TABLE users DROP COLUMN IF EXISTS zone_id;
@@
ALTER TABLE users DROP COLUMN IF EXISTS office_id;
@@
-- Assets: Migrate from office_id (FK to offices table) to office (VARCHAR code).
-- Migrate existing office codes from the offices table to assets.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'assets' AND column_name = 'office_id') THEN
        -- Populate office from the offices table before dropping the FK
        UPDATE assets a SET office = o.office_code
        FROM offices o
        WHERE a.office_id = o.id AND a.office IS NULL;
        
        -- Set a default office for any remaining NULL values
        UPDATE assets SET office = 'UNASSIGNED' WHERE office IS NULL;
    END IF;
END $$;
@@
-- Drop the old index on office_id
DROP INDEX IF EXISTS idx_assets_office_id;
@@
-- Drop the old office_id foreign key constraint
ALTER TABLE assets DROP CONSTRAINT IF EXISTS assets_office_id_fkey;
@@
-- Drop the old office_id column
ALTER TABLE assets DROP COLUMN IF EXISTS office_id;
@@
-- Add the new office VARCHAR column if it doesn't exist (with NOT NULL constraint)
ALTER TABLE assets ADD COLUMN IF NOT EXISTS office VARCHAR(100) NOT NULL DEFAULT 'UNASSIGNED';
@@
-- Remove the default constraint after the column is added
ALTER TABLE assets ALTER COLUMN office DROP DEFAULT;
@@
-- Sequence used to generate unique Employee IDs (NCT-EMP-00001, ...) in a
-- concurrency-safe manner. Created before Hibernate so it is present for
-- registration-time generation.
CREATE SEQUENCE IF NOT EXISTS employee_id_seq;
@@