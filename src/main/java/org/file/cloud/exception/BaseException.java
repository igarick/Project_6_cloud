package org.file.cloud.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ErrorInfo errorInfo;

    public BaseException(ErrorInfo errorInfo) {
        super(errorInfo.getErrorMessage());
        this.errorInfo = errorInfo;
    }

    public BaseException(ErrorInfo errorInfo, Throwable cause) {
        super(cause);
        this.errorInfo = errorInfo;
    }
}
