package com.diegodev.mrcatfood.seguridad;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.comun.BaseIntegracionTest;
import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SeguridadIntegracionTest extends BaseIntegracionTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt jwt;
    @Autowired private PasswordEncoder codificador;

    private String tokenCliente;
    private String tokenAdmin;

    @BeforeEach
    void preparar() {
        var cliente =
                usuarios.saveAndFlush(
                        new Usuario(
                                "Cliente",
                                "cliente-integracion@example.com",
                                "3001234567",
                                codificador.encode("clave1234"),
                                Rol.CLIENTE));
        var admin =
                usuarios.saveAndFlush(
                        new Usuario(
                                "Admin",
                                "admin-integracion@example.com",
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
    void clienteEnRutasDeAdministracionDevuelve403() throws Exception {
        mvc.perform(get("/api/admin/pedidos").header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(get("/api/admin/productos").header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void adminEnRutaAdminPuedeConsultarPedidos() throws Exception {
        mvc.perform(get("/api/admin/pedidos").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());
    }
}
