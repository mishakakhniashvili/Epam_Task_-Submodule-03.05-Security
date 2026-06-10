package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);

    private TrainerRepository trainerRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private UserRepository userRepository;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Transactional
    public Trainer create(Trainer trainer) {
        validateTrainerRequiredFields(trainer);
        User user = trainer.getUser();
        String username = generateUsername(trainer);

        user.setUsername(username);
        user.setPassword(passwordGenerator.generatePassword());
        user.setActive(true);

        Trainer createdTrainer = trainerRepository.save(trainer);
        LOGGER.info("Created trainer with id={} and username={}",
                createdTrainer.getId(),
                createdTrainer.getUser().getUsername());

        return createdTrainer;
    }

    @Transactional
    public Trainer update(String username, String password, Trainer trainer) {
        validateCredentials(username, password);
        validateTrainerRequiredFields(trainer);
        LOGGER.info("Updating trainer with id={}", trainer.getId());
        return trainerRepository.save(trainer);
    }

    public Optional<Trainer> findById(Long id) {

        LOGGER.info("Finding trainer with id={}", id);
        return trainerRepository.findById(id);
    }

    public List<Trainer> findAll() {
        LOGGER.info("finding all trainers");
        return trainerRepository.findAll();
    }

    //finds every existing username and generates a new one according to the rules
    private String generateUsername(Trainer trainer) {
        User user = trainer.getUser();
        List<String> existingUsernames = userRepository.findAllUsernames();

        return usernameGenerator.generateUsername(
                user.getFirstName(),
                user.getLastName(),
                existingUsernames
        );
    }



    public Optional<Trainer> findByUsername(String authUsername, String authPassword, String targetUsername) {
        validateCredentials(authUsername, authPassword);
        LOGGER.info("Finding trainer with username={}", targetUsername);
        return trainerRepository.findByUserUsername(targetUsername);
    }


    public boolean isCredentialsValid(String username, String password) {
        return trainerRepository.findByUserUsername(username)
                .map(trainer -> trainer.getUser().getPassword().equals(password))
                .orElse(false);
    }

    public void validateCredentials(String username, String password){
        if(!isCredentialsValid(username, password)){
            throw new AuthenticationException("Invalid credentials entered");}
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        validateCredentials(username, oldPassword);

        Trainer trainer = trainerRepository.findByUserUsername(username).orElseThrow();

        trainer.getUser().setPassword(newPassword);

        trainerRepository.save(trainer);

        LOGGER.info("Changed password for username={}", username);
    }


    @Transactional
    public void activate(String username, String password){
        validateCredentials(username, password);
        Trainer trainer = trainerRepository.findByUserUsername(username).orElseThrow();
        if(trainer.getUser().isActive()){
            throw new IllegalStateException("Trainer is already active.");}
        trainer.getUser().setActive(true);
        trainerRepository.save(trainer);
        LOGGER.info("Activated trainer with id={}", trainer.getId());
    }

    @Transactional
    public void deactivate(String username, String password){
        validateCredentials(username, password);
        Trainer trainer = trainerRepository.findByUserUsername(username).orElseThrow();
        if(!trainer.getUser().isActive()){
            throw new IllegalStateException("Trainer is already inactive.");}
        trainer.getUser().setActive(false);
        trainerRepository.save(trainer);
        LOGGER.info("Deactivated trainer with id={}", trainer.getId());
    }

    private void validateTrainerRequiredFields(Trainer trainer) {
        if (trainer == null) {
            throw new ValidationException("Trainer cannot be null");
        }

        if (trainer.getUser() == null) {
            throw new ValidationException("Trainer user cannot be null");
        }
        if (trainer.getSpecialization() == null) {
            throw new ValidationException("Trainer specialization cannot be null");
        }

        validateRequiredString(trainer.getUser().getFirstName(), "firstName");
        validateRequiredString(trainer.getUser().getLastName(), "lastName");
    }

    private void validateRequiredString(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be null or blank");
        }
    }
}