package org.file.cloud.exception;

public class RequestValidationException extends BaseException {
    public RequestValidationException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public RequestValidationException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
