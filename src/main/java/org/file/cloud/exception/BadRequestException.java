package org.file.cloud.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public BadRequestException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
