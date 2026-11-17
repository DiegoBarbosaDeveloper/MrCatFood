package com.diegodev.mrcatfood.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Cuenta de una persona en el sistema.
 *
 * <p>El correo es la credencial de acceso: se normaliza a minúsculas (RN-06) y su unicidad la
 * garantiza el índice funcional {@code ix_usuario_email} de la migración V1.
 */
@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(name = "conjunto_residencial", length = 120)
    private String conjuntoResidencial;

    @Column(length = 60)
    private String apartamento;

    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    public Usuario(String nombre, String email, String telefono, String passwordHash, Rol rol) {
        this.nombre = nombre;
        this.email = normalizarEmail(email);
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    /** Reemplaza la ubicación registrada y la deja lista para copiarla a un pedido. */
    public void actualizarUbicacion(String conjuntoResidencial, String apartamento) {
        this.conjuntoResidencial = conjuntoResidencial.trim();
        this.apartamento = apartamento.trim();
    }

    /** RN-06: sin espacios en los extremos y siempre en minúsculas. */
    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @PrePersist
    @PreUpdate
    void normalizaEmailAntesDeGuardar() {
        this.email = normalizarEmail(this.email);
    }
}
