package com.diegodev.mrcatfood.seguridad;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt jwt;
    @Autowired private PasswordEncoder codificador;

    private String tokenCliente;
    private String tokenAdmin;

    @BeforeEach
    void preparar() {
        usuarios.deleteAll();
        var cliente =
                usuarios.saveAndFlush(
                        new Usuario(
                                "Cliente",
                                "cliente@example.com",
                                "3001234567",
                                codificador.encode("clave1234"),
                                Rol.CLIENTE));
        var admin =
                usuarios.saveAndFlush(
                        new Usuario(
                                "Admin",
                                "admin@example.com",
                                "3001234567",
                                codificador.encode("clave1234"),
                                Rol.ADMIN));
        tokenCliente =
                jwt.emitir(new UsuarioAutenticado(cliente.getId(), cliente.getEmail(), Rol.CLIENTE))
                        .valor();
        tokenAdmin =
                jwt.emitir(new UsuarioAutenticado(admin.getId(), admin.getEmail(), Rol.ADMIN))
                        .valor();
    }

    @Test
    void sinTokenEnRutaProtegidaDevuelve401() throws Exception {
        mvc.perform(get("/api/admin/pedidos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void tokenAlteradoDevuelve401() throws Exception {
        String alterado = tokenCliente.substring(0, tokenCliente.length() - 2) + "xx";
        mvc.perform(get("/api/admin/pedidos").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void clienteEnRutaAdminDevuelve403() throws Exception {
        mvc.perform(get("/api/admin/pedidos").header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void adminEnRutaAdminPasaLaSeguridad() throws Exception {
        // No hay controlador todavía: 404 demuestra que la seguridad lo dejó pasar.
        mvc.perform(get("/api/admin/pedidos").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void rutaDeLoginEsPublica() throws Exception {
        mvc.perform(post("/api/auth/login")).andExpect(status().isBadRequest());
    }
}
