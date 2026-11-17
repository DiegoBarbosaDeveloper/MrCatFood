package com.diegodev.mrcatfood.usuario;

import com.diegodev.mrcatfood.config.OpenApiConfig;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticación")
public class AutenticacionController {

    private final UsuarioService servicio;
    private final ServicioJwt jwt;

    public AutenticacionController(UsuarioService servicio, ServicioJwt jwt) {
        this.servicio = servicio;
        this.jwt = jwt;
    }

    @PostMapping("/registro")
    @Operation(
            summary = "Registrar un cliente",
            description = "Crea siempre una cuenta CLIENTE y devuelve un JWT.")
    public ResponseEntity<TokenResponse> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        var emitido = servicio.registrar(solicitud);
        return ResponseEntity.status(201)
                .body(new TokenResponse(emitido.valor(), "Bearer", emitido.expiraEn()));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales y devuelve un JWT.")
    public ResponseEntity<TokenResponse> iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        var emitido = servicio.autenticar(solicitud);
        return ResponseEntity.ok(new TokenResponse(emitido.valor(), "Bearer", emitido.expiraEn()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el JWT presentado.")
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
    public ResponseEntity<Void> cerrarSesion(@RequestHeader("Authorization") String autorizacion) {
        jwt.revocar(autorizacion.substring("Bearer ".length()).trim());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/yo")
    @Operation(summary = "Consultar el usuario autenticado")
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
    public ResponseEntity<UsuarioResponse> yo(
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(servicio.buscarUsuarioActual(principal.id()));
    }
}
