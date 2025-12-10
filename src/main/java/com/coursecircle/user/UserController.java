package com.coursecircle.user;

import com.coursecircle.dto.CreateUserRequest;
import com.coursecircle.dto.UserResponse;
import com.coursecircle.exception.ResourceNotFoundException;
import com.coursecircle.school.SchoolEntity;
import com.coursecircle.school.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, SchoolRepository schoolRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setEmail(request.getEmail());
        entity.setDisplayName(request.getDisplayName());
        entity.setPasswordHash(passwordEncoder.encode("temp-" + UUID.randomUUID()));

        if (request.getSchoolId() != null) {
            SchoolEntity school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School not found: " + request.getSchoolId()));
            entity.setSchool(school);
        }

        UserEntity saved = userRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    private UserResponse toResponse(UserEntity entity) {
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setEmail(entity.getEmail());
        response.setDisplayName(entity.getDisplayName());
        response.setSchoolId(entity.getSchool() != null ? entity.getSchool().getId() : null);
        return response;
    }
}
