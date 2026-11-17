package com.diegodev.mrcatfood.seguridad;

import com.diegodev.mrcatfood.config.JwtProperties;
import com.diegodev.mrcatfood.usuario.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emite, valida y revoca los tokens de acceso.
 *
 * <p>El token es un JWT HS256 con {@code sub} (correo), {@code uid} (id), {@code rol}, {@code jti},
 * {@code iat} y {@code exp}. Un token no puede invalidarse por sí solo, así que el cierre de sesión
 * guarda su {@code jti} en {@code token_revocado} (HU-02).
 */
@Service
public class ServicioJwt {

    private final SecretKey clave;
    private final Duration vigencia;
    private final Clock reloj;
    private final TokenRevocadoRepository revocados;

    public ServicioJwt(
            SecretKey clave,
            JwtProperties propiedades,
            Clock reloj,
            TokenRevocadoRepository revocados) {
        this.clave = clave;
        this.vigencia = Duration.ofMinutes(propiedades.expiracionMinutos());
        this.reloj = reloj;
        this.revocados = revocados;
    }

    public TokenEmitido emitir(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now(reloj);
        Instant expiraEn = ahora.plus(vigencia);
        String jti = UUID.randomUUID().toString();

        String valor =
                Jwts.builder()
                        .subject(usuario.email())
                        .claim("uid", usuario.id())
                        .claim("rol", usuario.rol().name())
                        .id(jti)
                        .issuedAt(Date.from(ahora))
                        .expiration(Date.from(expiraEn))
                        .signWith(clave, Jwts.SIG.HS256)
                        .compact();

        return new TokenEmitido(valor, expiraEn, jti);
    }

    /**
     * Valida la firma y la vigencia y comprueba que la sesión no se haya cerrado.
     *
     * @throws TokenInvalidoException si el token no sirve
     */
    public UsuarioAutenticado validar(String token) {
        Claims reclamos = leerReclamos(token);

        if (revocados.existsById(reclamos.getId())) {
            throw new TokenInvalidoException("La sesión ya fue cerrada");
        }

        return new UsuarioAutenticado(idDe(reclamos), reclamos.getSubject(), rolDe(reclamos));
    }

    /** Cierra la sesión del token: a partir de ahora {@link #validar(String)} lo rechaza. */
    @Transactional
    public void revocar(String token) {
        Claims reclamos = leerReclamos(token);

        if (reclamos.getId() == null) {
            throw new TokenInvalidoException("El token no identifica una sesión");
        }
        if (revocados.existsById(reclamos.getId())) {
            return;
        }
        revocados.save(new TokenRevocado(reclamos.getId(), reclamos.getExpiration().toInstant()));
    }

    private Claims leerReclamos(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(clave)
                    .clock(() -> Date.from(Instant.now(reloj)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException("Token inválido o vencido", e);
        }
    }

    private static Long idDe(Claims reclamos) {
        Object uid = reclamos.get("uid");
        if (!(uid instanceof Number numero)) {
            throw new TokenInvalidoException("El token no identifica un usuario");
        }
        return numero.longValue();
    }

    private static Rol rolDe(Claims reclamos) {
        String rol = reclamos.get("rol", String.class);
        try {
            return Rol.valueOf(rol);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TokenInvalidoException("El token no declara un rol válido", e);
        }
    }
}
