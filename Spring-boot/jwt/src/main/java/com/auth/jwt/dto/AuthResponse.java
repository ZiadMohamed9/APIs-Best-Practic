package com.auth.jwt.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
