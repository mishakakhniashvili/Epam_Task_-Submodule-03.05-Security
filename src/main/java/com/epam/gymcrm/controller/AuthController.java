package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.LoginRequest;
import com.epam.gymcrm.dto.TokenResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import com.epam.gymcrm.security.JwtService;
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

    public AuthController(
            GymFacade gymFacade,
            GymCrmMetrics gymCrmMetrics,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.gymFacade = gymFacade;
        this.gymCrmMetrics = gymCrmMetrics;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
        LOGGER.info(
                "Login request received for username={}",
                request.username()
        );

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username(),
                                    request.password()
                            )
                    );

            String token = jwtService.generateToken(authentication);

            gymCrmMetrics.incrementLoginSuccess();

            TokenResponse response = new TokenResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationSeconds()
            );

            return ResponseEntity.ok(response);

        } catch (org.springframework.security.core.AuthenticationException ex) {
            gymCrmMetrics.incrementLoginFailure();

            LOGGER.warn(
                    "Login failed for username={}",
                    request.username()
            );

            throw new AuthenticationException("Invalid credentials");
        }
    }



    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        LOGGER.info("changePassword request received for username={}", request.getUsername());

        String newPassword = request.getNewPassword();
        String oldPassword = request.getOldPassword();
        String username = request.getUsername();

        boolean traineeValid = gymFacade.isTraineeCredentialsValid(username, oldPassword);
        if (traineeValid) {
            gymFacade.changeTraineePassword(username, oldPassword, newPassword);

            LOGGER.info("password changed for username={}", username);

            return ResponseEntity.ok().build();
        }

        boolean trainerValid = gymFacade.isTrainerCredentialsValid(username, oldPassword);
        if (trainerValid) {
            gymFacade.changeTrainerPassword(username, oldPassword, newPassword);

            LOGGER.info("password changed for username={}", username);

            return ResponseEntity.ok().build();
        }

        throw new AuthenticationException("Invalid credentials");
    }
}
