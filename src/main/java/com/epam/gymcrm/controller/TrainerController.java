package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.*;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerRegistrationRequest;
import com.epam.gymcrm.dto.trainer.TrainerTrainingResponse;
import com.epam.gymcrm.dto.trainer.TrainerUpdateRequest;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import com.epam.gymcrm.service.RegistrationResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.time.LocalDate;
import java.util.List;

@Api(tags = "Trainers")
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerController.class);

    private final GymFacade gymFacade;

    private final TrainerMapper trainerMapper;

    private final TrainingMapper trainingMapper;

    private final GymCrmMetrics gymCrmMetrics;

    public TrainerController(GymFacade gymFacade,  TrainerMapper trainerMapper,   TrainingMapper trainingMapper,  GymCrmMetrics gymCrmMetrics) {
        this.gymFacade = gymFacade;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
        this.gymCrmMetrics = gymCrmMetrics;

    }


    @ApiOperation("Register new trainer")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainer registered successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "Training type not found")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request
    ) {
        LOGGER.info("Trainer registration request received for firstName={}, lastName={}",
                request.getFirstName(),
                request.getLastName());


        RegistrationResult registration = gymFacade.createTrainer(
                request.getFirstName(),
                request.getLastName(),
                request.getSpecialization()
        );

        gymCrmMetrics.incrementTrainerRegistrations();

        RegistrationResponse response = new RegistrationResponse(
                registration.username(),
                registration.rawPassword()
        );

        LOGGER.info("Trainer registered successfully with username={}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ApiOperation("Get trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer profile returned successfully"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            Authentication authentication
    ) {
        String username = authentication.getName();

        LOGGER.info(
                "Trainer profile request received for username={}",
                username
        );

        Trainer trainer = gymFacade
                .findTrainerByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainer", username)
                );

        return ResponseEntity.ok(
                trainerMapper.toProfileResponse(trainer)
        );
    }

    @ApiOperation("Update trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer profile updated successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            Authentication authentication,
            @Valid @RequestBody TrainerUpdateRequest request
    ) {
        String username = authentication.getName();

        Trainer trainer = gymFacade.updateProfile(
                username,
                request.getFirstName(),
                request.getLastName(),
                request.getActive()
        );

        return ResponseEntity.ok(
                trainerMapper.toProfileResponse(trainer)
        );
    }


    @ApiOperation("Activate or deactivate trainer")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer status updated successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 409, message = "Trainer already has requested status")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTrainerStatus(
            Authentication authentication,
            @Valid @RequestBody ActivationRequest request
    ) {
        String username = authentication.getName();

        if (request.getActive()) {
            gymFacade.activateTrainer(username);
        } else {
            gymFacade.deactivateTrainer(username);
        }

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get trainer trainings list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer trainings returned successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false)
            String traineeUsername
    ) {
        String username = authentication.getName();

        List<Training> trainings = gymFacade.getTrainerTrainings(
                username,
                fromDate,
                toDate,
                traineeUsername
        );

        List<TrainerTrainingResponse> response = trainings.stream()
                .map(trainingMapper::toTrainerTrainingResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}
