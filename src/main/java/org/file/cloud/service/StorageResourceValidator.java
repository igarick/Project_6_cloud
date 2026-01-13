package org.file.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageResourceValidator {
    private final MinioStorageService minioStorageService;
    private final PathfinderService pathfinderService;
    private final UserRootFolderManager userRootFolderManager;

    public void validateResourceExistence(String username, String resourcePath) {
        String fullPath = pathfinderService.getFullPath(username, resourcePath);
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
//        String userRootFolder = userRootFolderManager.getUserRootFolder(username);
        String parentFolderPath = pathfinderService.extractParentFolderPath(resourcePath);

//        String fullParentPath = userRootFolder + parentFolderPath;
//        String fullPath = userRootFolder + resourcePath;

        String fullParentPath = pathfinderService.getFullPath(username, parentFolderPath);
        String fullPath = pathfinderService.getFullPath(username, resourcePath);


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

}
