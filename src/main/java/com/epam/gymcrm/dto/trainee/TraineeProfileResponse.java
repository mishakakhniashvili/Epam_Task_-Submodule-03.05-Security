package com.epam.gymcrm.dto.trainee;

import com.epam.gymcrm.dto.trainer.TrainerShortResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TraineeProfileResponse {
    String username;

    String firstName;

    String lastName ;

    LocalDate dateOfBirth;

    String address;

    boolean active;

    List<TrainerShortResponse> trainers;
}
