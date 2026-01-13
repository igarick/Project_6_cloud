package org.file.cloud.service;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.service.minio.MinioStorageService;
import org.file.cloud.util.resource.ResourceType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioDirectoryService {
    private final MinioStorageService minioStorageService;
    private final PathfinderService pathfinderService;
    private final UserRootFolderManager userRootFolderManager;

    public ResourceResponseDto createFolder(String username, String resourcePath) {
        String fullPath = pathfinderService.getFullPath(username, resourcePath);

        minioStorageService.createFolder(fullPath);
        log.info("Created folder: path = {}", resourcePath);

        return buildInfoToResponse(resourcePath);
    }

    public ResourceResponseDto buildInfoToResponse(String resourcePath) {
        String parentFolderPath = pathfinderService.extractParentFolderPath(resourcePath);
        log.info("Path to JSON - {}", parentFolderPath);

        String name;
        if (parentFolderPath.isEmpty()) {
            name = resourcePath.substring(0, resourcePath.length() - 1);
        } else {
            int length = parentFolderPath.length();
            name = resourcePath.substring(length, resourcePath.length() - 1);
        }
        log.info("Collected DTO: path - {}, name - {}, type - {}", parentFolderPath, name, ResourceType.DIRECTORY.name());
        return ResourceResponseDto.builder()
                .path(parentFolderPath)
                .name(name)
                .type(ResourceType.DIRECTORY.name())
                .build();
    }






}
