package com.epam.gymcrm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    private final ConcurrentMap<String, AttemptState> attempts =
            new ConcurrentHashMap<>();

    private final Clock clock;
    private final int maxFailedAttempts;
    private final Duration blockDuration;

    public LoginAttemptService(
            Clock clock,
            @Value("${security.login.max-failed-attempts}")
            int maxFailedAttempts,
            @Value("${security.login.block-duration-minutes}")
            long blockDurationMinutes
    ) {
        this.clock = clock;
        this.maxFailedAttempts = maxFailedAttempts;
        this.blockDuration =
                Duration.ofMinutes(blockDurationMinutes);
    }

    public boolean isBlocked(String username) {
        AttemptState state = attempts.get(username);

        if (state == null || state.blockedUntil() == null) {
            return false;
        }

        if (state.blockedUntil().isAfter(clock.instant())) {
            return true;
        }

        attempts.remove(username, state);
        return false;
    }

    public void loginFailed(String username) {
        attempts.compute(username, (key, currentState) -> {
            int failedAttempts =
                    currentState == null
                            ? 1
                            : currentState.failedAttempts() + 1;

            Instant blockedUntil =
                    failedAttempts >= maxFailedAttempts
                            ? clock.instant().plus(blockDuration)
                            : null;

            return new AttemptState(
                    failedAttempts,
                    blockedUntil
            );
        });
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    private record AttemptState(
            int failedAttempts,
            Instant blockedUntil
    ) {
    }
}