package com.diegodev.mrcatfood.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiConfigTest {

    @Autowired private MockMvc mvc;

    @Test
    void publicaEsquemaBearerYRutasConSuSeguridad() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("MrCatFood API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(
                        jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/auth/registro'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/auth/yo'].get.security[0].bearerAuth").isArray())
                .andExpect(
                        jsonPath("$.paths['/api/pedidos'].post.security[0].bearerAuth").isArray())
                .andExpect(
                        jsonPath("$.paths['/api/admin/productos'].get.security[0].bearerAuth")
                                .isArray());
    }

    @Test
    void swaggerUiEsPublica() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
