package com.diegodev.mrcatfood.producto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class MigracionProductoTest {

    @Test
    void creaCatalogoDeEjemploConRestriccionesDeIntegridad() {
        var fuenteDatos =
                new DriverManagerDataSource(
                        "jdbc:h2:mem:migracion_producto;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                        "sa",
                        "");
        var jdbc = new JdbcTemplate(fuenteDatos);
        jdbc.execute("CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE");
        jdbc.execute("CREATE TABLE esquema_base (id INTEGER)");
        var resultado =
                Flyway.configure()
                        .dataSource(fuenteDatos)
                        .locations("classpath:db/migration")
                        .baselineOnMigrate(true)
                        .baselineVersion("1")
                        .load()
                        .migrate();

        assertThat(resultado.migrationsExecuted).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM producto", Integer.class))
                .isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM producto_ingrediente", Integer.class))
                .isEqualTo(21);

        Map<String, Object> productoAgotado =
                jdbc.queryForMap(
                        "SELECT nombre, precio, stock, activo, version FROM producto WHERE nombre = ?",
                        "Sándwich vegetariano");
        assertThat(productoAgotado)
                .containsEntry("nombre", "Sándwich vegetariano")
                .containsEntry("stock", 0)
                .containsEntry("activo", true)
                .containsEntry("version", 0L);
        assertThat(productoAgotado.get("precio").toString()).isEqualTo("13000.00");

        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "INSERT INTO producto (nombre, precio, stock) VALUES (?, ?, ?)",
                                        "Precio inválido",
                                        -1,
                                        1))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "INSERT INTO producto (nombre, precio, stock) VALUES (?, ?, ?)",
                                        "Stock inválido",
                                        1000,
                                        -1))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update("DELETE FROM producto WHERE id = ?", 1L);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT COUNT(*) FROM producto_ingrediente WHERE producto_id = ?",
                                Integer.class,
                                1L))
                .isZero();
    }
}
