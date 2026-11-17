package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.config.PropiedadesPedido;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Valida que una entrega sea futura y esté dentro de su ventana configurada. */
@Component
public class ValidadorFechaEntrega {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final Clock reloj;
    private final PropiedadesPedido propiedades;

    public ValidadorFechaEntrega(Clock reloj, PropiedadesPedido propiedades) {
        this.reloj = reloj;
        this.propiedades = propiedades;
    }

    public void validarFechaEntrega(LocalDate fechaEntrega, LocalTime horaEntrega) {
        if (fechaEntrega == null || horaEntrega == null) {
            throw fechaInvalida("La fecha y la hora de entrega son obligatorias");
        }

        LocalDateTime fechaHoraEntrega = LocalDateTime.of(fechaEntrega, horaEntrega);
        LocalDateTime fechaHoraActual = LocalDateTime.now(reloj);
        if (!fechaHoraEntrega.isAfter(fechaHoraActual)) {
            throw fechaInvalida("La fecha y la hora de entrega deben ser futuras");
        }
        if (horaEntrega.isBefore(propiedades.entregaInicio())
                || horaEntrega.isAfter(propiedades.entregaFin())) {
            throw fechaInvalida(mensajeVentanaEntrega());
        }
    }

    private ApiException fechaInvalida(String mensaje) {
        return new ApiException(
                CodigoError.FECHA_ENTREGA_INVALIDA, mensaje, Map.of("fechaEntrega", mensaje));
    }

    private String mensajeVentanaEntrega() {
        return "Las entregas se programan entre las "
                + propiedades.entregaInicio().format(FORMATO_HORA)
                + " y las "
                + propiedades.entregaFin().format(FORMATO_HORA)
                + " ("
                + propiedades.zona().getId()
                + ")";
    }
}
