package com.coursecircle.exception;

public class SessionAlreadyActiveException extends BusinessException {

    public SessionAlreadyActiveException(String message) {
        super(message, "SESSION_ALREADY_ACTIVE");
    }
}
