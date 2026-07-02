package com.epam.gymcrm.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class TraineeTrainersUpdateRequest {
    @NotEmpty
    private List<@NotBlank String> trainerUsernames;
}
