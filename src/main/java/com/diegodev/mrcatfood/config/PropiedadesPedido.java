package com.diegodev.mrcatfood.config;

import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Horarios configurables para recibir pedidos y programar entregas. */
@ConfigurationProperties(prefix = "pedidos")
public record PropiedadesPedido(
        LocalTime horarioInicio,
        LocalTime horarioFin,
        LocalTime entregaInicio,
        LocalTime entregaFin,
        ZoneId zona) {

    public PropiedadesPedido {
        validarVentana("pedidos.horario", horarioInicio, horarioFin);
        validarVentana("pedidos.entrega", entregaInicio, entregaFin);
        if (zona == null) {
            throw new IllegalArgumentException("pedidos.zona es obligatoria");
        }
    }

    private static void validarVentana(String nombre, LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(nombre + " requiere hora de inicio y fin");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException(nombre + " debe comenzar antes de terminar");
        }
    }
}
