package com.epam.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private MutableClock clock;
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(
                Instant.parse("2026-07-03T00:00:00Z"),
                ZoneOffset.UTC
        );

        loginAttemptService = new LoginAttemptService(
                clock,
                3,
                5
        );
    }

    @Test
    void shouldNotBlockUsernameAfterOneFailedAttempt() {
        loginAttemptService.loginFailed("John.Smith");

        assertFalse(
                loginAttemptService.isBlocked("John.Smith")
        );
    }

    @Test
    void shouldNotBlockUsernameAfterTwoFailedAttempts() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");

        assertFalse(
                loginAttemptService.isBlocked("John.Smith")
        );
    }

    @Test
    void shouldBlockUsernameAfterThreeFailedAttempts() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");

        assertTrue(
                loginAttemptService.isBlocked("John.Smith")
        );
    }

    @Test
    void successfulLoginShouldClearFailedAttempts() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");

        loginAttemptService.loginSucceeded("John.Smith");

        loginAttemptService.loginFailed("John.Smith");

        assertFalse(
                loginAttemptService.isBlocked("John.Smith")
        );
    }

    @Test
    void blockShouldExpireAfterFiveMinutes() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");

        assertTrue(
                loginAttemptService.isBlocked("John.Smith")
        );

        clock.advance(Duration.ofMinutes(5));

        assertFalse(
                loginAttemptService.isBlocked("John.Smith")
        );
    }

    private static class MutableClock extends Clock {

        private Instant currentInstant;
        private final ZoneId zone;

        private MutableClock(
                Instant currentInstant,
                ZoneId zone
        ) {
            this.currentInstant = currentInstant;
            this.zone = zone;
        }

        void advance(Duration duration) {
            currentInstant =
                    currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(
                    currentInstant,
                    zone
            );
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}