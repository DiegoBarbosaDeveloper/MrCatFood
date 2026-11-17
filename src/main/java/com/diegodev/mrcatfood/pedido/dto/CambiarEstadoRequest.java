package com.diegodev.mrcatfood.pedido.dto;

import com.diegodev.mrcatfood.pedido.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(
        @NotNull(message = "El nuevo estado es obligatorio") EstadoPedido estado) {}
