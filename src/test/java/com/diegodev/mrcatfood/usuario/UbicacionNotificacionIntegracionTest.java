package com.diegodev.mrcatfood.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegodev.mrcatfood.comun.BaseIntegracionTest;
import com.diegodev.mrcatfood.pedido.Pedido;
import com.diegodev.mrcatfood.pedido.PedidoRepository;
import com.diegodev.mrcatfood.producto.Producto;
import com.diegodev.mrcatfood.producto.ProductoRepository;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class UbicacionNotificacionIntegracionTest extends BaseIntegracionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ProductoRepository productos;
    @Autowired private ServicioJwt jwt;

    @Test
    void guardaLaUbicacionDelClienteAutenticado() throws Exception {
        String token = registrarCliente("ubicacion-integracion@example.com");

        mvc.perform(
                        put("/api/usuarios/yo/ubicacion")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"conjuntoResidencial":"  Torres del Parque  ","apartamento":"  302  "}
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conjuntoResidencial").value("Torres del Parque"))
                .andExpect(jsonPath("$.apartamento").value("302"));

        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conjuntoResidencial").value("Torres del Parque"))
                .andExpect(jsonPath("$.apartamento").value("302"));
    }

    @Test
    void tokenSoloPermiteModificarLaUbicacionPropia() throws Exception {
        String token = registrarCliente("ubicacion-propia-integracion@example.com");
        Usuario otro =
                usuarios.saveAndFlush(
                        new Usuario(
                                "Otro",
                                "ubicacion-otra-integracion@example.com",
                                "3009999999",
                                "hash",
                                Rol.CLIENTE));

        mvc.perform(
                        put("/api/usuarios/yo/ubicacion")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"conjuntoResidencial":"Conjunto propio","apartamento":"101"}
                                        """))
                .andExpect(status().isOk());

        assertThat(usuarios.findById(otro.getId()).orElseThrow().getConjuntoResidencial()).isNull();
        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.conjuntoResidencial").value("Conjunto propio"));
    }

    @Test
    void pedidoAjenoNoFiltraElEnlaceDeWhatsApp() throws Exception {
        Usuario propietario = crearCliente("whatsapp-propietario-integracion@example.com", "501");
        Usuario intruso = crearCliente("whatsapp-intruso-integracion@example.com", "601");
        Pedido pedido = crearPedido(propietario);

        mvc.perform(
                        post("/api/pedidos/{pedidoId}/whatsapp", pedido.getId())
                                .header("Authorization", "Bearer " + tokenDe(intruso)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void enlaceDeWhatsappContieneElMensajeDelPedidoPropio() throws Exception {
        Usuario propietario = crearCliente("whatsapp-propio-integracion@example.com", "701");
        Pedido pedido = crearPedido(propietario);

        String cuerpo =
                mvc.perform(
                                post("/api/pedidos/{pedidoId}/whatsapp", pedido.getId())
                                        .header("Authorization", "Bearer " + tokenDe(propietario)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.enlace").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String enlace = MAPPER.readTree(cuerpo).get("enlace").asText();
        assertThat(enlace).startsWith("https://wa.me/573001234567?text=");
        assertThat(mensajeDe(enlace))
                .contains("Ubicación: Conjunto Los Gatos, Apto 701")
                .contains("Entrega: 17/11/2026 a las 12:30 p. m.")
                .contains("Total: $ 20.000")
                .contains("Notas: Sin notas");
    }

    private String registrarCliente(String email) throws Exception {
        String token =
                MAPPER.readTree(
                                mvc.perform(
                                                post("/api/auth/registro")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                """
                                                                {"nombre":"Cliente","email":"%s","telefono":"3001234567","password":"clave1234"}
                                                                """
                                                                        .formatted(email)))
                                        .andExpect(status().isCreated())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString())
                        .get("token")
                        .asText();
        return token;
    }

    private Usuario crearCliente(String email, String apartamento) {
        Usuario cliente = new Usuario("Cliente", email, "3001234567", "hash", Rol.CLIENTE);
        cliente.actualizarUbicacion("Conjunto Los Gatos", apartamento);
        return usuarios.saveAndFlush(cliente);
    }

    private Pedido crearPedido(Usuario cliente) {
        Producto producto =
                productos.saveAndFlush(
                        new Producto(
                                "Sándwich histórico",
                                "Descripción",
                                new BigDecimal("20000.00"),
                                20,
                                true));
        Pedido pedido =
                new Pedido(
                        "MRC-2026-000301",
                        cliente,
                        LocalDate.of(2026, 11, 17),
                        LocalTime.of(12, 30),
                        null,
                        new BigDecimal("20000.00"),
                        new BigDecimal("20000.00"));
        pedido.agregarDetalle(producto, "Sándwich histórico", new BigDecimal("20000.00"), 1);
        return pedidos.saveAndFlush(pedido);
    }

    private String tokenDe(Usuario usuario) {
        return jwt.emitir(new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), Rol.CLIENTE))
                .valor();
    }

    private String mensajeDe(String enlace) {
        String textoCodificado = enlace.substring(enlace.indexOf("?text=") + "?text=".length());
        return URLDecoder.decode(textoCodificado, StandardCharsets.UTF_8);
    }
}
