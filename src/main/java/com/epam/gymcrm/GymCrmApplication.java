package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class GymCrmApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

//        GymFacade gymFacade = context.getBean(GymFacade.class);
//
//        Trainee trainee = new Trainee(
//                null,
//                "John",
//                "Smith",
//                null,
//                null,
//                false,
//                LocalDate.of(2000, 1, 1),
//                "Tbilisi"
//        );
//
//        Trainer trainer = new Trainer(
//                null,
//                "Mike",
//                "Brown",
//                null,
//                null,
//                false,
//                "Fitness"
//        );
//
//        Trainee createdTrainee = gymFacade.createTrainee(trainee);
//        Trainer createdTrainer = gymFacade.createTrainer(trainer);
//
//        Training training = new Training(
//                null,
//                createdTrainee.getUserId(),
//                createdTrainer.getUserId(),
//                "Morning training",
//                new TrainingType("Fitness"),
//                LocalDate.now(),
//                60
//        );
//
//        Training createdTraining = gymFacade.createTraining(training);
//
//        System.out.println("Created trainee username: " + createdTrainee.getUsername());
//        System.out.println("Created trainer username: " + createdTrainer.getUsername());
//        System.out.println("Created training ID: " + createdTraining.getTrainingId());

        context.close();
    }
}