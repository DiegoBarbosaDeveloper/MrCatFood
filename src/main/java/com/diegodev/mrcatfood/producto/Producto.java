package com.diegodev.mrcatfood.producto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Sándwich ofrecido en el catálogo con su inventario disponible. */
@Entity
@Table(name = "producto")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 600)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean activo = true;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Ingrediente> ingredientes = new ArrayList<>();

    public Producto(
            String nombre, String descripcion, BigDecimal precio, Integer stock, boolean activo) {
        this.activo = activo;
        actualizarDatos(nombre, descripcion, precio, stock);
    }

    public void agregarIngrediente(String nombreIngrediente) {
        ingredientes.add(new Ingrediente(this, nombreIngrediente.trim()));
    }

    public void actualizar(
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            List<String> nombresIngredientes) {
        actualizarDatos(nombre, descripcion, precio, stock);
        ingredientes.clear();
        nombresIngredientes.forEach(this::agregarIngrediente);
    }

    public boolean estaDisponible() {
        return activo && stock > 0;
    }

    private void actualizarDatos(
            String nombre, String descripcion, BigDecimal precio, Integer stock) {
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
        this.precio = precio.setScale(2, RoundingMode.HALF_UP);
        this.stock = stock;
    }
}
