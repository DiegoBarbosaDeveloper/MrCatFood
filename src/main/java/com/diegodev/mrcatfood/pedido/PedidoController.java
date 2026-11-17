package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.config.OpenApiConfig;
import com.diegodev.mrcatfood.pedido.dto.CrearPedidoRequest;
import com.diegodev.mrcatfood.pedido.dto.EnlaceWhatsAppResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PrevisualizacionPedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PrevisualizarPedidoRequest;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operaciones del cliente autenticado sobre sus pedidos. */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class PedidoController {

    private final PedidoService servicio;
    private final WhatsAppService servicioWhatsApp;

    public PedidoController(PedidoService servicio, WhatsAppService servicioWhatsApp) {
        this.servicio = servicio;
        this.servicioWhatsApp = servicioWhatsApp;
    }

    @PostMapping("/preview")
    @Operation(
            summary = "Previsualizar un pedido",
            description = "Calcula el resumen sin persistirlo.")
    public ResponseEntity<PrevisualizacionPedidoResponse> previsualizarPedido(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @Valid @RequestBody PrevisualizarPedidoRequest solicitud) {
        return ResponseEntity.ok(servicio.previsualizarPedido(principal.id(), solicitud));
    }

    @PostMapping
    @Operation(
            summary = "Registrar un pedido",
            description = "Recalcula los totales y descuenta el stock.")
    public ResponseEntity<PedidoResponse> registrarPedido(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @Valid @RequestBody CrearPedidoRequest solicitud) {
        PedidoResponse pedido = servicio.registrarPedido(principal.id(), solicitud);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/pedidos/" + pedido.id()))
                .body(pedido);
    }

    @GetMapping("/mios")
    @Operation(summary = "Consultar los pedidos del usuario autenticado")
    public ResponseEntity<List<PedidoResponse>> consultarPedidosPropios(
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(servicio.consultarPedidosCliente(principal.id()));
    }

    @GetMapping("/{pedidoId}")
    @Operation(summary = "Consultar un pedido propio")
    public ResponseEntity<PedidoResponse> consultarPedidoPropio(
            @AuthenticationPrincipal UsuarioAutenticado principal, @PathVariable Long pedidoId) {
        return ResponseEntity.ok(servicio.consultarPedidoCliente(principal.id(), pedidoId));
    }

    @PostMapping("/{pedidoId}/whatsapp")
    @Operation(summary = "Generar el enlace de WhatsApp de un pedido propio")
    public ResponseEntity<EnlaceWhatsAppResponse> generarEnlaceWhatsApp(
            @AuthenticationPrincipal UsuarioAutenticado principal, @PathVariable Long pedidoId) {
        String enlace = servicioWhatsApp.generarEnlaceParaCliente(principal.id(), pedidoId);
        return ResponseEntity.ok(new EnlaceWhatsAppResponse(enlace));
    }
}
