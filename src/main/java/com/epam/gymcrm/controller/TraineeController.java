package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.RegistrationResponse;
import com.epam.gymcrm.dto.TraineeProfileResponse;
import com.epam.gymcrm.dto.TraineeRegistrationRequest;
import com.epam.gymcrm.dto.TraineeUpdateRequest;
import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.mapper.TraineeMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeController.class);

    private final GymFacade gymFacade;

    private final TraineeMapper traineeMapper;

    public TraineeController(GymFacade gymFacade,  TraineeMapper traineeMapper) {
        this.gymFacade = gymFacade;
        this.traineeMapper = traineeMapper;
    }

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

        RegistrationResponse response = new RegistrationResponse(
                createdTrainee.getUser().getUsername(),
                createdTrainee.getUser().getPassword()
        );

        LOGGER.info("Trainee registered successfully with username={}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

        LOGGER.info("Trainer profile successfully updated for username={}", request.getUsername());

        return  ResponseEntity.ok(response);
    }

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
}