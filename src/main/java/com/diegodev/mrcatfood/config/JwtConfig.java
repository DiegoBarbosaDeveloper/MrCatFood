package com.diegodev.mrcatfood.config;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra la configuración del token y la clave HS256 derivada del secreto. */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    SecretKey claveJwt(JwtProperties propiedades) {
        return Keys.hmacShaKeyFor(propiedades.secreto().getBytes(StandardCharsets.UTF_8));
    }
}
