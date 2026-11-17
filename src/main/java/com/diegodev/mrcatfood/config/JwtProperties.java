package com.diegodev.mrcatfood.config;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parámetros de firma del token. El secreto solo vive en el entorno (mínimo 32 bytes) y la
 * expiración se expresa en minutos.
 */
@ConfigurationProperties(prefix = "seguridad.jwt")
public record JwtProperties(String secreto, long expiracionMinutos) {

    public JwtProperties {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "seguridad.jwt.secreto es obligatorio y debe tener al menos 32 bytes");
        }
        if (expiracionMinutos <= 0) {
            throw new IllegalArgumentException(
                    "seguridad.jwt.expiracion-minutos debe ser mayor que cero");
        }
    }
}
