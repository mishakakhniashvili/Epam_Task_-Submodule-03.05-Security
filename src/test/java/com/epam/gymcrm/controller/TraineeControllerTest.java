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
import com.epam.gymcrm.metrics.GymCrmMetrics;
import com.epam.gymcrm.service.RegistrationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

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

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        traineeController = new TraineeController(
                gymFacade,
                traineeMapper,
                trainerMapper,
                trainingMapper,
                gymCrmMetrics
        );
    }

    @Test
    void registerTraineeShouldReturnCreatedRegistrationResponse() {
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest();

        setField(request, "firstName", "John");
        setField(request, "lastName", "Smith");
        setField(
                request,
                "dateOfBirth",
                LocalDate.of(2000, 1, 1)
        );
        setField(request, "address", "Tbilisi");

        RegistrationResult registration =
                new RegistrationResult(
                        "John.Smith",
                        "pass123"
                );

        when(gymFacade.createTrainee(any(Trainee.class)))
                .thenReturn(registration);

        ResponseEntity<RegistrationResponse> response =
                traineeController.registerTrainee(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                "John.Smith",
                response.getBody().getUsername()
        );
        assertEquals(
                "pass123",
                response.getBody().getPassword()
        );

        verify(gymFacade)
                .createTrainee(any(Trainee.class));

        verify(gymCrmMetrics)
                .incrementTraineeRegistrations();
    }

    @Test
    void getTraineeProfileShouldUseAuthenticatedUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        User user = new User(
                "John",
                "Smith",
                "John.Smith",
                "hashed-password",
                true
        );

        Trainee trainee = new Trainee(
                user,
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        TraineeProfileResponse profileResponse =
                new TraineeProfileResponse(
                        "John.Smith",
                        "John",
                        "Smith",
                        LocalDate.of(2000, 1, 1),
                        "Tbilisi",
                        true,
                        List.of()
                );

        when(gymFacade.findTraineeByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(traineeMapper.toProfileResponse(trainee))
                .thenReturn(profileResponse);

        ResponseEntity<TraineeProfileResponse> response =
                traineeController.getTraineeProfile(
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());
        assertSame(profileResponse, response.getBody());

        verify(authentication).getName();

        verify(gymFacade)
                .findTraineeByUsername("John.Smith");

        verify(traineeMapper)
                .toProfileResponse(trainee);
    }

    @Test
    void updateTraineeProfileShouldUseAuthenticatedUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        TraineeUpdateRequest request =
                new TraineeUpdateRequest();

        setField(request, "username", "Other.User");
        setField(request, "firstName", "John");
        setField(request, "lastName", "Updated");
        setField(
                request,
                "dateOfBirth",
                LocalDate.of(2000, 1, 1)
        );
        setField(request, "address", "Batumi");
        setField(request, "active", true);

        User user = new User(
                "John",
                "Updated",
                "John.Smith",
                "hashed-password",
                true
        );

        Trainee trainee = new Trainee(
                user,
                LocalDate.of(2000, 1, 1),
                "Batumi"
        );

        TraineeProfileResponse profileResponse =
                new TraineeProfileResponse(
                        "John.Smith",
                        "John",
                        "Updated",
                        LocalDate.of(2000, 1, 1),
                        "Batumi",
                        true,
                        List.of()
                );

        when(gymFacade.updateProfile(
                "John.Smith",
                "John",
                "Updated",
                LocalDate.of(2000, 1, 1),
                "Batumi",
                true
        )).thenReturn(trainee);

        when(traineeMapper.toProfileResponse(trainee))
                .thenReturn(profileResponse);

        ResponseEntity<TraineeProfileResponse> response =
                traineeController.updateTraineeProfile(
                        authentication,
                        request
                );

        assertEquals(200, response.getStatusCode().value());
        assertSame(profileResponse, response.getBody());

        verify(authentication).getName();

        verify(gymFacade).updateProfile(
                "John.Smith",
                "John",
                "Updated",
                LocalDate.of(2000, 1, 1),
                "Batumi",
                true
        );

        verify(traineeMapper)
                .toProfileResponse(trainee);
    }

    @Test
    void deleteTraineeProfileShouldUseAuthenticatedUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        ResponseEntity<Void> response =
                traineeController.deleteTraineeProfile(
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());

        verify(authentication).getName();

        verify(gymFacade)
                .deleteTraineeByUsername("John.Smith");
    }

    @Test
    void updateTraineeStatusShouldActivateAuthenticatedTrainee() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        ActivationRequest request =
                new ActivationRequest();

        setField(request, "username", "Other.User");
        setField(request, "active", true);

        ResponseEntity<Void> response =
                traineeController.updateTraineeStatus(
                        authentication,
                        request
                );

        assertEquals(200, response.getStatusCode().value());

        verify(authentication).getName();

        verify(gymFacade)
                .activateTrainee("John.Smith");

        verify(gymFacade, never())
                .deactivateTrainee(anyString());
    }

    @Test
    void updateTraineeStatusShouldDeactivateAuthenticatedTrainee() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        ActivationRequest request =
                new ActivationRequest();

        setField(request, "username", "Other.User");
        setField(request, "active", false);

        ResponseEntity<Void> response =
                traineeController.updateTraineeStatus(
                        authentication,
                        request
                );

        assertEquals(200, response.getStatusCode().value());

        verify(authentication).getName();

        verify(gymFacade)
                .deactivateTrainee("John.Smith");

        verify(gymFacade, never())
                .activateTrainee(anyString());
    }

    @Test
    void getNotAssignedTrainersShouldUseAuthenticatedUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        User trainerUser = new User(
                "Mike",
                "Brown",
                "Mike.Brown",
                "pass",
                true
        );

        TrainingType fitness =
                new TrainingType("Fitness");

        Trainer trainer =
                new Trainer(trainerUser, fitness);

        TrainerShortResponse shortResponse =
                new TrainerShortResponse(
                        "Mike.Brown",
                        "Mike",
                        "Brown",
                        "Fitness"
                );

        when(gymFacade.getTrainersNotAssignedToTrainee(
                "John.Smith"
        )).thenReturn(List.of(trainer));

        when(trainerMapper.toShortResponse(trainer))
                .thenReturn(shortResponse);

        ResponseEntity<List<TrainerShortResponse>> response =
                traineeController.getNotAssignedTrainers(
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(
                shortResponse,
                response.getBody().get(0)
        );

        verify(authentication).getName();

        verify(gymFacade)
                .getTrainersNotAssignedToTrainee(
                        "John.Smith"
                );

        verify(trainerMapper)
                .toShortResponse(trainer);
    }

    @Test
    void updateTraineeTrainersListShouldUseAuthenticatedUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        TraineeTrainersUpdateRequest request =
                new TraineeTrainersUpdateRequest();

        setField(request, "username", "Other.User");
        setField(
                request,
                "trainerUsernames",
                List.of("Mike.Brown")
        );

        User traineeUser = new User(
                "John",
                "Smith",
                "John.Smith",
                "pass123",
                true
        );

        Trainee trainee = new Trainee(
                traineeUser,
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        User trainerUser = new User(
                "Mike",
                "Brown",
                "Mike.Brown",
                "pass",
                true
        );

        TrainingType fitness =
                new TrainingType("Fitness");

        Trainer trainer =
                new Trainer(trainerUser, fitness);

        trainee.setTrainers(Set.of(trainer));

        TrainerShortResponse shortResponse =
                new TrainerShortResponse(
                        "Mike.Brown",
                        "Mike",
                        "Brown",
                        "Fitness"
                );

        when(gymFacade.updateTraineeTrainersList(
                "John.Smith",
                List.of("Mike.Brown")
        )).thenReturn(trainee);

        when(trainerMapper.toShortResponse(trainer))
                .thenReturn(shortResponse);

        ResponseEntity<List<TrainerShortResponse>> response =
                traineeController.updateTraineeTrainersList(
                        authentication,
                        request
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(
                shortResponse,
                response.getBody().get(0)
        );

        verify(authentication).getName();

        verify(gymFacade).updateTraineeTrainersList(
                "John.Smith",
                List.of("Mike.Brown")
        );

        verify(trainerMapper)
                .toShortResponse(trainer);
    }

    @Test
    void getTraineeTrainingsShouldUseAuthenticatedUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        User traineeUser = new User(
                "John",
                "Smith",
                "John.Smith",
                "pass123",
                true
        );

        Trainee trainee = new Trainee(
                traineeUser,
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        User trainerUser = new User(
                "Mike",
                "Brown",
                "Mike.Brown",
                "pass",
                true
        );

        TrainingType fitness =
                new TrainingType("Fitness");

        Trainer trainer =
                new Trainer(trainerUser, fitness);

        Training training = new Training(
                "Morning Training",
                LocalDate.of(2026, 5, 10),
                trainer,
                trainee,
                fitness,
                60
        );

        TraineeTrainingResponse trainingResponse =
                new TraineeTrainingResponse(
                        "Morning Training",
                        "2026-05-10",
                        "Fitness",
                        60,
                        "Mike Brown"
                );

        LocalDate fromDate =
                LocalDate.of(2026, 1, 1);

        LocalDate toDate =
                LocalDate.of(2026, 12, 31);

        when(gymFacade.getTraineeTrainings(
                "John.Smith",
                fromDate,
                toDate,
                "Mike.Brown",
                "Fitness"
        )).thenReturn(List.of(training));

        when(trainingMapper.toTraineeTrainingResponse(training))
                .thenReturn(trainingResponse);

        ResponseEntity<List<TraineeTrainingResponse>> response =
                traineeController.getTraineeTrainings(
                        authentication,
                        fromDate,
                        toDate,
                        "Mike.Brown",
                        "Fitness"
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(
                trainingResponse,
                response.getBody().get(0)
        );

        verify(authentication).getName();

        verify(gymFacade).getTraineeTrainings(
                "John.Smith",
                fromDate,
                toDate,
                "Mike.Brown",
                "Fitness"
        );

        verify(trainingMapper)
                .toTraineeTrainingResponse(training);
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) {
        try {
            Field field =
                    target.getClass()
                            .getDeclaredField(fieldName);

            field.setAccessible(true);
            field.set(target, value);

        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}