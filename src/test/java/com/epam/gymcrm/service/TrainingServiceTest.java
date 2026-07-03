package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    void setUp() {
        trainingRepository = mock(TrainingRepository.class);
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);

        trainingService = new TrainingService();
        trainingService.setTrainingRepository(trainingRepository);
        trainingService.setTraineeRepository(traineeRepository);
        trainingService.setTrainerRepository(trainerRepository);
        trainingService.setTrainingTypeRepository(
                trainingTypeRepository
        );
    }

    @Test
    void shouldAddTrainingWhenTrainerAndTraineeExist() {
        TrainingType fitness =
                new TrainingType("Fitness");

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

        when(trainerRepository.findByUserUsername(
                "Mike.Brown"
        )).thenReturn(Optional.of(trainer));

        when(traineeRepository.findByUserUsername(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(invocation -> {
                    Training training =
                            invocation.getArgument(0);

                    training.setId(1L);
                    return training;
                });

        Training createdTraining =
                trainingService.addTraining(
                        "Mike.Brown",
                        "John.Smith",
                        "Morning Training",
                        LocalDate.of(2026, 5, 10),
                        60
                );

        assertEquals(1L, createdTraining.getId());

        assertEquals(
                "Morning Training",
                createdTraining.getTrainingName()
        );

        assertEquals(
                LocalDate.of(2026, 5, 10),
                createdTraining.getTrainingDate()
        );

        assertEquals(
                60,
                createdTraining.getTrainingDuration()
        );

        assertSame(
                trainer,
                createdTraining.getTrainer()
        );

        assertSame(
                trainee,
                createdTraining.getTrainee()
        );

        assertSame(
                fitness,
                createdTraining.getTrainingType()
        );

        verify(trainerRepository)
                .findByUserUsername("Mike.Brown");

        verify(traineeRepository)
                .findByUserUsername("John.Smith");

        verify(trainingRepository)
                .save(any(Training.class));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenTrainerDoesNotExist() {
        when(trainerRepository.findByUserUsername(
                "Missing.Trainer"
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.addTraining(
                        "Missing.Trainer",
                        "John.Smith",
                        "Morning Training",
                        LocalDate.of(2026, 5, 10),
                        60
                )
        );

        verify(traineeRepository, never())
                .findByUserUsername(anyString());

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenTraineeDoesNotExist() {
        TrainingType fitness =
                new TrainingType("Fitness");

        Trainer trainer = new Trainer(
                new User(
                        "Mike",
                        "Brown",
                        "Mike.Brown",
                        "hashed-password",
                        true
                ),
                fitness
        );

        when(trainerRepository.findByUserUsername(
                "Mike.Brown"
        )).thenReturn(Optional.of(trainer));

        when(traineeRepository.findByUserUsername(
                "Missing.Trainee"
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.addTraining(
                        "Mike.Brown",
                        "Missing.Trainee",
                        "Morning Training",
                        LocalDate.of(2026, 5, 10),
                        60
                )
        );

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenTrainingDurationIsInvalid() {
        assertThrows(
                ValidationException.class,
                () -> trainingService.addTraining(
                        "Mike.Brown",
                        "John.Smith",
                        "Morning Training",
                        LocalDate.of(2026, 5, 10),
                        0
                )
        );

        verifyNoInteractions(
                trainerRepository,
                traineeRepository,
                trainingRepository
        );
    }

    @Test
    void shouldReturnTraineeTrainingsWithFilters() {
        LocalDate fromDate =
                LocalDate.of(2026, 1, 1);

        LocalDate toDate =
                LocalDate.of(2026, 12, 31);

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

        List<Training> trainings =
                List.of(new Training());

        when(traineeRepository.findByUserUsername(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainingRepository.findTraineeTrainings(
                "John.Smith",
                fromDate,
                toDate,
                "Mike.Brown",
                "Fitness"
        )).thenReturn(trainings);

        List<Training> result =
                trainingService.getTraineeTrainings(
                        "John.Smith",
                        fromDate,
                        toDate,
                        "Mike.Brown",
                        "Fitness"
                );

        assertEquals(trainings, result);

        verify(traineeRepository)
                .findByUserUsername("John.Smith");

        verify(trainingRepository)
                .findTraineeTrainings(
                        "John.Smith",
                        fromDate,
                        toDate,
                        "Mike.Brown",
                        "Fitness"
                );
    }

    @Test
    void shouldReturnTrainerTrainingsWithFilters() {
        LocalDate fromDate =
                LocalDate.of(2026, 1, 1);

        LocalDate toDate =
                LocalDate.of(2026, 12, 31);

        TrainingType fitness =
                new TrainingType("Fitness");

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

        List<Training> trainings =
                List.of(new Training());

        when(trainerRepository.findByUserUsername(
                "Mike.Brown"
        )).thenReturn(Optional.of(trainer));

        when(trainingRepository.findTrainerTrainings(
                "Mike.Brown",
                fromDate,
                toDate,
                "John.Smith"
        )).thenReturn(trainings);

        List<Training> result =
                trainingService.getTrainerTrainings(
                        "Mike.Brown",
                        fromDate,
                        toDate,
                        "John.Smith"
                );

        assertEquals(trainings, result);

        verify(trainerRepository)
                .findByUserUsername("Mike.Brown");

        verify(trainingRepository)
                .findTrainerTrainings(
                        "Mike.Brown",
                        fromDate,
                        toDate,
                        "John.Smith"
                );
    }

    @Test
    void shouldThrowValidationExceptionWhenFromDateIsAfterToDate() {
        assertThrows(
                ValidationException.class,
                () -> trainingService.getTrainerTrainings(
                        "Mike.Brown",
                        LocalDate.of(2026, 12, 31),
                        LocalDate.of(2026, 1, 1),
                        "John.Smith"
                )
        );

        verifyNoInteractions(
                trainerRepository,
                trainingRepository
        );
    }
}