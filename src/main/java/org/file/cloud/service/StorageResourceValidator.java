package org.file.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceAlreadyExistsException;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.exception.folder.ResourceFileNotFoundException;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageResourceValidator {
    private final MinioStorageService minioStorageService;
    private final PathService pathService;

//    private static final String RESOURCE_ALREADY_EXISTS_ERROR_MESSAGE = "Resource lying on the path = %s already exists";

    public void validateResourceExistence(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);
        if (resourcePath.endsWith("/")) {
            if (!minioStorageService.isFolderExists(fullPath)) {
                log.warn("Folder not found: path = {}", fullPath);
                throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
            }
            log.info("Folder exists: path = {}", fullPath);
        } else {
            if (minioStorageService.isFileExists(fullPath)) {
                log.info("File exists: path = {}", fullPath);
            }
        }
    }

    public void validateFolderExistence(String username, String resourcePath) {
        String parentFolderPath = pathService.extractParentFolderPath(resourcePath);

        String fullParentPath = pathService.getFullPath(username, parentFolderPath);
        String fullPath = pathService.getFullPath(username, resourcePath);

        if (!minioStorageService.isFolderExists(fullParentPath)) {
            log.warn("Parent folder does not exist: path = {}", fullParentPath);
            throw new ResourceException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
        }
        log.info("Parent folder exist: path = {}", fullParentPath);

        if (minioStorageService.isFolderExists(fullPath)) {
            log.warn("Folder already exists: path = {}", fullPath);
            throw new ResourceException(ErrorInfo.FOLDER_ALREADY_EXISTS);
        }
        log.info("Folder does not exist: path = {}", fullPath);
    }

    public void validateParentFolderExistence(String username, String resourcePath) {
        String parentFolderPath = pathService.extractParentFolderPath(resourcePath);
        String fullParentPath = pathService.getFullPath(username, parentFolderPath);

        if (!minioStorageService.isFolderExists(fullParentPath)) {
            log.warn("Invalid or empty path to the new folder: path = {}", fullParentPath);
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        log.info("Parent folder exist: path = {}", fullParentPath);
    }

    public void validateFileExistence(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);
        try {
            minioStorageService.isFileExists(fullPath);
            log.info("File already exists: path = {}", fullPath);

//            String errorMessage = String.format(RESOURCE_ALREADY_EXISTS_ERROR_MESSAGE, fullPath);
            String errorMessage = String.format(ErrorInfo.RESOURCE_ALREADY_EXISTS.getErrorMessage(), fullPath);
            throw new ResourceAlreadyExistsException(errorMessage, ErrorInfo.RESOURCE_ALREADY_EXISTS.getStatusCode());

        } catch (ResourceFileNotFoundException e) {
            log.info("File does not exist: path = {}", fullPath);
        }
    }

}
