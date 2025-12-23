package org.file.cloud.exception;

import org.file.cloud.dto.ErrorMessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorMessageDto> handleBaseErrors(BaseException e) {
        return ResponseEntity
                .status(e.getErrorInfo().getStatusCode())
                .body(new ErrorMessageDto(e.getMessage()));
    }
}
