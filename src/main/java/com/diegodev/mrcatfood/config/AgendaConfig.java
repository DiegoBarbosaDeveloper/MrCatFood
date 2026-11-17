package com.diegodev.mrcatfood.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita las tareas programadas, entre ellas la purga de tokens revocados. */
@Configuration
@EnableScheduling
public class AgendaConfig {}
