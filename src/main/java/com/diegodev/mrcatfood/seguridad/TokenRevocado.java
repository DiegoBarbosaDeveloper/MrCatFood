package com.diegodev.mrcatfood.seguridad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Identificador de un token que ya no sirve. Solo se guardan los {@code jti} hasta su expiración;
 * después los elimina {@link PurgaTokensRevocados}.
 */
@Entity
@Table(name = "token_revocado")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRevocado {

    @Id
    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    public TokenRevocado(String jti, Instant expiraEn) {
        this.jti = jti;
        this.expiraEn = OffsetDateTime.ofInstant(expiraEn, ZoneOffset.UTC);
    }
}
