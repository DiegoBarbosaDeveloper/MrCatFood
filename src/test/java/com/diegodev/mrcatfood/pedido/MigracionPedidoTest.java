package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class MigracionPedidoTest {

    @Test
    void creaPedidosConCopiasHistoricasYRestriccionesDeIntegridad() {
        var fuenteDatos =
                new DriverManagerDataSource(
                        "jdbc:h2:mem:migracion_pedido;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                        "sa",
                        "");
        var jdbc = new JdbcTemplate(fuenteDatos);
        jdbc.execute("CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE");
        jdbc.execute(
                "CREATE TABLE usuario ("
                        + "id BIGSERIAL PRIMARY KEY, nombre VARCHAR(120) NOT NULL, "
                        + "email VARCHAR(180) NOT NULL, telefono VARCHAR(30) NOT NULL, "
                        + "password_hash VARCHAR(100) NOT NULL, rol VARCHAR(20) NOT NULL, "
                        + "conjunto_residencial VARCHAR(120), apartamento VARCHAR(60))");
        jdbc.execute("CREATE TABLE producto (id BIGSERIAL PRIMARY KEY)");
        jdbc.update("INSERT INTO producto DEFAULT VALUES");

        var resultado =
                Flyway.configure()
                        .dataSource(fuenteDatos)
                        .locations("classpath:db/migration")
                        .baselineOnMigrate(true)
                        .baselineVersion("2")
                        .load()
                        .migrate();

        assertThat(resultado.migrationsExecuted).isEqualTo(1);

        long clienteId = crearCliente(jdbc);
        long pedidoId = crearPedido(jdbc, clienteId);
        crearDetalle(jdbc, pedidoId, 1L);

        var pedido =
                jdbc.queryForMap(
                        "SELECT conjunto_residencial, apartamento, subtotal, total, estado "
                                + "FROM pedido WHERE id = ?",
                        pedidoId);
        assertThat(pedido)
                .containsEntry("conjunto_residencial", "Conjunto Los Gatos")
                .containsEntry("apartamento", "Torre 2 - 301")
                .containsEntry("estado", "PENDIENTE");
        assertThat(pedido.get("subtotal").toString()).isEqualTo("30000.00");
        assertThat(pedido.get("total").toString()).isEqualTo("30000.00");

        var detalle =
                jdbc.queryForMap(
                        "SELECT nombre_producto, precio_unitario, cantidad, subtotal "
                                + "FROM detalle_pedido WHERE pedido_id = ?",
                        pedidoId);
        assertThat(detalle)
                .containsEntry("nombre_producto", "Sándwich de pollo")
                .containsEntry("cantidad", 2);
        assertThat(detalle.get("precio_unitario").toString()).isEqualTo("15000.00");
        assertThat(detalle.get("subtotal").toString()).isEqualTo("30000.00");

        jdbc.update("DELETE FROM producto WHERE id = ?", 1L);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT producto_id FROM detalle_pedido WHERE pedido_id = ?",
                                Long.class,
                                pedidoId))
                .isNull();

        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "UPDATE detalle_pedido SET cantidad = 0 WHERE pedido_id = ?",
                                        pedidoId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "UPDATE pedido SET estado = 'DESCONOCIDO' WHERE id = ?",
                                        pedidoId))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update("DELETE FROM pedido WHERE id = ?", pedidoId);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT COUNT(*) FROM detalle_pedido WHERE pedido_id = ?",
                                Integer.class,
                                pedidoId))
                .isZero();
    }

    private long crearCliente(JdbcTemplate jdbc) {
        jdbc.update(
                "INSERT INTO usuario "
                        + "(nombre, email, telefono, password_hash, rol, conjunto_residencial, apartamento) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                "Cliente Pedido",
                "pedido@example.com",
                "3001234567",
                "hash",
                "CLIENTE",
                "Conjunto Los Gatos",
                "Torre 2 - 301");
        return jdbc.queryForObject(
                "SELECT id FROM usuario WHERE email = ?", Long.class, "pedido@example.com");
    }

    private long crearPedido(JdbcTemplate jdbc, long clienteId) {
        jdbc.update(
                "INSERT INTO pedido "
                        + "(codigo, cliente_id, conjunto_residencial, apartamento, fecha_entrega, "
                        + "hora_entrega, notas, subtotal, total, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "MRC-2026-000001",
                clienteId,
                "Conjunto Los Gatos",
                "Torre 2 - 301",
                LocalDate.of(2026, 11, 17),
                LocalTime.of(18, 30),
                "Sin salsas",
                new BigDecimal("30000.00"),
                new BigDecimal("30000.00"),
                "PENDIENTE");
        return jdbc.queryForObject(
                "SELECT id FROM pedido WHERE codigo = ?", Long.class, "MRC-2026-000001");
    }

    private void crearDetalle(JdbcTemplate jdbc, long pedidoId, long productoId) {
        jdbc.update(
                "INSERT INTO detalle_pedido "
                        + "(pedido_id, producto_id, nombre_producto, precio_unitario, cantidad, subtotal) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                pedidoId,
                productoId,
                "Sándwich de pollo",
                new BigDecimal("15000.00"),
                2,
                new BigDecimal("30000.00"));
    }
}
