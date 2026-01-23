package org.file.cloud.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.builder.ResponseDtoBuilder;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioDirectoryService {
    private final MinioStorageService minioStorageService;
    private final PathService pathService;
    private final ResponseDtoBuilder responseDtoBuilder;
    private final UserRootFolderManager userRootFolderManager;
    private final UserService userService;

    public ResponseResourceDto createFolder(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);
        minioStorageService.createFolder(fullPath);
        log.info("Created folder: path = {}", resourcePath);

        return responseDtoBuilder.buildFolderDto(resourcePath);
    }

    public List<ResponseResourceDto> showFolderContent(String username, String path) {
        Long userId = userService.getUserId(username);
        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        String fullPath = userRootFolder + path;
        log.info("fullPath = " + fullPath);
        Iterable<Result<Item>> objects = minioStorageService.getObjects(fullPath, false);
        List<ResponseResourceDto> result = new ArrayList<>();
        try {
            for (Result<Item> object : objects) {
                String objectName = object.get().objectName();
                if (fullPath.equals(objectName)) {
                    log.info("Skip directory folder: objectName = {}", objectName);
                    continue;
                }
                String resourcePath = objectName.substring(userRootFolder.length());
                log.info("resourcePath = " + resourcePath);
                result.add(responseDtoBuilder.buildResourceDto(username, resourcePath));
            }
        } catch (Exception e) {
            log.error("Failed to get resource: query = {}, error: {}", path, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
        }
        return result;
    }



}
