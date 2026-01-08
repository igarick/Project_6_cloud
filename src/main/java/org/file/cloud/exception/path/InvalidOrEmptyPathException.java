package org.file.cloud.exception.path;

import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

public class InvalidOrEmptyPathException extends BaseException {
    public InvalidOrEmptyPathException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public InvalidOrEmptyPathException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
