package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.producto.Producto;
import com.diegodev.mrcatfood.usuario.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** Pedido persistido con copias históricas de la ubicación y los importes. */
@Entity
@Table(name = "pedido")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(name = "conjunto_residencial", nullable = false, length = 120)
    private String conjuntoResidencial;

    @Column(nullable = false, length = 60)
    private String apartamento;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @Column(name = "hora_entrega", nullable = false)
    private LocalTime horaEntrega;

    @Column(length = 500)
    private String notas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido(
            String codigoTemporal,
            Usuario cliente,
            LocalDate fechaEntrega,
            LocalTime horaEntrega,
            String notas,
            BigDecimal subtotal,
            BigDecimal total) {
        this.codigo = codigoTemporal;
        this.cliente = cliente;
        this.conjuntoResidencial = cliente.getConjuntoResidencial();
        this.apartamento = cliente.getApartamento();
        this.fechaEntrega = fechaEntrega;
        this.horaEntrega = horaEntrega;
        this.notas = notas;
        this.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        this.total = total.setScale(2, RoundingMode.HALF_UP);
        this.estado = EstadoPedido.PENDIENTE;
    }

    public void asignarCodigoDefinitivo(String codigo) {
        this.codigo = codigo;
    }

    public void agregarDetalle(
            Producto producto, String nombreProducto, BigDecimal precioUnitario, Integer cantidad) {
        detalles.add(new DetallePedido(this, producto, nombreProducto, precioUnitario, cantidad));
    }
}
