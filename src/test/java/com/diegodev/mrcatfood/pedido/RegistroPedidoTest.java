package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.pedido.dto.CrearPedidoRequest;
import com.diegodev.mrcatfood.pedido.dto.PedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.ProductoCantidadRequest;
import com.diegodev.mrcatfood.producto.Producto;
import com.diegodev.mrcatfood.producto.ProductoRepository;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RegistroPedidoTest.RelojPruebaConfig.class)
class RegistroPedidoTest {

    @Autowired private MockMvc mvc;
    @Autowired private PedidoService servicio;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt servicioJwt;

    @BeforeEach
    @AfterEach
    void limpiarDatos() {
        pedidos.deleteAll();
        productos.deleteAll();
        List.of(
                        "registro@example.com",
                        "concurrencia@example.com",
                        "stock-http@example.com",
                        "rollback@example.com")
                .forEach(
                        email -> usuarios.findByEmailIgnoreCase(email).ifPresent(usuarios::delete));
        usuarios.flush();
    }

    @Test
    void registraPedidoConCodigoYCopiasYDescuentaElStock() throws Exception {
        Usuario cliente = crearCliente("registro@example.com");
        Producto producto = crearProducto("Sándwich de pollo", "15000.00", 5);

        mvc.perform(
                        post("/api/pedidos")
                                .header("Authorization", "Bearer " + tokenDe(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "productos": [{"productoId": %d, "cantidad": 2}],
                                          "fechaEntrega": "2026-11-17",
                                          "horaEntrega": "12:30",
                                          "notas": "Sin salsas"
                                        }
                                        """
                                                .formatted(producto.getId())))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        org.hamcrest.Matchers.matchesPattern("/api/pedidos/\\d+")))
                .andExpect(
                        jsonPath(
                                "$.codigo",
                                org.hamcrest.Matchers.matchesPattern("MRC-2026-\\d{6,}")))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.subtotal").value(30000.00))
                .andExpect(jsonPath("$.total").value(30000.00))
                .andExpect(jsonPath("$.conjuntoResidencial").value("Conjunto Los Gatos"))
                .andExpect(jsonPath("$.apartamento").value("Torre 2 - 301"))
                .andExpect(jsonPath("$.detalles[0].nombreProducto").value("Sándwich de pollo"))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(15000.00));

        assertThat(pedidos.count()).isOne();
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(3);
    }

    @Test
    void dosPedidosSimultaneosPorLaUltimaUnidadSoloConfirmanUno() throws Exception {
        Usuario cliente = crearCliente("concurrencia@example.com");
        Producto producto = crearProducto("Último sándwich", "18000.00", 1);
        CrearPedidoRequest solicitud = solicitud(producto.getId(), 1);
        var listos = new CountDownLatch(2);
        var inicio = new CountDownLatch(1);

        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            Future<Object> primerResultado =
                    ejecutor.submit(
                            () ->
                                    registrarAlMismoTiempo(
                                            cliente.getId(), solicitud, listos, inicio));
            Future<Object> segundoResultado =
                    ejecutor.submit(
                            () ->
                                    registrarAlMismoTiempo(
                                            cliente.getId(), solicitud, listos, inicio));
            assertThat(listos.await(5, TimeUnit.SECONDS)).isTrue();
            inicio.countDown();

            List<Object> resultados =
                    List.of(
                            primerResultado.get(10, TimeUnit.SECONDS),
                            segundoResultado.get(10, TimeUnit.SECONDS));
            assertThat(resultados).filteredOn(PedidoResponse.class::isInstance).hasSize(1);
            assertThat(resultados)
                    .filteredOn(ApiException.class::isInstance)
                    .singleElement()
                    .satisfies(
                            resultado ->
                                    assertThat(((ApiException) resultado).codigo())
                                            .isEqualTo(CodigoError.STOCK_INSUFICIENTE));
        }

        assertThat(pedidos.count()).isOne();
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isZero();
    }

    @Test
    void stockInsuficienteDevuelve409SinCrearPedido() throws Exception {
        Usuario cliente = crearCliente("stock-http@example.com");
        Producto producto = crearProducto("Sándwich limitado", "16000.00", 1);

        mvc.perform(
                        post("/api/pedidos")
                                .header("Authorization", "Bearer " + tokenDe(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "productos": [{"productoId": %d, "cantidad": 2}],
                                          "fechaEntrega": "2026-11-17",
                                          "horaEntrega": "12:30"
                                        }
                                        """
                                                .formatted(producto.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("STOCK_INSUFICIENTE"));

        assertThat(pedidos.count()).isZero();
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isOne();
    }

    @Test
    void stockInsuficienteRevierteDescuentosAnteriores() {
        Usuario cliente = crearCliente("rollback@example.com");
        Producto disponible = crearProducto("Primero disponible", "10000.00", 3);
        Producto insuficiente = crearProducto("Segundo insuficiente", "12000.00", 1);
        CrearPedidoRequest solicitud =
                new CrearPedidoRequest(
                        List.of(
                                new ProductoCantidadRequest(disponible.getId(), 2),
                                new ProductoCantidadRequest(insuficiente.getId(), 2)),
                        LocalDate.of(2026, 11, 17),
                        LocalTime.of(12, 30),
                        null);

        assertThatThrownBy(() -> servicio.registrarPedido(cliente.getId(), solicitud))
                .isInstanceOfSatisfying(
                        ApiException.class,
                        excepcion ->
                                assertThat(excepcion.codigo())
                                        .isEqualTo(CodigoError.STOCK_INSUFICIENTE));

        assertThat(pedidos.count()).isZero();
        assertThat(productos.findById(disponible.getId()).orElseThrow().getStock()).isEqualTo(3);
        assertThat(productos.findById(insuficiente.getId()).orElseThrow().getStock()).isEqualTo(1);
    }

    private Object registrarAlMismoTiempo(
            Long clienteId,
            CrearPedidoRequest solicitud,
            CountDownLatch listos,
            CountDownLatch inicio)
            throws InterruptedException {
        listos.countDown();
        inicio.await();
        try {
            return servicio.registrarPedido(clienteId, solicitud);
        } catch (ApiException excepcion) {
            return excepcion;
        }
    }

    private CrearPedidoRequest solicitud(Long productoId, int cantidad) {
        return new CrearPedidoRequest(
                List.of(new ProductoCantidadRequest(productoId, cantidad)),
                LocalDate.of(2026, 11, 17),
                LocalTime.of(12, 30),
                null);
    }

    private Usuario crearCliente(String email) {
        Usuario cliente = new Usuario("Cliente Pedido", email, "3001234567", "hash", Rol.CLIENTE);
        cliente.actualizarUbicacion("Conjunto Los Gatos", "Torre 2 - 301");
        return usuarios.saveAndFlush(cliente);
    }

    private Producto crearProducto(String nombre, String precio, int stock) {
        return productos.saveAndFlush(
                new Producto(nombre, "Descripción", new BigDecimal(precio), stock, true));
    }

    private String tokenDe(Usuario usuario) {
        return servicioJwt
                .emitir(
                        new UsuarioAutenticado(
                                usuario.getId(), usuario.getEmail(), usuario.getRol()))
                .valor();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RelojPruebaConfig {

        @Bean
        @Primary
        Clock relojPruebas() {
            return Clock.fixed(Instant.parse("2026-11-16T17:00:00Z"), ZoneId.of("America/Bogota"));
        }
    }
}
