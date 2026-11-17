package com.diegodev.mrcatfood.producto;

import com.diegodev.mrcatfood.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Administración")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class AdminProductoController {

    private final ProductoService servicio;

    public AdminProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(
            summary = "Consultar todos los productos",
            description = "Incluye productos inactivos.")
    public ResponseEntity<List<ProductoResponse>> consultarProductos() {
        return ResponseEntity.ok(servicio.buscarTodosLosProductos());
    }

    @PostMapping
    @Operation(summary = "Crear un producto")
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestBody ProductoRequest solicitud) {
        return ResponseEntity.status(201).body(servicio.crearProducto(solicitud));
    }

    @PutMapping("/{productoId}")
    @Operation(summary = "Actualizar un producto")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long productoId, @Valid @RequestBody ProductoRequest solicitud) {
        return ResponseEntity.ok(servicio.actualizarProducto(productoId, solicitud));
    }

    @PatchMapping("/{productoId}/disponibilidad")
    @Operation(summary = "Actualizar stock y disponibilidad")
    public ResponseEntity<ProductoResponse> actualizarDisponibilidad(
            @PathVariable Long productoId, @Valid @RequestBody DisponibilidadRequest solicitud) {
        return ResponseEntity.ok(servicio.actualizarDisponibilidad(productoId, solicitud));
    }
}
