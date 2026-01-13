package org.file.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathfinderService {
    private final UserService userService;
    private final UserRootFolderManager userRootFolderManager;

    private static final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";


    public String getFullPath(String username, String resourcePath) {
        String userRootFolder = userRootFolderManager.getUserRootFolder(username);
        return userRootFolder + resourcePath;
    }

    public String extractParentFolderPath(String resourcePath) {
        String parentFolder;
        int lastSlash = resourcePath.lastIndexOf("/", resourcePath.length() - 2);
        if (lastSlash == -1) {
            parentFolder = "";
        } else {
            parentFolder = resourcePath.substring(0, lastSlash + 1);
        }
        log.info("Extracted parent folder path: {}", parentFolder);
        return parentFolder;
    }



}
