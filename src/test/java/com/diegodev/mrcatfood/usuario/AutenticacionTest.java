package com.diegodev.mrcatfood.usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.TokenInvalidoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutenticacionTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt jwt;
    @Autowired private PasswordEncoder codificador;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void registroValidoDevuelve201YTokenDeCliente() throws Exception {
        String cuerpo =
                "{\"nombre\":\"Ana Pérez\",\"email\":\"valido@example.com\",\"telefono\":\"3001234567\",\"password\":\"clave1234\"}";
        MvcResult resultado =
                mvc.perform(
                                post("/api/auth/registro")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(cuerpo))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.token").isNotEmpty())
                        .andExpect(jsonPath("$.tipo").value("Bearer"))
                        .andReturn();

        String token =
                MAPPER.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
        org.assertj.core.api.Assertions.assertThat(jwt.validar(token).rol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void registroConCorreoDuplicadoDevuelve409() throws Exception {
        String cuerpo =
                "{\"nombre\":\"Dup\",\"email\":\"dup@example.com\",\"telefono\":\"3001234567\",\"password\":\"clave1234\"}";
        mvc.perform(
                post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
        mvc.perform(
                        post("/api/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(CodigoError.CORREO_EN_USO.name()));
    }

    @Test
    void registroConContrasenaCortaDevuelve400() throws Exception {
        String cuerpo =
                "{\"nombre\":\"Ana\",\"email\":\"corta@example.com\",\"telefono\":\"3001234567\",\"password\":\"corta\"}";
        mvc.perform(
                        post("/api/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(CodigoError.DATOS_INVALIDOS.name()))
                .andExpect(jsonPath("$.campos.password").exists());
    }

    @Test
    void registroConRolEnElCuerpoDevuelve400() throws Exception {
        String cuerpo =
                "{\"nombre\":\"Ana\",\"email\":\"rol@example.com\",\"telefono\":\"3001234567\",\"password\":\"clave1234\",\"rol\":\"ADMIN\"}";
        mvc.perform(
                        post("/api/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(CodigoError.DATOS_INVALIDOS.name()));
    }

    @Test
    void loginCorrectoDevuelve200YToken() throws Exception {
        String registro =
                "{\"nombre\":\"Login\",\"email\":\"login@example.com\",\"telefono\":\"3001234567\",\"password\":\"clave1234\"}";
        mvc.perform(
                post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro));
        String cuerpoLogin = "{\"email\":\"login@example.com\",\"password\":\"clave1234\"}";
        mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpoLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        String registro =
                "{\"nombre\":\"LoginBad\",\"email\":\"loginbad@example.com\",\"telefono\":\"3001234567\",\"password\":\"clave1234\"}";
        mvc.perform(
                post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro));
        String cuerpoLogin = "{\"email\":\"loginbad@example.com\",\"password\":\"otra-clave\"}";
        mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpoLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(CodigoError.CREDENCIALES_INVALIDAS.name()));
    }

    @Test
    void logoutInvalidaElToken() throws Exception {
        String cuerpo =
                "{\"nombre\":\"Logout\",\"email\":\"logout@example.com\",\"telefono\":\"3001234567\",\"password\":\"clave1234\"}";
        MvcResult registro =
                mvc.perform(
                                post("/api/auth/registro")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(cuerpo))
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
        String cuerpo =
                "{\"nombre\":\"Yo\",\"email\":\"yo@example.com\",\"telefono\":\"3001234567\",\"password\":\"clave1234\"}";
        MvcResult registro =
                mvc.perform(
                                post("/api/auth/registro")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(cuerpo))
                        .andExpect(status().isCreated())
                        .andReturn();
        String token =
                MAPPER.readTree(registro.getResponse().getContentAsString()).get("token").asText();

        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("yo@example.com"))
                .andExpect(jsonPath("$.nombre").value("Yo"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
    }
}
