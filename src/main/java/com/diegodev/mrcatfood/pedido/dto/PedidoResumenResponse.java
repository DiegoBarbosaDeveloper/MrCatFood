package com.diegodev.mrcatfood.pedido.dto;

import com.diegodev.mrcatfood.pedido.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record PedidoResumenResponse(
        Long id,
        String codigo,
        EstadoPedido estado,
        ClientePedidoResponse cliente,
        String conjuntoResidencial,
        String apartamento,
        LocalDate fechaEntrega,
        LocalTime horaEntrega,
        BigDecimal total,
        OffsetDateTime creadoEn) {}
