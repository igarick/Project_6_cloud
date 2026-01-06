package org.file.cloud.exception.folder;

import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

public class FolderException extends BaseException {
    public FolderException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public FolderException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
