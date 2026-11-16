package com.diegodev.mrcatfood.producto;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Administración del catálogo, protegida por el rol ADMIN en la cadena de seguridad. */
@RestController
@RequestMapping("/api/admin/productos")
public class AdminProductoController {

    private final ProductoService servicio;

    public AdminProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestBody ProductoRequest solicitud) {
        return ResponseEntity.status(201).body(servicio.crearProducto(solicitud));
    }

    @PutMapping("/{productoId}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long productoId, @Valid @RequestBody ProductoRequest solicitud) {
        return ResponseEntity.ok(servicio.actualizarProducto(productoId, solicitud));
    }
}
