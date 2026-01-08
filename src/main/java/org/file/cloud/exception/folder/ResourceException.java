package org.file.cloud.exception.folder;

import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

public class ResourceException extends BaseException {
    public ResourceException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public ResourceException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
