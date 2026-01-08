package org.file.cloud.validator;

import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;

@Slf4j
public class PathValidator {
    private static final String VALID_FOLDER_NAME_PATTERN = "^[^\\\\/:*?\"<>|]+$";
    private static final int MAX_PATH_LENGTH = 255;

    public static boolean isValid(String path) {
        if (path == null || path.isBlank()) {
            log.warn("Empty path to the new folder");
            return false;
        }

        if (path.startsWith("/") || path.contains("//")) {
            log.warn("Invalid path formate to the new folder");
            return false;
        }

        if (!isPathSegmentsValid(path)) {
            log.warn("Invalid or empty path segment to the new folder");
            return false;
        }

        if (!isValidPathLength(path)) {
            log.warn("Path exceeds maximum length");
            return false;
        }
        return true;
    }

    private static boolean isPathSegmentsValid(String path) {
        String[] pathSegments = path.split("/");
        for (String segment : pathSegments) {
            if (segment.isBlank() || !segment.matches(VALID_FOLDER_NAME_PATTERN)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPathLength(String path) {
        String s = path.replace("/", "");
        return s.length() < MAX_PATH_LENGTH;
    }
}
