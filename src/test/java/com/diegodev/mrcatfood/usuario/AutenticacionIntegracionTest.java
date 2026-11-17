package com.diegodev.mrcatfood.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.comun.BaseIntegracionTest;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.TokenInvalidoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AutenticacionIntegracionTest extends BaseIntegracionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private MockMvc mvc;
    @Autowired private ServicioJwt jwt;

    @Test
    void registroValidoDevuelve201YTokenDeCliente() throws Exception {
        MvcResult resultado =
                mvc.perform(
                                post("/api/auth/registro")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"nombre":"Ana Pérez","email":"ana-integracion@example.com","telefono":"3001234567","password":"clave1234"}
                                                """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.token").isNotEmpty())
                        .andExpect(jsonPath("$.tipo").value("Bearer"))
                        .andReturn();

        String token =
                MAPPER.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
        assertThat(jwt.validar(token).rol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void registroConCorreoDuplicadoDevuelve409() throws Exception {
        String cuerpo =
                """
                {"nombre":"Dup","email":"dup-integracion@example.com","telefono":"3001234567","password":"clave1234"}
                """;
        mvc.perform(
                        post("/api/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpo))
                .andExpect(status().isCreated());

        mvc.perform(
                        post("/api/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(CodigoError.CORREO_EN_USO.name()));
    }

    @Test
    void registroConRolEnElCuerpoDevuelve400() throws Exception {
        mvc.perform(
                        post("/api/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"nombre":"Ana","email":"escalada@example.com","telefono":"3001234567","password":"clave1234","rol":"ADMIN"}
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(CodigoError.DATOS_INVALIDOS.name()));
    }

    @Test
    void loginCorrectoYIncorrectoDevuelven200Y401() throws Exception {
        mvc.perform(
                        post("/api/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"nombre":"Login","email":"login-integracion@example.com","telefono":"3001234567","password":"clave1234"}
                                        """))
                .andExpect(status().isCreated());

        mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"email":"login-integracion@example.com","password":"clave1234"}
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"email":"login-integracion@example.com","password":"otra-clave"}
                                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(CodigoError.CREDENCIALES_INVALIDAS.name()));
    }

    @Test
    void logoutInvalidaElToken() throws Exception {
        MvcResult registro =
                mvc.perform(
                                post("/api/auth/registro")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"nombre":"Logout","email":"logout-integracion@example.com","telefono":"3001234567","password":"clave1234"}
                                                """))
                        .andExpect(status().isCreated())
                        .andReturn();
        String token =
                MAPPER.readTree(registro.getResponse().getContentAsString()).get("token").asText();

        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jwt.validar(token))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void yoDevuelveLosDatosDelTokenSinElHash() throws Exception {
        MvcResult registro =
                mvc.perform(
                                post("/api/auth/registro")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"nombre":"Yo","email":"yo-integracion@example.com","telefono":"3001234567","password":"clave1234"}
                                                """))
                        .andExpect(status().isCreated())
                        .andReturn();
        String token =
                MAPPER.readTree(registro.getResponse().getContentAsString()).get("token").asText();

        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("yo-integracion@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
    }
}
