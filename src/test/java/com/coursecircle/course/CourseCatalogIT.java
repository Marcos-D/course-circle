package com.coursecircle.course;

import com.coursecircle.dto.AuthResponse;
import com.coursecircle.dto.CourseResponse;
import com.coursecircle.dto.LoginRequest;
import com.coursecircle.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("ci")
class CourseCatalogIT {

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

    @Autowired
    private CourseRepository courseRepository;

    private String bearerToken;

    @BeforeEach
    void setup() {
        String email = "catalog-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setDisplayName("Catalog User");

        restTemplate.postForEntity(url("/api/auth/register"), registerRequest, AuthResponse.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                url("/api/auth/login"), loginRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        bearerToken = loginResponse.getBody() != null ? loginResponse.getBody().getToken() : null;
        assertNotNull(bearerToken);

        courseRepository.deleteAll();
        CourseEntity c1 = new CourseEntity();
        c1.setCode("CSE8A");
        c1.setName("Intro to Programming I");
        c1.setTerm("Fall 2025");

        CourseEntity c2 = new CourseEntity();
        c2.setCode("CSE100");
        c2.setName("Advanced Data Structures");
        c2.setTerm("Spring 2026");

        CourseEntity c3 = new CourseEntity();
        c3.setCode("CSE120");
        c3.setName("Operating Systems");
        c3.setTerm("Fall 2025");

        courseRepository.saveAll(Arrays.asList(c1, c2, c3));
    }

    @Test
    void listsSeededCourses() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        ResponseEntity<CourseResponse[]> response = restTemplate.exchange(
                url("/api/courses"), HttpMethod.GET, new HttpEntity<>(headers), CourseResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<CourseResponse> courses = Arrays.asList(response.getBody());

        assertTrue(courses.stream().anyMatch(c -> "CSE8A".equals(c.getCode())));
        assertTrue(courses.stream().anyMatch(c -> "CSE100".equals(c.getCode())));
        assertTrue(courses.stream().anyMatch(c -> "CSE120".equals(c.getCode())));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
