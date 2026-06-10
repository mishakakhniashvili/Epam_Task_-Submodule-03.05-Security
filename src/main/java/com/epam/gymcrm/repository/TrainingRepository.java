package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("""
            select training
            from Training training
            where training.trainee.user.username = :traineeUsername
              and (:fromDate is null or training.trainingDate >= :fromDate)
              and (:toDate is null or training.trainingDate <= :toDate)
              and (:trainerUsername is null or training.trainer.user.username = :trainerUsername)
              and (:trainingTypeName is null or training.trainingType.trainingTypeName = :trainingTypeName)
            """)
    List<Training> findTraineeTrainings(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerUsername") String trainerUsername,
            @Param("trainingTypeName") String trainingTypeName
    );

    @Query("""
            select training
            from Training training
            where training.trainer.user.username = :trainerUsername
              and (:fromDate is null or training.trainingDate >= :fromDate)
              and (:toDate is null or training.trainingDate <= :toDate)
              and (:traineeUsername is null or training.trainee.user.username = :traineeUsername)
            """)
    List<Training> findTrainerTrainings(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeUsername") String traineeUsername
    );
}