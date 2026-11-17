package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.config.OpenApiConfig;
import com.diegodev.mrcatfood.pedido.dto.CambiarEstadoRequest;
import com.diegodev.mrcatfood.pedido.dto.PaginaResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResumenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consulta administrativa global de pedidos. */
@RestController
@RequestMapping("/api/admin/pedidos")
@Tag(name = "Administración")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class AdminPedidoController {

    private final PedidoService servicio;

    public AdminPedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(
            summary = "Consultar todos los pedidos",
            description = "Admite filtros y paginación.")
    public ResponseEntity<PaginaResponse<PedidoResumenResponse>> consultarPedidos(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return ResponseEntity.ok(
                servicio.consultarPedidosAdministracion(
                        estado, fechaDesde, fechaHasta, pagina, tamano));
    }

    @GetMapping("/{pedidoId}")
    @Operation(summary = "Consultar el detalle administrativo de un pedido")
    public ResponseEntity<PedidoResponse> consultarDetalle(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(servicio.consultarPedidoAdministracion(pedidoId));
    }

    @PatchMapping("/{pedidoId}/estado")
    @Operation(summary = "Cambiar el estado de un pedido")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long pedidoId, @Valid @RequestBody CambiarEstadoRequest solicitud) {
        return ResponseEntity.ok(servicio.cambiarEstadoPedido(pedidoId, solicitud.estado()));
    }
}
