package org.file.cloud.exception;

public class DuplicateUserException extends BaseException {
    public DuplicateUserException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public DuplicateUserException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
