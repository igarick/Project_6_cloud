package org.file.cloud.exception.path;

import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

public class InvalidOrEmptyPathToNewFolderException extends BaseException {
    public InvalidOrEmptyPathToNewFolderException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public InvalidOrEmptyPathToNewFolderException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
