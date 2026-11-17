package com.diegodev.mrcatfood.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CrearPedidoRequest(
        @NotEmpty(message = "Incluye al menos un producto")
                List<@Valid ProductoCantidadRequest> productos,
        @NotNull(message = "La fecha de entrega es obligatoria") LocalDate fechaEntrega,
        @NotNull(message = "La hora de entrega es obligatoria") LocalTime horaEntrega,
        @Size(max = 500, message = "Las notas deben tener máximo 500 caracteres") String notas) {

    public PrevisualizarPedidoRequest comoPrevisualizacion() {
        return new PrevisualizarPedidoRequest(productos, fechaEntrega, horaEntrega, notas);
    }
}
