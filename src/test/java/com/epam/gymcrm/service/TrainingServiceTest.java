package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TrainingService trainingService;
    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;

    @BeforeEach
    void setUp() {
        trainingDao = Mockito.mock(TrainingDao.class);
        traineeDao = Mockito.mock(TraineeDao.class);
        trainerDao = Mockito.mock(TrainerDao.class);
        trainingTypeDao = Mockito.mock(TrainingTypeDao.class);

        trainingService = new TrainingService();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);
        trainingService.setTrainingTypeDao(trainingTypeDao);
    }

    @Test
    void shouldAddTrainingWhenTrainerCredentialsAreValid() {
        TrainingType fitness = new TrainingType("Fitness");

        Trainee trainee = new Trainee(
                new User("John", "Smith", "John.Smith", "traineePass", true),
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        Trainer trainer = new Trainer(
                new User("Mike", "Brown", "Mike.Brown", "trainerPass", true),
                fitness
        );

        when(trainerDao.findByUsername("Mike.Brown")).thenReturn(Optional.of(trainer));
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(fitness));
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(1L);
            return training;
        });

        Training createdTraining = trainingService.addTraining(
                "Mike.Brown",
                "trainerPass",
                "John.Smith",
                "Morning Training",
                "Fitness",
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

        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenTrainerPasswordIsWrong() {
        TrainingType fitness = new TrainingType("Fitness");

        Trainer trainer = new Trainer(
                new User("Mike", "Brown", "Mike.Brown", "trainerPass", true),
                fitness
        );

        when(trainerDao.findByUsername("Mike.Brown")).thenReturn(Optional.of(trainer));

        assertThrows(AuthenticationException.class,
                () -> trainingService.addTraining(
                        "Mike.Brown",
                        "wrongPass",
                        "John.Smith",
                        "Morning Training",
                        "Fitness",
                        LocalDate.of(2026, 5, 10),
                        60
                ));

        verify(trainingDao, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenTrainingDurationIsInvalid() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(
                        "Mike.Brown",
                        "trainerPass",
                        "John.Smith",
                        "Morning Training",
                        "Fitness",
                        LocalDate.of(2026, 5, 10),
                        0
                ));

        verify(trainingDao, never()).save(any());
    }

    @Test
    void shouldReturnTraineeTrainingsWithFilters() {
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);

        Trainee trainee = new Trainee(
                new User("John", "Smith", "John.Smith", "traineePass", true),
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        List<Training> trainings = List.of(new Training());

        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainingDao.findTraineeTrainings(
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
    }

    @Test
    void shouldReturnTrainerTrainingsWithFilters() {
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);

        TrainingType fitness = new TrainingType("Fitness");
        Trainer trainer = new Trainer(
                new User("Mike", "Brown", "Mike.Brown", "trainerPass", true),
                fitness
        );

        List<Training> trainings = List.of(new Training());

        when(trainerDao.findByUsername("Mike.Brown")).thenReturn(Optional.of(trainer));
        when(trainingDao.findTrainerTrainings(
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
    }

    @Test
    void shouldThrowValidationExceptionWhenFromDateIsAfterToDate() {
        assertThrows(ValidationException.class,
                () -> trainingService.getTrainerTrainings(
                        "Mike.Brown",
                        "trainerPass",
                        LocalDate.of(2026, 12, 31),
                        LocalDate.of(2026, 1, 1),
                        "John.Smith"
                ));
    }
}