package com.diegodev.mrcatfood.producto;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductosActivos() {
        return productos.buscarActivosConIngredientes().stream().map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscarProductoActivo(Long productoId) {
        Producto producto =
                productos
                        .buscarActivoPorIdConIngredientes(productoId)
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                CodigoError.RECURSO_NO_ENCONTRADO,
                                                "Producto no encontrado"));
        return ProductoResponse.de(producto);
    }

    @Transactional
    public ProductoResponse crearProducto(ProductoRequest solicitud) {
        Producto producto =
                new Producto(
                        solicitud.nombre(),
                        solicitud.descripcion(),
                        solicitud.precio(),
                        solicitud.stock(),
                        true);
        solicitud.ingredientes().forEach(producto::agregarIngrediente);
        return ProductoResponse.de(productos.save(producto));
    }

    @Transactional
    public ProductoResponse actualizarProducto(Long productoId, ProductoRequest solicitud) {
        Producto producto = buscarProductoPorId(productoId);
        producto.actualizar(
                solicitud.nombre(),
                solicitud.descripcion(),
                solicitud.precio(),
                solicitud.stock(),
                solicitud.ingredientes());
        return ProductoResponse.de(producto);
    }

    private Producto buscarProductoPorId(Long productoId) {
        return productos
                .findById(productoId)
                .orElseThrow(
                        () ->
                                new ApiException(
                                        CodigoError.RECURSO_NO_ENCONTRADO,
                                        "Producto no encontrado"));
    }
}
