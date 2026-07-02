package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerService trainerService;
    private TrainerRepository trainerRepository;
    private UserRepository userRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        trainerRepository = mock(TrainerRepository.class);
        userRepository = mock(UserRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        passwordEncoder = mock(PasswordEncoder.class);

        trainerService = new TrainerService();
        trainerService.setTrainerRepository(trainerRepository);
        trainerService.setUserRepository(userRepository);
        trainerService.setTrainingTypeRepository(trainingTypeRepository);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
        trainerService.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void shouldCreateTrainerWithGeneratedUsernameHashedPasswordAndActiveStatus() {
        TrainingType fitness = new TrainingType("Fitness");
        User user = new User(
                "Mike",
                "Brown",
                null,
                null,
                false
        );

        Trainer trainer = new Trainer(user, fitness);

        when(userRepository.findAllUsernames())
                .thenReturn(List.of());

        when(usernameGenerator.generateUsername(
                "Mike",
                "Brown",
                List.of()
        )).thenReturn("Mike.Brown");

        when(passwordGenerator.generatePassword())
                .thenReturn("pass123456");

        when(passwordEncoder.encode("pass123456"))
                .thenReturn("hashed-pass123456");

        when(trainerRepository.save(trainer))
                .thenAnswer(invocation -> {
                    trainer.setId(1L);
                    trainer.getUser().setId(10L);
                    return trainer;
                });

        RegistrationResult result =
                trainerService.create(trainer);

        assertEquals("Mike.Brown", result.username());
        assertEquals("pass123456", result.rawPassword());
        assertEquals(1L, trainer.getId());
        assertEquals(
                "Mike.Brown",
                trainer.getUser().getUsername()
        );
        assertEquals(
                "hashed-pass123456",
                trainer.getUser().getPassword()
        );
        assertTrue(trainer.getUser().isActive());

        verify(passwordEncoder).encode("pass123456");
        verify(trainerRepository).save(trainer);
    }

    @Test
    void shouldCreateTrainerFromRegistrationFields() {
        TrainingType fitness = new TrainingType("Fitness");

        when(trainingTypeRepository.findByName("Fitness"))
                .thenReturn(Optional.of(fitness));

        when(userRepository.findAllUsernames())
                .thenReturn(List.of());

        when(usernameGenerator.generateUsername(
                "Mike",
                "Brown",
                List.of()
        )).thenReturn("Mike.Brown");

        when(passwordGenerator.generatePassword())
                .thenReturn("pass123456");

        when(passwordEncoder.encode("pass123456"))
                .thenReturn("hashed-pass123456");

        when(trainerRepository.save(any(Trainer.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        RegistrationResult result =
                trainerService.create(
                        "Mike",
                        "Brown",
                        "Fitness"
                );

        assertEquals("Mike.Brown", result.username());
        assertEquals("pass123456", result.rawPassword());

        ArgumentCaptor<Trainer> captor =
                ArgumentCaptor.forClass(Trainer.class);

        verify(trainerRepository).save(captor.capture());

        Trainer savedTrainer = captor.getValue();

        assertEquals(
                fitness,
                savedTrainer.getSpecialization()
        );
        assertEquals(
                "Mike",
                savedTrainer.getUser().getFirstName()
        );
        assertEquals(
                "Brown",
                savedTrainer.getUser().getLastName()
        );
        assertEquals(
                "Mike.Brown",
                savedTrainer.getUser().getUsername()
        );
        assertEquals(
                "hashed-pass123456",
                savedTrainer.getUser().getPassword()
        );
        assertTrue(savedTrainer.getUser().isActive());
    }

    @Test
    void shouldThrowValidationExceptionWhenCreatingTrainerWithoutSpecialization() {
        User user = new User(
                "Mike",
                "Brown",
                null,
                null,
                false
        );

        Trainer trainer = new Trainer(user, null);

        assertThrows(
                ValidationException.class,
                () -> trainerService.create(trainer)
        );

        verify(trainerRepository, never())
                .save(any());

        verify(passwordEncoder, never())
                .encode(any());
    }

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        TrainingType fitness =
                new TrainingType("Fitness");

        User user = new User(
                "Mike",
                "Brown",
                "Mike.Brown",
                "hashed-oldpass",
                true
        );

        Trainer trainer = new Trainer(user, fitness);

        when(trainerRepository.findByUserUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(passwordEncoder.matches(
                "oldpass",
                "hashed-oldpass"
        )).thenReturn(true);

        when(passwordEncoder.encode("newpass"))
                .thenReturn("hashed-newpass");

        when(trainerRepository.save(trainer))
                .thenReturn(trainer);

        trainerService.changePassword(
                "Mike.Brown",
                "oldpass",
                "newpass"
        );

        assertEquals(
                "hashed-newpass",
                trainer.getUser().getPassword()
        );

        verify(passwordEncoder).matches(
                "oldpass",
                "hashed-oldpass"
        );

        verify(passwordEncoder).encode("newpass");
        verify(trainerRepository).save(trainer);
    }

    @Test
    void shouldDeactivateActiveTrainerWithoutCheckingPassword() {
        TrainingType fitness =
                new TrainingType("Fitness");

        User user = new User(
                "Mike",
                "Brown",
                "Mike.Brown",
                "hashed-password",
                true
        );

        Trainer trainer = new Trainer(user, fitness);

        when(trainerRepository.findByUserUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainerRepository.save(trainer))
                .thenReturn(trainer);

        trainerService.deactivate("Mike.Brown");

        assertFalse(trainer.getUser().isActive());

        verify(trainerRepository)
                .findByUserUsername("Mike.Brown");

        verify(trainerRepository).save(trainer);

        verify(passwordEncoder, never())
                .matches(any(), any());
    }
}