package com.epam.gymcrm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void shouldGeneratePasswordWithLengthTen() {
        String password = passwordGenerator.generatePassword();

        assertEquals(10, password.length());
    }

    @Test
    void shouldGeneratePasswordOnlyWithAllowedCharacters() {
        String password = passwordGenerator.generatePassword();

        assertTrue(password.matches("[A-Za-z0-9]+"));
    }
}