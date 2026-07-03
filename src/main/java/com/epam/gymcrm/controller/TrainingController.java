package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.AddTrainingRequest;
import com.epam.gymcrm.dto.TrainingTypeResponse;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;

@Api(tags = "Trainings")
@RestController
@RequestMapping("/api")
public class TrainingController {

    private final GymFacade gymFacade;

    private GymCrmMetrics gymCrmMetrics;

    public TrainingController(GymFacade gymFacade,  GymCrmMetrics gymCrmMetrics) {
        this.gymFacade = gymFacade;
        this.gymCrmMetrics = gymCrmMetrics;

    }
    @ApiOperation("Add new training")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training added successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Invalid trainer credentials"),
            @ApiResponse(code = 404, message = "Trainer or trainee not found")
    })
    @PostMapping("/trainings")
    public ResponseEntity<Void> addTraining(
            Authentication authentication,
            @Valid @RequestBody AddTrainingRequest request
    ) {
        String trainerUsername = authentication.getName();

        gymFacade.addTraining(
                trainerUsername,
                request.getTraineeUsername(),
                request.getTrainingName(),
                request.getTrainingDate(),
                request.getTrainingDuration()
        );

        gymCrmMetrics.incrementTrainingsCreated();

        return ResponseEntity.ok().build();
    }
    @ApiOperation("Get training types")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training types returned successfully"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    @GetMapping("/training-types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes(){


        List<TrainingType> trainingTypes = gymFacade.getTrainingTypes();

        List<TrainingTypeResponse> response = trainingTypes.stream()
                .map(type -> new TrainingTypeResponse(type.getId(), type.getTrainingTypeName()))
                .toList();

        return ResponseEntity.ok(response);
    }

}
