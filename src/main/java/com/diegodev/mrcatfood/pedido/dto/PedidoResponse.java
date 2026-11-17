package com.diegodev.mrcatfood.pedido.dto;

import com.diegodev.mrcatfood.pedido.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String codigo,
        EstadoPedido estado,
        ClientePedidoResponse cliente,
        List<DetalleResponse> detalles,
        BigDecimal subtotal,
        BigDecimal total,
        String conjuntoResidencial,
        String apartamento,
        LocalDate fechaEntrega,
        LocalTime horaEntrega,
        String notas,
        OffsetDateTime creadoEn) {}
