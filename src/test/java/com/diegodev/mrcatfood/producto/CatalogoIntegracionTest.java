package com.diegodev.mrcatfood.producto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.comun.BaseIntegracionTest;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import com.diegodev.mrcatfood.usuario.Rol;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CatalogoIntegracionTest extends BaseIntegracionTest {

    @Autowired private MockMvc mvc;
    @Autowired private ProductoRepository productos;
    @Autowired private ServicioJwt jwt;

    @Test
    void catalogoPublicoSoloMuestraProductosActivos() throws Exception {
        Producto activo = producto("Activo integración", "Disponible", "15000", 5, true);
        activo.agregarIngrediente("Pan artesanal");
        Producto guardado = productos.saveAndFlush(activo);
        Producto inactivo =
                productos.saveAndFlush(
                        producto("Inactivo integración", "Oculto", "18000", 8, false));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Activo integración')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.nombre == 'Inactivo integración')]").isEmpty())
                .andExpect(
                        jsonPath("$[?(@.nombre == 'Activo integración')].ingredientes[0]")
                                .value("Pan artesanal"));

        mvc.perform(get("/api/productos/{productoId}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Activo integración"));
        mvc.perform(get("/api/productos/{productoId}", inactivo.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void detalleDeProductoInactivoDevuelve404() throws Exception {
        Producto inactivo =
                productos.saveAndFlush(producto("Inactivo", "Oculto", "18000", 8, false));

        mvc.perform(get("/api/productos/{productoId}", inactivo.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(CodigoError.RECURSO_NO_ENCONTRADO.name()));
    }

    @Test
    void clienteNoPuedeCrearNiCambiarDisponibilidad() throws Exception {
        Producto producto =
                productos.saveAndFlush(producto("Protegido", "Descripción", "10000", 3, true));

        mvc.perform(
                        post("/api/admin/productos")
                                .header("Authorization", autorizacion(Rol.CLIENTE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitudProducto()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(CodigoError.ACCESO_DENEGADO.name()));

        mvc.perform(
                        patch("/api/admin/productos/{productoId}/disponibilidad", producto.getId())
                                .header("Authorization", autorizacion(Rol.CLIENTE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"activo\":false,\"stock\":0}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(CodigoError.ACCESO_DENEGADO.name()));
    }

    @Test
    void administradoraPuedeCrearYElCambioSeVeEnElCatalogo() throws Exception {
        mvc.perform(
                        post("/api/admin/productos")
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "nombre": "Nuevo integración",
                                          "descripcion": "Descripción",
                                          "precio": 15000.50,
                                          "stock": 4,
                                          "ingredientes": ["Pan artesanal", "Queso"]
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Nuevo integración"))
                .andExpect(jsonPath("$.precio").value(15000.50))
                .andExpect(jsonPath("$.ingredientes.length()").value(2));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Nuevo integración')]").isNotEmpty());
    }

    private String autorizacion(Rol rol) {
        return "Bearer "
                + jwt.emitir(
                                new UsuarioAutenticado(
                                        100L, rol.name().toLowerCase() + "@integracion.com", rol))
                        .valor();
    }

    private String solicitudProducto() {
        return """
                {
                  "nombre": "Intento",
                  "descripcion": "Descripción",
                  "precio": 12000,
                  "stock": 3,
                  "ingredientes": ["Pan artesanal"]
                }
                """;
    }

    private Producto producto(
            String nombre, String descripcion, String precio, Integer stock, boolean activo) {
        return new Producto(nombre, descripcion, new BigDecimal(precio), stock, activo);
    }
}
