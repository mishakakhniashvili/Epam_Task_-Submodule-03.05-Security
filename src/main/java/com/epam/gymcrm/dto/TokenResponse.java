package com.epam.gymcrm.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}