package com.diegodev.mrcatfood.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Registra la configuración del número de WhatsApp de la propietaria. */
@Configuration
@EnableConfigurationProperties(WhatsAppProperties.class)
public class WhatsAppConfig {}
