package com.diegodev.mrcatfood.producto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo público de sándwiches activos. */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Catálogo")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(
            summary = "Consultar el catálogo público",
            description = "Devuelve únicamente productos activos.")
    public ResponseEntity<List<ProductoResponse>> consultarCatalogo() {
        return ResponseEntity.ok(servicio.buscarProductosActivos());
    }

    @GetMapping("/{productoId}")
    @Operation(summary = "Consultar el detalle de un producto activo")
    public ResponseEntity<ProductoResponse> consultarDetalle(@PathVariable Long productoId) {
        return ResponseEntity.ok(servicio.buscarProductoActivo(productoId));
    }
}
