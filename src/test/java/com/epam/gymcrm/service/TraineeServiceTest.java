package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        userRepository = mock(UserRepository.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
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
        User user = new User(
                "John",
                "Smith",
                null,
                null,
                false
        );

        Trainee trainee = new Trainee(
                user,
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        when(userRepository.findAllUsernames())
                .thenReturn(List.of());

        when(usernameGenerator.generateUsername(
                "John",
                "Smith",
                List.of()
        )).thenReturn("John.Smith");

        when(passwordGenerator.generatePassword())
                .thenReturn("pass123456");

        when(passwordEncoder.encode("pass123456"))
                .thenReturn("hashed-pass123456");

        when(traineeRepository.save(trainee))
                .thenAnswer(invocation -> {
                    trainee.setId(1L);
                    trainee.getUser().setId(10L);
                    return trainee;
                });

        RegistrationResult result =
                traineeService.create(trainee);

        assertEquals("John.Smith", result.username());
        assertEquals("pass123456", result.rawPassword());
        assertEquals(1L, trainee.getId());
        assertEquals(
                "John.Smith",
                trainee.getUser().getUsername()
        );
        assertEquals(
                "hashed-pass123456",
                trainee.getUser().getPassword()
        );
        assertTrue(trainee.getUser().isActive());

        verify(passwordEncoder)
                .encode("pass123456");

        verify(traineeRepository)
                .save(trainee);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreatingTraineeWithoutFirstName() {
        User user = new User(
                null,
                "Smith",
                null,
                null,
                false
        );

        Trainee trainee = new Trainee(
                user,
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        assertThrows(
                ValidationException.class,
                () -> traineeService.create(trainee)
        );

        verify(traineeRepository, never())
                .save(any());

        verify(passwordEncoder, never())
                .encode(any());
    }

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        User user = new User(
                "John",
                "Smith",
                "John.Smith",
                "hashed-oldpass",
                true
        );

        Trainee trainee = new Trainee(
                user,
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        /*
         * changePassword performs two repository lookups:
         * 1. credential validation
         * 2. loading the entity before changing the password
         */
        when(traineeRepository.findByUserUsername("John.Smith"))
                .thenReturn(
                        Optional.of(trainee),
                        Optional.of(trainee)
                );

        when(passwordEncoder.matches(
                "oldpass",
                "hashed-oldpass"
        )).thenReturn(true);

        when(passwordEncoder.encode("newpass"))
                .thenReturn("hashed-newpass");

        when(traineeRepository.save(trainee))
                .thenReturn(trainee);

        traineeService.changePassword(
                "John.Smith",
                "oldpass",
                "newpass"
        );

        assertEquals(
                "hashed-newpass",
                trainee.getUser().getPassword()
        );

        verify(passwordEncoder).matches(
                "oldpass",
                "hashed-oldpass"
        );

        verify(passwordEncoder)
                .encode("newpass");

        verify(traineeRepository)
                .save(trainee);
    }

    @Test
    void shouldDeactivateActiveTraineeWithoutCheckingPassword() {
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

        when(traineeRepository.findByUserUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(traineeRepository.save(trainee))
                .thenReturn(trainee);

        traineeService.deactivate("John.Smith");

        assertFalse(trainee.getUser().isActive());

        verify(traineeRepository)
                .findByUserUsername("John.Smith");

        verify(traineeRepository)
                .save(trainee);

        verify(passwordEncoder, never())
                .matches(any(), any());
    }

    @Test
    void shouldUpdateTraineeTrainersListWithoutCheckingPassword() {
        User traineeUser = new User(
                "John",
                "Smith",
                "John.Smith",
                "hashed-password",
                true
        );

        Trainee trainee = new Trainee(
                traineeUser,
                LocalDate.of(2000, 1, 1),
                "Tbilisi"
        );

        TrainingType fitness =
                new TrainingType("Fitness");

        Trainer firstTrainer = new Trainer(
                new User(
                        "Mike",
                        "Brown",
                        "Mike.Brown",
                        "pass1",
                        true
                ),
                fitness
        );

        Trainer secondTrainer = new Trainer(
                new User(
                        "Anna",
                        "White",
                        "Anna.White",
                        "pass2",
                        true
                ),
                fitness
        );

        when(traineeRepository.findByUserUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUserUsername("Mike.Brown"))
                .thenReturn(Optional.of(firstTrainer));

        when(trainerRepository.findByUserUsername("Anna.White"))
                .thenReturn(Optional.of(secondTrainer));

        when(traineeRepository.save(trainee))
                .thenReturn(trainee);

        Trainee result =
                traineeService.updateTraineeTrainersList(
                        "John.Smith",
                        List.of(
                                "Mike.Brown",
                                "Anna.White"
                        )
                );

        assertEquals(2, result.getTrainers().size());
        assertTrue(
                result.getTrainers().contains(firstTrainer)
        );
        assertTrue(
                result.getTrainers().contains(secondTrainer)
        );

        verify(traineeRepository)
                .save(trainee);

        verify(passwordEncoder, never())
                .matches(any(), any());
    }
}