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


    public Trainee updateTrainee(String username, String password, Trainee trainee) {
        return traineeService.update(username, password, trainee);
    }

    public RegistrationResult  createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    public RegistrationResult  createTrainer(String firstName, String lastName, String specializationName) {
        return trainerService.create(firstName, lastName, specializationName);
    }

    public Trainer updateTrainer(String username, String password, Trainer trainer) {
        return trainerService.update(username, password, trainer);
    }

    public Optional<Trainee> findTraineeByUsername(String authUsername,String authPassword,String targetUsername) {
        return traineeService.findByUsername(authUsername, authPassword, targetUsername);
    }

    public Optional<Trainer> findTrainerByUsername(String authUsername,String authPassword,String targetUsername) {
        return trainerService.findByUsername(authUsername, authPassword, targetUsername);
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

    public void activateTrainee(String username, String password) {
        traineeService.activate(username, password);
    }

    public void deactivateTrainee(String username, String password) {
        traineeService.deactivate(username, password);
    }

    public void activateTrainer(String username, String password) {
        trainerService.activate(username, password);
    }

    public void deactivateTrainer(String username, String password) {
        trainerService.deactivate(username, password);
    }

    public void deleteTraineeByUsername(String username, String password) {
        traineeService.deleteByUsername(username, password);
    }
    public Training addTraining(
            String trainerUsername,
            String trainerPassword,
            String traineeUsername,
            String trainingName,
            LocalDate trainingDate,
            Integer trainingDuration
    ) {
        return trainingService.addTraining(
                trainerUsername,
                trainerPassword,
                traineeUsername,
                trainingName,
                trainingDate,
                trainingDuration
        );
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
    public List<Training> getTrainerTrainings(
            String trainerUsername,
            String trainerPassword,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername
    ) {
        return trainingService.getTrainerTrainings(
                trainerUsername,
                trainerPassword,
                fromDate,
                toDate,
                traineeUsername
        );
    }

    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername){
        return traineeService.getTrainersNotAssignedToTrainee(traineeUsername);
    }

    public Trainee updateTraineeTrainersList(
            String traineeUsername,
            String traineePassword,
            List<String> trainerUsernames
    ) {
        return traineeService.updateTraineeTrainersList(
                traineeUsername,
                traineePassword,
                trainerUsernames
        );
    }

    public Trainee updateProfile(
            String username,
            String password,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            Boolean active){
        return traineeService.updateProfile(
                username, password, firstName, lastName, dateOfBirth, address, active
        );
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
            String password,
            String firstName,
            String lastName,
            Boolean active){
        return trainerService.updateProfile(
                username, password, firstName, lastName, active
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


}