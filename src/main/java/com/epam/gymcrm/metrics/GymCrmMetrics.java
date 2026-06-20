package com.epam.gymcrm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymCrmMetrics {
    private final Counter traineeRegistrationCounter;
    private final Counter trainerRegistrationCounter;
    private final Counter trainingCreationCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    public GymCrmMetrics(MeterRegistry meterRegistry) {
        traineeRegistrationCounter = Counter.builder("gym.trainee.registrations")
                .description("Number of registered trainees")
                .register(meterRegistry);
        trainerRegistrationCounter = Counter.builder("gym.trainer.registrations")
                .description("Number of registered trainers")
                .register(meterRegistry);
        trainingCreationCounter = Counter.builder("gym.trainings.created")
                .description("Number of registered trainings")
                .register(meterRegistry);
        loginSuccessCounter = Counter.builder("gym.login.success")
                .description("Number of successful logins")
                .register(meterRegistry);
        loginFailureCounter = Counter.builder("gym.login.failure")
                .description("Number of unsuccessful logins")
                .register(meterRegistry);
    }
    public void incrementTraineeRegistrations() {
        traineeRegistrationCounter.increment();
    }
    public void incrementTrainerRegistrations() {
        trainerRegistrationCounter.increment();
    }
    public void incrementTrainingsCreated() {
        trainingCreationCounter.increment();
    }
    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }
    public void incrementLoginFailure() {
        loginFailureCounter.increment();
    }
}
