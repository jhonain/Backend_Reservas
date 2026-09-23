package com.vasquez.reservas_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(

        @Schema(
                description = "JWT de corta duración para acceder a endpoints protegidos",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        String accessToken,

        @Schema(
                description = "Tipo de token que debe enviarse en Authorization",
                example = "Bearer"
        )
        String tokenType,
        @Schema(
                description = "Tiempo restante de validez del access token, expresado en segundos",
                example = "900"
        )
        long expiresInSeconds
) {

    public static LoginResponse de(
            String accessToken,
            long expiresInSeconds
    ) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresInSeconds
        );
    }
}