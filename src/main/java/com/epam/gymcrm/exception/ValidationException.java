package com.epam.gymcrm.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(String cause) {
         super("Invalid parameter entered: " + cause);

    }
}