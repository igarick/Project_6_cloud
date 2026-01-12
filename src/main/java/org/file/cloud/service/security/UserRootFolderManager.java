package org.file.cloud.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.UsernameDto;
import org.file.cloud.service.UserService;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRootFolderManager {
    private final MinioStorageService minioStorageService;
    private final UserService userService;

    private static final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    public void createUserRootFolder(UsernameDto usernameDto) {
        Long id = userService.getUserId(usernameDto.getUsername());
        String userRootFolder = String.format(USER_ROOT_FOLDER_TEMPLATE, id);

        minioStorageService.createFolder(userRootFolder);
        log.info("Created root folder - {}", userRootFolder);
    }
}
