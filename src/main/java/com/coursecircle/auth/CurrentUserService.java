package com.coursecircle.auth;

import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    /**
     * Stubbed user lookup; in production this should be replaced with real authentication
     * (JWT, OAuth, session cookies, etc.).
     */
    public CurrentUser getCurrentUser() {
        return new CurrentUser(1L, "student@example.edu", "Sample Student", null);
    }
}
