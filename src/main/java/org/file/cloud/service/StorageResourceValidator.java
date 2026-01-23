package org.file.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceAlreadyExistsException;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageResourceValidator {
    private final MinioStorageService minioStorageService;
    private final PathService pathService;

    public void ensureResourceExists(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);
        if (resourcePath.endsWith("/")) {
            if (!minioStorageService.isFolderExists(fullPath)) {
                log.warn("Folder not found: path = {}", fullPath);
                throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
            }
            log.info("Folder exists: path = {}", fullPath);
            return;
        }

        if (minioStorageService.isFileExists(fullPath)) {
            log.info("File exists: path = {}", fullPath);
        } else {
            log.warn("File not found: path = {}", fullPath);
            throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
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
        log.info("Parent folder exists: path = {}", fullParentPath);

        if (minioStorageService.isFolderExists(fullPath)) {
            log.error("Folder already exists: path = {}", fullPath);
            throw new ResourceException(ErrorInfo.FOLDER_ALREADY_EXISTS);
        }
        log.info("Folder does not exist: path = {}", fullPath);
    }

    public void validateParentFolderExistence(String username, String resourcePath) {
        String parentFolderPath = pathService.extractParentFolderPath(resourcePath);
        String fullParentPath = pathService.getFullPath(username, parentFolderPath);

        if (!minioStorageService.isFolderExists(fullParentPath)) {
            log.warn("Parent folder does not exist: path = {}", fullParentPath);
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        log.info("Parent folder exists: path = {}", fullParentPath);
    }

    public void ensureFileDoesNotExist(String fullPath, String to) {
        if (minioStorageService.isFileExists(fullPath)) {
            log.info("File already exists: path = {}", fullPath);
            throw new ResourceAlreadyExistsException(ErrorInfo.RESOURCE_ALREADY_EXISTS, to);
        }
        log.info("File does not exist: path = {}", fullPath);
    }


//    public void ensureFileDoesNotExist(String username, String resourcePath) {
//        String fullPath = pathService.getFullPath(username, resourcePath);
//        if (minioStorageService.isFileExists(fullPath)) {
//            log.info("File already exists: path = {}", fullPath);
//            throw new ResourceAlreadyExistsException(ErrorInfo.RESOURCE_ALREADY_EXISTS, resourcePath);
//        }
//        log.info("File does not exist: path = {}", fullPath);
//    }

    public void ensureFolderDoesNotExist(String fullPath, String to) {
        if (minioStorageService.isFolderExists(fullPath)) {
            log.error("Folder already exists: path = {}", fullPath);
            throw new ResourceAlreadyExistsException(ErrorInfo.RESOURCE_ALREADY_EXISTS, to);
        }
        log.info("Folder does not exist: path = {}", fullPath);
    }

//    public void ensureFolderDoesNotExist(String username, String resourcePath) {
//        String fullPath = pathService.getFullPath(username, resourcePath);
//        if (minioStorageService.isFolderExists(fullPath)) {
//            log.error("Folder already exists: path = {}", fullPath);
//            throw new ResourceException(ErrorInfo.FOLDER_ALREADY_EXISTS);
//        }
//        log.info("Folder does not exist: path = {}", fullPath);
//    }

    public boolean isFolderExists(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);
        return minioStorageService.isFolderExists(fullPath);
    }













}
