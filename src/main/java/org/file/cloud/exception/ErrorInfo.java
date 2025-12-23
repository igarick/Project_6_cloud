package org.file.cloud.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static jakarta.servlet.http.HttpServletResponse.*;

@AllArgsConstructor
@Getter
public enum ErrorInfo {
    INVALID_USERNAME("Username must contain 3-15 letters or digits", SC_BAD_REQUEST),
    EMPTY_USERNAME("Name must be not empty", SC_BAD_REQUEST),


    end("Name must be between 3 and 15 characters", SC_BAD_REQUEST);

    private final String errorMessage;
    private final int statusCode;
}
