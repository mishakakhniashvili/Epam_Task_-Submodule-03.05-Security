package com.epam.gymcrm.health;

import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserRepositoryHealthIndicatorTest {

    private UserRepository userRepository;
    private UserRepositoryHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        healthIndicator = new UserRepositoryHealthIndicator(userRepository);
    }

    @Test
    void shouldReturnUpWhenRepositoryIsAvailable() {
        when(userRepository.count()).thenReturn(3L);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(3L, health.getDetails().get("usersCount"));
    }

    @Test
    void shouldReturnUpWhenRepositoryIsEmpty() {
        when(userRepository.count()).thenReturn(0L);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(0L, health.getDetails().get("usersCount"));
    }

    @Test
    void shouldReturnDownWhenRepositoryThrowsException() {
        when(userRepository.count())
                .thenThrow(new RuntimeException("Database unavailable"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(
                health.getDetails().get("error")
                        .toString()
                        .contains("Database unavailable")
        );
    }
}