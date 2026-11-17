package com.diegodev.mrcatfood.producto;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> consultarProductos() {
        return ResponseEntity.ok(servicio.buscarTodosLosProductos());
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

    @PatchMapping("/{productoId}/disponibilidad")
    public ResponseEntity<ProductoResponse> actualizarDisponibilidad(
            @PathVariable Long productoId, @Valid @RequestBody DisponibilidadRequest solicitud) {
        return ResponseEntity.ok(servicio.actualizarDisponibilidad(productoId, solicitud));
    }
}
