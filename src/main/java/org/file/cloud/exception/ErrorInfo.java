package org.file.cloud.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static jakarta.servlet.http.HttpServletResponse.*;

@AllArgsConstructor
@Getter
public enum ErrorInfo {
    EMPTY_FIELD_ERROR("Field must be not empty", SC_BAD_REQUEST),
    INVALID_USERNAME("Username must contain 5-15 letters or digits", SC_BAD_REQUEST),
    INVALID_PASSWORD("Password must contain 5-15 characters except whitespace", SC_BAD_REQUEST),
    PASSWORD_MATCHES_ERROR("Passwords must match", SC_BAD_REQUEST),



    end("Name must be between 3 and 15 characters", SC_BAD_REQUEST);

    private final String errorMessage;
    private final int statusCode;
}
