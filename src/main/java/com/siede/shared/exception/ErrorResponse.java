package com.siede.shared.exception;

import java.util.List;

public record ErrorResponse(int status, String error, String message, List<FieldError> errors) {

    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null);
    }

    public static ErrorResponse of(int status, String error, String message, List<FieldError> errors) {
        return new ErrorResponse(status, error, message, errors);
    }
}