package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.producto.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Línea persistida con copias históricas del nombre y precio del producto. */
@Entity
@Table(name = "detalle_pedido")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false, length = 120)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public DetallePedido(
            Pedido pedido,
            Producto producto,
            String nombreProducto,
            BigDecimal precioUnitario,
            Integer cantidad) {
        this.pedido = pedido;
        this.producto = producto;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario.setScale(2, RoundingMode.HALF_UP);
        this.cantidad = cantidad;
        this.subtotal =
                this.precioUnitario
                        .multiply(BigDecimal.valueOf(cantidad))
                        .setScale(2, RoundingMode.HALF_UP);
    }
}
