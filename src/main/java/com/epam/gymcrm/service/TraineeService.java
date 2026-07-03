package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TraineeService {

    private UserRepository userRepository;
    private TraineeRepository traineeRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TrainerRepository trainerRepository;
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResult create(Trainee trainee) {
        validateTraineeRequiredFields(trainee);
        User user = trainee.getUser();
        String username = generateUsername(trainee);
        String rawPassword = passwordGenerator.generatePassword();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);

        Trainee createdTrainee = traineeRepository.save(trainee);

        LOGGER.info("Created trainee with id={} and username={}",
                createdTrainee.getId(),
                createdTrainee.getUser().getUsername());

        return new RegistrationResult(
                createdTrainee.getUser().getUsername(),
                rawPassword
        );
    }

    //finds every existing username and generates a new one according to the rules
    private String generateUsername(Trainee trainee) {
        User user = trainee.getUser();
        List<String> existingUsernames = userRepository.findAllUsernames();

        return usernameGenerator.generateUsername(
                user.getFirstName(),
                user.getLastName(),
                existingUsernames
        );
    }

    @Transactional
    public void deleteById(Long id) {
        LOGGER.info("Deleting trainee with id={}", id);
        traineeRepository.deleteById(id);
    }

    public Optional<Trainee> findById(Long id) {
        LOGGER.info("finding trainee with id={}", id);
        return traineeRepository.findById(id);
    }

    public List<Trainee> findAll() {
        return traineeRepository.findAll();
    }

    public Optional<Trainee> findByUsername(String username) {
        validateRequiredString(username, "username");

        LOGGER.info("Finding trainee with username={}", username);

        return traineeRepository.findByUserUsername(username);
    }

    public boolean isCredentialsValid(String username, String password) {
        return traineeRepository.findByUserUsername(username)
                .map(trainee -> passwordEncoder.matches(
                        password,
                        trainee.getUser().getPassword()
                ))
                .orElse(false);
    }

    public void validateCredentials(String username, String password){
        if(!isCredentialsValid(username, password)){
            throw new AuthenticationException("Invalid credentials entered");
        }
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        validateCredentials(username, oldPassword);

        Trainee trainee = traineeRepository.findByUserUsername(username).orElseThrow();

        trainee.getUser().setPassword(passwordEncoder.encode(newPassword));

        traineeRepository.save(trainee);

        LOGGER.info("Changed password for username={}", username);
    }

    @Transactional
    public void activate(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("trainee", username)
                );

        if (trainee.getUser().isActive()) {
            throw new IllegalStateException("Trainee is already active.");
        }

        trainee.getUser().setActive(true);
        traineeRepository.save(trainee);

        LOGGER.info("Activated trainee with id={}", trainee.getId());
    }

    @Transactional
    public void deactivate(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("trainee", username)
                );

        if (!trainee.getUser().isActive()) {
            throw new IllegalStateException("Trainee is already inactive.");
        }

        trainee.getUser().setActive(false);
        traineeRepository.save(trainee);

        LOGGER.info("Deactivated trainee with id={}", trainee.getId());
    }

    @Transactional
    public void deleteByUsername(String username){
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("trainee", username));
        traineeRepository.deleteById(trainee.getId());
        LOGGER.info("Deleted trainee with id={}", trainee.getId());
    }

    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername){
        if(traineeRepository.findByUserUsername(traineeUsername).isPresent()){
        return trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername);
        }
        throw new EntityNotFoundException("trainee", traineeUsername);
    }

    @Transactional
    public Trainee updateTraineeTrainersList(
            String traineeUsername,
            List<String> trainerUsernames
    ) {
        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainee", traineeUsername));

        if (trainerUsernames == null) {
            throw new ValidationException("Trainer usernames cannot be null");
        }

        Set<Trainer> trainers = new HashSet<>();

        for (String trainerUsername : trainerUsernames) {
            if (trainerUsername == null || trainerUsername.isBlank()) {
                throw new ValidationException("Trainer username cannot be blank");
            }

            Trainer trainer = trainerRepository.findByUserUsername(trainerUsername)
                    .orElseThrow(() -> new EntityNotFoundException("trainer", trainerUsername));

            trainers.add(trainer);
        }

        trainee.setTrainers(trainers);

        return traineeRepository.save(trainee);
    }

    private void validateTraineeRequiredFields(Trainee trainee) {
        if (trainee == null) {
            throw new ValidationException("Trainee cannot be null");
        }

        if (trainee.getUser() == null) {
            throw new ValidationException("Trainee user cannot be null");
        }

        validateRequiredString(trainee.getUser().getFirstName(), "firstName");
        validateRequiredString(trainee.getUser().getLastName(), "lastName");
    }

    private void validateRequiredString(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be null or blank");
        }
    }

    @Transactional
    public Trainee updateProfile(
            String username,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            Boolean active
    ) {
        validateRequiredString(username, "username");
        validateRequiredString(firstName, "firstName");
        validateRequiredString(lastName, "lastName");

        if (active == null) {
            throw new ValidationException("active cannot be null");
        }

        Trainee trainee = traineeRepository
                .findByUserUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("trainee", username)
                );

        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.getUser().setActive(active);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        return traineeRepository.save(trainee);
    }
}