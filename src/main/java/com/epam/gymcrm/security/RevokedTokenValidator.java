package com.epam.gymcrm.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class RevokedTokenValidator
        implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error REVOKED_TOKEN_ERROR =
            new OAuth2Error(
                    "invalid_token",
                    "Token has been revoked",
                    null
            );

    private final RevokedTokenService revokedTokenService;

    public RevokedTokenValidator(
            RevokedTokenService revokedTokenService
    ) {
        this.revokedTokenService = revokedTokenService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (revokedTokenService.isRevoked(jwt.getId())) {
            return OAuth2TokenValidatorResult.failure(
                    REVOKED_TOKEN_ERROR
            );
        }

        return OAuth2TokenValidatorResult.success();
    }
}