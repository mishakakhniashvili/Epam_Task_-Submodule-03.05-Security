package com.epam.gymcrm.service;

public record RegistrationResult(
        String username,
        String rawPassword
) {
}