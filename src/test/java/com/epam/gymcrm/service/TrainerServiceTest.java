package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerService trainerService;
    private TrainerRepository trainerRepository;
    private UserRepository userRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        trainerRepository = Mockito.mock(TrainerRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        usernameGenerator = Mockito.mock(UsernameGenerator.class);
        passwordGenerator = Mockito.mock(PasswordGenerator.class);

        trainerService = new TrainerService();
        trainerService.setTrainerRepository(trainerRepository);
        trainerService.setUserRepository(userRepository);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void shouldCreateTrainerWithGeneratedUsernamePasswordAndActiveStatus() {
        TrainingType fitness = new TrainingType("Fitness");
        User user = new User("Mike", "Brown", null, null, false);
        Trainer trainer = new Trainer(user, fitness);

        when(userRepository.findAllUsernames()).thenReturn(List.of());
        when(usernameGenerator.generateUsername("Mike", "Brown", List.of()))
                .thenReturn("Mike.Brown");
        when(passwordGenerator.generatePassword()).thenReturn("pass123456");
        when(trainerRepository.save(trainer)).thenAnswer(invocation -> {
            trainer.setId(1L);
            trainer.getUser().setId(10L);
            return trainer;
        });

        Trainer createdTrainer = trainerService.create(trainer);

        assertEquals(1L, createdTrainer.getId());
        assertEquals("Mike.Brown", createdTrainer.getUser().getUsername());
        assertEquals("pass123456", createdTrainer.getUser().getPassword());
        assertTrue(createdTrainer.getUser().isActive());

        verify(trainerRepository).save(trainer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreatingTrainerWithoutSpecialization() {
        User user = new User("Mike", "Brown", null, null, false);
        Trainer trainer = new Trainer(user, null);

        assertThrows(ValidationException.class, () -> trainerService.create(trainer));

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTrainerWhenCredentialsAreValid() {
        TrainingType fitness = new TrainingType("Fitness");
        TrainingType yoga = new TrainingType("Yoga");

        User authUser = new User("Mike", "Brown", "Mike.Brown", "oldpass", true);
        Trainer existingTrainer = new Trainer(authUser, fitness);

        User updatedUser = new User("Mike", "Brown", "Mike.Brown", "oldpass", true);
        Trainer updatedTrainer = new Trainer(updatedUser, yoga);
        updatedTrainer.setId(1L);

        when(trainerRepository.findByUserUsername("Mike.Brown")).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(updatedTrainer)).thenReturn(updatedTrainer);

        Trainer result = trainerService.update("Mike.Brown", "oldpass", updatedTrainer);

        assertEquals(yoga, result.getSpecialization());
        verify(trainerRepository).save(updatedTrainer);
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenUpdatingWithWrongPassword() {
        TrainingType fitness = new TrainingType("Fitness");

        User authUser = new User("Mike", "Brown", "Mike.Brown", "oldpass", true);
        Trainer existingTrainer = new Trainer(authUser, fitness);

        User updatedUser = new User("Mike", "Brown", "Mike.Brown", "oldpass", true);
        Trainer updatedTrainer = new Trainer(updatedUser, fitness);

        when(trainerRepository.findByUserUsername("Mike.Brown")).thenReturn(Optional.of(existingTrainer));

        assertThrows(AuthenticationException.class,
                () -> trainerService.update("Mike.Brown", "wrongpass", updatedTrainer));

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldChangePasswordWhenOldPasswordIsValid() {
        TrainingType fitness = new TrainingType("Fitness");
        User user = new User("Mike", "Brown", "Mike.Brown", "oldpass", true);
        Trainer trainer = new Trainer(user, fitness);

        when(trainerRepository.findByUserUsername("Mike.Brown")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.changePassword("Mike.Brown", "oldpass", "newpass");

        assertEquals("newpass", trainer.getUser().getPassword());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void shouldDeactivateActiveTrainer() {
        TrainingType fitness = new TrainingType("Fitness");
        User user = new User("Mike", "Brown", "Mike.Brown", "password", true);
        Trainer trainer = new Trainer(user, fitness);

        when(trainerRepository.findByUserUsername("Mike.Brown")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.deactivate("Mike.Brown", "password");

        assertFalse(trainer.getUser().isActive());
        verify(trainerRepository).save(trainer);
    }
}