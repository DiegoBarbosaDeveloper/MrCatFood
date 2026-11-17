package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.config.WhatsAppProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

/** Genera el enlace wa.me que el cliente abre para enviar el pedido (RF-09). */
@Service
public class WhatsAppService {

    private final WhatsAppProperties propiedades;
    private final MensajePedidoFormatter formateador;

    public WhatsAppService(WhatsAppProperties propiedades, MensajePedidoFormatter formateador) {
        this.propiedades = propiedades;
        this.formateador = formateador;
    }

    public String generarEnlace(String mensaje) {
        String mensajeCodificado =
                URLEncoder.encode(mensaje, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + propiedades.numero() + "?text=" + mensajeCodificado;
    }
}
