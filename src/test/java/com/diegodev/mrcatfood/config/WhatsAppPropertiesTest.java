package com.diegodev.mrcatfood.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WhatsAppPropertiesTest {

    @Test
    void conservaUnNumeroYaNormalizado() {
        WhatsAppProperties propiedades = new WhatsAppProperties("573001234567");

        assertThat(propiedades.numero()).isEqualTo("573001234567");
    }

    @ParameterizedTest
    @ValueSource(strings = {"+57 300 123 4567", "+57-300-123-4567", "(57) 300 1234567"})
    void normalizaSeparadoresComunes(String numero) {
        WhatsAppProperties propiedades = new WhatsAppProperties(numero);

        assertThat(propiedades.numero()).isEqualTo("573001234567");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "abc123456789", "5730012345a", "12345", "1234567890123456"})
    void rechazaNumerosInvalidos(String numero) {
        assertThatThrownBy(() -> new WhatsAppProperties(numero))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("whatsapp.numero");
    }

    @Test
    void rechazaNumeroAusente() {
        assertThatThrownBy(() -> new WhatsAppProperties(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("whatsapp.numero");
    }

    @Test
    void aceptaLosLimitesDeLongitud() {
        assertThat(new WhatsAppProperties("1234567890").numero()).isEqualTo("1234567890");
        assertThat(new WhatsAppProperties("123456789012345").numero()).isEqualTo("123456789012345");
    }
}
