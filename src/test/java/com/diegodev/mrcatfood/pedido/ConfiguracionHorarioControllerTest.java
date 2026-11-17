package com.diegodev.mrcatfood.pedido;

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
class ConfiguracionHorarioControllerTest {

    @Autowired private MockMvc mvc;

    @Test
    void publicaLosHorariosSinExigirAutenticacion() throws Exception {
        mvc.perform(get("/api/configuracion/horario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horarioRecepcionInicio").value("08:00"))
                .andExpect(jsonPath("$.horarioRecepcionFin").value("20:00"))
                .andExpect(jsonPath("$.ventanaEntregaInicio").value("11:00"))
                .andExpect(jsonPath("$.ventanaEntregaFin").value("21:00"))
                .andExpect(jsonPath("$.zona").value("America/Bogota"));
    }
}
