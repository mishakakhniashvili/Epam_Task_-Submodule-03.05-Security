package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.LoginRequest;
import com.epam.gymcrm.dto.TokenResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import com.epam.gymcrm.security.JwtService;
import com.epam.gymcrm.security.LoginAttemptService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Authentication")
@RestController
@RequestMapping("/api")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final GymCrmMetrics gymCrmMetrics;

    private final GymFacade gymFacade;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final LoginAttemptService loginAttemptService;

    public AuthController(
            GymFacade gymFacade,
            GymCrmMetrics gymCrmMetrics,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            LoginAttemptService loginAttemptService
    ) {
        this.gymFacade = gymFacade;
        this.gymCrmMetrics = gymCrmMetrics;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    @ApiOperation("Login user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Login successful"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String username = request.username();

        LOGGER.info(
                "Login request received for username={}",
                username
        );

        if (loginAttemptService.isBlocked(username)) {
            gymCrmMetrics.incrementLoginFailure();

            LOGGER.warn(
                    "Login rejected because username={} is temporarily blocked",
                    username
            );

            throw new AuthenticationException(
                    "User is temporarily blocked"
            );
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    request.password()
                            )
                    );

            loginAttemptService.loginSucceeded(username);

            String token =
                    jwtService.generateToken(authentication);

            gymCrmMetrics.incrementLoginSuccess();

            TokenResponse response = new TokenResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationSeconds()
            );

            return ResponseEntity.ok(response);

        } catch (org.springframework.security.core.AuthenticationException ex) {
            loginAttemptService.loginFailed(username);
            gymCrmMetrics.incrementLoginFailure();

            LOGGER.warn(
                    "Login failed for username={}",
                    username
            );

            throw new AuthenticationException(
                    "Invalid credentials"
            );
        }
    }


    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        String username = authentication.getName();

        LOGGER.info(
                "Password change request received for username={}",
                username
        );

        if (gymFacade.isTraineeCredentialsValid(
                username,
                request.getOldPassword()
        )) {
            gymFacade.changeTraineePassword(
                    username,
                    request.getOldPassword(),
                    request.getNewPassword()
            );

        } else if (gymFacade.isTrainerCredentialsValid(
                username,
                request.getOldPassword()
        )) {
            gymFacade.changeTrainerPassword(
                    username,
                    request.getOldPassword(),
                    request.getNewPassword()
            );

        } else {
            throw new AuthenticationException(
                    "Invalid credentials"
            );
        }

        LOGGER.info(
                "Password successfully changed for username={}",
                username
        );

        return ResponseEntity.ok().build();
    }
}
