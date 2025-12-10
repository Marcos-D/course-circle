package com.coursecircle.auth;

import com.coursecircle.dto.AuthResponse;
import com.coursecircle.dto.LoginRequest;
import com.coursecircle.dto.RegisterRequest;
import com.coursecircle.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("ci")
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void requiresJwtForProtectedEndpoints() {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setDisplayName("Test User");
        registerRequest.setRole(UserRole.STUDENT);

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                "/api/auth/register", registerRequest, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertNotNull(loginResponse.getBody().getToken());
        String token = loginResponse.getBody().getToken();

        ResponseEntity<String> unauthorizedHealth = restTemplate.getForEntity("/api/health", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedHealth.getStatusCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> authorizedHealth = restTemplate.exchange(
                "/api/health", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, authorizedHealth.getStatusCode());
    }
}
