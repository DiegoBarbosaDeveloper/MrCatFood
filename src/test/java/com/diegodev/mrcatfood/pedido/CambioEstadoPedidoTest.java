package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.producto.Producto;
import com.diegodev.mrcatfood.producto.ProductoRepository;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
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
class CambioEstadoPedidoTest {

    @Autowired private MockMvc mvc;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt servicioJwt;
    @Autowired private EntityManager entityManager;

    private Usuario admin;
    private Usuario cliente;

    @BeforeEach
    void preparar() {
        admin = crearUsuario("admin-estado@example.com", Rol.ADMIN);
        cliente = crearUsuario("cliente-estado@example.com", Rol.CLIENTE);
    }

    @Test
    void avanzaPorLasTransicionesPermitidasYRechazaCambiosDesdeEntregado() throws Exception {
        Producto producto = crearProducto(8);
        Pedido pedido = crearPedido("MRC-2026-000301", producto, 1);
        String tokenAdmin = tokenDe(admin);

        cambiarEstado(pedido.getId(), tokenAdmin, "CONFIRMADO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
        cambiarEstado(pedido.getId(), tokenAdmin, "EN_PREPARACION")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));
        cambiarEstado(pedido.getId(), tokenAdmin, "ENTREGADO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
        cambiarEstado(pedido.getId(), tokenAdmin, "CANCELADO")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));
    }

    @Test
    void cancelarDevuelveElStockUnaSolaVez() throws Exception {
        Producto producto = crearProducto(3);
        Pedido pedido = crearPedido("MRC-2026-000302", producto, 2);
        String tokenAdmin = tokenDe(admin);

        cambiarEstado(pedido.getId(), tokenAdmin, "CANCELADO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
        entityManager.clear();
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(5);

        cambiarEstado(pedido.getId(), tokenAdmin, "CANCELADO")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));
        entityManager.clear();
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(5);
    }

    @Test
    void clienteNoPuedeCambiarElEstado() throws Exception {
        Pedido pedido = crearPedido("MRC-2026-000303", crearProducto(4), 1);

        cambiarEstado(pedido.getId(), tokenDe(cliente), "CONFIRMADO")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    private org.springframework.test.web.servlet.ResultActions cambiarEstado(
            Long pedidoId, String token, String estado) throws Exception {
        return mvc.perform(
                patch("/api/admin/pedidos/{pedidoId}/estado", pedidoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"" + estado + "\"}"));
    }

    private Usuario crearUsuario(String email, Rol rol) {
        Usuario usuario = new Usuario("Usuario", email, "3001234567", "hash", rol);
        usuario.actualizarUbicacion("Conjunto Los Gatos", "Apto 601");
        return usuarios.saveAndFlush(usuario);
    }

    private Producto crearProducto(int stock) {
        return productos.saveAndFlush(
                new Producto(
                        "Sándwich de estado",
                        "Descripción",
                        new BigDecimal("15000.00"),
                        stock,
                        true));
    }

    private Pedido crearPedido(String codigo, Producto producto, int cantidad) {
        BigDecimal precio = new BigDecimal("15000.00");
        BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
        Pedido pedido =
                new Pedido(
                        codigo,
                        cliente,
                        LocalDate.of(2026, 11, 17),
                        LocalTime.of(13, 0),
                        null,
                        total,
                        total);
        pedido.agregarDetalle(producto, "Copia de estado", precio, cantidad);
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
