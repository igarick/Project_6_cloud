package org.file.cloud.exception;

public class DaoException extends BaseException {
    public DaoException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public DaoException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
