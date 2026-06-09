package com.epam.gymcrm.service;

import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UsernameGenerator {

    public String generateUsername(String firstName, String lastName, Collection<String> existingUsernames) {
        String baseUsername = firstName + "." + lastName;

        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }

        int counter = 1;
        String username = baseUsername + counter;

        while (existingUsernames.contains(username)) {
            counter++;
            username = baseUsername + counter;
        }

        return username;
    }
}