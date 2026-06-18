package com.goalkeeperdash.common.error;

import org.springframework.http.HttpStatus;

/**
 * Canonical error codes returned in the standard error envelope ({@code error.code}).
 * Each maps to an HTTP status. Keep codes stable; clients may switch on them.
 */
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    NATION_LOCKED(HttpStatus.CONFLICT),
    CONFLICT(HttpStatus.CONFLICT),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    INVALID_SESSION(HttpStatus.BAD_REQUEST),
    SESSION_CONSUMED(HttpStatus.CONFLICT),
    SESSION_EXPIRED(HttpStatus.CONFLICT),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
    NO_ACTIVE_SEASON(HttpStatus.CONFLICT),
    NATION_NOT_CHOSEN(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
