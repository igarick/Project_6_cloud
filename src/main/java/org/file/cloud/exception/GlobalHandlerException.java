package org.file.cloud.exception;

import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.ErrorMessageDto;
import org.file.cloud.exception.folder.ResourceAlreadyExistsException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorMessageDto> handleBaseErrors(BaseException e) {
        return ResponseEntity
                .status(e.getErrorInfo().getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessageDto(e.getErrorInfo().getErrorMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorMessageDto> handleAuthenticationErrors(Exception e) {
        log.error("User authentication error", e);
        return ResponseEntity
                .status(401)
                .body(new ErrorMessageDto("Invalid username or password"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageDto> handleUnexpectedErrors(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
                .status(500)
                .body(new ErrorMessageDto("Internal server error"));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorMessageDto> handleAlreadyExistsError(ResourceAlreadyExistsException e) {
        String errorMessage = String.format(e.getErrorInfo().getErrorMessage(), e.getPath());
        return ResponseEntity
                .status(e.getErrorInfo().getStatusCode())
                .body(new ErrorMessageDto(errorMessage));
    }

}
