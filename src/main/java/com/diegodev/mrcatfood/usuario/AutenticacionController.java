package com.diegodev.mrcatfood.usuario;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final UsuarioService servicio;
    private final ServicioJwt jwt;
    private final UsuarioRepository usuarios;

    public AutenticacionController(
            UsuarioService servicio, ServicioJwt jwt, UsuarioRepository usuarios) {
        this.servicio = servicio;
        this.jwt = jwt;
        this.usuarios = usuarios;
    }

    @PostMapping("/registro")
    public ResponseEntity<TokenResponse> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        var emitido = servicio.registrar(solicitud);
        return ResponseEntity.status(201)
                .body(new TokenResponse(emitido.valor(), "Bearer", emitido.expiraEn()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        var emitido = servicio.autenticar(solicitud);
        return ResponseEntity.ok(new TokenResponse(emitido.valor(), "Bearer", emitido.expiraEn()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(@RequestHeader("Authorization") String autorizacion) {
        jwt.revocar(autorizacion.substring("Bearer ".length()).trim());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/yo")
    public ResponseEntity<UsuarioResponse> yo(
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        var usuario =
                usuarios.findById(principal.id())
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                CodigoError.RECURSO_NO_ENCONTRADO,
                                                "Usuario no encontrado"));
        return ResponseEntity.ok(UsuarioResponse.de(usuario));
    }
}
