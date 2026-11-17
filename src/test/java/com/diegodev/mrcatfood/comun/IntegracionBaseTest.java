package com.diegodev.mrcatfood.comun;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class IntegracionBaseTest extends BaseIntegracionTest {

    @Autowired private DataSource fuenteDatos;

    @Test
    void levantaPostgresEfimeroConLasMigracionesAplicadas() throws Exception {
        try (var conexion = fuenteDatos.getConnection()) {
            assertThat(conexion.getMetaData().getURL())
                    .contains("jdbc:postgresql://")
                    .doesNotContain("mydatabase");
        }

        JdbcTemplate jdbc = new JdbcTemplate(fuenteDatos);
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from flyway_schema_history where success = true",
                                Integer.class))
                .isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from usuario", Integer.class)).isOne();
    }
}
