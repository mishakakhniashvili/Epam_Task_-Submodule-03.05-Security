package com.epam.gymcrm.config;

import com.epam.gymcrm.security.RevokedTokenService;
import com.epam.gymcrm.security.RevokedTokenValidator;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${security.jwt.secret-base64}") String encodedSecret
    ) {
        byte[] secretBytes = Base64.getDecoder().decode(encodedSecret);

        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        JWKSource<SecurityContext> source =
                new ImmutableSecret<>(secretKey);

        return new NimbusJwtEncoder(source);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey,
            RevokedTokenService revokedTokenService
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> standardValidator =
                JwtValidators.createDefaultWithIssuer("gym-crm");

        OAuth2TokenValidator<Jwt> revokedTokenValidator =
                new RevokedTokenValidator(revokedTokenService);

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        standardValidator,
                        revokedTokenValidator
                )
        );

        return decoder;
    }
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}