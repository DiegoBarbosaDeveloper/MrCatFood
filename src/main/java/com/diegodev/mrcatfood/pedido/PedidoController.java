package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.pedido.dto.PrevisualizacionPedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PrevisualizarPedidoRequest;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operaciones del cliente autenticado sobre sus pedidos. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/preview")
    public ResponseEntity<PrevisualizacionPedidoResponse> previsualizarPedido(
            @AuthenticationPrincipal UsuarioAutenticado principal,
            @Valid @RequestBody PrevisualizarPedidoRequest solicitud) {
        return ResponseEntity.ok(servicio.previsualizarPedido(principal.id(), solicitud));
    }
}
