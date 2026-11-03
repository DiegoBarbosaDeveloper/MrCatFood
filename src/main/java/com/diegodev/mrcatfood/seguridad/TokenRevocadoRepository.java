package com.diegodev.mrcatfood.seguridad;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, String> {

    /** Borra los tokens cuya expiración ya pasó. Devuelve cuántos se eliminaron. */
    long deleteByExpiraEnBefore(OffsetDateTime limite);
}
