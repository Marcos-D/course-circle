package com.coursecircle.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    List<SessionEntity> findByUserIdAndEndedAtIsNull(Long userId);
}
