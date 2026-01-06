package org.file.cloud.validator;

import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathToNewFolderException;

public class PathValidator {
    private final String FOLDER_NAME_PATTERN = "^[A-Za-z0-9._ /+-]+$";

    public void validate(String path) {
        if (path == null || path.isBlank() || !path.endsWith("/")) {
            throw new InvalidOrEmptyPathToNewFolderException(ErrorInfo.INVALID_OR_EMPTY_PATH);
        }
        if (path.matches(FOLDER_NAME_PATTERN)) {

        }
    }
}
