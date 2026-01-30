package org.file.cloud.exception.minio;

import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

public class ResourceStorageException extends BaseException {
    public ResourceStorageException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public ResourceStorageException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
