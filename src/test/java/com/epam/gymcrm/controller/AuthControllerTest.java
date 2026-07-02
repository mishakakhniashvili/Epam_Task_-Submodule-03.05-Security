package com.epam.gymcrm.controller;

import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.LoginRequest;
import com.epam.gymcrm.dto.TokenResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.metrics.GymCrmMetrics;
import com.epam.gymcrm.security.JwtService;
import com.epam.gymcrm.security.LoginAttemptService;
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

    @Mock
    private GymCrmMetrics gymCrmMetrics;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginAttemptService loginAttemptService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                gymFacade,
                gymCrmMetrics,
                authenticationManager,
                jwtService,
                loginAttemptService
        );
    }

    @Test
    void loginShouldReturnJwtWhenCredentialsAreValid() {
        LoginRequest request =
                new LoginRequest(
                        "John.Smith",
                        "pass"
                );

        Authentication authentication =
                mock(Authentication.class);

        when(loginAttemptService.isBlocked("John.Smith"))
                .thenReturn(false);

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenReturn(authentication);

        when(jwtService.generateToken(authentication))
                .thenReturn("signed-jwt-token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(1800L);

        ResponseEntity<TokenResponse> response =
                authController.login(request);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "signed-jwt-token",
                response.getBody().accessToken()
        );

        assertEquals(
                "Bearer",
                response.getBody().tokenType()
        );

        assertEquals(
                1800L,
                response.getBody().expiresIn()
        );

        verify(loginAttemptService)
                .loginSucceeded("John.Smith");

        verify(loginAttemptService, never())
                .loginFailed(anyString());

        verify(gymCrmMetrics)
                .incrementLoginSuccess();

        verify(gymCrmMetrics, never())
                .incrementLoginFailure();
    }

    @Test
    void failedLoginShouldRecordFailedAttempt() {
        LoginRequest request =
                new LoginRequest(
                        "John.Smith",
                        "wrong"
                );

        when(loginAttemptService.isBlocked("John.Smith"))
                .thenReturn(false);

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenThrow(
                new BadCredentialsException(
                        "Bad credentials"
                )
        );

        assertThrows(
                AuthenticationException.class,
                () -> authController.login(request)
        );

        verify(loginAttemptService)
                .loginFailed("John.Smith");

        verify(loginAttemptService, never())
                .loginSucceeded(anyString());

        verify(gymCrmMetrics)
                .incrementLoginFailure();

        verify(jwtService, never())
                .generateToken(any());
    }

    @Test
    void blockedUsernameShouldNotBeAuthenticated() {
        LoginRequest request =
                new LoginRequest(
                        "John.Smith",
                        "pass"
                );

        when(loginAttemptService.isBlocked("John.Smith"))
                .thenReturn(true);

        AuthenticationException exception =
                assertThrows(
                        AuthenticationException.class,
                        () -> authController.login(request)
                );

        assertEquals(
                "User is temporarily blocked",
                exception.getMessage()
        );

        verify(authenticationManager, never())
                .authenticate(any());

        verify(jwtService, never())
                .generateToken(any());

        verify(loginAttemptService, never())
                .loginSucceeded(anyString());

        verify(loginAttemptService, never())
                .loginFailed(anyString());

        verify(gymCrmMetrics)
                .incrementLoginFailure();
    }

    @Test
    void changePasswordShouldUseAuthenticatedTraineeUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        setField(request, "username", "Other.User");
        setField(request, "oldPassword", "oldPass");
        setField(request, "newPassword", "newPass");

        when(gymFacade.isTraineeCredentialsValid(
                "John.Smith",
                "oldPass"
        )).thenReturn(true);

        ResponseEntity<Void> response =
                authController.changePassword(
                        authentication,
                        request
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(gymFacade).changeTraineePassword(
                "John.Smith",
                "oldPass",
                "newPass"
        );

        verify(gymFacade, never())
                .changeTrainerPassword(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void changePasswordShouldUseAuthenticatedTrainerUsername() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("Mike.Brown");

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        setField(request, "username", "Other.User");
        setField(request, "oldPassword", "oldPass");
        setField(request, "newPassword", "newPass");

        when(gymFacade.isTraineeCredentialsValid(
                "Mike.Brown",
                "oldPass"
        )).thenReturn(false);

        when(gymFacade.isTrainerCredentialsValid(
                "Mike.Brown",
                "oldPass"
        )).thenReturn(true);

        ResponseEntity<Void> response =
                authController.changePassword(
                        authentication,
                        request
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(gymFacade).changeTrainerPassword(
                "Mike.Brown",
                "oldPass",
                "newPass"
        );

        verify(gymFacade, never())
                .changeTraineePassword(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void changePasswordShouldThrowWhenOldPasswordIsInvalid() {
        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("John.Smith");

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        setField(request, "username", "Other.User");
        setField(request, "oldPassword", "wrongPass");
        setField(request, "newPassword", "newPass");

        when(gymFacade.isTraineeCredentialsValid(
                "John.Smith",
                "wrongPass"
        )).thenReturn(false);

        when(gymFacade.isTrainerCredentialsValid(
                "John.Smith",
                "wrongPass"
        )).thenReturn(false);

        assertThrows(
                AuthenticationException.class,
                () -> authController.changePassword(
                        authentication,
                        request
                )
        );

        verify(gymFacade, never())
                .changeTraineePassword(
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(gymFacade, never())
                .changeTrainerPassword(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) {
        try {
            Field field = target.getClass()
                    .getDeclaredField(fieldName);

            field.setAccessible(true);
            field.set(target, value);

        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}