package com.vasquez.reservas_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    OpenAPI reservasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Reservas")
                        .version("v1")
                        .description("""
                                API REST para gestionar usuarios, autenticación,
                                servicios, disponibilidad y reservas.

                                ## Autenticación

                                1. Regístrate o inicia sesión en `/api/auth/login`.
                                2. Copia el `accessToken` de la respuesta.
                                3. Pulsa **Authorize** en Swagger UI.
                                4. Escribe: `Bearer TU_ACCESS_TOKEN`.
                                5. Prueba los endpoints protegidos.

                                El refresh token se maneja mediante la cookie
                                segura `refreshToken`.
                                """)
                        .contact(new Contact()
                                .name("Equipo Reservas")
                                .email("jhonainvazquescarrero@gmail.com"))
                        .license(new License()
                                .name("Uso académico")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Pega el access token JWT. "
                                                        + "Swagger agregará el prefijo Bearer."
                                        )
                        ));
    }
}