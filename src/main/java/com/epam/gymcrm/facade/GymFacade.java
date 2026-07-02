package com.epam.gymcrm.facade;

import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.service.RegistrationResult;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public RegistrationResult createTrainee(Trainee trainee) {
        return traineeService.create(trainee);
    }

    public RegistrationResult  createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    public RegistrationResult  createTrainer(String firstName, String lastName, String specializationName) {
        return trainerService.create(firstName, lastName, specializationName);
    }

    public Optional<Trainee> findTraineeByUsername(String username) {
        return traineeService.findByUsername(username);
    }

    public Optional<Trainer> findTrainerByUsername(String username) {
        return trainerService.findByUsername(username);
    }

    public boolean isTraineeCredentialsValid(String username, String password) {
        return traineeService.isCredentialsValid(username, password);
    }

    public boolean isTrainerCredentialsValid(String username, String password) {
        return trainerService.isCredentialsValid(username, password);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public List<Training> getTraineeTrainings(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName
    ) {
        return trainingService.getTraineeTrainings(
                traineeUsername,
                fromDate,
                toDate,
                trainerUsername,
                trainingTypeName
        );
    }

    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername){
        return traineeService.getTrainersNotAssignedToTrainee(traineeUsername);
    }

    public Trainee updateProfile(
            String username,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            Boolean active
    ) {
        return traineeService.updateProfile(
                username,
                firstName,
                lastName,
                dateOfBirth,
                address,
                active
        );
    }

    public Trainer updateProfile(
            String username,
            String firstName,
            String lastName,
            Boolean active
    ) {
        return trainerService.updateProfile(
                username,
                firstName,
                lastName,
                active
        );
    }

    public void deleteTraineeByUsername(String username) {
        traineeService.deleteByUsername(username);
    }

    public void activateTrainee(String username) {
        traineeService.activate(username);
    }

    public void deactivateTrainee(String username) {
        traineeService.deactivate(username);
    }

    public void activateTrainer(String username) {
        trainerService.activate(username);
    }

    public void deactivateTrainer(String username) {
        trainerService.deactivate(username);
    }

    public List<TrainingType> getTrainingTypes() {
        return trainingService.getTrainingTypes();
    }

    public Trainee updateTraineeTrainersList(
            String traineeUsername,
            List<String> trainerUsernames
    ) {
        return traineeService.updateTraineeTrainersList(
                traineeUsername,
                trainerUsernames
        );
    }
    public List<Training> getTrainerTrainings(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername
    ) {
        return trainingService.getTrainerTrainings(
                trainerUsername,
                fromDate,
                toDate,
                traineeUsername
        );
    }

    public Training addTraining(
            String trainerUsername,
            String traineeUsername,
            String trainingName,
            LocalDate trainingDate,
            Integer trainingDuration
    ) {
        return trainingService.addTraining(
                trainerUsername,
                traineeUsername,
                trainingName,
                trainingDate,
                trainingDuration
        );
    }
}