package tz.go.nactvet.ict_inventory_management.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Generates unique Employee IDs in a concurrency-safe manner using a database
 * sequence (employee_id_seq) rather than SELECT MAX(employee_id)+1.
 *
 * <p>The sequence guarantees uniqueness even when multiple staff register at
 * exactly the same time. The numeric value is formatted as NCT-EMP-00001,
 * NCT-EMP-00002, ...</p>
 */
@Component
public class EmployeeIdGenerator {

    private static final String SEQUENCE = "employee_id_seq";

    private final JdbcTemplate jdbcTemplate;

    public EmployeeIdGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String next() {
        Long next = jdbcTemplate.queryForObject(
                "SELECT nextval('" + SEQUENCE + "')", Long.class);
        if (next == null) {
            next = 1L;
        }
        return String.format("NCT-EMP-%05d", next);
    }
}
