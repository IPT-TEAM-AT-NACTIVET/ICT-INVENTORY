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
-- Allow office to be NULL for infrastructure assets (servers, access points, etc.)
-- that may not belong to a specific office.
ALTER TABLE assets ALTER COLUMN office DROP NOT NULL;
@@
-- Sequence used to generate unique Employee IDs (NCT-EMP-00001, ...) in a
-- concurrency-safe manner. Created before Hibernate so it is present for
-- registration-time generation.
CREATE SEQUENCE IF NOT EXISTS employee_id_seq;
@@
-- Asset audit columns: created_by and updated_by reference the authenticated
-- user who registered/last modified the asset (not the associated user). Added
-- before Hibernate so the nullable FK columns exist for existing rows.
ALTER TABLE assets ADD COLUMN IF NOT EXISTS created_by BIGINT;
@@
ALTER TABLE assets ADD COLUMN IF NOT EXISTS updated_by BIGINT;
@@
CREATE INDEX IF NOT EXISTS idx_assets_created_by ON assets(created_by);
@@
CREATE INDEX IF NOT EXISTS idx_assets_updated_by ON assets(updated_by);
@@
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'assets_created_by_fkey') THEN
        ALTER TABLE assets ADD CONSTRAINT assets_created_by_fkey
            FOREIGN KEY (created_by) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'assets_updated_by_fkey') THEN
        ALTER TABLE assets ADD CONSTRAINT assets_updated_by_fkey
            FOREIGN KEY (updated_by) REFERENCES users(id);
    END IF;
END $$;
@@
-- Role model consolidated to single ADMIN role.
-- Handles all possible historical role values (ADMIN, STAFF, GENERAL_ADMIN,
-- ICT_OFFICER) and migrates them to ADMIN. Idempotent for fresh databases.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_role_check') THEN
        ALTER TABLE users DROP CONSTRAINT users_role_check;
    END IF;
    UPDATE users SET role = 'ADMIN' WHERE role IN ('STAFF', 'GENERAL_ADMIN', 'ICT_OFFICER', 'ADMIN');
END $$;
@@
-- Ensure the constraint exists for fresh databases that never had it.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_role_check') THEN
        ALTER TABLE users ADD CONSTRAINT users_role_check
            CHECK (role IN ('ADMIN'));
    END IF;
END $$;
@@
-- Assets: replace the FK to the system users table (user_id) with a free-text
-- "user of asset" full name entered by the ICT Officer. The person using the
-- asset need not have a system account, so there is NO foreign key here.
-- Backfill user_of_asset from the previously assigned account's full name, then
-- drop the now-unused user_id index/FK/column. Idempotent and non-destructive.
ALTER TABLE assets ADD COLUMN IF NOT EXISTS user_of_asset VARCHAR(255);
@@
-- Backfill user_of_asset from the previously assigned user's full name.
-- Guarded by a column-existence check so this runs only on the first migration
-- (once user_id is dropped on a later startup the UPDATE is a no-op). Idempotent.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'assets'
          AND column_name = 'user_id'
    ) THEN
        UPDATE assets a SET user_of_asset = u.full_name
        FROM users u
        WHERE a.user_id = u.id AND a.user_of_asset IS NULL AND a.user_id IS NOT NULL;
    END IF;
END $$;
@@
-- Assets where older rows were backfilled with the 'UNASSIGNED' placeholder are
-- left as-is, but the column is now nullable so infrastructure assets (servers,
-- access points, etc.) can be recorded without a direct user. Idempotent.
ALTER TABLE assets ALTER COLUMN user_of_asset DROP NOT NULL;
@@
-- Drop the old user_id index (already removed from the entity).
DROP INDEX IF EXISTS idx_assets_user_id;
@@
-- Drop any foreign key constraint that pointed from assets.user_id to users(id),
-- then remove the unused user_id column. Postgres drops dependent constraints
-- automatically when the column is dropped.
DO $$
DECLARE c record;
BEGIN
    FOR c IN
        SELECT conname, conkey FROM pg_constraint
        WHERE conrelid = 'assets'::regclass AND contype = 'f'
    LOOP
        IF EXISTS (
            SELECT 1 FROM pg_attribute
            WHERE attrelid = 'assets'::regclass
              AND attname = 'user_id'
              AND attnum = ANY (c.conkey)
        ) THEN
            EXECUTE format('ALTER TABLE assets DROP CONSTRAINT %I', c.conname);
        END IF;
    END LOOP;
END $$;
@@
ALTER TABLE assets DROP COLUMN IF EXISTS user_id;
@@

-- Approval audit columns: tracks which ADMIN approved a pending user and when.
ALTER TABLE users ADD COLUMN IF NOT EXISTS approved_by BIGINT;
@@
ALTER TABLE users ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;
@@
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_approved_by_fkey') THEN
        ALTER TABLE users ADD CONSTRAINT users_approved_by_fkey
            FOREIGN KEY (approved_by) REFERENCES users(id);
    END IF;
END $$;
@@
CREATE INDEX IF NOT EXISTS idx_users_approved_by ON users(approved_by);
@@