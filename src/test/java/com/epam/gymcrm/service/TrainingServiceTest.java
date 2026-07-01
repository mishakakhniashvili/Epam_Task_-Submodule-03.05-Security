package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TrainingService trainingService;
    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        trainingRepository = mock(TrainingRepository.class);
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        trainingService = new TrainingService();
        trainingService.setTrainingRepository(trainingRepository);
        trainingService.setTraineeRepository(traineeRepository);
        trainingService.setTrainerRepository(trainerRepository);
        trainingService.setTrainingTypeRepository(trainingTypeRepository);
        trainingService.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void shouldAddTrainingWhenTrainerCredentialsAreValid() {
        TrainingType fitness = new TrainingType("Fitness");

        Trainee trainee = new Trainee(
                new User(
                        "John",
                        "Smith",
                        "John.Smith",
                        "hashed-trainee-pass",
                        true
                ),
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        Trainer trainer = new Trainer(
                new User(
                        "Mike",
                        "Brown",
                        "Mike.Brown",
                        "hashed-trainer-pass",
                        true
                ),
                fitness
        );

        when(trainerRepository.findByUserUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("trainerPass", "hashed-trainer-pass"))
                .thenReturn(true);
        when(traineeRepository.findByUserUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));
        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(invocation -> {
                    Training training = invocation.getArgument(0);
                    training.setId(1L);
                    return training;
                });

        Training createdTraining = trainingService.addTraining(
                "Mike.Brown",
                "trainerPass",
                "John.Smith",
                "Morning Training",
                LocalDate.of(2026, 5, 10),
                60
        );

        assertEquals(1L, createdTraining.getId());
        assertEquals("Morning Training", createdTraining.getTrainingName());
        assertEquals(LocalDate.of(2026, 5, 10), createdTraining.getTrainingDate());
        assertEquals(60, createdTraining.getTrainingDuration());
        assertSame(trainer, createdTraining.getTrainer());
        assertSame(trainee, createdTraining.getTrainee());
        assertSame(fitness, createdTraining.getTrainingType());

        verify(passwordEncoder).matches("trainerPass", "hashed-trainer-pass");
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenTrainerPasswordIsWrong() {
        TrainingType fitness = new TrainingType("Fitness");

        Trainer trainer = new Trainer(
                new User(
                        "Mike",
                        "Brown",
                        "Mike.Brown",
                        "hashed-trainer-pass",
                        true
                ),
                fitness
        );

        when(trainerRepository.findByUserUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("wrongPass", "hashed-trainer-pass"))
                .thenReturn(false);

        assertThrows(
                AuthenticationException.class,
                () -> trainingService.addTraining(
                        "Mike.Brown",
                        "wrongPass",
                        "John.Smith",
                        "Morning Training",
                        LocalDate.of(2026, 5, 10),
                        60
                )
        );

        verify(trainingRepository, never()).save(any());
        verify(traineeRepository, never()).findByUserUsername(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenTrainingDurationIsInvalid() {
        assertThrows(
                ValidationException.class,
                () -> trainingService.addTraining(
                        "Mike.Brown",
                        "trainerPass",
                        "John.Smith",
                        "Morning Training",
                        LocalDate.of(2026, 5, 10),
                        0
                )
        );

        verify(trainingRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void shouldReturnTraineeTrainingsWithFilters() {
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);

        Trainee trainee = new Trainee(
                new User(
                        "John",
                        "Smith",
                        "John.Smith",
                        "hashed-trainee-pass",
                        true
                ),
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        List<Training> trainings = List.of(new Training());

        when(traineeRepository.findByUserUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("traineePass", "hashed-trainee-pass"))
                .thenReturn(true);
        when(trainingRepository.findTraineeTrainings(
                "John.Smith",
                fromDate,
                toDate,
                "Mike.Brown",
                "Fitness"
        )).thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainings(
                "John.Smith",
                "traineePass",
                fromDate,
                toDate,
                "Mike.Brown",
                "Fitness"
        );

        assertEquals(trainings, result);
        verify(passwordEncoder).matches("traineePass", "hashed-trainee-pass");
    }

    @Test
    void shouldReturnTrainerTrainingsWithFilters() {
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);

        TrainingType fitness = new TrainingType("Fitness");

        Trainer trainer = new Trainer(
                new User(
                        "Mike",
                        "Brown",
                        "Mike.Brown",
                        "hashed-trainer-pass",
                        true
                ),
                fitness
        );

        List<Training> trainings = List.of(new Training());

        when(trainerRepository.findByUserUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("trainerPass", "hashed-trainer-pass"))
                .thenReturn(true);
        when(trainingRepository.findTrainerTrainings(
                "Mike.Brown",
                fromDate,
                toDate,
                "John.Smith"
        )).thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainings(
                "Mike.Brown",
                "trainerPass",
                fromDate,
                toDate,
                "John.Smith"
        );

        assertEquals(trainings, result);
        verify(passwordEncoder).matches("trainerPass", "hashed-trainer-pass");
    }

    @Test
    void shouldThrowValidationExceptionWhenFromDateIsAfterToDate() {
        assertThrows(
                ValidationException.class,
                () -> trainingService.getTrainerTrainings(
                        "Mike.Brown",
                        "trainerPass",
                        LocalDate.of(2026, 12, 31),
                        LocalDate.of(2026, 1, 1),
                        "John.Smith"
                )
        );

        verify(passwordEncoder, never()).matches(any(), any());
    }
}
