package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeService traineeService;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private UserDao userDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        traineeDao = Mockito.mock(TraineeDao.class);
        trainerDao = Mockito.mock(TrainerDao.class);
        userDao = Mockito.mock(UserDao.class);
        usernameGenerator = Mockito.mock(UsernameGenerator.class);
        passwordGenerator = Mockito.mock(PasswordGenerator.class);

        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setTrainerDao(trainerDao);
        traineeService.setUserDao(userDao);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void shouldCreateTraineeWithGeneratedUsernamePasswordAndActiveStatus() {
        User user = new User("John", "Smith", null, null, false);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        when(userDao.findAllUsernames()).thenReturn(List.of());
        when(usernameGenerator.generateUsername("John", "Smith", List.of()))
                .thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("pass123456");
        when(traineeDao.save(trainee)).thenAnswer(invocation -> {
            trainee.setId(1L);
            trainee.getUser().setId(10L);
            return trainee;
        });

        Trainee createdTrainee = traineeService.create(trainee);

        assertEquals(1L, createdTrainee.getId());
        assertEquals("John.Smith", createdTrainee.getUser().getUsername());
        assertEquals("pass123456", createdTrainee.getUser().getPassword());
        assertTrue(createdTrainee.getUser().isActive());

        verify(traineeDao).save(trainee);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreatingTraineeWithoutFirstName() {
        User user = new User(null, "Smith", null, null, false);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        assertThrows(ValidationException.class, () -> traineeService.create(trainee));

        verify(traineeDao, never()).save(any());
    }

    @Test
    void shouldUpdateTraineeWhenCredentialsAreValid() {
        User authUser = new User("John", "Smith", "John.Smith", "oldpass", true);
        Trainee existingTrainee = new Trainee(authUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        User updatedUser = new User("John", "Smith", "John.Smith", "oldpass", true);
        Trainee updatedTrainee = new Trainee(updatedUser, LocalDate.of(2000, 1, 1), "Batumi");
        updatedTrainee.setId(1L);

        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(existingTrainee));
        when(traineeDao.update(updatedTrainee)).thenReturn(updatedTrainee);

        Trainee result = traineeService.update("John.Smith", "oldpass", updatedTrainee);

        assertEquals("Batumi", result.getAddress());
        verify(traineeDao).update(updatedTrainee);
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenUpdatingWithWrongPassword() {
        User authUser = new User("John", "Smith", "John.Smith", "oldpass", true);
        Trainee existingTrainee = new Trainee(authUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        User updatedUser = new User("John", "Smith", "John.Smith", "oldpass", true);
        Trainee updatedTrainee = new Trainee(updatedUser, LocalDate.of(2000, 1, 1), "Batumi");

        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(existingTrainee));

        assertThrows(AuthenticationException.class,
                () -> traineeService.update("John.Smith", "wrongpass", updatedTrainee));

        verify(traineeDao, never()).update(any());
    }

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        User user = new User("John", "Smith", "John.Smith", "oldpass", true);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        traineeService.changePassword("John.Smith", "oldpass", "newpass");

        assertEquals("newpass", trainee.getUser().getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldDeactivateActiveTrainee() {
        User user = new User("John", "Smith", "John.Smith", "password", true);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        traineeService.deactivate("John.Smith", "password");

        assertFalse(trainee.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldUpdateTraineeTrainersList() {
        User traineeUser = new User("John", "Smith", "John.Smith", "password", true);
        Trainee trainee = new Trainee(traineeUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        TrainingType fitness = new TrainingType("Fitness");

        Trainer firstTrainer = new Trainer(
                new User("Mike", "Brown", "Mike.Brown", "pass1", true),
                fitness
        );
        Trainer secondTrainer = new Trainer(
                new User("Anna", "White", "Anna.White", "pass2", true),
                fitness
        );

        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Brown")).thenReturn(Optional.of(firstTrainer));
        when(trainerDao.findByUsername("Anna.White")).thenReturn(Optional.of(secondTrainer));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee result = traineeService.updateTraineeTrainersList(
                "John.Smith",
                "password",
                List.of("Mike.Brown", "Anna.White")
        );

        assertEquals(2, result.getTrainers().size());
        assertTrue(result.getTrainers().contains(firstTrainer));
        assertTrue(result.getTrainers().contains(secondTrainer));
        verify(traineeDao).update(trainee);
    }
}