package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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

    public AuthController(GymFacade gymFacade, GymCrmMetrics gymCrmMetrics) {
        this.gymFacade = gymFacade;
        this.gymCrmMetrics = gymCrmMetrics;
    }

    @ApiOperation("Login user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Login successful"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        LOGGER.info("Login request received for username={}", username);

        boolean traineeValid = gymFacade.isTraineeCredentialsValid(username, password);
        boolean trainerValid = gymFacade.isTrainerCredentialsValid(username, password);

        if (!traineeValid && !trainerValid) {
            gymCrmMetrics.incrementLoginFailure();
            throw new AuthenticationException("Invalid credentials");
        }

        gymCrmMetrics.incrementLoginSuccess();
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Change user password")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password changed successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
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
