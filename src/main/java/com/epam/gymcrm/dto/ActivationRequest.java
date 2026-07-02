package com.epam.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ActivationRequest {
    @NotNull
    private Boolean active;
}
