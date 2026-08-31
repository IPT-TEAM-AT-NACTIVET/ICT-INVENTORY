# ICT Inventory System — NACTVET Organisation Structure Migration Report

**Date:** 2026-08-29
**Scope:** Replace the legacy "Department"-era organisation model with the official NACTVET structure (Directorates → Sections, independent Units, typed Offices (Zanzibar/Zone) as parents of Zones) while preserving all users and assets.

---

## 1. Before / After snapshot

### Tables (row counts)

| Table        | Before | After | Notes |
|--------------|--------|-------|-------|
| users        | 7      | 7     | preserved, remapped onto new structure |
| assets       | 4      | 4     | preserved, remapped via their user |
| departments  | 2      | **dropped** | legacy rows discarded |
| directorates | —      | 4     | exact NACTVET directorates |
| sections     | —      | 8     | 2 per directorate, spec names |
| units        | 8      | 6     | legacy unit rows removed, users re-pointed |
| offices      | 2      | 2     | legacy sample rooms replaced by typed offices |
| zones        | 9      | 9     | preserved; office affiliation added |

### Seeded NACTVET master data

**Directorates (4):**
1. Institutional Operations
2. Admission, Examination and Certification
3. Quality Assurance
4. Corporate Services

**Sections (8, 2 per directorate, names verbatim from NACTVET spec):**
- Student Records and Registration · Learning and Teaching Infrastructure (D1)
- Admissions · Examination and Certification (D2)
- Internal Quality Assurance and Audits · Standards, Compliance and Regulatory Support (D3)
- Finance and Accounts · Human Resources and Administration (D4)

**Units (6):** Communications and Marketing, Finance and Accounts, Procurement Management, Internal Audit, Information and Communication Technology, Legal Services.

**Offices (2, typed):** Zanzibar Office (ZANZIBAR), Zone Office (ZONE).

**Zones (9, preserved):** Head Office, Eastern, Central, Northern, Zanzibar, Western, Lake, Southern Highlands, Southern Zone Offices.
- Head Office → **Zanzibar Office**; all other zones → **Zone Office**.

### Data mapping applied

| Legacy data | Disposition |
|-------------|-------------|
| Legacy department rows (2) | Dropped with the `departments` table |
| Legacy units "Software Development" / "Software Dev" | Re-pointed users to unit "Information and Communication Technology" (id 7), then deleted |
| Legacy sample offices "Office Room 101" / "Room 101" | Users detached (`office_id → NULL`), offices deleted pre-Hibernate |
| Staff (users with role STAFF) | Assigned to **Corporate Services** directorate |
| Staff with a zone | `office_id` filled from their zone's office (auto-derivation) |
| Admin (id 1) | Untouched |
| All 4 assets | Untouched; their directorate/office display resolves through their assigned user |

---

## 2. Schema changes

**Removed (via `src/main/resources/schema.sql`, which runs *before* Hibernate — `spring.sql.init.mode: always`):**
- `users.department_id`
- `units.department_id`
- `offices.zone_id`
- `departments` table + `departments_id_seq`
- Legacy office rows (after detaching referencing users) so Hibernate can safely add the **NOT NULL** `offices.office_type`

**Added (Hibernate `ddl-auto: update`):**
- `directorates` table
- `sections` table `(directorate_id → directorates.id)`
- `users.directorate_id`, `users.section_id` (FK, nullable)
- `zones.office_id` (FK, nullable)
- `offices.office_type varchar(255) NOT NULL` with `CHECK` constraint for `ZANZIBAR`/`ZONE`

### Why schema.sql deletes the offices pre-Hibernate
Hibernate's `update` cannot add a `NOT NULL` column to a table that still contains rows (`column "office_type" of relation "offices" contains null values`). Deleting the legacy sample offices in `schema.sql` first lets the column addition succeed on an empty table.

---

## 3. Backend changes

- **Entities:** `Directorate`, `Section` (new); `OfficeType` enum (`ZANZIBAR`, `ZONE`); `User` gains `directorate`/`section`; `Office` loses `zone` gains required `officeType`; `Zone` gains nullable `office`. `Department` deleted.
- **DTOs/services/controllers:** Directorate/Section CRUD under `/admin/directorates` and `/admin/sections` (±`/directorate/{id}`, `/office/{id}` variants elsewhere); Unit/Office/Zone requests updated (Unit is standalone, Office carries `officeType`, Zone carries `officeId`); Staff/Profile/Asset `department*` fields renamed to `directorate*`/`section*`; delete-protection for units/sections via usage counts.
- **Reports & dashboard:** `ReportController` now `/by-directorate`, `/by-section` (plus zone/device-type/status/inventory/filtered/CSV); filter params `directorateId`/`sectionId`; dashboard aggregates `assetsByDirectorate` and `assetsBySection`.
- **Reference API:** `/reference/directorates`, `/reference/sections?directorateId=`, `/reference/units`, `/reference/zones?officeId=`, `/reference/offices`, `/reference/device-types`.
- **Startup seeder:** `config/MasterDataInitializer.java` (`ApplicationRunner`, lowest precedence) idempotently seeds the NACTVET structure and performs the legacy→new mapping listed in §1.
- **DB access note:** PostgreSQL at `jdbc:postgresql://localhost:5439/ict_inventory`.

---

## 4. Frontend changes

- **Models** (`master-data.model.ts`, `asset.model.ts`, `staff.model.ts`, `profile.model.ts`, `dashboard.model.ts`): `Department` → `Directorate` + `Section`; `Unit` flattened; `Office` gains `officeType`; `Zone` gains `officeId/officeName`; dashboard/section aggregates renamed.
- **Services** (`reference`, `master-data`, `report`): updated to the new endpoint surface; removed department-linked helpers (`getUnitsByDepartment`, `getOfficesByZone`, `by-department`).
- **Master-data pages:** "Departments" → **Directorates** page; new **Sections** page (directorate-dependent); Offices page edits take an office **type** (Zanzibar/Zone); Zones page can associate an office; Units page is flat.
- **Forms** (staff-management, staff setup wizard, profile): Directorate → Section dependent select (mirroring the old Department → Unit pattern), flat Unit / Zone / Office selects.
- **Admin UI:** routes/menu now expose Directorates and Sections; inventory, verification, reports, dashboard and staff asset pages show Directorates/Sections.
- Build: `ng build` succeeds.

---

## 5. Issues encountered & resolutions

| # | Issue | Resolution |
|---|-------|------------|
| 1 | Hibernate cannot add `offices.office_type NOT NULL` while legacy rows exist | Detach users + delete legacy sample offices in `schema.sql` (pre-Hibernate), then add the column |
| 2 | Seeder `DELETE FROM offices` failed with FK violation (`users.office_id`) | Handled by the `schema.sql` detach-then-delete (issue 1) |
| 3 | Stale `Department*.class` leftovers in `target/classes` (no `clean`) got packaged into the jar; Hibernate `update` then recreated an *empty* `departments` table each start | `mvn clean package` → jar contains no Department classes; verified `to_regclass('public.departments')` is now `NULL` |
| 4 | E2E marker mismatch on verification/reports (uppercase CSS text) | Made Puppeteer harness matching case-insensitive |

---

## 6. Verification results

- **Backend build/tests:** `mvn -B clean package` BUILD SUCCESS; `mvn -B test` 1/1 passed.
- **Backend migration startup:** all 8 seeder steps completed (`directorates`, `sections`, `users-unit-repoint`, `units`, `offices`, `zones`, `users-directorate`, `users-office`; "NACTVET master data is in place").
- **Schema checks (PostgreSQL):** `directorates=4 sections=8 units=6 offices=2 zones=9 users=7 assets=4`; `departments` gone; `users.department_id` gone; `users.directorate_id/section_id/office_id` FK'd; `offices.office_type NOT NULL` + `CHECK` present.
- **API smoke tests:** login OK; `/reference/directorates`, `/sections`, `/units`, `/offices`, `/zones` return the new structure; dashboard aggregates `assetsByDirectorate: {Corporate Services: 4}`; staff list carries `directorateName`; asset list filters by `directorateId` and resolves `officeName: Zanzibar Office`; report `by-directorate` returns Corporate Services (4 assets).
- **E2E (Puppeteer, admin/admin123):** 14/14 checks passed, no JS console errors — login, dashboard, and every admin page (users, directorates, sections, units, zones, offices, device-types, inventory, verification, reports) load with data and no `Loading…`.
- **Functional UI checks:** 4 directorates, 8 sections, typed offices, 9 zones, by-directorate report, staff list, and inventory all render the migrated NACTVET data.

**Final state:** legacy Department model fully removed (schema + code + frontend); NACTVET master data seeded; all 7 users and 4 assets preserved and mapped to the new organisation.