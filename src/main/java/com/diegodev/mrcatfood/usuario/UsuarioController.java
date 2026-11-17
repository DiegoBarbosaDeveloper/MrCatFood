package com.diegodev.mrcatfood.usuario;

import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operaciones sobre el perfil del usuario autenticado. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @PutMapping("/yo/ubicacion")
    public ResponseEntity<UsuarioResponse> actualizarUbicacion(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @Valid @RequestBody UbicacionRequest solicitud) {
        return ResponseEntity.ok(servicio.actualizarUbicacion(principal.id(), solicitud));
    }
}
