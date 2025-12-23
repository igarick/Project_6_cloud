package org.file.cloud.validator;

import lombok.experimental.UtilityClass;
import org.file.cloud.dto.UserDto;
import org.file.cloud.exception.BadRequestException;
import org.file.cloud.exception.ErrorInfo;
import org.springframework.util.StringUtils;

@UtilityClass
public class RequestValidator {
    public static void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException(ErrorInfo.EMPTY_USERNAME);
        }

        if (!name.matches("^[a-zA-Z0-9]{3,15}$")) {
            throw new BadRequestException(ErrorInfo.INVALID_USERNAME);
        }
    }
}
