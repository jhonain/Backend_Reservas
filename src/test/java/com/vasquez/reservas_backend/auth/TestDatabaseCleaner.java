package com.vasquez.reservas_backend.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public TestDatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void limpiar() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        truncarSiExiste("REFRESHTOKENS");
        truncarSiExiste("RESERVAS");
        truncarSiExiste("DISPONIBILIDADES");
        truncarSiExiste("SERVICIOS");
        truncarSiExiste("USUARIOS");

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private void truncarSiExiste(String tabla) {
        Integer existe = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND TABLE_NAME = ?
                """,
                Integer.class,
                tabla
        );

        if (existe != null && existe > 0) {
            jdbcTemplate.execute(
                    "TRUNCATE TABLE PUBLIC." + tabla + " RESTART IDENTITY"
            );
        }
    }
}