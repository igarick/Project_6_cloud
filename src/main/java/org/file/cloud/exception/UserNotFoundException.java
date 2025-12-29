package org.file.cloud.exception;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public UserNotFoundException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
