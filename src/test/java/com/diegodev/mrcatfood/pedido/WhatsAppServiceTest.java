package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.diegodev.mrcatfood.config.WhatsAppProperties;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WhatsAppServiceTest {

    private final WhatsAppService servicio =
            new WhatsAppService(
                    new WhatsAppProperties("573001234567"),
                    new MensajePedidoFormatter(),
                    mock(PedidoRepository.class));

    @Test
    void generaElEnlaceConElNumeroNormalizado() {
        String enlace = servicio.generarEnlace("Hola");

        assertThat(enlace).isEqualTo("https://wa.me/573001234567?text=Hola");
    }

    @Test
    void codificaAcentosEspaciosYSaltosDeLinea() {
        String enlace = servicio.generarEnlace("Sándwich de pollo\nTotal: $ 30.000");

        assertThat(enlace)
                .contains("%C3%A1")
                .contains("%20")
                .contains("%0A")
                .contains("Total%3A%20%24%2030.000")
                .doesNotContain("+");
    }

    @Test
    void elMensajeSeRecuperaSinPerdidas() {
        String mensaje = "Nuevo pedido\nSándwich — $ 30.000";

        String enlace = servicio.generarEnlace(mensaje);
        String textoCodificado = enlace.substring(enlace.indexOf("?text=") + "?text=".length());
        String recuperado = URLDecoder.decode(textoCodificado, StandardCharsets.UTF_8);

        assertThat(recuperado).isEqualTo(mensaje);
    }
}
