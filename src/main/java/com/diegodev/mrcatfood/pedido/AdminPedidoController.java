package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.pedido.dto.CambiarEstadoRequest;
import com.diegodev.mrcatfood.pedido.dto.PaginaResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResumenResponse;
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
public class AdminPedidoController {

    private final PedidoService servicio;

    public AdminPedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
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
    public ResponseEntity<PedidoResponse> consultarDetalle(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(servicio.consultarPedidoAdministracion(pedidoId));
    }

    @PatchMapping("/{pedidoId}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long pedidoId, @Valid @RequestBody CambiarEstadoRequest solicitud) {
        return ResponseEntity.ok(servicio.cambiarEstadoPedido(pedidoId, solicitud.estado()));
    }
}
