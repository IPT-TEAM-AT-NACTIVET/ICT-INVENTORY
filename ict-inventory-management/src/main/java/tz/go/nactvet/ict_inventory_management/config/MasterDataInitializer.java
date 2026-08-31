package tz.go.nactvet.ict_inventory_management.config;

import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs after Hibernate has applied the schema update and seeds / restructures the
 * master data to the official NACTVET organisational structure:
 *
 * <ul>
 *   <li>4 Directorates (each with 2 Sections)</li>
 *   <li>6 independent Units</li>
 *   <li>8 Zones (the official NACTVET zones)</li>
 *   <li>Offices are NOT seeded: office codes must come from the real NACTVET list
 *       and are entered by administrators.</li>
 * </ul>
 *
 * <p>Legacy master data (the old "ICT Department", the "Software Development"/
 * "Software Dev" units, the sample room offices, and the non-Council "Head Office"
 * zone) is removed while existing users and assets are preserved and re-mapped onto
 * the new structure. "Head Office" staff/assets are re-mapped to the Eastern Zone
 * Office (NACTVET headquarters is in Dar es Salaam, which falls under the Eastern
 * zone). All statements are idempotent.</p>
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MasterDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MasterDataInitializer.class);

    private static final String[] ZONES = {
            "Eastern Zone Office",
            "Central Zone Office",
            "Northern Zone Office",
            "Zanzibar Zone Office",
            "Western Zone Office",
            "Lake Zone Office",
            "Southern Highlands Zone Office",
            "Southern Zone Office"
    };

    private final DataSource dataSource;

    public MasterDataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (product == null || !product.toLowerCase().contains("postgresql")) {
                log.info("[MasterDataInitializer] Skipped (dialect={}).", product);
                return;
            }
            seed(connection);
        } catch (Exception e) {
            log.error("[MasterDataInitializer] Failed to seed NACTVET master data.", e);
        }
    }

    private void seed(Connection connection) throws Exception {
        Map<String, String> steps = new LinkedHashMap<>();
        steps.put("directorates", seedDirectorates());
        steps.put("sections", seedSections());
        steps.put("users-unit-repoint", repointLegacyUnits());
        steps.put("units", seedUnits());
        steps.put("zones", seedZones());
        steps.put("users-directorate", assignStaffDirectorate());

        try (Statement statement = connection.createStatement()) {
            for (Map.Entry<String, String> entry : steps.entrySet()) {
                for (String sql : entry.getValue().split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        statement.execute(trimmed);
                    }
                }
                log.info("[MasterDataInitializer] Step '{}' complete.", entry.getKey());
            }
        }
        log.info("[MasterDataInitializer] NACTVET master data is in place.");
    }

    private String seedDirectorates() {
        return """
                INSERT INTO directorates (id, name, code, description, created_at, updated_at) VALUES
                  (1, 'Institutional Operations', 'D1', NULL, now(), now()),
                  (2, 'Admission, Examination and Certification', 'D2', NULL, now(), now()),
                  (3, 'Quality Assurance', 'D3', NULL, now(), now()),
                  (4, 'Corporate Services', 'D4', NULL, now(), now())
                ON CONFLICT (id) DO NOTHING;
                SELECT setval(pg_get_serial_sequence('directorates', 'id'), (SELECT COALESCE(MAX(id), 1) FROM directorates))
                """;
    }

    private String seedSections() {
        return """
                INSERT INTO sections (id, name, code, description, directorate_id, created_at, updated_at) VALUES
                  (1, 'Student Records and Registration', 'S1', NULL, 1, now(), now()),
                  (2, 'Learning and Teaching Infrastructure', 'S2', NULL, 1, now(), now()),
                  (3, 'Admissions', 'S3', NULL, 2, now(), now()),
                  (4, 'Examination and Certification', 'S4', NULL, 2, now(), now()),
                  (5, 'Internal Quality Assurance and Audits', 'S5', NULL, 3, now(), now()),
                  (6, 'Standards, Compliance and Regulatory Support', 'S6', NULL, 3, now(), now()),
                  (7, 'Finance and Accounts', 'S7', NULL, 4, now(), now()),
                  (8, 'Human Resources and Administration', 'S8', NULL, 4, now(), now())
                ON CONFLICT (id) DO NOTHING;
                SELECT setval(pg_get_serial_sequence('sections', 'id'), (SELECT COALESCE(MAX(id), 1) FROM sections))
                """;
    }

    private String repointLegacyUnits() {
        return """
                UPDATE users u SET unit_id = (SELECT id FROM units WHERE name = 'Information and Communication Technology')
                WHERE u.unit_id IN (SELECT id FROM units WHERE name IN ('Software Development', 'Software Dev'));
                DELETE FROM units WHERE name IN ('Software Development', 'Software Dev')
                """;
    }

    private String seedUnits() {
        return """
                INSERT INTO units (name, code, description, created_at, updated_at) VALUES
                  ('Procurement Management', 'U1', NULL, now(), now()),
                  ('Internal Audit', 'U2', NULL, now(), now()),
                  ('Communications and Marketing', 'U3', NULL, now(), now()),
                  ('Legal Services', 'U4', NULL, now(), now()),
                  ('Information and Communication Technology', 'U5', NULL, now(), now()),
                  ('Finance and Accounts', 'U6', NULL, now(), now())
                ON CONFLICT (name) DO NOTHING
                """;
    }

    private String seedZones() {
        StringBuilder builder = new StringBuilder();
        for (String name : ZONES) {
            builder.append("INSERT INTO zones (name, description, status, created_at, updated_at) VALUES (")
                    .append("'").append(name).append("', NULL, 'ACTIVE', now(), now()) ON CONFLICT (name) DO NOTHING;");
        }
        builder.append("UPDATE users SET zone_id = (SELECT id FROM zones WHERE name = 'Eastern Zone Office')")
                .append(" WHERE zone_id IN (SELECT id FROM zones WHERE name = 'Head Office');");
        builder.append("DELETE FROM zones WHERE name = 'Head Office';");
        return builder.toString();
    }

    private String assignStaffDirectorate() {
        return """
                UPDATE users SET directorate_id = (SELECT id FROM directorates WHERE name = 'Corporate Services')
                WHERE role = 'STAFF' AND directorate_id IS NULL
                """;
    }
}