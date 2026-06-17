package com.goalkeeperdash.common.error;

import java.util.List;

/**
 * Standard error envelope:
 * <pre>{ "error": { "code": "NATION_LOCKED", "message": "…", "traceId": "…", "fields": [...] } }</pre>
 */
public record ApiError(Body error) {

    public record Body(String code, String message, String traceId, List<FieldError> fields) {}

    public record FieldError(String field, String message) {}

    public static ApiError of(String code, String message, String traceId, List<FieldError> fields) {
        return new ApiError(new Body(code, message, traceId, fields == null || fields.isEmpty() ? null : fields));
    }
}
