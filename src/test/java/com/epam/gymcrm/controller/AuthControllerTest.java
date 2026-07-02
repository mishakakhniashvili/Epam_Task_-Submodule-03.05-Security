package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.LoginRequest;
import com.epam.gymcrm.dto.TokenResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import com.epam.gymcrm.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                gymFacade,
                gymCrmMetrics,
                authenticationManager,
                jwtService
        );
    }

    @Test
    void loginShouldReturnJwtWhenCredentialsAreValid() {
        LoginRequest request =
                new LoginRequest("John.Smith", "pass");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(authentication))
                .thenReturn("signed-jwt-token");
        when(jwtService.getExpirationSeconds())
                .thenReturn(1800L);

        ResponseEntity<TokenResponse> response =
                authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                "signed-jwt-token",
                response.getBody().accessToken()
        );
        assertEquals("Bearer", response.getBody().tokenType());
        assertEquals(1800L, response.getBody().expiresIn());

        verify(gymCrmMetrics).incrementLoginSuccess();
        verify(gymCrmMetrics, never()).incrementLoginFailure();
    }

    @Test
    void loginShouldThrowAuthenticationExceptionWhenCredentialsAreInvalid() {
        LoginRequest request =
                new LoginRequest("bad", "wrong");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                AuthenticationException.class,
                () -> authController.login(request)
        );

        verify(gymCrmMetrics).incrementLoginFailure();
        verify(gymCrmMetrics, never()).incrementLoginSuccess();
        verify(jwtService, never()).generateToken(any());
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