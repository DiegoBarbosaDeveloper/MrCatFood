package com.diegodev.mrcatfood.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Ingrediente declarado por un producto; no pertenece a un catálogo independiente. */
@Entity
@Table(name = "producto_ingrediente")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 120)
    private String nombre;

    Ingrediente(Producto producto, String nombre) {
        this.producto = producto;
        this.nombre = nombre;
    }
}
