package com.diegodev.mrcatfood.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj único del sistema: las reglas de horario y la expiración del token dependen de él. */
@Configuration
public class RelojConfig {

    public static final String ZONA = "America/Bogota";

    @Bean
    Clock reloj() {
        return Clock.system(ZoneId.of(ZONA));
    }
}
