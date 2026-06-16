package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ActivationRequest;
import com.epam.gymcrm.dto.RegistrationResponse;
import com.epam.gymcrm.dto.trainee.*;
import com.epam.gymcrm.dto.trainer.TrainerShortResponse;
import com.epam.gymcrm.entity.*;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.mapper.TrainingMapper;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private GymFacade gymFacade;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        traineeController = new TraineeController(
                gymFacade,
                traineeMapper,
                trainerMapper,
                trainingMapper
        );
    }

    @Test
    void registerTraineeShouldReturnCreatedRegistrationResponse() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        setField(request, "firstName", "John");
        setField(request, "lastName", "Smith");
        setField(request, "dateOfBirth", LocalDate.of(2000, 1, 1));
        setField(request, "address", "Tbilisi");

        User createdUser = new User("John", "Smith", "John.Smith", "pass123", true);
        Trainee createdTrainee = new Trainee(createdUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        when(gymFacade.createTrainee(any(Trainee.class))).thenReturn(createdTrainee);

        ResponseEntity<RegistrationResponse> response = traineeController.registerTrainee(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("John.Smith", response.getBody().getUsername());
        assertEquals("pass123", response.getBody().getPassword());
        verify(gymFacade).createTrainee(any(Trainee.class));
    }

    @Test
    void getTraineeProfileShouldReturnProfileResponse() {
        User user = new User("John", "Smith", "John.Smith", "pass123", true);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        TraineeProfileResponse profileResponse = new TraineeProfileResponse(
                "John1",
                "John",
                "Smith",
                LocalDate.of(2000, 1, 1),
                "Tbilisi",
                true,
                List.of()
        );

        when(gymFacade.findTraineeByUsername("John.Smith", "pass123", "John.Smith"))
                .thenReturn(Optional.of(trainee));
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

        ResponseEntity<TraineeProfileResponse> response =
                traineeController.getTraineeProfile("John.Smith", "pass123");

        assertEquals(200, response.getStatusCode().value());
        assertSame(profileResponse, response.getBody());
    }

    @Test
    void updateTraineeProfileShouldReturnUpdatedProfileResponse() {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        setField(request, "username", "John.Smith");
        setField(request, "firstName", "John");
        setField(request, "lastName", "Updated");
        setField(request, "dateOfBirth", LocalDate.of(2000, 1, 1));
        setField(request, "address", "Batumi");
        setField(request, "active", true);

        User user = new User("John", "Updated", "John.Smith", "pass123", true);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Batumi");

        TraineeProfileResponse profileResponse = new TraineeProfileResponse(
                "John1",
                "John",
                "Updated",
                LocalDate.of(2000, 1, 1),
                "Batumi",
                true,
                List.of()
        );

        when(gymFacade.updateProfile(
                "John.Smith",
                "pass123",
                "John",
                "Updated",
                LocalDate.of(2000, 1, 1),
                "Batumi",
                true
        )).thenReturn(trainee);

        when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

        ResponseEntity<TraineeProfileResponse> response =
                traineeController.updateTraineeProfile("pass123", request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(profileResponse, response.getBody());
    }

    @Test
    void deleteTraineeProfileShouldReturnOk() {
        ResponseEntity<Void> response =
                traineeController.deleteTraineeProfile("John.Smith", "pass123");

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).deleteTraineeByUsername("John.Smith", "pass123");
    }

    @Test
    void updateTraineeStatusShouldActivateWhenActiveTrue() {
        ActivationRequest request = new ActivationRequest();
        setField(request, "username", "John.Smith");
        setField(request, "active", true);

        ResponseEntity<Void> response =
                traineeController.updateTraineeStatus("pass123", request);

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).activateTrainee("John.Smith", "pass123");
        verify(gymFacade, never()).deactivateTrainee(anyString(), anyString());
    }

    @Test
    void updateTraineeStatusShouldDeactivateWhenActiveFalse() {
        ActivationRequest request = new ActivationRequest();
        setField(request, "username", "John.Smith");
        setField(request, "active", false);

        ResponseEntity<Void> response =
                traineeController.updateTraineeStatus("pass123", request);

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).deactivateTrainee("John.Smith", "pass123");
        verify(gymFacade, never()).activateTrainee(anyString(), anyString());
    }

    @Test
    void getNotAssignedTrainersShouldReturnShortTrainerList() {
        User trainerUser = new User("Mike", "Brown", "Mike.Brown", "pass", true);
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer(trainerUser, fitness);

        TrainerShortResponse shortResponse = new TrainerShortResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                "Fitness"
        );

        when(gymFacade.getTrainersNotAssignedToTrainee("John.Smith", "pass123"))
                .thenReturn(List.of(trainer));
        when(trainerMapper.toShortResponse(trainer)).thenReturn(shortResponse);

        ResponseEntity<List<TrainerShortResponse>> response =
                traineeController.getNotAssignedTrainers("John.Smith", "pass123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(shortResponse, response.getBody().get(0));
    }

    @Test
    void updateTraineeTrainersListShouldReturnUpdatedTrainerList() {
        TraineeTrainersUpdateRequest request = new TraineeTrainersUpdateRequest();
        setField(request, "username", "John.Smith");
        setField(request, "trainerUsernames", List.of("Mike.Brown"));

        User traineeUser = new User("John", "Smith", "John.Smith", "pass123", true);
        Trainee trainee = new Trainee(traineeUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        User trainerUser = new User("Mike", "Brown", "Mike.Brown", "pass", true);
        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer(trainerUser, fitness);

        trainee.setTrainers(Set.of(trainer));

        TrainerShortResponse shortResponse = new TrainerShortResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                "Fitness"
        );

        when(gymFacade.updateTraineeTrainersList(
                "John.Smith",
                "pass123",
                List.of("Mike.Brown")
        )).thenReturn(trainee);

        when(trainerMapper.toShortResponse(trainer)).thenReturn(shortResponse);

        ResponseEntity<List<TrainerShortResponse>> response =
                traineeController.updateTraineeTrainersList("pass123", request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(shortResponse, response.getBody().get(0));
    }

    @Test
    void getTraineeTrainingsShouldReturnTrainingList() {
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

        TraineeTrainingResponse trainingResponse = new TraineeTrainingResponse(
                "Morning Training",
                "2026-05-10",
                "Fitness",
                60,
                "Mike Brown"
        );

        when(gymFacade.getTraineeTrainings(
                "John.Smith",
                "pass123",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "Mike.Brown",
                "Fitness"
        )).thenReturn(List.of(training));

        when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(trainingResponse);

        ResponseEntity<List<TraineeTrainingResponse>> response =
                traineeController.getTraineeTrainings(
                        "John.Smith",
                        "pass123",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        "Mike.Brown",
                        "Fitness"
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