package com.epam.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RevokedTokenValidatorTest {

    private RevokedTokenService revokedTokenService;
    private RevokedTokenValidator validator;

    @BeforeEach
    void setUp() {
        revokedTokenService =
                mock(RevokedTokenService.class);

        validator =
                new RevokedTokenValidator(
                        revokedTokenService
                );
    }

    @Test
    void shouldAcceptTokenThatIsNotRevoked() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getId())
                .thenReturn("active-token-id");

        when(revokedTokenService.isRevoked(
                "active-token-id"
        )).thenReturn(false);

        OAuth2TokenValidatorResult result =
                validator.validate(jwt);

        assertFalse(result.hasErrors());

        verify(revokedTokenService)
                .isRevoked("active-token-id");
    }

    @Test
    void shouldRejectRevokedToken() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getId())
                .thenReturn("revoked-token-id");

        when(revokedTokenService.isRevoked(
                "revoked-token-id"
        )).thenReturn(true);

        OAuth2TokenValidatorResult result =
                validator.validate(jwt);

        assertTrue(result.hasErrors());
        assertEquals(
                1,
                result.getErrors().size()
        );

        assertEquals(
                "invalid_token",
                result.getErrors()
                        .iterator()
                        .next()
                        .getErrorCode()
        );

        verify(revokedTokenService)
                .isRevoked("revoked-token-id");
    }
}