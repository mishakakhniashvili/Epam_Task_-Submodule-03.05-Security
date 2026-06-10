package com.epam.gymcrm.service;

import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Transactional
    public Training create(Training training) {
        Training createdTraining = trainingRepository.save(training);

        LOGGER.info("Created training with id={} and name={}",
                createdTraining.getId(),
                createdTraining.getTrainingName());

        return createdTraining;
    }

    @Transactional
    public Training addTraining(
            String trainerUsername,
            String trainerPassword,
            String traineeUsername,
            String trainingName,
            String trainingTypeName,
            LocalDate trainingDate,
            Integer trainingDuration
    ) {
        validateParameter(trainerUsername);
        validateParameter(trainerPassword);
        validateParameter(traineeUsername);
        validateParameter(trainingName);
        validateParameter(trainingTypeName);

        if (trainingDate == null) {
            throw new ValidationException("Training date is null");
        }

        if (trainingDuration == null) {
            throw new ValidationException("Training duration is null");
        }

        if (trainingDuration <= 0) {
            throw new ValidationException("Training duration is less or equal to 0");
        }

        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainer", trainerUsername));

        if (!trainer.getUser().getPassword().equals(trainerPassword)) {
            throw new AuthenticationException("Wrong Password");
        }

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainee", traineeUsername));

        TrainingType trainingType = trainingTypeRepository.findByName(trainingTypeName)
                .orElseThrow(() -> new EntityNotFoundException("trainingType", trainingTypeName));

        Training training = new Training(
                trainingName,
                trainingDate,
                trainer,
                trainee,
                trainingType,
                trainingDuration
        );

        return create(training);
    }

    public Optional<Training> findById(Long id) {
        LOGGER.info("Finding training with id={}", id);
        return trainingRepository.findById(id);
    }

    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(
            String traineeUsername,
            String traineePassword,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName
    ) {
        validateParameter(traineeUsername);
        validateParameter(traineePassword);

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("From date cannot be after to date");
        }

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainee", traineeUsername));

        if (!trainee.getUser().getPassword().equals(traineePassword)) {
            throw new AuthenticationException("Invalid credentials entered");
        }

        return trainingRepository.findTraineeTrainings(
                traineeUsername,
                fromDate,
                toDate,
                trainerUsername,
                trainingTypeName
        );
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(
            String trainerUsername,
            String trainerPassword,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername
    ) {
        validateParameter(trainerUsername);
        validateParameter(trainerPassword);

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("From date cannot be after to date");
        }

        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainer", trainerUsername));

        if (!trainer.getUser().getPassword().equals(trainerPassword)) {
            throw new AuthenticationException("Invalid credentials entered");
        }

        return trainingRepository.findTrainerTrainings(
                trainerUsername,
                fromDate,
                toDate,
                traineeUsername
        );
    }

    private void validateParameter(String parameter) {
        if (parameter == null) {
            throw new ValidationException("Parameter should not be null");
        }

        if (parameter.isBlank()) {
            throw new ValidationException("Parameter should not be empty");
        }
    }
}