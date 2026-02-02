package org.file.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRootFolderManager {
    private final MinioStorageService minioStorageService;

    private static final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    public void createUserRootFolder(Long id) {
        String userRootFolder = String.format(USER_ROOT_FOLDER_TEMPLATE, id);

        minioStorageService.createFolder(userRootFolder);
        log.info("Created root folder: user id = {}, path = {}", id, userRootFolder);
    }

    public String getUserRootFolder(Long userId) {
        return String.format(USER_ROOT_FOLDER_TEMPLATE, userId);
    }
}
