package com.coursecircle.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseFileRepository extends JpaRepository<CourseFileEntity, Long> {

    List<CourseFileEntity> findByCourseIdOrderByUploadedAtDesc(Long courseId);
}
