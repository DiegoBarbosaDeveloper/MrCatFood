package com.diegodev.mrcatfood.producto;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(
            """
            SELECT DISTINCT producto
            FROM Producto producto
            LEFT JOIN FETCH producto.ingredientes
            ORDER BY producto.nombre
            """)
    List<Producto> buscarTodosConIngredientes();

    @Query(
            """
            SELECT DISTINCT producto
            FROM Producto producto
            LEFT JOIN FETCH producto.ingredientes
            WHERE producto.id = :productoId AND producto.activo = true
            """)
    Optional<Producto> buscarActivoPorIdConIngredientes(@Param("productoId") Long productoId);
}
