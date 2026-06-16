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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public TrainerController(GymFacade gymFacade,  TrainerMapper trainerMapper,   TrainingMapper trainingMapper) {
        this.gymFacade = gymFacade;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
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

    @ApiOperation("Get trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer profile returned successfully"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
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

    @ApiOperation("Update trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer profile updated successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @RequestParam String password,
            @Valid @RequestBody TrainerUpdateRequest request
    ){
        LOGGER.info("Trainer profile update request received for username={}", request.getUsername());
        Trainer trainer = gymFacade.updateProfile(
                request.getUsername(),
                password,
                request.getFirstName(),
                request.getLastName(),
                request.getActive()
        );
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        LOGGER.info("Trainer profile successfully updated for username={}", request.getUsername());

        return  ResponseEntity.ok(response);
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
            @RequestParam String password,
            @Valid @RequestBody ActivationRequest request
    ){
        LOGGER.info("Trainer status update request received for username={}", request.getUsername());

        if(request.getActive()){
            gymFacade.activateTrainer(request.getUsername(),  password);
        }
        else{
            gymFacade.deactivateTrainer(request.getUsername(), password);
        }

        LOGGER.info("Trainer status successfully updated for username={}", request.getUsername());

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
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeUsername
    ) {
        LOGGER.info("Trainer trainings list request received for username={}", username);

        List<Training> trainings = gymFacade.getTrainerTrainings(
                username,
                password,
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
