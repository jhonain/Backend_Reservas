package com.vasquez.reservas_backend.auth.dto;


public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {

    public static RefreshTokenResponse de(String accessToken, long expiresInSeconds) {
        return new RefreshTokenResponse(accessToken, "Bearer", expiresInSeconds);
    }
}