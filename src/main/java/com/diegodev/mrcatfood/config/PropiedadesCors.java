package com.diegodev.mrcatfood.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Orígenes autorizados para consumir la API desde el navegador (aplicativo web). */
@ConfigurationProperties(prefix = "cors")
public record PropiedadesCors(List<String> origenesPermitidos) {

    public PropiedadesCors {
        if (origenesPermitidos == null) {
            throw new IllegalArgumentException("cors.origenes-permitidos es obligatorio");
        }
        origenesPermitidos =
                origenesPermitidos.stream()
                        .map(String::trim)
                        .filter(origen -> !origen.isBlank())
                        .toList();
        if (origenesPermitidos.isEmpty()) {
            throw new IllegalArgumentException("cors.origenes-permitidos es obligatorio");
        }
    }
}
