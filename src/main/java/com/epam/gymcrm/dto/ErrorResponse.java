package com.epam.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    @NotBlank(message = "status is required")
    private int status;

    @NotBlank(message = "error is required")
    private String error;

    private String message;

}