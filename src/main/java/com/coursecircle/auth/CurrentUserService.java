package com.coursecircle.auth;

import com.coursecircle.user.UserEntity;
import com.coursecircle.user.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the current authenticated user from the security context so downstream services
 * can enforce ownership and attribution.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No authenticated user in context");
        }
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
        return new CurrentUser(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getSchool() != null ? user.getSchool().getId() : null,
                user.getRole() != null ? user.getRole().name() : null);
    }
}
