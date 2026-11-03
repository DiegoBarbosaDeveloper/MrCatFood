package com.diegodev.mrcatfood.seguridad;

import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Purga diaria de los {@code jti} cuya expiración ya pasó. */
@Component
public class PurgaTokensRevocados {

    private static final Logger log = LoggerFactory.getLogger(PurgaTokensRevocados.class);

    private final TokenRevocadoRepository revocados;
    private final java.time.Clock reloj;

    public PurgaTokensRevocados(TokenRevocadoRepository revocados, java.time.Clock reloj) {
        this.revocados = revocados;
        this.reloj = reloj;
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgarVencidos() {
        long borrados = revocados.deleteByExpiraEnBefore(OffsetDateTime.now(reloj));
        if (borrados > 0) {
            log.info("Se purgaron {} tokens revocados vencidos", borrados);
        }
    }
}
