package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.epam.gymcrm.dao.TraineeDao;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);
    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao){
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }
    @Transactional
    public Training create(Training training) {
        Training createdTraining = trainingDao.save(training);

        LOGGER.info("Created training with id={} and name={}", createdTraining.getId(), createdTraining.getTrainingName());

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
    ){
        validateParameter(trainerUsername);
        validateParameter(trainerPassword);
        validateParameter(traineeUsername);
        validateParameter(trainingName);
        validateParameter(trainingTypeName);
        if(trainingDate == null ){
            throw new ValidationException("Training date is null");
        }
        if(trainingDuration == null){
            throw new ValidationException("Training duration is null ");
        } else if (trainingDuration <=0) {
            throw new ValidationException("Training duration is less or equal to 0");
        }
        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainer" , trainerUsername));

        if(!trainer.getUser().getPassword().equals(trainerPassword)){
            throw new AuthenticationException("Wrong Password");
        }

        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainee",traineeUsername));

        TrainingType trainingType = trainingTypeDao.findByName(trainingTypeName)
                .orElseThrow(() -> new EntityNotFoundException("trainingType",trainingTypeName) );



        return create(new Training(
                trainingName,
                trainingDate,
                trainer,
                trainee,
                trainingType,
                trainingDuration));
    }

    public Optional<Training> findById(Long id) {
        LOGGER.info("finding training with id={}", id);
        return trainingDao.findById(id);
    }

    public List<Training> findAll() {
        return trainingDao.findAll();
    }

    private void validateParameter(String parameter) {
        if( parameter==null){
            throw  new ValidationException("Parameter should not be null");
        }
        else if( parameter.isBlank()){
            throw  new ValidationException("Parameter should not be empty");
        }
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(
            String traineeUsername,
            String traineePassword,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName
    ){
        validateParameter(traineeUsername);
        validateParameter(traineePassword);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("From date cannot be after to date");
        }
        Trainee trainee =traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainee",traineeUsername));
        if (!trainee.getUser().getPassword().equals(traineePassword)) {
            throw new AuthenticationException("Invalid credentials entered");
        }

        return trainingDao.findTraineeTrainings(
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
    ){
        validateParameter(trainerUsername);
        validateParameter(trainerPassword);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("From date cannot be after to date");
        }
        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainer",trainerUsername));
        if (!trainer.getUser().getPassword().equals(trainerPassword)) {
            throw new AuthenticationException("Invalid credentials entered");
        }

        return trainingDao.findTrainerTrainings(
                 trainerUsername,
                 fromDate,
                 toDate,
                 traineeUsername
        );
    }
}