package org.file.cloud.exception.folder;

import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

public class ResourceFileNotFoundException extends BaseException {
    public ResourceFileNotFoundException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public ResourceFileNotFoundException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}

