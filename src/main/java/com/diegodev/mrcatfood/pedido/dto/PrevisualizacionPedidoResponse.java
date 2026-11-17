package com.diegodev.mrcatfood.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PrevisualizacionPedidoResponse(
        List<DetalleResponse> detalles,
        BigDecimal subtotal,
        BigDecimal total,
        String conjuntoResidencial,
        String apartamento,
        LocalDate fechaEntrega,
        LocalTime horaEntrega,
        String notas) {}
