package com.diegodev.mrcatfood.pedido;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository
        extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    @Override
    @EntityGraph(attributePaths = "cliente")
    Page<Pedido> findAll(Specification<Pedido> specification, Pageable pageable);

    @Query(
            """
            SELECT DISTINCT pedido
            FROM Pedido pedido
            LEFT JOIN FETCH pedido.detalles detalle
            LEFT JOIN FETCH detalle.producto
            WHERE pedido.cliente.id = :clienteId
            ORDER BY pedido.creadoEn DESC
            """)
    List<Pedido> buscarTodosDelCliente(@Param("clienteId") Long clienteId);

    @Query(
            """
            SELECT DISTINCT pedido
            FROM Pedido pedido
            LEFT JOIN FETCH pedido.detalles detalle
            LEFT JOIN FETCH detalle.producto
            WHERE pedido.id = :pedidoId AND pedido.cliente.id = :clienteId
            """)
    Optional<Pedido> buscarPorIdYCliente(
            @Param("pedidoId") Long pedidoId, @Param("clienteId") Long clienteId);

    @Query(
            """
            SELECT DISTINCT pedido
            FROM Pedido pedido
            JOIN FETCH pedido.cliente
            LEFT JOIN FETCH pedido.detalles detalle
            LEFT JOIN FETCH detalle.producto
            WHERE pedido.id = :pedidoId
            """)
    Optional<Pedido> buscarDetalleAdministrativo(@Param("pedidoId") Long pedidoId);
}
