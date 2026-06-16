package com.epam.gymcrm.dto.trainer;

import com.epam.gymcrm.dto.trainee.TraineeShortResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrainerProfileResponse {
    private String username;

    private String firstName;

    private String lastName;

    private String specialization;

    private Boolean active;

    private List<TraineeShortResponse> trainees;
}
