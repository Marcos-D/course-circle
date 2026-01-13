package com.coursecircle.session;

import com.coursecircle.auth.CurrentUser;
import com.coursecircle.config.AppProperties;
import com.coursecircle.course.CourseEntity;
import com.coursecircle.course.CourseRepository;
import com.coursecircle.dto.SessionResponse;
import com.coursecircle.dto.StartSessionRequest;
import com.coursecircle.exception.ResourceNotFoundException;
import com.coursecircle.exception.SessionAlreadyActiveException;
import com.coursecircle.user.UserEntity;
import com.coursecircle.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public SessionService(SessionRepository sessionRepository,
                          CourseRepository courseRepository,
                          UserRepository userRepository,
                          AppProperties appProperties) {
        this.sessionRepository = sessionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    /**
     * Business rule: each user may have at most N active sessions (MVP: 1), enforced before creating a session.
     */
    @Transactional
    public SessionResponse startSession(CurrentUser currentUser, StartSessionRequest request) {
        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUser.getId()));
        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseId()));

        List<SessionEntity> activeSessions = sessionRepository.findByUserIdAndEndedAtIsNull(currentUser.getId());
        if (activeSessions.size() >= appProperties.getSessions().getMaxActivePerUser()) {
            throw new SessionAlreadyActiveException("User already has an active study session.");
        }

        SessionEntity entity = new SessionEntity();
        entity.setUser(user);
        entity.setCourse(course);
        entity.setStartedAt(Instant.now());

        SessionEntity saved = sessionRepository.save(entity);
        log.info("Started session {} for user {} course {}", saved.getId(), currentUser.getId(), course.getId());

        return toResponse(saved);
    }

    @Transactional
    public SessionResponse endSession(CurrentUser currentUser, Long sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        // TODO: enforce that the current user owns this session once authentication is implemented.
        session.setEndedAt(Instant.now());
        SessionEntity saved = sessionRepository.save(session);
        log.info("Ended session {} for user {}", sessionId, currentUser.getId());
        return toResponse(saved);
    }

    @Transactional
    public int endAllActiveSessions(CurrentUser currentUser) {
        List<SessionEntity> activeSessions = sessionRepository.findByUserIdAndEndedAtIsNull(currentUser.getId());
        if (activeSessions.isEmpty()) {
            return 0;
        }
        Instant endedAt = Instant.now();
        activeSessions.forEach(session -> session.setEndedAt(endedAt));
        sessionRepository.saveAll(activeSessions);
        log.info("Ended {} active sessions for user {}", activeSessions.size(), currentUser.getId());
        return activeSessions.size();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions() {
        return sessionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SessionResponse getActiveSession(CurrentUser currentUser) {
        List<SessionEntity> activeSessions = sessionRepository.findByUserIdAndEndedAtIsNull(currentUser.getId());
        if (activeSessions.isEmpty()) {
            throw new ResourceNotFoundException("No active session for user: " + currentUser.getId());
        }
        return toResponse(activeSessions.get(0));
    }

    private SessionResponse toResponse(SessionEntity entity) {
        SessionResponse response = new SessionResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        response.setCourseId(entity.getCourse() != null ? entity.getCourse().getId() : null);
        response.setStartedAt(entity.getStartedAt());
        response.setEndedAt(entity.getEndedAt());
        return response;
    }
}
