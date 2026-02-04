package org.file.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.repository.UserRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathService {
    private final UserRootFolderManager userRootFolderManager;
    private final UserRepository userRepository;
//    private final UserService userService;

    public String getFullPath(String username, String resourcePath) {
//        Long userId = userService.getUserId(username);
        Long userId = userRepository.findIdByUsernameIgnoreCase(username);
        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
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
        log.info("Parent folder path = {}", parentFolder);
        return parentFolder;
    }


}
