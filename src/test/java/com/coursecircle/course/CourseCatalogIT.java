package com.coursecircle.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.coursecircle.CourseCircleApplication.class)
@Testcontainers
@ActiveProfiles("ci")
public class CourseCatalogIT {

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

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private String bearerToken;

    @BeforeEach
    void setup() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "Docker not available, skipping ITs");

        String email = "catalog-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        restTemplate.postForEntity(url("/api/auth/register"),
                Map.of("email", email, "password", password, "displayName", "Catalog User"),
                Map.class);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                url("/api/auth/login"), Map.of("email", email, "password", password), Map.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        bearerToken = loginResponse.getBody() != null ? (String) loginResponse.getBody().get("token") : null;
        assertNotNull(bearerToken);

        List<Map<String, String>> samples = Arrays.asList(
                Map.of("code", "CSE8A", "name", "Intro to Programming I", "term", "Fall 2025"),
                Map.of("code", "CSE100", "name", "Advanced Data Structures", "term", "Spring 2026"),
                Map.of("code", "CSE120", "name", "Operating Systems", "term", "Fall 2025")
        );

        for (Map<String, String> course : samples) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken);
            restTemplate.exchange(url("/api/courses"), HttpMethod.POST, new HttpEntity<>(course, headers), Map.class);
        }
    }

    @Test
    void listsSeededCourses() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        ResponseEntity<Map[]> response = restTemplate.exchange(
                url("/api/courses"), HttpMethod.GET, new HttpEntity<>(headers), Map[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Map> courses = Arrays.asList(response.getBody());

        assertTrue(courses.stream().anyMatch(c -> "CSE8A".equals(c.get("code"))));
        assertTrue(courses.stream().anyMatch(c -> "CSE100".equals(c.get("code"))));
        assertTrue(courses.stream().anyMatch(c -> "CSE120".equals(c.get("code"))));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
