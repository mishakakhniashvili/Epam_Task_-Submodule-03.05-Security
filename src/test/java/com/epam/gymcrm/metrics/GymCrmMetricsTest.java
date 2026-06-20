package com.epam.gymcrm.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymCrmMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private GymCrmMetrics gymCrmMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymCrmMetrics = new GymCrmMetrics(meterRegistry);
    }

    @Test
    void shouldIncrementTraineeRegistrationCounter() {
        gymCrmMetrics.incrementTraineeRegistrations();

        double count = meterRegistry
                .get("gym.trainee.registrations")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldIncrementTrainerRegistrationCounter() {
        gymCrmMetrics.incrementTrainerRegistrations();

        double count = meterRegistry
                .get("gym.trainer.registrations")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldIncrementTrainingCreationCounter() {
        gymCrmMetrics.incrementTrainingsCreated();

        double count = meterRegistry
                .get("gym.trainings.created")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldIncrementLoginSuccessCounter() {
        gymCrmMetrics.incrementLoginSuccess();

        double count = meterRegistry
                .get("gym.login.success")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldIncrementLoginFailureCounter() {
        gymCrmMetrics.incrementLoginFailure();

        double count = meterRegistry
                .get("gym.login.failure")
                .counter()
                .count();

        assertEquals(1.0, count);
    }
}