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
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminPedidoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt servicioJwt;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;

    private Usuario admin;
    private Usuario cliente;
    private Producto producto;

    @BeforeEach
    void preparar() {
        admin = crearUsuario("admin-pedidos@example.com", Rol.ADMIN);
        cliente = crearUsuario("cliente-admin-pedidos@example.com", Rol.CLIENTE);
        producto =
                productos.saveAndFlush(
                        new Producto(
                                "Sándwich administrativo",
                                "Descripción",
                                new BigDecimal("17000.00"),
                                20,
                                true));
    }

    @Test
    void listaPedidosConFiltrosYPaginacion() throws Exception {
        crearPedido("MRC-2026-000201", LocalDate.of(2026, 11, 17));
        Pedido confirmado = crearPedido("MRC-2026-000202", LocalDate.of(2026, 11, 18));
        crearPedido("MRC-2026-000203", LocalDate.of(2026, 11, 20));
        jdbc.update("UPDATE pedido SET estado = 'CONFIRMADO' WHERE id = ?", confirmado.getId());
        entityManager.clear();

        mvc.perform(
                        get("/api/admin/pedidos")
                                .header("Authorization", "Bearer " + tokenDe(admin))
                                .queryParam("estado", "PENDIENTE")
                                .queryParam("fechaDesde", "2026-11-17")
                                .queryParam("fechaHasta", "2026-11-20")
                                .queryParam("pagina", "0")
                                .queryParam("tamano", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.contenido[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.contenido[0].cliente.nombre").value("Cliente Pedidos"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamano").value(1))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void adminConsultaDetalleDeCualquierPedido() throws Exception {
        Pedido pedido = crearPedido("MRC-2026-000204", LocalDate.of(2026, 11, 19));

        mvc.perform(
                        get("/api/admin/pedidos/{pedidoId}", pedido.getId())
                                .header("Authorization", "Bearer " + tokenDe(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("MRC-2026-000204"))
                .andExpect(jsonPath("$.cliente.email").doesNotExist())
                .andExpect(jsonPath("$.cliente.nombre").value("Cliente Pedidos"))
                .andExpect(jsonPath("$.cliente.telefono").value("3001234567"))
                .andExpect(jsonPath("$.detalles[0].nombreProducto").value("Copia histórica"))
                .andExpect(jsonPath("$.total").value(17000.00));
    }

    @Test
    void clienteNoPuedeConsultarListadoAdministrativo() throws Exception {
        mvc.perform(get("/api/admin/pedidos").header("Authorization", "Bearer " + tokenDe(cliente)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void rangoInvertidoYEstadoDesconocidoDevuelven400() throws Exception {
        mvc.perform(
                        get("/api/admin/pedidos")
                                .header("Authorization", "Bearer " + tokenDe(admin))
                                .queryParam("fechaDesde", "2026-11-20")
                                .queryParam("fechaHasta", "2026-11-17"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));

        mvc.perform(
                        get("/api/admin/pedidos")
                                .header("Authorization", "Bearer " + tokenDe(admin))
                                .queryParam("estado", "DESCONOCIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    private Usuario crearUsuario(String email, Rol rol) {
        String nombre = rol == Rol.ADMIN ? "Admin Pedidos" : "Cliente Pedidos";
        Usuario usuario = new Usuario(nombre, email, "3001234567", "hash", rol);
        usuario.actualizarUbicacion("Conjunto Los Gatos", "Apto 501");
        return usuarios.saveAndFlush(usuario);
    }

    private Pedido crearPedido(String codigo, LocalDate fechaEntrega) {
        BigDecimal total = new BigDecimal("17000.00");
        Pedido pedido =
                new Pedido(codigo, cliente, fechaEntrega, LocalTime.of(13, 0), null, total, total);
        pedido.agregarDetalle(producto, "Copia histórica", total, 1);
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
