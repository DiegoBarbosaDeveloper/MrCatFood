package com.diegodev.mrcatfood.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.diegodev.mrcatfood.config.JwtProperties;
import com.diegodev.mrcatfood.usuario.Rol;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ServicioJwtTest {

    private static final String SECRETO = "secreto-de-pruebas-con-mas-de-32-bytes-0123456789";
    private static final Instant AHORA = Instant.parse("2026-11-03T12:00:00Z");

    private TokenRevocadoRepository revocados;
    private Clock reloj;
    private SecretKey clave;
    private ServicioJwt servicio;

    @BeforeEach
    void preparar() {
        revocados = Mockito.mock(TokenRevocadoRepository.class);
        reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
        clave = Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8));
        servicio = new ServicioJwt(clave, new JwtProperties(SECRETO, 120), reloj, revocados);
    }

    @Test
    void emitirYValidarDevuelveLaIdentidad() {
        var usuario = new UsuarioAutenticado(7L, "ana@example.com", Rol.CLIENTE);

        var emitido = servicio.emitir(usuario);

        assertThat(emitido.valor()).isNotBlank();
        assertThat(emitido.jti()).matches("[0-9a-f-]{36}");
        assertThat(emitido.expiraEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(120)));
        when(revocados.existsById(emitido.jti())).thenReturn(false);

        var validado = servicio.validar(emitido.valor());

        assertThat(validado).isEqualTo(usuario);
    }

    @Test
    void tokenAlteradoEsRechazado() {
        var emitido = servicio.emitir(new UsuarioAutenticado(7L, "ana@example.com", Rol.CLIENTE));

        String alterado = emitido.valor().substring(0, emitido.valor().length() - 2) + "xx";

        assertThatThrownBy(() -> servicio.validar(alterado))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void tokenVencidoEsRechazado() {
        var emitido = servicio.emitir(new UsuarioAutenticado(7L, "ana@example.com", Rol.CLIENTE));
        var relojFuturo = Clock.fixed(AHORA.plus(Duration.ofMinutes(121)), ZoneOffset.UTC);
        var servicioFuturo =
                new ServicioJwt(clave, new JwtProperties(SECRETO, 120), relojFuturo, revocados);

        assertThatThrownBy(() -> servicioFuturo.validar(emitido.valor()))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void tokenRevocadoEsRechazado() {
        var emitido = servicio.emitir(new UsuarioAutenticado(7L, "ana@example.com", Rol.CLIENTE));
        when(revocados.existsById(emitido.jti())).thenReturn(true);

        assertThatThrownBy(() -> servicio.validar(emitido.valor()))
                .isInstanceOf(TokenInvalidoException.class)
                .hasMessageContaining("cerrada");
    }

    @Test
    void revocarGuardaElJtiYValidarDejaDeServir() {
        var emitido = servicio.emitir(new UsuarioAutenticado(7L, "ana@example.com", Rol.CLIENTE));
        when(revocados.existsById(emitido.jti())).thenReturn(false);

        servicio.revocar(emitido.valor());

        verify(revocados).save(any(TokenRevocado.class));
        when(revocados.existsById(emitido.jti())).thenReturn(true);

        assertThatThrownBy(() -> servicio.validar(emitido.valor()))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void revocarDosVecesNoDuplicaElRegistro() {
        var emitido = servicio.emitir(new UsuarioAutenticado(7L, "ana@example.com", Rol.CLIENTE));
        when(revocados.existsById(emitido.jti())).thenReturn(true);

        servicio.revocar(emitido.valor());

        verify(revocados, never()).save(any(TokenRevocado.class));
    }

    @Test
    void tokenMalFormadoEsRechazado() {
        assertThatThrownBy(() -> servicio.validar("esto.no.es.un.jwt"))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void jtiEsUnUUID() {
        var emitido = servicio.emitir(new UsuarioAutenticado(7L, "ana@example.com", Rol.CLIENTE));

        assertThat(UUID.fromString(emitido.jti())).isNotNull();
    }
}
