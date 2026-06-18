package com.goalkeeperdash.user.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/** Request/response payloads for the auth endpoints (§4.6). */
public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(@NotBlank String idToken) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record LogoutRequest(@NotBlank String refreshToken) {}

    public record AuthResponse(String accessToken, String refreshToken, UserDto user) {}

    public record UserDto(UUID id, String displayName, String nationCode) {}
}
