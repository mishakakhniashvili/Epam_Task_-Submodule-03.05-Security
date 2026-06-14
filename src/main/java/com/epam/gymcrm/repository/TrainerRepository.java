package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.Trainer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    @EntityGraph(attributePaths = {
            "user",
            "specialization",
            "trainees",
            "trainees.user",
    })
    Optional<Trainer> findByUserUsername(String username);

    @Query("""
            select trainer
            from Trainer trainer
            where trainer not in (
                select assignedTrainer
                from Trainee trainee
                join trainee.trainers assignedTrainer
                where trainee.user.username = :traineeUsername
            )
            """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);
}