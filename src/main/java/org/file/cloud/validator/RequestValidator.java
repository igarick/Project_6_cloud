package org.file.cloud.validator;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.UserSignInDto;
import org.file.cloud.dto.UserSignUpDto;
import org.file.cloud.exception.RequestValidationException;
import org.file.cloud.exception.ErrorInfo;
import org.springframework.util.StringUtils;

@Slf4j
@UtilityClass
public class RequestValidator {
    private static final String NAME_PATTERN = "^[a-zA-Z0-9]{5,15}$";
    private static final String PASSWORD_PATTERN = "^\\S{5,15}$";

    public void validateSignUpInput(UserSignUpDto userSignUpDto) {
        String name = userSignUpDto.getUsername();
        String password = userSignUpDto.getPassword();
        String confirmPassword = userSignUpDto.getConfirmPassword();

        if (!StringUtils.hasText(name) ||
            !StringUtils.hasText(password) ||
            !StringUtils.hasText(confirmPassword)) {
            log.warn("Empty name or password field");
            throw new RequestValidationException(ErrorInfo.EMPTY_FIELD_ERROR);
        }
        validateName(name);
        validatePassword(password);
        validatePasswordMatches(password, confirmPassword);
    }

    public void validateSignInInput(UserSignInDto userSignInDto) {
        String name = userSignInDto.getUsername();
        String password = userSignInDto.getPassword();

        if (!StringUtils.hasText(name) ||
            !StringUtils.hasText(password)) {
            log.warn("Empty name or password field");
            throw new RequestValidationException(ErrorInfo.EMPTY_FIELD_ERROR);
        }
        validateName(name);
        validatePassword(password);
    }

    private void validateName(String name) {
        if (!name.matches(NAME_PATTERN)) {
            log.warn("Incorrect name");
            throw new RequestValidationException(ErrorInfo.INVALID_USERNAME);
        }
    }

    private void validatePassword(String password) {
        if (!password.matches(PASSWORD_PATTERN)) {
            log.warn("Incorrect password");
            throw new RequestValidationException(ErrorInfo.INVALID_PASSWORD);
        }
    }

    private void validatePasswordMatches(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            log.warn("Passwords don't match");
            throw new RequestValidationException(ErrorInfo.PASSWORD_MATCHES_ERROR);
        }
    }
}
