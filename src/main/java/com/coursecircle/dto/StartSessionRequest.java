package com.coursecircle.dto;

import jakarta.validation.constraints.NotNull;

public class StartSessionRequest {

    @NotNull
    private Long courseId;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
