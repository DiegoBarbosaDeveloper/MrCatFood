package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.config.PropiedadesPedido;
import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/** Impide registrar pedidos fuera del horario de recepción configurado. */
@Component
public class ValidadorHorarioRecepcion {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final Clock reloj;
    private final PropiedadesPedido propiedades;

    public ValidadorHorarioRecepcion(Clock reloj, PropiedadesPedido propiedades) {
        this.reloj = reloj;
        this.propiedades = propiedades;
    }

    public void validarHorarioRecepcion() {
        LocalTime horaActual = LocalTime.now(reloj);
        if (estaFueraDeVentana(horaActual, propiedades.horarioInicio(), propiedades.horarioFin())) {
            throw new ApiException(CodigoError.FUERA_DE_HORARIO, mensajeHorarioValido());
        }
    }

    private boolean estaFueraDeVentana(LocalTime hora, LocalTime inicio, LocalTime fin) {
        return hora.isBefore(inicio) || hora.isAfter(fin);
    }

    private String mensajeHorarioValido() {
        return "Los pedidos se reciben entre las "
                + propiedades.horarioInicio().format(FORMATO_HORA)
                + " y las "
                + propiedades.horarioFin().format(FORMATO_HORA)
                + " ("
                + propiedades.zona().getId()
                + ")";
    }
}
