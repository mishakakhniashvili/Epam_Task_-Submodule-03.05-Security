package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ActivationRequest;
import com.epam.gymcrm.dto.RegistrationResponse;
import com.epam.gymcrm.dto.trainer.*;
import com.epam.gymcrm.entity.*;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private GymFacade gymFacade;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        trainerController = new TrainerController(
                gymFacade,
                trainerMapper,
                trainingMapper,
                gymCrmMetrics
        );
    }

    @Test
    void registerTrainerShouldReturnCreatedRegistrationResponse() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        setField(request, "firstName", "Mike");
        setField(request, "lastName", "Brown");
        setField(request, "specialization", "Fitness");

        User createdUser = new User("Mike", "Brown", "Mike.Brown", "pass123", true);
        TrainingType fitness = new TrainingType("Fitness");
        Trainer createdTrainer = new Trainer(createdUser, fitness);

        when(gymFacade.createTrainer("Mike", "Brown", "Fitness")).thenReturn(createdTrainer);

        ResponseEntity<RegistrationResponse> response = trainerController.registerTrainer(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Mike.Brown", response.getBody().getUsername());
        assertEquals("pass123", response.getBody().getPassword());
        verify(gymCrmMetrics).incrementTrainerRegistrations();
    }

    @Test
    void getTrainerProfileShouldReturnProfileResponse() {
        User user = new User("Mike", "Brown", "Mike.Brown", "pass123", true);
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer(user, fitness);

        TrainerProfileResponse profileResponse = new TrainerProfileResponse(
                "Mike1",
                "Mike",
                "Brown",
                "Fitness",
                true,
                List.of()
        );

        when(gymFacade.findTrainerByUsername("Mike.Brown", "pass123", "Mike.Brown"))
                .thenReturn(Optional.of(trainer));
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

        ResponseEntity<TrainerProfileResponse> response =
                trainerController.getTrainerProfile("Mike.Brown", "pass123");

        assertEquals(200, response.getStatusCode().value());
        assertSame(profileResponse, response.getBody());
    }

    @Test
    void updateTrainerProfileShouldReturnUpdatedProfileResponse() {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        setField(request, "username", "Mike.Brown");
        setField(request, "firstName", "Mike");
        setField(request, "lastName", "Updated");
        setField(request, "active", true);

        User user = new User("Mike", "Updated", "Mike.Brown", "pass123", true);
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer(user, fitness);

        TrainerProfileResponse profileResponse = new TrainerProfileResponse(
                "Mike1",
                "Mike",
                "Updated",
                "Fitness",
                true,
                List.of()
        );

        when(gymFacade.updateProfile(
                "Mike.Brown",
                "pass123",
                "Mike",
                "Updated",
                true
        )).thenReturn(trainer);

        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

        ResponseEntity<TrainerProfileResponse> response =
                trainerController.updateTrainerProfile("pass123", request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(profileResponse, response.getBody());
    }

    @Test
    void updateTrainerStatusShouldActivateWhenActiveTrue() {
        ActivationRequest request = new ActivationRequest();
        setField(request, "username", "Mike.Brown");
        setField(request, "active", true);

        ResponseEntity<Void> response =
                trainerController.updateTrainerStatus("pass123", request);

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).activateTrainer("Mike.Brown", "pass123");
        verify(gymFacade, never()).deactivateTrainer(anyString(), anyString());
    }

    @Test
    void updateTrainerStatusShouldDeactivateWhenActiveFalse() {
        ActivationRequest request = new ActivationRequest();
        setField(request, "username", "Mike.Brown");
        setField(request, "active", false);

        ResponseEntity<Void> response =
                trainerController.updateTrainerStatus("pass123", request);

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).deactivateTrainer("Mike.Brown", "pass123");
        verify(gymFacade, never()).activateTrainer(anyString(), anyString());
    }

    @Test
    void getTrainerTrainingsShouldReturnTrainingList() {
        User traineeUser = new User("John", "Smith", "John.Smith", "pass123", true);
        Trainee trainee = new Trainee(traineeUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        User trainerUser = new User("Mike", "Brown", "Mike.Brown", "pass", true);
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer(trainerUser, fitness);

        Training training = new Training(
                "Morning Training",
                LocalDate.of(2026, 5, 10),
                trainer,
                trainee,
                fitness,
                60
        );

        TrainerTrainingResponse trainingResponse = new TrainerTrainingResponse(
                "Morning Training",
                "2026-05-10",
                "Fitness",
                60,
                "John Smith"
        );

        when(gymFacade.getTrainerTrainings(
                "Mike.Brown",
                "pass123",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "John.Smith"
        )).thenReturn(List.of(training));

        when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(trainingResponse);

        ResponseEntity<List<TrainerTrainingResponse>> response =
                trainerController.getTrainerTrainings(
                        "Mike.Brown",
                        "pass123",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        "John.Smith"
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(trainingResponse, response.getBody().get(0));
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}