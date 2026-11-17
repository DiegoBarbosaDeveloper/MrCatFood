package com.diegodev.mrcatfood.config;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Habilita CORS para el aplicativo web con los orígenes configurados en el entorno. */
@Configuration
@EnableConfigurationProperties(PropiedadesCors.class)
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(PropiedadesCors propiedades) {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(propiedades.origenesPermitidos());
        configuracion.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracion.setExposedHeaders(List.of("Location"));
        configuracion.setAllowCredentials(false);
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }
}
