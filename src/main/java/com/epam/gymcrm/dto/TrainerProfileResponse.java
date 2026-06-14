package com.epam.gymcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrainerProfileResponse {
    private String firstName;

    private String lastName;

    private String specialization;

    private Boolean active;

    private List<TraineeShortResponse> trainees;
}
