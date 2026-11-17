package com.diegodev.mrcatfood.pedido.dto;

import java.math.BigDecimal;

public record DetalleResponse(
        Long productoId,
        String nombreProducto,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal) {}
