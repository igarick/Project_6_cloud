package org.file.cloud.validator;

import lombok.experimental.UtilityClass;
import org.file.cloud.dto.UserDto;
import org.file.cloud.exception.RequestValidationException;
import org.file.cloud.exception.ErrorInfo;
import org.springframework.util.StringUtils;

@UtilityClass
public class RequestValidator {
    private static final String NAME_PATTERN = "^[a-zA-Z0-9]{5,15}$";
    private static final String PASSWORD_PATTERN = "^\\S{5,15}$";

    public void validate(UserDto userDto) {
        String name = userDto.getUsername();
        String password = userDto.getPassword();
        String confirmPassword = userDto.getConfirmPassword();

        if (!StringUtils.hasText(name) ||
            !StringUtils.hasText(password) ||
            !StringUtils.hasText(confirmPassword)) {
            throw new RequestValidationException(ErrorInfo.EMPTY_FIELD_ERROR);
        }
        validateName(name);
        validatePassword(password);
        validatePasswordMatches(password, confirmPassword);
    }

    private void validateName(String name) {
        if (!name.matches(NAME_PATTERN)) {
            throw new RequestValidationException(ErrorInfo.INVALID_USERNAME);
        }
    }

    private void validatePassword(String password) {
        if (!password.matches(PASSWORD_PATTERN)) {
            throw new RequestValidationException(ErrorInfo.INVALID_PASSWORD);
        }
    }

    private void validatePasswordMatches(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new RequestValidationException(ErrorInfo.PASSWORD_MATCHES_ERROR);
        }
    }
}
