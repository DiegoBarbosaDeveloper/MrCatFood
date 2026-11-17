package com.diegodev.mrcatfood.comun;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controlador solo para las pruebas de manejo de errores. Vive en src/test. */
@RestController
@RequestMapping("/_prueba/errores")
@Validated
public class ControladorPruebaErrores {

    public record Cuerpo(@NotBlank String nombre, @Min(1) Integer cantidad) {}

    @GetMapping("/{codigo}")
    public ResponseEntity<Void> lanzar(@PathVariable String codigo) {
        CodigoError error = CodigoError.valueOf(codigo);
        if (error == CodigoError.DATOS_INVALIDOS) {
            throw new ApiException(error, "mensaje de prueba", Map.of("campo", "detalle"));
        }
        throw new ApiException(error, "mensaje de prueba");
    }

    @PostMapping("/validacion")
    public ResponseEntity<Void> validar(@Valid @RequestBody Cuerpo cuerpo) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inesperado")
    public ResponseEntity<Void> inesperado() {
        throw new IllegalStateException("boom");
    }
}
