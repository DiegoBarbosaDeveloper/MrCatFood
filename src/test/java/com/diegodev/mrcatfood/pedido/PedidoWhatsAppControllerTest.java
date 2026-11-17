package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
class PedidoWhatsAppControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private MockMvc mvc;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicioJwt servicioJwt;

    @Test
    void pedidoPropioDevuelveElEnlaceConElMensajeCompleto() throws Exception {
        Usuario cliente = crearCliente("whatsapp-propio@example.com", "501");
        Pedido pedido =
                crearPedido(
                        "MRC-2026-000201",
                        cliente,
                        crearProducto(),
                        "Sándwich histórico",
                        "20000.00");

        String cuerpo =
                mvc.perform(
                                post("/api/pedidos/{pedidoId}/whatsapp", pedido.getId())
                                        .header("Authorization", "Bearer " + tokenDe(cliente)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.enlace").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String enlace = MAPPER.readTree(cuerpo).get("enlace").asText();
        assertThat(enlace).startsWith("https://wa.me/573001234567?text=");
        assertThat(mensajeDe(enlace))
                .isEqualTo(
                        """
                        Nuevo pedido MrCatFood

                        Código: MRC-2026-000201
                        Cliente: Cliente — 3001234567
                        Ubicación: Conjunto Los Gatos, Apto 501
                        Entrega: 17/11/2026 a las 12:30 p. m.
                        Notas: Sin notas

                        Detalle:
                        - 1 × Sándwich histórico — $ 20.000

                        Total: $ 20.000""");
    }

    @Test
    void pedidoAjenoResponde404() throws Exception {
        Usuario propietario = crearCliente("whatsapp-propietario@example.com", "601");
        Usuario intruso = crearCliente("whatsapp-intruso@example.com", "602");
        Pedido pedido =
                crearPedido(
                        "MRC-2026-000202",
                        propietario,
                        crearProducto(),
                        "Pedido privado",
                        "18000.00");

        mvc.perform(
                        post("/api/pedidos/{pedidoId}/whatsapp", pedido.getId())
                                .header("Authorization", "Bearer " + tokenDe(intruso)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void pedidoInexistenteResponde404() throws Exception {
        Usuario cliente = crearCliente("whatsapp-inexistente@example.com", "701");

        mvc.perform(
                        post("/api/pedidos/{pedidoId}/whatsapp", 999_999L)
                                .header("Authorization", "Bearer " + tokenDe(cliente)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(post("/api/pedidos/{pedidoId}/whatsapp", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    private String mensajeDe(String enlace) {
        String textoCodificado = enlace.substring(enlace.indexOf("?text=") + "?text=".length());
        return URLDecoder.decode(textoCodificado, StandardCharsets.UTF_8);
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
