package org.file.cloud.exception;

public class ResourceStorageException extends BaseException {
    public ResourceStorageException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public ResourceStorageException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
