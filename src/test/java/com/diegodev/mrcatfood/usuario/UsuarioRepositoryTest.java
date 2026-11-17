package com.diegodev.mrcatfood.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired private UsuarioRepository repositorio;

    private Usuario cliente(String email) {
        return new Usuario("Ana Pérez", email, "3001234567", "hash-bcrypt", Rol.CLIENTE);
    }

    @Test
    void guardaYRecuperaUnUsuario() {
        Usuario guardado = repositorio.saveAndFlush(cliente("ana@example.com"));

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getCreadoEn()).isNotNull();
        assertThat(guardado.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(guardado.isActivo()).isTrue();
        assertThat(repositorio.findById(guardado.getId()))
                .get()
                .extracting(Usuario::getNombre)
                .isEqualTo("Ana Pérez");
    }

    @Test
    void normalizaElCorreoAlGuardar() {
        Usuario guardado = repositorio.saveAndFlush(cliente("  Ana.Perez@Example.COM  "));

        assertThat(guardado.getEmail()).isEqualTo("ana.perez@example.com");
    }

    @Test
    void buscaElCorreoSinDistinguirMayusculas() {
        repositorio.saveAndFlush(cliente("ana@example.com"));

        assertThat(repositorio.findByEmailIgnoreCase("ANA@Example.com")).isPresent();
        assertThat(repositorio.existsByEmailIgnoreCase("Ana@EXAMPLE.com")).isTrue();
    }

    @Test
    void informaCuandoElCorreoNoExiste() {
        assertThat(repositorio.findByEmailIgnoreCase("nadie@example.com")).isEmpty();
        assertThat(repositorio.existsByEmailIgnoreCase("nadie@example.com")).isFalse();
    }

    @Test
    void normalizarEmailRecortaEspaciosYConvierteAMinusculas() {
        assertThat(Usuario.normalizarEmail("  Cliente@MrCatFood.CO "))
                .isEqualTo("cliente@mrcatfood.co");
        assertThat(Usuario.normalizarEmail(null)).isNull();
    }
}
