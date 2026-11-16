package com.diegodev.mrcatfood.producto;

import java.math.BigDecimal;
import java.util.List;

/** Información pública de un sándwich activo. */
public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        List<String> ingredientes,
        BigDecimal precio,
        Integer stock,
        boolean disponible) {

    public static ProductoResponse de(Producto producto) {
        List<String> nombresIngredientes =
                producto.getIngredientes().stream().map(Ingrediente::getNombre).toList();
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                nombresIngredientes,
                producto.getPrecio(),
                producto.getStock(),
                producto.estaDisponible());
    }
}
