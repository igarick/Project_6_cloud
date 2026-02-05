package org.file.cloud.validator;

import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.InvalidOrEmptyPathException;

@Slf4j
public class PathValidator {
    private static final String VALID_FOLDER_NAME_PATTERN = "^[^\\\\/:*?\"<>|]+$";
    private static final int MAX_PATH_LENGTH = 255;

    public static boolean isValidPathOrRoot(String path) {
        log.info("Start validation the path = {}", path);
        if (path == null || path.isBlank()) {
            log.info("Root directory: path = {}", path);
            return true;
        }
        return isPathValidInternal(path);
    }

    public static boolean isValidPath(String path) {
        log.info("Start validation the path = {}", path);
        if (path == null || path.isBlank()) {
            log.warn("Empty path");
            return false;
        }
        return isPathValidInternal(path);
    }

    private static boolean isPathValidInternal(String path) {
        if (path.startsWith("/") || path.contains("//")) {
            log.warn("Invalid path format");
            return false;
        }

        if (!arePathSegmentsValid(path)) {
            log.warn("Invalid or empty path segment");
            return false;
        }

        if (!isValidPathLength(path)) {
            log.warn("Path exceeds maximum length");
            return false;
        }
        log.info("Valid path = {}", path);
        return true;
    }

    private static boolean arePathSegmentsValid(String path) {
        String[] pathSegments = path.split("/");
        for (String segment : pathSegments) {
            log.info("Path segment - {}", segment);
            if (segment.isBlank() || !segment.matches(VALID_FOLDER_NAME_PATTERN)) {
                return false;
            }
        }
        log.info("All segments for path are valid: path = {}", path);
        return true;
    }

    private static boolean isValidPathLength(String path) {
        String s = path.replace("/", "");
        return s.length() < MAX_PATH_LENGTH;
    }

    public static void validateForMove(String from, String to) {
        if (from.endsWith("/") && !to.endsWith("/") ||
            (!from.endsWith("/") && to.endsWith("/"))) {
            log.warn("Source and target types do not match (from = {}, to = {})", from, to);
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        if (from.endsWith("/") && to.startsWith(from) && !to.equals(from)) {
            log.warn("Cannot move folder into its own subfolder: from = {}, to = {}", from, to);
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
    }
}
