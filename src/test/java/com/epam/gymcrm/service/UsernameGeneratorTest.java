package com.epam.gymcrm.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameGeneratorTest {

    private final UsernameGenerator usernameGenerator = new UsernameGenerator();

    @Test
    void shouldGenerateUsernameWithoutSuffixWhenUsernameDoesNotExist() {
        String username = usernameGenerator.generateUsername(
                "John",
                "Smith",
                List.of("Mike.Brown")
        );

        assertEquals("John.Smith", username);
    }

    @Test
    void shouldGenerateUsernameWithSuffixWhenUsernameAlreadyExists() {
        String username = usernameGenerator.generateUsername(
                "John",
                "Smith",
                List.of("John.Smith")
        );

        assertEquals("John.Smith1", username);
    }

    @Test
    void shouldGenerateNextAvailableUsernameSuffix() {
        String username = usernameGenerator.generateUsername(
                "John",
                "Smith",
                List.of("John.Smith", "John.Smith1", "John.Smith2")
        );

        assertEquals("John.Smith3", username);
    }
}