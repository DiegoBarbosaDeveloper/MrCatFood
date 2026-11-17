package com.diegodev.mrcatfood.producto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Estado de publicación e inventario disponible de un producto. */
@JsonIgnoreProperties(ignoreUnknown = false)
public record DisponibilidadRequest(
        @NotNull(message = "es obligatorio") Boolean activo,
        @NotNull(message = "es obligatorio") @Min(value = 0, message = "no puede ser negativo")
                Integer stock) {}
