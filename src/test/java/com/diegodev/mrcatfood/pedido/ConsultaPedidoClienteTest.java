package com.diegodev.mrcatfood.pedido;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.time.LocalDate;
import java.time.LocalTime;
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
class ConsultaPedidoClienteTest {

    @Autowired private MockMvc mvc;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt servicioJwt;

    @Test
    void historialDevuelveUnicamenteLosPedidosDelCliente() throws Exception {
        Usuario primerCliente = crearCliente("historial-uno@example.com", "Apto 101");
        Usuario segundoCliente = crearCliente("historial-dos@example.com", "Apto 202");
        Producto producto = crearProducto();
        crearPedido("MRC-2026-000101", primerCliente, producto, "Nombre histórico", "14000.00");
        crearPedido("MRC-2026-000102", primerCliente, producto, "Otro pedido", "15000.00");
        crearPedido("MRC-2026-000103", segundoCliente, producto, "Pedido ajeno", "16000.00");

        mvc.perform(
                        get("/api/pedidos/mios")
                                .header("Authorization", "Bearer " + tokenDe(primerCliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.codigo == 'MRC-2026-000101')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.codigo == 'MRC-2026-000102')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.codigo == 'MRC-2026-000103')]").isEmpty());
    }

    @Test
    void detallePropioConservaLasCopiasHistoricas() throws Exception {
        Usuario cliente = crearCliente("detalle-propio@example.com", "Apto 301");
        Producto producto = crearProducto();
        Pedido pedido =
                crearPedido("MRC-2026-000104", cliente, producto, "Sándwich anterior", "12500.00");
        producto.actualizar(
                "Sándwich actualizado",
                "Descripción actualizada",
                new BigDecimal("30000.00"),
                10,
                java.util.List.of("Pan"));
        productos.saveAndFlush(producto);
        cliente.actualizarUbicacion("Conjunto Nuevo", "Apto 999");
        usuarios.saveAndFlush(cliente);

        mvc.perform(
                        get("/api/pedidos/{pedidoId}", pedido.getId())
                                .header("Authorization", "Bearer " + tokenDe(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("MRC-2026-000104"))
                .andExpect(jsonPath("$.conjuntoResidencial").value("Conjunto Los Gatos"))
                .andExpect(jsonPath("$.apartamento").value("Apto 301"))
                .andExpect(jsonPath("$.detalles[0].nombreProducto").value("Sándwich anterior"))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(12500.00));
    }

    @Test
    void pedidoAjenoResponde404() throws Exception {
        Usuario propietario = crearCliente("propietario@example.com", "Apto 401");
        Usuario intruso = crearCliente("intruso@example.com", "Apto 402");
        Pedido pedido =
                crearPedido(
                        "MRC-2026-000105",
                        propietario,
                        crearProducto(),
                        "Pedido privado",
                        "18000.00");

        mvc.perform(
                        get("/api/pedidos/{pedidoId}", pedido.getId())
                                .header("Authorization", "Bearer " + tokenDe(intruso)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    private Usuario crearCliente(String email, String apartamento) {
        Usuario cliente = new Usuario("Cliente", email, "3001234567", "hash", Rol.CLIENTE);
        cliente.actualizarUbicacion("Conjunto Los Gatos", apartamento);
        return usuarios.saveAndFlush(cliente);
    }

    private Producto crearProducto() {
        return productos.saveAndFlush(
                new Producto(
                        "Sándwich actual", "Descripción", new BigDecimal("20000.00"), 20, true));
    }

    private Pedido crearPedido(
            String codigo,
            Usuario cliente,
            Producto producto,
            String nombreHistorico,
            String precioHistorico) {
        BigDecimal precio = new BigDecimal(precioHistorico);
        Pedido pedido =
                new Pedido(
                        codigo,
                        cliente,
                        LocalDate.of(2026, 11, 17),
                        LocalTime.of(12, 30),
                        null,
                        precio,
                        precio);
        pedido.agregarDetalle(producto, nombreHistorico, precio, 1);
        return pedidos.saveAndFlush(pedido);
    }

    private String tokenDe(Usuario usuario) {
        return servicioJwt
                .emitir(
                        new UsuarioAutenticado(
                                usuario.getId(), usuario.getEmail(), usuario.getRol()))
                .valor();
    }
}
