package com.diegodev.mrcatfood.producto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import com.diegodev.mrcatfood.usuario.Rol;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminProductoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ProductoRepository productos;
    @Autowired private ServicioJwt jwt;

    @Test
    void administradoraCreaProductoActivoConIngredientes() throws Exception {
        mvc.perform(
                        post("/api/admin/productos")
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitudProducto("Sándwich nuevo", "15000.50", 8)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Sándwich nuevo"))
                .andExpect(jsonPath("$.precio").value(15000.50))
                .andExpect(jsonPath("$.stock").value(8))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.ingredientes.length()").value(2));
    }

    @Test
    void administradoraEditaProductoYReemplazaIngredientes() throws Exception {
        Producto producto =
                new Producto(
                        "Nombre anterior",
                        "Descripción anterior",
                        new BigDecimal("10000"),
                        2,
                        true);
        producto.agregarIngrediente("Ingrediente anterior");
        Producto guardado = productos.saveAndFlush(producto);

        mvc.perform(
                        put("/api/admin/productos/{productoId}", guardado.getId())
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitudProducto("Nombre actualizado", "17500", 6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre actualizado"))
                .andExpect(jsonPath("$.precio").value(17500.00))
                .andExpect(jsonPath("$.stock").value(6))
                .andExpect(jsonPath("$.ingredientes.length()").value(2))
                .andExpect(jsonPath("$.ingredientes[0]").value("Pan artesanal"));
    }

    @Test
    void clienteNoPuedeCrearNiEditarProductos() throws Exception {
        Producto producto =
                productos.saveAndFlush(
                        new Producto(
                                "Producto protegido",
                                "Descripción",
                                new BigDecimal("10000"),
                                1,
                                true));

        mvc.perform(
                        post("/api/admin/productos")
                                .header("Authorization", autorizacion(Rol.CLIENTE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitudProducto("Intento", "12000", 3)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(CodigoError.ACCESO_DENEGADO.name()));

        mvc.perform(
                        put("/api/admin/productos/{productoId}", producto.getId())
                                .header("Authorization", autorizacion(Rol.CLIENTE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitudProducto("Intento", "12000", 3)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(CodigoError.ACCESO_DENEGADO.name()));
    }

    @Test
    void datosInvalidosDevuelven400ConErroresPorCampo() throws Exception {
        String cuerpo =
                """
                {
                  "nombre": " ",
                  "descripcion": "",
                  "precio": -1,
                  "stock": -1,
                  "ingredientes": []
                }
                """;

        mvc.perform(
                        post("/api/admin/productos")
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(CodigoError.DATOS_INVALIDOS.name()))
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.descripcion").exists())
                .andExpect(jsonPath("$.campos.precio").exists())
                .andExpect(jsonPath("$.campos.stock").exists())
                .andExpect(jsonPath("$.campos.ingredientes").exists());
    }

    @Test
    void editarProductoInexistenteDevuelve404() throws Exception {
        mvc.perform(
                        put("/api/admin/productos/{productoId}", Long.MAX_VALUE)
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitudProducto("Inexistente", "12000", 3)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(CodigoError.RECURSO_NO_ENCONTRADO.name()));
    }

    @Test
    void listadoAdministrativoIncluyeProductosInactivos() throws Exception {
        productos.save(
                new Producto("Producto activo", "Descripción", new BigDecimal("10000"), 2, true));
        productos.saveAndFlush(
                new Producto(
                        "Producto inactivo", "Descripción", new BigDecimal("12000"), 4, false));

        mvc.perform(get("/api/admin/productos").header("Authorization", autorizacion(Rol.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[1].activo").value(false));
    }

    @Test
    void cambioDePrecioSeReflejaEnDetallePublico() throws Exception {
        Producto producto =
                productos.saveAndFlush(
                        new Producto(
                                "Producto con precio",
                                "Descripción",
                                new BigDecimal("10000"),
                                2,
                                true));

        mvc.perform(
                        put("/api/admin/productos/{productoId}", producto.getId())
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitudProducto("Producto con precio", "22500", 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(22500.00));

        mvc.perform(get("/api/productos/{productoId}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(22500.00));
    }

    @Test
    void cambioDeStockYActivacionSeReflejaEnCatalogoPublico() throws Exception {
        Producto producto =
                productos.saveAndFlush(
                        new Producto(
                                "Producto disponible",
                                "Descripción",
                                new BigDecimal("10000"),
                                3,
                                true));

        mvc.perform(
                        patch("/api/admin/productos/{productoId}/disponibilidad", producto.getId())
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"activo\":true,\"stock\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(jsonPath("$.disponible").value(false));

        mvc.perform(get("/api/productos/{productoId}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(jsonPath("$.disponible").value(false));

        mvc.perform(
                        patch("/api/admin/productos/{productoId}/disponibilidad", producto.getId())
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"activo\":false,\"stock\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(get("/api/productos/{productoId}", producto.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteNoPuedeCambiarDisponibilidadYStockNegativoDevuelve400() throws Exception {
        Producto producto =
                productos.saveAndFlush(
                        new Producto(
                                "Producto protegido",
                                "Descripción",
                                new BigDecimal("10000"),
                                3,
                                true));

        mvc.perform(
                        patch("/api/admin/productos/{productoId}/disponibilidad", producto.getId())
                                .header("Authorization", autorizacion(Rol.CLIENTE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"activo\":false,\"stock\":0}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(CodigoError.ACCESO_DENEGADO.name()));

        mvc.perform(
                        patch("/api/admin/productos/{productoId}/disponibilidad", producto.getId())
                                .header("Authorization", autorizacion(Rol.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"activo\":true,\"stock\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(CodigoError.DATOS_INVALIDOS.name()))
                .andExpect(jsonPath("$.campos.stock").exists());
    }

    private String autorizacion(Rol rol) {
        String token =
                jwt.emitir(
                                new UsuarioAutenticado(
                                        100L, rol.name().toLowerCase() + "@test.com", rol))
                        .valor();
        return "Bearer " + token;
    }

    private String solicitudProducto(String nombre, String precio, int stock) {
        return """
                {
                  "nombre": "%s",
                  "descripcion": "Descripción del producto",
                  "precio": %s,
                  "stock": %d,
                  "ingredientes": ["Pan artesanal", "Queso"]
                }
                """
                .formatted(nombre, precio, stock);
    }
}
