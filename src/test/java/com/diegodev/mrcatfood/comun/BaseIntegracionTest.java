package com.diegodev.mrcatfood.comun;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/** Base para las pruebas sobre PostgreSQL efímero; se excluye de {@code ./gradlew test}. */
@Tag("integracion")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integracion")
@Import(BaseIntegracionTest.Contenedores.class)
public abstract class BaseIntegracionTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class Contenedores {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>("postgres:18");
        }
    }
}
