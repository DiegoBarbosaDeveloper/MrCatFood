package com.diegodev.mrcatfood.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj único del sistema: las reglas de horario y la expiración del token dependen de él. */
@Configuration
@EnableConfigurationProperties(PropiedadesPedido.class)
public class RelojConfig {

    @Bean
    Clock reloj(PropiedadesPedido propiedades) {
        return Clock.system(propiedades.zona());
    }
}
