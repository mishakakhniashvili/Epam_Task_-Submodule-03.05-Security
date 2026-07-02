package com.epam.gymcrm.security;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RevokedTokenService {

    private final ConcurrentMap<String, Instant> revokedTokens =
            new ConcurrentHashMap<>();

    private final Clock clock;

    public RevokedTokenService(Clock clock) {
        this.clock = clock;
    }

    public void revoke(String tokenId, Instant expiresAt) {
        if (tokenId == null || expiresAt == null) {
            throw new IllegalArgumentException(
                    "Token ID and expiration time are required"
            );
        }

        if (expiresAt.isAfter(clock.instant())) {
            revokedTokens.put(tokenId, expiresAt);
        }
    }

    public boolean isRevoked(String tokenId) {
        if (tokenId == null) {
            return false;
        }

        Instant expiresAt = revokedTokens.get(tokenId);

        if (expiresAt == null) {
            return false;
        }

        if (!expiresAt.isAfter(clock.instant())) {
            revokedTokens.remove(tokenId, expiresAt);
            return false;
        }

        return true;
    }
}