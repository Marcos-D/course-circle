package com.coursecircle.auth;

public class CurrentUser {

    private final Long id;
    private final String email;
    private final String displayName;
    private final Long schoolId;

    public CurrentUser(Long id, String email, String displayName, Long schoolId) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.schoolId = schoolId;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getSchoolId() {
        return schoolId;
    }
}
