package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private GymFacade gymFacade;

    private AuthController authController;

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    @BeforeEach
    void setUp() {
        authController = new AuthController(gymFacade,gymCrmMetrics);
    }

    @Test
    void loginShouldReturnOkWhenTraineeCredentialsAreValid() {
        when(gymFacade.isTraineeCredentialsValid("John.Smith", "pass"))
                .thenReturn(true);

        ResponseEntity<Void> response = authController.login("John.Smith", "pass");

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).isTraineeCredentialsValid("John.Smith", "pass");
        verify(gymCrmMetrics).incrementLoginSuccess();
        verify(gymCrmMetrics, never()).incrementLoginFailure();
    }

    @Test
    void loginShouldReturnOkWhenTrainerCredentialsAreValid() {
        when(gymFacade.isTraineeCredentialsValid("Mike.Brown", "pass"))
                .thenReturn(false);
        when(gymFacade.isTrainerCredentialsValid("Mike.Brown", "pass"))
                .thenReturn(true);

        ResponseEntity<Void> response = authController.login("Mike.Brown", "pass");

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).isTrainerCredentialsValid("Mike.Brown", "pass");
        verify(gymCrmMetrics).incrementLoginSuccess();
        verify(gymCrmMetrics, never()).incrementLoginFailure();
    }

    @Test
    void loginShouldThrowAuthenticationExceptionWhenCredentialsAreInvalid() {
        when(gymFacade.isTraineeCredentialsValid("bad", "bad"))
                .thenReturn(false);
        when(gymFacade.isTrainerCredentialsValid("bad", "bad"))
                .thenReturn(false);

        assertThrows(
                AuthenticationException.class,
                () -> authController.login("bad", "bad")
        );
        verify(gymCrmMetrics).incrementLoginFailure();
        verify(gymCrmMetrics, never()).incrementLoginSuccess();
    }

    @Test
    void changePasswordShouldChangeTraineePasswordWhenTraineeCredentialsAreValid() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        setField(request, "username", "John.Smith");
        setField(request, "oldPassword", "oldPass");
        setField(request, "newPassword", "newPass");

        when(gymFacade.isTraineeCredentialsValid("John.Smith", "oldPass"))
                .thenReturn(true);

        ResponseEntity<Void> response = authController.changePassword(request);

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).changeTraineePassword("John.Smith", "oldPass", "newPass");
        verify(gymFacade, never()).changeTrainerPassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePasswordShouldChangeTrainerPasswordWhenTrainerCredentialsAreValid() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        setField(request, "username", "Mike.Brown");
        setField(request, "oldPassword", "oldPass");
        setField(request, "newPassword", "newPass");

        when(gymFacade.isTraineeCredentialsValid("Mike.Brown", "oldPass"))
                .thenReturn(false);
        when(gymFacade.isTrainerCredentialsValid("Mike.Brown", "oldPass"))
                .thenReturn(true);

        ResponseEntity<Void> response = authController.changePassword(request);

        assertEquals(200, response.getStatusCode().value());
        verify(gymFacade).changeTrainerPassword("Mike.Brown", "oldPass", "newPass");
        verify(gymFacade, never()).changeTraineePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePasswordShouldThrowAuthenticationExceptionWhenCredentialsAreInvalid() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        setField(request, "username", "bad");
        setField(request, "oldPassword", "bad");
        setField(request, "newPassword", "newPass");

        when(gymFacade.isTraineeCredentialsValid("bad", "bad"))
                .thenReturn(false);
        when(gymFacade.isTrainerCredentialsValid("bad", "bad"))
                .thenReturn(false);

        assertThrows(
                AuthenticationException.class,
                () -> authController.changePassword(request)
        );
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}