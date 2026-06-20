package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ActivationRequest;
import com.epam.gymcrm.dto.RegistrationResponse;
import com.epam.gymcrm.dto.trainee.*;
import com.epam.gymcrm.dto.trainer.TrainerShortResponse;
import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.metrics.GymCrmMetrics;
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

@Api(tags = "Trainees")
@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeController.class);

    private final GymFacade gymFacade;

    private final TraineeMapper traineeMapper;

    private final TrainerMapper trainerMapper;

    private final TrainingMapper trainingMapper;

    private final GymCrmMetrics gymCrmMetrics;

    public TraineeController(GymFacade gymFacade,  TraineeMapper traineeMapper,  TrainerMapper trainerMapper,   TrainingMapper trainingMapper,  GymCrmMetrics gymCrmMetrics) {
        this.gymFacade = gymFacade;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
        this.gymCrmMetrics = gymCrmMetrics;

    }

    @ApiOperation("Register new trainee")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainee registered successfully"),
            @ApiResponse(code = 400, message = "Invalid request")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request
    ) {
        LOGGER.info("Trainee registration request received for firstName={}, lastName={}",
                request.getFirstName(),
                request.getLastName());

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                null,
                null,
                false
        );

        Trainee trainee = new Trainee(
                user,
                request.getDateOfBirth(),
                request.getAddress()
        );

        Trainee createdTrainee = gymFacade.createTrainee(trainee);

        gymCrmMetrics.incrementTraineeRegistrations();

        RegistrationResponse response = new RegistrationResponse(
                createdTrainee.getUser().getUsername(),
                createdTrainee.getUser().getPassword()
        );

        LOGGER.info("Trainee registered successfully with username={}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ApiOperation("Get trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile returned successfully"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @RequestParam String username,
            @RequestParam String password
    ){
        LOGGER.info("Trainee profile request received for username={}", username);

        Trainee trainee = gymFacade.findTraineeByUsername(username, password, username).orElseThrow(
                () -> new EntityNotFoundException("Trainee" , username)
        );

        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);
        LOGGER.info("Trainee profile successfully retrieved for username={}", username);
        return ResponseEntity.ok(response);
    }

    @ApiOperation("Update trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile updated successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @RequestParam String password,
            @Valid @RequestBody TraineeUpdateRequest request
            ){
        LOGGER.info("Trainee profile update request received for username={}", request.getUsername());
        Trainee trainee = gymFacade.updateProfile(
                request.getUsername(),
                password,
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getAddress(),
                request.getActive()
        );
        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        LOGGER.info("Trainee profile successfully updated for username={}", request.getUsername());

        return  ResponseEntity.ok(response);
    }

    @ApiOperation("Delete trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile deleted successfully"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteTraineeProfile(
            @RequestParam String username,
            @RequestParam String password
    ){
        LOGGER.info("Trainee profile delete request received for username={}", username);

        gymFacade.deleteTraineeByUsername(username, password);

        LOGGER.info("Trainee profile successfully deleted for username={}", username);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Activate or deactivate trainee")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee status updated successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 409, message = "Trainee already has requested status")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTraineeStatus(
            @RequestParam String password,
            @Valid @RequestBody ActivationRequest request
    ){
        LOGGER.info("Trainee status update request received for username={}", request.getUsername());

        if(request.getActive()){
            gymFacade.activateTrainee(request.getUsername(),  password);
        }
        else{
            gymFacade.deactivateTrainee(request.getUsername(), password);
        }

        LOGGER.info("Trainee status successfully updated for username={}", request.getUsername());

        return ResponseEntity.ok().build();
    }


    @ApiOperation("Get active trainers not assigned to trainee")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Not assigned trainers returned successfully"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/not-assigned-trainers")
    public ResponseEntity<List<TrainerShortResponse>> getNotAssignedTrainers(
            @RequestParam String username,
            @RequestParam String password
    ){
        LOGGER.info("Trainee not assigned trainers request received for username= {}", username);

        List<Trainer> trainers = gymFacade.getTrainersNotAssignedToTrainee(username, password);

        List<TrainerShortResponse> response = trainers.stream()
                .map(trainerMapper::toShortResponse)
                .toList();

        return ResponseEntity.ok(response);
    }


    @ApiOperation("Update trainee trainers list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee trainers list updated successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainee or trainer not found")
    })
    @PutMapping("/trainers")
    public ResponseEntity<List<TrainerShortResponse>> updateTraineeTrainersList(
            @RequestParam String password,
            @Valid @RequestBody TraineeTrainersUpdateRequest request
    ) {
        LOGGER.info("Trainee trainers list update request received for username={}", request.getUsername());

        Trainee trainee = gymFacade.updateTraineeTrainersList(
                request.getUsername(),
                password,
                request.getTrainerUsernames()
        );

        List<TrainerShortResponse> response = trainee.getTrainers().stream()
                .map(trainerMapper::toShortResponse)
                .toList();

        LOGGER.info("Trainee trainers list successfully updated for username={}", request.getUsername());

        return ResponseEntity.ok(response);
    }


    @ApiOperation("Get trainee trainings list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee trainings returned successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/trainings")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerUsername,
            @RequestParam(required = false) String trainingTypeName
    ) {
        LOGGER.info("Trainee trainings list request received for username={}", username);

        List<Training> trainings = gymFacade.getTraineeTrainings(
                username,
                password,
                fromDate,
                toDate,
                trainerUsername,
                trainingTypeName
        );

        List<TraineeTrainingResponse> response = trainings.stream()
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}