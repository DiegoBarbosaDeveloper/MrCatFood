package com.diegodev.mrcatfood.producto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ProductoRepository productos;

    @Test
    void catalogoPublicoDevuelveSoloActivosConTodaLaInformacion() throws Exception {
        Producto disponible =
                producto("Sándwich disponible", "Descripción disponible", "15000", 5, true);
        disponible.agregarIngrediente("Pan artesanal");
        disponible.agregarIngrediente("Pollo");
        productos.save(disponible);

        productos.save(producto("Sándwich inactivo", "No debe aparecer", "18000", 8, false));

        Producto agotado = producto("Sándwich agotado", "Sin existencias", "13000", 0, true);
        agotado.agregarIngrediente("Pan artesanal");
        productos.saveAndFlush(agotado);

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Sándwich agotado"))
                .andExpect(jsonPath("$[0].descripcion").value("Sin existencias"))
                .andExpect(jsonPath("$[0].precio").value(13000.00))
                .andExpect(jsonPath("$[0].stock").value(0))
                .andExpect(jsonPath("$[0].disponible").value(false))
                .andExpect(jsonPath("$[0].ingredientes[0]").value("Pan artesanal"))
                .andExpect(jsonPath("$[1].nombre").value("Sándwich disponible"))
                .andExpect(jsonPath("$[1].precio").value(15000.00))
                .andExpect(jsonPath("$[1].stock").value(5))
                .andExpect(jsonPath("$[1].disponible").value(true))
                .andExpect(jsonPath("$[1].ingredientes.length()").value(2))
                .andExpect(jsonPath("$[?(@.nombre == 'Sándwich inactivo')]").isEmpty());
    }

    @Test
    void catalogoVacioDevuelveListaVacia() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    private Producto producto(
            String nombre, String descripcion, String precio, Integer stock, boolean activo) {
        return new Producto(nombre, descripcion, new BigDecimal(precio), stock, activo);
    }
}
