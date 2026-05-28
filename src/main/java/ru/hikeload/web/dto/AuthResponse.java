package ru.hikeload.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        Long userId,
        String email,
        String displayName,
        String accessToken,
        String tokenType
) {
    public static AuthResponse withToken(Long userId, String email, String displayName, String accessToken) {
        return new AuthResponse(userId, email, displayName, accessToken, "Bearer");
    }

    public static AuthResponse withoutToken(Long userId, String email, String displayName) {
        return new AuthResponse(userId, email, displayName, null, null);
    }
}
