package tz.go.nactvet.ict_inventory_management.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs after Hibernate has updated the schema (ddl-auto: update) and applies the
 * small fixes that Hibernate's schema generator cannot perform on existing tables,
 * such as dropping the NOT NULL constraint on {@code users.email}.
 *
 * <p>New staff accounts are created without an email (the staff member provides it
 * during their profile setup), so the email column must be nullable. Hibernate only
 * adds/updates columns forward and never relaxes a NOT NULL constraint on an already
 * existing column, hence this manual fix at startup.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaFixer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaFixer.class);

    private final DataSource dataSource;

    public SchemaFixer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        makeEmailColumnNullable();
    }

    private void makeEmailColumnNullable() {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            boolean columnExists;
            boolean nullable;

            try (ResultSet rs = connection.getMetaData().getColumns(null, null, "users", "email")) {
                columnExists = rs.next();
                nullable = false;
                if (columnExists) {
                    int nullability = rs.getInt("NULLABLE");
                    nullable = nullability == DatabaseMetaData.columnNullable
                            || nullability == DatabaseMetaData.columnNullableUnknown;
                }
            }

            if (!columnExists) {
                log.info("[SchemaFixer] users.email does not exist yet; nothing to fix.");
                return;
            }
            if (nullable) {
                log.info("[SchemaFixer] users.email is already nullable; nothing to fix.");
                return;
            }

            String ddl = product != null && product.toLowerCase().contains("h2")
                    ? "ALTER TABLE users ALTER COLUMN email NULL"
                    : "ALTER TABLE users ALTER COLUMN email DROP NOT NULL";

            try (Statement statement = connection.createStatement()) {
                statement.execute(ddl);
            }
            log.info("[SchemaFixer] Dropped NOT NULL constraint on users.email ({}).", ddl);
        } catch (Exception e) {
            log.error("[SchemaFixer] Failed to relax the NOT NULL constraint on users.email. "
                    + "Staff accounts cannot be created without an email until this is fixed. "
                    + "Run manually: ALTER TABLE users ALTER COLUMN email DROP NOT NULL;", e);
        }
    }
}