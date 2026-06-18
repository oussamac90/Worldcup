package com.goalkeeperdash.common.error;

/**
 * Application-level exception carrying a stable {@link ErrorCode}. Thrown from
 * service/controller layers and translated into the standard error envelope by
 * {@link GlobalExceptionHandler}.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode code;

    public ApiException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public static ApiException notFound(String message) {
        return new ApiException(ErrorCode.NOT_FOUND, message);
    }

    public static ApiException conflict(ErrorCode code, String message) {
        return new ApiException(code, message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(ErrorCode.UNAUTHORIZED, message);
    }
}
