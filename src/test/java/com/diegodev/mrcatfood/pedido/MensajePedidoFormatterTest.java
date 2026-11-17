package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;

import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MensajePedidoFormatterTest {

    private final MensajePedidoFormatter formateador = new MensajePedidoFormatter();

    private Pedido crearPedido(LocalTime horaEntrega, String notas) {
        Usuario cliente =
                new Usuario("Ana Pérez", "ana@example.com", "3001234567", "hash", Rol.CLIENTE);
        cliente.actualizarUbicacion("Conjunto Los Gatos", "101");
        Pedido pedido =
                new Pedido(
                        "MRC-2026-000123",
                        cliente,
                        LocalDate.of(2026, 11, 17),
                        horaEntrega,
                        notas,
                        new BigDecimal("43000"),
                        new BigDecimal("43000"));
        pedido.agregarDetalle(null, "Sándwich de pollo", new BigDecimal("15000"), 2);
        pedido.agregarDetalle(null, "Sándwich vegetariano", new BigDecimal("13000"), 1);
        return pedido;
    }

    @Test
    void construyeElMensajeCompleto() {
        String mensaje = formateador.formatear(crearPedido(LocalTime.of(12, 30), "Sin cebolla"));

        assertThat(mensaje)
                .isEqualTo(
                        """
                        Nuevo pedido MrCatFood

                        Código: MRC-2026-000123
                        Cliente: Ana Pérez — 3001234567
                        Ubicación: Conjunto Los Gatos, Apto 101
                        Entrega: 17/11/2026 a las 12:30 p. m.
                        Notas: Sin cebolla

                        Detalle:
                        - 2 × Sándwich de pollo — $ 30.000
                        - 1 × Sándwich vegetariano — $ 13.000

                        Total: $ 43.000""");
    }

    @Test
    void usaElTextoPorDefectoCuandoNoHayNotas() {
        Pedido sinNotas = crearPedido(LocalTime.of(12, 30), null);
        Pedido notasVacias = crearPedido(LocalTime.of(12, 30), "   ");

        assertThat(formateador.formatear(sinNotas)).contains("Notas: Sin notas");
        assertThat(formateador.formatear(notasVacias)).contains("Notas: Sin notas");
    }

    @ParameterizedTest
    @CsvSource({
        "0, 30, 12:30 a. m.",
        "11, 59, 11:59 a. m.",
        "12, 0, 12:00 p. m.",
        "23, 59, 11:59 p. m."
    })
    void formateaLaHoraEnDoceHoras(int hora, int minuto, String esperado) {
        String mensaje = formateador.formatear(crearPedido(LocalTime.of(hora, minuto), null));

        assertThat(mensaje).contains("Entrega: 17/11/2026 a las " + esperado);
    }

    @Test
    void conservaLaUbicacionDelPedidoAunqueCambieLaDelCliente() {
        Pedido pedido = crearPedido(LocalTime.of(12, 30), null);
        pedido.getCliente().actualizarUbicacion("Otro conjunto", "999");

        assertThat(formateador.formatear(pedido))
                .contains("Ubicación: Conjunto Los Gatos, Apto 101")
                .doesNotContain("Otro conjunto");
    }

    @Test
    void omiteDecimalesCuandoElImporteEsEntero() {
        String mensaje = formateador.formatear(crearPedido(LocalTime.of(12, 30), null));

        assertThat(mensaje).contains("Total: $ 43.000").doesNotContain("$ 43.000,00");
    }
}
