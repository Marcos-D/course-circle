package com.coursecircle.session;

import com.coursecircle.auth.CurrentUser;
import com.coursecircle.auth.CurrentUserService;
import com.coursecircle.dto.SessionResponse;
import com.coursecircle.dto.StartSessionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
@Validated
public class SessionController {

    private final SessionService sessionService;
    private final CurrentUserService currentUserService;

    public SessionController(SessionService sessionService, CurrentUserService currentUserService) {
        this.sessionService = sessionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> startSession(@Valid @RequestBody StartSessionRequest request) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        SessionResponse response = sessionService.startSession(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> listSessions() {
        return ResponseEntity.ok(sessionService.listSessions());
    }

    @GetMapping("/active")
    public ResponseEntity<SessionResponse> getActiveSession() {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        try {
            SessionResponse response = sessionService.getActiveSession(currentUser);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.noContent().build();
        }
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<SessionResponse> endSession(@PathVariable("id") Long sessionId) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        SessionResponse response = sessionService.endSession(currentUser, sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end-all")
    public ResponseEntity<Void> endAllSessions() {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        sessionService.endAllActiveSessions(currentUser);
        return ResponseEntity.noContent().build();
    }
}
