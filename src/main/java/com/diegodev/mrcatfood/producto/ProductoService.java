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
}
