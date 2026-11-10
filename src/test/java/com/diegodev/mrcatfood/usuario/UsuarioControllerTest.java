package com.diegodev.mrcatfood.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;

    @Test
    void guardaUbicacionYLaDevuelveEnElPerfil() throws Exception {
        String token = registrarCliente("ubicacion@example.com");

        mvc.perform(
                        put("/api/usuarios/yo/ubicacion")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"conjuntoResidencial\":\"Torres del Parque\",\"apartamento\":\"302\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conjuntoResidencial").value("Torres del Parque"))
                .andExpect(jsonPath("$.apartamento").value("302"));

        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conjuntoResidencial").value("Torres del Parque"))
                .andExpect(jsonPath("$.apartamento").value("302"));
    }

    @Test
    void exigeTokenParaActualizarUbicacion() throws Exception {
        mvc.perform(
                        put("/api/usuarios/yo/ubicacion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"conjuntoResidencial\":\"Los Pinos\",\"apartamento\":\"4B\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void tokenSoloPermiteModificarLaUbicacionPropia() throws Exception {
        String tokenPrimerCliente = registrarCliente("propietario-ubicacion@example.com");
        registrarCliente("otro-ubicacion@example.com");

        mvc.perform(
                        put("/api/usuarios/yo/ubicacion")
                                .header("Authorization", "Bearer " + tokenPrimerCliente)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"conjuntoResidencial\":\"Conjunto propio\",\"apartamento\":\"101\"}"))
                .andExpect(status().isOk());

        Usuario otroCliente =
                usuarios.findByEmailIgnoreCase("otro-ubicacion@example.com").orElseThrow();
        assertThat(otroCliente.getConjuntoResidencial()).isNull();
        assertThat(otroCliente.getApartamento()).isNull();
    }

    private String registrarCliente(String email) throws Exception {
        String cuerpo =
                MAPPER.writeValueAsString(
                        new RegistroRequest(
                                "Cliente Ubicación", email, "3001234567", "clave1234", null));
        MvcResult resultado =
                mvc.perform(
                                post("/api/auth/registro")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(cuerpo))
                        .andExpect(status().isCreated())
                        .andReturn();
        return MAPPER.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }
}
