package com.epam.gymcrm.health;

import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainingTypeRepositoryHealthIndicatorTest {

    private TrainingTypeRepository trainingTypeRepository;
    private TrainingTypeRepositoryHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        healthIndicator =
                new TrainingTypeRepositoryHealthIndicator(trainingTypeRepository);
    }

    @Test
    void shouldReturnUpWhenTrainingTypesExist() {
        when(trainingTypeRepository.count()).thenReturn(5L);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(5L, health.getDetails().get("trainingTypesCount"));
    }

    @Test
    void shouldReturnDownWhenTrainingTypesDoNotExist() {
        when(trainingTypeRepository.count()).thenReturn(0L);

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(
                "No training types found",
                health.getDetails().get("reason")
        );
    }

    @Test
    void shouldReturnDownWhenRepositoryThrowsException() {
        when(trainingTypeRepository.count())
                .thenThrow(new RuntimeException("Database unavailable"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(
                health.getDetails().get("error")
                        .toString()
                        .contains("Database unavailable")
        );
    }
}