package com.diegodev.mrcatfood.producto;

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
}
