package org.file.cloud.exception;

public class InvalidOrEmptyPathException extends BaseException {
    public InvalidOrEmptyPathException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public InvalidOrEmptyPathException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
