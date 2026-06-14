package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.TraineeShortResponse;
import com.epam.gymcrm.dto.TrainerProfileResponse;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrainerMapper {

    public TrainerProfileResponse toProfileResponse(Trainer trainer){
        User user = trainer.getUser();
        List<TraineeShortResponse> trainees = trainer.getTrainees().stream()
                .map((trainee) -> new TraineeShortResponse(
                                trainee.getUser().getUsername(),
                                trainee.getUser().getFirstName(),
                                trainee.getUser().getLastName()
                        )
                )
                .toList();

        return new TrainerProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                user.isActive(),
                trainees
        );
    }
}
