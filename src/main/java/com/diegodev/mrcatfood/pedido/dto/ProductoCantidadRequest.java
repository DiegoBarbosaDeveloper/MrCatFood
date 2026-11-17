package com.diegodev.mrcatfood.pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductoCantidadRequest(
        @NotNull(message = "El producto es obligatorio") Long productoId,
        @NotNull(message = "La cantidad es obligatoria")
                @Positive(message = "La cantidad debe ser mayor que cero")
                Integer cantidad) {}
