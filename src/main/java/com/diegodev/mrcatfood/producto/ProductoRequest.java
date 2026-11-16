package com.diegodev.mrcatfood.producto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/** Datos editables de un producto; la disponibilidad activa se administra por separado. */
@JsonIgnoreProperties(ignoreUnknown = false)
public record ProductoRequest(
        @NotBlank(message = "es obligatorio")
                @Size(max = 120, message = "debe tener máximo 120 caracteres")
                String nombre,
        @NotBlank(message = "es obligatoria")
                @Size(max = 600, message = "debe tener máximo 600 caracteres")
                String descripcion,
        @NotNull(message = "es obligatorio")
                @DecimalMin(value = "0.00", message = "no puede ser negativo")
                @Digits(integer = 10, fraction = 2, message = "debe tener máximo 2 decimales")
                BigDecimal precio,
        @NotNull(message = "es obligatorio") @Min(value = 0, message = "no puede ser negativo")
                Integer stock,
        @NotEmpty(message = "debe incluir al menos un ingrediente")
                List<
                                @NotBlank(message = "no puede estar vacío")
                                @Size(max = 120, message = "debe tener máximo 120 caracteres")
                                String>
                        ingredientes) {}
