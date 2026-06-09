package com.epam.gymcrm.dao;

import com.epam.gymcrm.entity.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Training save(Training training) {
        entityManager.persist(training);
        return training;
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    public List<Training> findAll() {
        return entityManager.createQuery("select tr from Training tr", Training.class).getResultList();
    }

    public List<Training> findTraineeTrainings(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName
    ) {
        return entityManager
                .createQuery(
                        "select tr from Training tr " +
                                "where tr.trainee.user.username = :traineeUsername " +
                                "and (:fromDate is null or tr.trainingDate >= :fromDate) " +
                                "and (:toDate is null or tr.trainingDate <= :toDate) " +
                                "and (:trainerUsername is null or tr.trainer.user.username = :trainerUsername) " +
                                "and (:trainingTypeName is null or tr.trainingType.trainingTypeName = :trainingTypeName)",
                        Training.class
                )
                .setParameter("traineeUsername", traineeUsername)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("trainerUsername", trainerUsername)
                .setParameter("trainingTypeName", trainingTypeName)
                .getResultList();
    }

    public List<Training> findTrainerTrainings(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername
    ) {
        return entityManager
                .createQuery(
                        "select tr from Training tr " +
                                "where tr.trainer.user.username = :trainerUsername " +
                                "and (:fromDate is null or tr.trainingDate >= :fromDate) " +
                                "and (:toDate is null or tr.trainingDate <= :toDate) " +
                                "and (:traineeUsername is null or tr.trainee.user.username = :traineeUsername) ",
                        Training.class
                )
                .setParameter("trainerUsername", trainerUsername)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }

}