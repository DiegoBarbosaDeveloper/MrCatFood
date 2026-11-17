package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.config.PropiedadesPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Publica los horarios configurados para recepción y entrega. */
@RestController
@RequestMapping("/api/configuracion/horario")
@Tag(name = "Configuración")
public class ConfiguracionHorarioController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final PropiedadesPedido propiedades;

    public ConfiguracionHorarioController(PropiedadesPedido propiedades) {
        this.propiedades = propiedades;
    }

    @GetMapping
    @Operation(summary = "Consultar horarios de recepción y entrega")
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
