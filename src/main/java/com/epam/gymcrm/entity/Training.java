package com.epam.gymcrm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @Column(name = "training_date",nullable = false)
    private LocalDate trainingDate;

    @Column(name="training_duration", nullable = false)
    private Integer trainingDuration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id",  nullable = false)
    private Trainer trainer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id",  nullable = false)
    private Trainee trainee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    public Training() {

    }

    public Training(
            String trainingName,
            LocalDate trainingDate,
            Trainer trainer,
            Trainee trainee,
            TrainingType trainingType,
            Integer trainingDuration) {
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.trainer = trainer;
        this.trainee = trainee;
        this.trainingType = trainingType;
    }


}
