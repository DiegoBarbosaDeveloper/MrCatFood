package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.config.PropiedadesPedido;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Publica los horarios configurados para recepción y entrega. */
@RestController
@RequestMapping("/api/configuracion/horario")
public class ConfiguracionHorarioController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final PropiedadesPedido propiedades;

    public ConfiguracionHorarioController(PropiedadesPedido propiedades) {
        this.propiedades = propiedades;
    }

    @GetMapping
    public ResponseEntity<HorarioResponse> consultarHorario() {
        return ResponseEntity.ok(
                new HorarioResponse(
                        propiedades.horarioInicio().format(FORMATO_HORA),
                        propiedades.horarioFin().format(FORMATO_HORA),
                        propiedades.entregaInicio().format(FORMATO_HORA),
                        propiedades.entregaFin().format(FORMATO_HORA),
                        propiedades.zona().getId()));
    }
}
