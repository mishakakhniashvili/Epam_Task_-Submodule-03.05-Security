package com.epam.gymcrm.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, String value) {
        super("Entity " + entityName + " with value " + value + " not found");
    }
}