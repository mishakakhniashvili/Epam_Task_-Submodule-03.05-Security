package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeService traineeService;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private UserRepository userRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private PasswordEncoder passwordEncoder;
    @BeforeEach
    void setUp() {
        traineeRepository = Mockito.mock(TraineeRepository.class);
        trainerRepository = Mockito.mock(TrainerRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        usernameGenerator = Mockito.mock(UsernameGenerator.class);
        passwordGenerator = Mockito.mock(PasswordGenerator.class);
        passwordEncoder = mock(PasswordEncoder.class);

        traineeService = new TraineeService();
        traineeService.setPasswordEncoder(passwordEncoder);
        traineeService.setTraineeRepository(traineeRepository);
        traineeService.setTrainerRepository(trainerRepository);
        traineeService.setUserRepository(userRepository);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void shouldCreateTraineeWithGeneratedUsernamePasswordAndActiveStatus() {
        User user = new User("John", "Smith", null, null, false);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        when(userRepository.findAllUsernames()).thenReturn(List.of());
        when(usernameGenerator.generateUsername("John", "Smith", List.of()))
                .thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("pass123456");
        when(traineeRepository.save(trainee)).thenAnswer(invocation -> {
            trainee.setId(1L);
            trainee.getUser().setId(10L);
            return trainee;
        });
        when(passwordEncoder.encode("pass123456"))
                .thenReturn("hashed-pass123456");

        RegistrationResult result = traineeService.create(trainee);
        assertEquals("John.Smith", result.username());
        assertEquals("pass123456", result.rawPassword());

        assertEquals(1L, trainee.getId());
        assertEquals("John.Smith", trainee.getUser().getUsername());
        assertEquals("hashed-pass123456", trainee.getUser().getPassword());
        assertTrue(trainee.getUser().isActive());

        verify(passwordEncoder).encode("pass123456");
        verify(traineeRepository).save(trainee);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreatingTraineeWithoutFirstName() {
        User user = new User(null, "Smith", null, null, false);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        assertThrows(ValidationException.class, () -> traineeService.create(trainee));

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTraineeWhenCredentialsAreValid() {
        User authUser = new User("John", "Smith", "John.Smith", "hashed-oldpass", true);
        Trainee existingTrainee = new Trainee(authUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        User updatedUser = new User("John", "Smith", "John.Smith", "hashed-oldpass", true);
        Trainee updatedTrainee = new Trainee(updatedUser, LocalDate.of(2000, 1, 1), "Batumi");
        updatedTrainee.setId(1L);

        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(existingTrainee));
        when(traineeRepository.save(updatedTrainee)).thenReturn(updatedTrainee);
        when(passwordEncoder.matches("oldpass", "hashed-oldpass"))
                .thenReturn(true);

        Trainee result = traineeService.update("John.Smith", "oldpass", updatedTrainee);

        assertEquals("Batumi", result.getAddress());
        verify(traineeRepository).save(updatedTrainee);
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenUpdatingWithWrongPassword() {
        User authUser =
                new User("John", "Smith", "John.Smith", "hashed-oldpass", true);
        Trainee existingTrainee = new Trainee(authUser, LocalDate.of(2000, 1, 1), "Tbilisi");

        User updatedUser = new User("John", "Smith", "John.Smith", "oldpass", true);
        Trainee updatedTrainee = new Trainee(updatedUser, LocalDate.of(2000, 1, 1), "Batumi");

        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(existingTrainee));
        when(passwordEncoder.matches("wrongpass", "hashed-oldpass"))
                .thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> traineeService.update("John.Smith", "wrongpass", updatedTrainee));

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        User user =
                new User("John", "Smith", "John.Smith", "hashed-oldpass", true);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(passwordEncoder.matches("oldpass", "hashed-oldpass"))
                .thenReturn(true);

        when(passwordEncoder.encode("newpass"))
                .thenReturn("hashed-newpass");

        traineeService.changePassword("John.Smith", "oldpass", "newpass");

        assertEquals("hashed-newpass", trainee.getUser().getPassword());
        verify(passwordEncoder).encode("newpass");
        verify(traineeRepository).save(trainee);
    }

    @Test
    void shouldDeactivateActiveTrainee() {
        User user =
                new User("John", "Smith", "John.Smith", "hashed-password", true);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Tbilisi");

        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(passwordEncoder.matches("password", "hashed-password"))
                .thenReturn(true);

        traineeService.deactivate("John.Smith", "password");

        assertFalse(trainee.getUser().isActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void shouldUpdateTraineeTrainersList() {
        User traineeUser = new User("John", "Smith", "John.Smith", "hashed-password", true);
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

        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername("Mike.Brown")).thenReturn(Optional.of(firstTrainer));
        when(trainerRepository.findByUserUsername("Anna.White")).thenReturn(Optional.of(secondTrainer));
        when(traineeRepository.save(trainee)).thenReturn(trainee);
        when(passwordEncoder.matches("password", "hashed-password"))
                .thenReturn(true);

        Trainee result = traineeService.updateTraineeTrainersList(
                "John.Smith",
                "password",
                List.of("Mike.Brown", "Anna.White")
        );

        assertEquals(2, result.getTrainers().size());
        assertTrue(result.getTrainers().contains(firstTrainer));
        assertTrue(result.getTrainers().contains(secondTrainer));
        verify(traineeRepository).save(trainee);
    }
}