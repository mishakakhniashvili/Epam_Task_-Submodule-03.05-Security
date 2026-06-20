package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.AddTrainingRequest;
import com.epam.gymcrm.dto.TrainingTypeResponse;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private GymFacade gymFacade;

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        trainingController = new TrainingController(gymFacade, gymCrmMetrics);
    }

    @Test
    void addTrainingShouldReturnOkAndCallFacade() {
        AddTrainingRequest request = new AddTrainingRequest();
        setField(request, "trainerUsername", "Mike.Brown");
        setField(request, "traineeUsername", "John.Smith");
        setField(request, "trainingName", "Morning Training");
        setField(request, "trainingDate", LocalDate.of(2026, 5, 10));
        setField(request, "trainingDuration", 60);

        ResponseEntity<Void> response =
                trainingController.addTraining("trainerPass", request);

        assertEquals(200, response.getStatusCode().value());

        verify(gymFacade).addTraining(
                "Mike.Brown",
                "trainerPass",
                "John.Smith",
                "Morning Training",
                LocalDate.of(2026, 5, 10),
                60
        );
        verify(gymCrmMetrics).incrementTrainingsCreated();
    }

    @Test
    void getTrainingTypesShouldReturnTypesWhenTraineeCredentialsAreValid() {
        TrainingType fitness = new TrainingType("Fitness");
        fitness.setId(1L);

        when(gymFacade.isTraineeCredentialsValid("John.Smith", "pass123"))
                .thenReturn(true);
        when(gymFacade.getTrainingTypes()).thenReturn(List.of(fitness));

        ResponseEntity<List<TrainingTypeResponse>> response =
                trainingController.getTrainingTypes("John.Smith", "pass123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("Fitness", response.getBody().get(0).getTrainingTypeName());
    }

    @Test
    void getTrainingTypesShouldReturnTypesWhenTrainerCredentialsAreValid() {
        TrainingType yoga = new TrainingType("Yoga");
        yoga.setId(2L);

        when(gymFacade.isTraineeCredentialsValid("Mike.Brown", "pass123"))
                .thenReturn(false);
        when(gymFacade.isTrainerCredentialsValid("Mike.Brown", "pass123"))
                .thenReturn(true);
        when(gymFacade.getTrainingTypes()).thenReturn(List.of(yoga));

        ResponseEntity<List<TrainingTypeResponse>> response =
                trainingController.getTrainingTypes("Mike.Brown", "pass123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(2L, response.getBody().get(0).getId());
        assertEquals("Yoga", response.getBody().get(0).getTrainingTypeName());
    }

    @Test
    void getTrainingTypesShouldThrowAuthenticationExceptionWhenCredentialsAreInvalid() {
        when(gymFacade.isTraineeCredentialsValid("bad", "bad"))
                .thenReturn(false);
        when(gymFacade.isTrainerCredentialsValid("bad", "bad"))
                .thenReturn(false);

        assertThrows(
                AuthenticationException.class,
                () -> trainingController.getTrainingTypes("bad", "bad")
        );
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