package com.epam.gymcrm.controller;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final GymFacade gymFacade;

    public AuthController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        LOGGER.info("Login request received for username={}", username);

        boolean traineeValid = gymFacade.isTraineeCredentialsValid(username, password);
        boolean trainerValid = gymFacade.isTrainerCredentialsValid(username, password);

        if (!traineeValid && !trainerValid) {
            throw new AuthenticationException("Invalid credentials");
        }

        return ResponseEntity.ok().build();
    }
}
