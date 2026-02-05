package org.file.cloud.exception;

public class ResourceException extends BaseException {
    public ResourceException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public ResourceException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
