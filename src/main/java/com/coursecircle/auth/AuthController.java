package com.coursecircle.auth;

import com.coursecircle.dto.AuthResponse;
import com.coursecircle.dto.LoginRequest;
import com.coursecircle.dto.RegisterRequest;
import com.coursecircle.exception.ResourceNotFoundException;
import com.coursecircle.user.UserEntity;
import com.coursecircle.user.UserRepository;
import com.coursecircle.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse("Email already registered"));
        }
        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.STUDENT);
        UserEntity saved = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String token = jwtService.generateToken(userDetails, Map.of("role", saved.getRole().name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, Map.of("role", user.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
