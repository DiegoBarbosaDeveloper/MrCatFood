package com.diegodev.mrcatfood.comun;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ControladorPruebaErrores.class)
class ManejadorGlobalErroresTest {

    @Autowired private MockMvc mvc;

    @Test
    void apiExceptionSeTraduceAProblemDetail() throws Exception {
        mvc.perform(get("/_prueba/errores/CORREO_EN_USO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_EN_USO"))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.instance").value("/_prueba/errores/CORREO_EN_USO"));
    }

    @Test
    void apiExceptionConCamposLosIncluye() throws Exception {
        mvc.perform(get("/_prueba/errores/DATOS_INVALIDOS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.campo").value("detalle"));
    }

    @Test
    void validacionDeDtoDevuelve400ConCampos() throws Exception {
        mvc.perform(
                        post("/_prueba/errores/validacion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nombre\":\"\",\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.cantidad").exists());
    }

    @Test
    void cuerpoIlegibleDevuelve400() throws Exception {
        mvc.perform(
                        post("/_prueba/errores/validacion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void errorInesperadoDevuelve500SinDetallesInternos() throws Exception {
        mvc.perform(get("/_prueba/errores/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado"));
    }
}
