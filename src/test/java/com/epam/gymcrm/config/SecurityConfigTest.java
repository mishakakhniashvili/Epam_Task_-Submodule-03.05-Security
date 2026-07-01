package com.epam.gymcrm.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void traineeRegistrationShouldBePublic() throws Exception {
        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "Security",
                              "lastName": "Test"
                            }
                            """))
                .andExpect(status().isCreated());
    }


    @Test
    void trainerRegistrationShouldBePublic() throws Exception {
        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "Security",
                              "lastName": "Trainer"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/login")
                        .param("username", "missing.user")
                        .param("password", "wrongPassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void trainingTypesShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordEncoderShouldHashAndSaltPasswords() {
        String firstHash = passwordEncoder.encode("samePassword");
        String secondHash = passwordEncoder.encode("samePassword");

        assertNotEquals("samePassword", firstHash);
        assertNotEquals(firstHash, secondHash);
        assertTrue(passwordEncoder.matches("samePassword", firstHash));
        assertTrue(passwordEncoder.matches("samePassword", secondHash));
    }

}