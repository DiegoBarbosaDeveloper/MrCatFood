package com.diegodev.mrcatfood.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Número de la propietaria para los enlaces wa.me. Se normaliza sin '+', espacios, guiones ni
 * paréntesis y debe quedar en formato internacional de 10 a 15 dígitos.
 */
@ConfigurationProperties(prefix = "whatsapp")
public record WhatsAppProperties(String numero) {

    private static final String PATRON_NUMERO = "\\d{10,15}";

    public WhatsAppProperties {
        String numeroNormalizado = normalizar(numero);
        if (!numeroNormalizado.matches(PATRON_NUMERO)) {
            throw new IllegalArgumentException(
                    "whatsapp.numero es obligatorio y debe tener entre 10 y 15 dígitos");
        }
        numero = numeroNormalizado;
    }

    private static String normalizar(String numero) {
        if (numero == null) {
            return "";
        }
        return numero.replaceAll("[+\\s()\\-]", "");
    }
}
