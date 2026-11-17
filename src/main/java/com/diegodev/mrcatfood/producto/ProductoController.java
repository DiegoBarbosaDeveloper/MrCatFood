package com.diegodev.mrcatfood.producto;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo público de sándwiches activos. */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> consultarCatalogo() {
        return ResponseEntity.ok(servicio.buscarProductosActivos());
    }

    @GetMapping("/{productoId}")
    public ResponseEntity<ProductoResponse> consultarDetalle(@PathVariable Long productoId) {
        return ResponseEntity.ok(servicio.buscarProductoActivo(productoId));
    }
}
