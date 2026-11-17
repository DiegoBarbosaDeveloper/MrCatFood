package com.diegodev.mrcatfood.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CreadorCuentaAdministradoraTest {

    @Autowired private UsuarioRepository usuarios;
    @Autowired private CreadorCuentaAdministradora creador;

    @Test
    void creaLaAdministradoraAlArrancar() {
        var administradoras =
                usuarios.findAll().stream().filter(u -> u.getRol() == Rol.ADMIN).toList();

        assertThat(administradoras).hasSize(1);
        assertThat(administradoras.get(0).getEmail()).isEqualTo("admin-test@mrcatfood.local");
        assertThat(administradoras.get(0).getPasswordHash()).startsWith("$2");
    }

    @Test
    void ejecutarDosVecesNoDuplicaNiCambiaLaContrasena() {
        var antes = usuarios.findByEmailIgnoreCase("admin-test@mrcatfood.local").orElseThrow();
        String hashAntes = antes.getPasswordHash();

        creador.run(null);

        var despues = usuarios.findByEmailIgnoreCase("admin-test@mrcatfood.local").orElseThrow();
        assertThat(usuarios.count()).isEqualTo(1);
        assertThat(despues.getPasswordHash()).isEqualTo(hashAntes);
    }
}
