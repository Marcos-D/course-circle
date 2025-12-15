package com.coursecircle.auth;

import com.coursecircle.dto.AuthResponse;
import com.coursecircle.dto.LoginRequest;
import com.coursecircle.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("ci")
class AuthFlowIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void dataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void protectedEndpointRequiresJwtAndThenSucceedsWithToken() {
        String healthUrl = url("/api/health");

        ResponseEntity<String> unauthorized = restTemplate.getForEntity(healthUrl, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getStatusCode());

        String email = "user-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setDisplayName("Test User");

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                url("/api/auth/register"), registerRequest, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getBody());
        assertNotNull(registerResponse.getBody().getToken());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                url("/api/auth/login"), loginRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        String token = loginResponse.getBody().getToken();
        assertNotNull(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> authorized = restTemplate.exchange(
                healthUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, authorized.getStatusCode());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
