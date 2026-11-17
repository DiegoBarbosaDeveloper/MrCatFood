package com.diegodev.mrcatfood.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/** Metadatos y autenticación de la documentación OpenAPI. */
@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "MrCatFood API",
                        version = "1.0.0",
                        description =
                                "API REST para registro de clientes, catálogo y gestión de pedidos.",
                        contact =
                                @Contact(
                                        name = "MrCatFood",
                                        url = "https://github.com/DiegoBarbosaDeveloper/MrCatFood"),
                        license = @License(name = "Uso académico")),
        tags = {
            @Tag(name = "Autenticación", description = "Registro, sesión y usuario actual"),
            @Tag(name = "Usuarios", description = "Perfil y ubicación del cliente"),
            @Tag(name = "Catálogo", description = "Consulta pública de sándwiches"),
            @Tag(name = "Pedidos", description = "Operaciones del cliente sobre sus pedidos"),
            @Tag(name = "Administración", description = "Catálogo y pedidos para el rol ADMIN"),
            @Tag(name = "Configuración", description = "Parámetros públicos del servicio")
        })
@SecurityScheme(
        name = OpenApiConfig.ESQUEMA_BEARER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description =
                "Token obtenido en registro o inicio de sesión. No escribas el prefijo Bearer.")
public class OpenApiConfig {

    public static final String ESQUEMA_BEARER = "bearerAuth";
}
