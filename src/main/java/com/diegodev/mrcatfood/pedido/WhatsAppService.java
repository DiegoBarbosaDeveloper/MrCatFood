package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.config.WhatsAppProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Genera el enlace wa.me que el cliente abre para enviar el pedido (RF-09). */
@Service
public class WhatsAppService {

    private final WhatsAppProperties propiedades;
    private final MensajePedidoFormatter formateador;
    private final PedidoRepository pedidos;

    public WhatsAppService(
            WhatsAppProperties propiedades,
            MensajePedidoFormatter formateador,
            PedidoRepository pedidos) {
        this.propiedades = propiedades;
        this.formateador = formateador;
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public String generarEnlaceParaCliente(Long clienteId, Long pedidoId) {
        Pedido pedido =
                pedidos.buscarPorIdYCliente(pedidoId, clienteId)
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                CodigoError.RECURSO_NO_ENCONTRADO,
                                                "El pedido solicitado no existe"));
        return generarEnlace(formateador.formatear(pedido));
    }

    public String generarEnlace(String mensaje) {
        String mensajeCodificado =
                URLEncoder.encode(mensaje, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + propiedades.numero() + "?text=" + mensajeCodificado;
    }
}
