package com.diegodev.mrcatfood.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PropiedadesCorsTest {

    @Test
    void conservaLaListaDeOrigenes() {
        PropiedadesCors propiedades =
                new PropiedadesCors(List.of("http://localhost:8081", "https://app.example.com"));

        assertThat(propiedades.origenesPermitidos())
                .containsExactly("http://localhost:8081", "https://app.example.com");
    }

    @Test
    void recortaEspaciosYDescartaEntradasVacias() {
        PropiedadesCors propiedades =
                new PropiedadesCors(List.of(" http://localhost:8081 ", " ", ""));

        assertThat(propiedades.origenesPermitidos()).containsExactly("http://localhost:8081");
    }

    @Test
    void rechazaListaAusenteOVacia() {
        assertThatThrownBy(() -> new PropiedadesCors(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cors.origenes-permitidos");
        assertThatThrownBy(() -> new PropiedadesCors(List.of(" ", "")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cors.origenes-permitidos");
    }
}
