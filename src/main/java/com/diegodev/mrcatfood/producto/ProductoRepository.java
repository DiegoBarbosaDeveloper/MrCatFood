package com.diegodev.mrcatfood.producto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query(
            """
            SELECT DISTINCT producto
            FROM Producto producto
            LEFT JOIN FETCH producto.ingredientes
            WHERE producto.activo = true
            ORDER BY producto.nombre
            """)
    List<Producto> buscarActivosConIngredientes();
}
