package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.RegistrationResponse;
import com.epam.gymcrm.dto.TrainerProfileResponse;
import com.epam.gymcrm.dto.TrainerRegistrationRequest;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.mapper.TrainerMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerController.class);

    private final GymFacade gymFacade;

    private final TrainerMapper trainerMapper;

    public TrainerController(GymFacade gymFacade,  TrainerMapper trainerMapper) {
        this.gymFacade = gymFacade;
        this.trainerMapper = trainerMapper;
    }


    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request
    ) {
        LOGGER.info("Trainer registration request received for firstName={}, lastName={}",
                request.getFirstName(),
                request.getLastName());


        Trainer createdTrainer = gymFacade.createTrainer(
                request.getFirstName(),
                request.getLastName(),
                request.getSpecialization()
        );

        RegistrationResponse response = new RegistrationResponse(
                createdTrainer.getUser().getUsername(),
                createdTrainer.getUser().getPassword()
        );

        LOGGER.info("Trainer registered successfully with username={}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @RequestParam String username,
            @RequestParam String password){
        LOGGER.info("trainer profile request for username={}", username);

        Trainer trainer = gymFacade.findTrainerByUsername(username, password, username).orElseThrow(
                () -> new EntityNotFoundException("Trainer", username)
        );

        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        LOGGER.info("Trainer profile successfully retrieved for username={}", username);

        return ResponseEntity.ok(response);
    }
}
