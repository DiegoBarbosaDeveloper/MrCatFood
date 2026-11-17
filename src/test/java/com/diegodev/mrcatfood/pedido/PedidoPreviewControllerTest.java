package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.producto.Producto;
import com.diegodev.mrcatfood.producto.ProductoRepository;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
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
class PedidoPreviewControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoRepository productos;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ServicioJwt servicioJwt;

    @Test
    void previsualizaTotalesYUbicacionSinPersistirElPedido() throws Exception {
        Usuario cliente = crearClienteConUbicacion();
        Producto pollo =
                productos.save(
                        new Producto(
                                "Sándwich de pollo",
                                "Pollo y vegetales",
                                new BigDecimal("15000.00"),
                                10,
                                true));
        Producto vegetariano =
                productos.saveAndFlush(
                        new Producto(
                                "Sándwich vegetariano",
                                "Vegetales y queso",
                                new BigDecimal("9000.00"),
                                5,
                                true));
        long pedidosAntes = pedidos.count();

        mvc.perform(
                        post("/api/pedidos/preview")
                                .header("Authorization", "Bearer " + tokenDe(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "productos": [
                                            {"productoId": %d, "cantidad": 2},
                                            {"productoId": %d, "cantidad": 1}
                                          ],
                                          "fechaEntrega": "2099-11-17",
                                          "horaEntrega": "12:30",
                                          "notas": "  Sin salsas  "
                                        }
                                        """
                                                .formatted(pollo.getId(), vegetariano.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles.length()").value(2))
                .andExpect(jsonPath("$.detalles[0].nombreProducto").value("Sándwich de pollo"))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(15000.00))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(2))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(30000.00))
                .andExpect(jsonPath("$.detalles[1].subtotal").value(9000.00))
                .andExpect(jsonPath("$.subtotal").value(39000.00))
                .andExpect(jsonPath("$.total").value(39000.00))
                .andExpect(jsonPath("$.conjuntoResidencial").value("Conjunto Los Gatos"))
                .andExpect(jsonPath("$.apartamento").value("Torre 2 - 301"))
                .andExpect(jsonPath("$.fechaEntrega").value("2099-11-17"))
                .andExpect(jsonPath("$.horaEntrega").value("12:30:00"))
                .andExpect(jsonPath("$.notas").value("Sin salsas"));

        assertThat(pedidos.count()).isEqualTo(pedidosAntes);
        assertThat(productos.findById(pollo.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void calculaLosCentavosConRedondeoHalfUp() throws Exception {
        Usuario cliente = crearClienteConUbicacion();
        Producto producto =
                productos.saveAndFlush(
                        new Producto(
                                "Sándwich especial",
                                "Con queso",
                                new BigDecimal("12345.67"),
                                10,
                                true));

        mvc.perform(
                        post("/api/pedidos/preview")
                                .header("Authorization", "Bearer " + tokenDe(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "productos": [{"productoId": %d, "cantidad": 2}],
                                          "fechaEntrega": "2099-11-17",
                                          "horaEntrega": "12:30"
                                        }
                                        """
                                                .formatted(producto.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(12345.67))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(24691.34))
                .andExpect(jsonPath("$.subtotal").value(24691.34))
                .andExpect(jsonPath("$.total").value(24691.34));
    }

    @Test
    void productoInactivoNoPuedePrevisualizarse() throws Exception {
        Usuario cliente = crearClienteConUbicacion();
        Producto inactivo =
                productos.saveAndFlush(
                        new Producto(
                                "Sándwich inactivo",
                                "No ofrecido",
                                new BigDecimal("12000.00"),
                                4,
                                false));

        mvc.perform(
                        post("/api/pedidos/preview")
                                .header("Authorization", "Bearer " + tokenDe(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "productos": [{"productoId": %d, "cantidad": 1}],
                                          "fechaEntrega": "2099-11-17",
                                          "horaEntrega": "12:30"
                                        }
                                        """
                                                .formatted(inactivo.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void cantidadInvalidaDevuelve400() throws Exception {
        Usuario cliente = crearClienteConUbicacion();

        mvc.perform(
                        post("/api/pedidos/preview")
                                .header("Authorization", "Bearer " + tokenDe(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "productos": [{"productoId": 1, "cantidad": 0}],
                                          "fechaEntrega": "2099-11-17",
                                          "horaEntrega": "12:30"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void horaDeEntregaFueraDeLaVentanaDevuelve422() throws Exception {
        Usuario cliente = crearClienteConUbicacion();

        mvc.perform(
                        post("/api/pedidos/preview")
                                .header("Authorization", "Bearer " + tokenDe(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "productos": [{"productoId": 1, "cantidad": 1}],
                                          "fechaEntrega": "2099-11-17",
                                          "horaEntrega": "10:59"
                                        }
                                        """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("FECHA_ENTREGA_INVALIDA"))
                .andExpect(jsonPath("$.detail").value(containsString("11:00")))
                .andExpect(jsonPath("$.detail").value(containsString("21:00")));
    }

    private Usuario crearClienteConUbicacion() {
        Usuario cliente =
                new Usuario(
                        "Cliente Pedido", "preview@example.com", "3001234567", "hash", Rol.CLIENTE);
        cliente.actualizarUbicacion("Conjunto Los Gatos", "Torre 2 - 301");
        return usuarios.saveAndFlush(cliente);
    }

    private String tokenDe(Usuario usuario) {
        return servicioJwt
                .emitir(
                        new UsuarioAutenticado(
                                usuario.getId(), usuario.getEmail(), usuario.getRol()))
                .valor();
    }
}
