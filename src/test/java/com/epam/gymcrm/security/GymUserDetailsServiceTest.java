package com.epam.gymcrm.security;

import com.epam.gymcrm.entity.User;
import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GymUserDetailsServiceTest {

    private UserRepository userRepository;
    private GymUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new GymUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadActiveUserFromDatabase() {
        User user = new User(
                "John",
                "Smith",
                "John.Smith",
                "hashed-password",
                true
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("John.Smith");

        assertEquals("John.Smith", result.getUsername());
        assertEquals("hashed-password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(
                result.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_USER")
                        )
        );

        verify(userRepository).findByUsername("John.Smith");
    }

    @Test
    void shouldReturnDisabledUserWhenDatabaseUserIsInactive() {
        User user = new User(
                "John",
                "Smith",
                "John.Smith",
                "hashed-password",
                false
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("John.Smith");

        assertFalse(result.isEnabled());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing.user"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing.user")
        );
    }
}