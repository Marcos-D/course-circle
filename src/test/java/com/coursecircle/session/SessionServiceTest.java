package com.coursecircle.session;

import com.coursecircle.auth.CurrentUser;
import com.coursecircle.config.AppProperties;
import com.coursecircle.course.CourseEntity;
import com.coursecircle.course.CourseRepository;
import com.coursecircle.dto.StartSessionRequest;
import com.coursecircle.exception.SessionAlreadyActiveException;
import com.coursecircle.user.UserEntity;
import com.coursecircle.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Ensures the one-active-session business rule is enforced at the service layer, independent of web/DB adapters.
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    private AppProperties appProperties;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setup() {
        appProperties = new AppProperties();
        appProperties.getSessions().setMaxActivePerUser(1);
        sessionService = new SessionService(sessionRepository, courseRepository, userRepository, appProperties);
    }

    @Test
    void startSession_throwsWhenActiveSessionExists() {
        CurrentUser currentUser = new CurrentUser(1L, "a@b.com", "Test User", null);
        StartSessionRequest request = new StartSessionRequest();
        request.setCourseId(10L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new UserEntity()));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(new CourseEntity()));
        when(sessionRepository.findByUserIdAndEndedAtIsNull(currentUser.getId()))
                .thenReturn(Collections.singletonList(new SessionEntity()));

        assertThrows(SessionAlreadyActiveException.class, () -> sessionService.startSession(currentUser, request));
    }
}
