package com.diegodev.mrcatfood.pedido;

/** Horarios vigentes que consume el aplicativo para orientar al cliente. */
public record HorarioResponse(
        String horarioRecepcionInicio,
        String horarioRecepcionFin,
        String ventanaEntregaInicio,
        String ventanaEntregaFin,
        String zona) {}
