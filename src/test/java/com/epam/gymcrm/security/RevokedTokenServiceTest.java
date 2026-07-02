package com.epam.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class RevokedTokenServiceTest {

    private MutableClock clock;
    private RevokedTokenService revokedTokenService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(
                Instant.parse("2026-07-03T00:00:00Z"),
                ZoneOffset.UTC
        );

        revokedTokenService =
                new RevokedTokenService(clock);
    }

    @Test
    void shouldMarkActiveTokenAsRevoked() {
        Instant expiresAt =
                clock.instant().plus(Duration.ofMinutes(30));

        revokedTokenService.revoke(
                "token-id",
                expiresAt
        );

        assertTrue(
                revokedTokenService.isRevoked("token-id")
        );
    }

    @Test
    void shouldNotStoreAlreadyExpiredToken() {
        Instant expiresAt =
                clock.instant().minus(Duration.ofMinutes(1));

        revokedTokenService.revoke(
                "expired-token-id",
                expiresAt
        );

        assertFalse(
                revokedTokenService.isRevoked(
                        "expired-token-id"
                )
        );
    }

    @Test
    void shouldRemoveRevocationAfterTokenExpires() {
        Instant expiresAt =
                clock.instant().plus(Duration.ofMinutes(30));

        revokedTokenService.revoke(
                "token-id",
                expiresAt
        );

        assertTrue(
                revokedTokenService.isRevoked("token-id")
        );

        clock.advance(Duration.ofMinutes(30));

        assertFalse(
                revokedTokenService.isRevoked("token-id")
        );
    }

    @Test
    void shouldReturnFalseForUnknownToken() {
        assertFalse(
                revokedTokenService.isRevoked("unknown-token")
        );
    }

    @Test
    void shouldReturnFalseWhenTokenIdIsNull() {
        assertFalse(
                revokedTokenService.isRevoked(null)
        );
    }

    @Test
    void shouldRejectNullTokenId() {
        Instant expiresAt =
                clock.instant().plus(Duration.ofMinutes(30));

        assertThrows(
                IllegalArgumentException.class,
                () -> revokedTokenService.revoke(
                        null,
                        expiresAt
                )
        );
    }

    @Test
    void shouldRejectNullExpirationTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> revokedTokenService.revoke(
                        "token-id",
                        null
                )
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