package com.diegodev.mrcatfood.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos necesarios para entregar los pedidos del cliente. */
public record UbicacionRequest(
        @NotBlank(message = "es obligatorio")
                @Size(max = 120, message = "debe tener máximo 120 caracteres")
                String conjuntoResidencial,
        @NotBlank(message = "es obligatorio")
                @Size(max = 60, message = "debe tener máximo 60 caracteres")
                String apartamento) {}
