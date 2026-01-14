package org.file.cloud.builder;

import io.minio.GetObjectAttributesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.service.PathService;
import org.file.cloud.service.minio.MinioStorageService;
import org.file.cloud.util.resource.ResourceType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseDtoBuilder {
    private final PathService pathService;
    private final MinioStorageService minioStorageService;

    public ResourceResponseDto buildResourceDto(String username, String resourcePath) {
        String parentFolderPath = pathService.extractParentFolderPath(resourcePath);
        String fullPath = pathService.getFullPath(username, resourcePath);

        String name;
        Long size = null;
        String type;
        if (!resourcePath.endsWith("/")) {
            name = getFileName(parentFolderPath, resourcePath);
            size = getFileSize(fullPath);
            type = ResourceType.FILE.name();
        } else {
            name = getFolderName(parentFolderPath, resourcePath);
            type = ResourceType.DIRECTORY.name();
        }
        log.info("Collected resource DTO: path - {}, name - {}, size - {}, type - {}", parentFolderPath, name, size, type);
        return ResourceResponseDto.builder()
                .path(parentFolderPath)
                .name(name)
                .size(size)
                .type(type)
                .build();
    }

    private String getFileName(String parentFolderPath, String resourcePath) {
        String name;
        if (parentFolderPath.isEmpty()) {
            name = resourcePath;
        } else {
            int length = parentFolderPath.length();
            name = resourcePath.substring(length);
        }
        log.info("File name - {}", name);
        return name;
    }

    private String getFolderName(String parentFolderPath, String resourcePath) {
        String name;
        if (parentFolderPath.isEmpty()) {
            name = resourcePath.substring(0, resourcePath.length() - 1);
        } else {
            int length = parentFolderPath.length();
            name = resourcePath.substring(length, resourcePath.length() - 1);
        }
        log.info("Folder name - {}", name);
        return name;
    }

    private Long getFileSize(String fullPath) {
        GetObjectAttributesResponse fileAttributes = minioStorageService.getFileAttributes(fullPath);
        Long objectSize = fileAttributes.result().objectSize();
        if (objectSize == null) {
            return 0L;
        }
        return objectSize;
    }

    public ResourceResponseDto buildFolderDto(String resourcePath) {
        String parentFolderPath = pathService.extractParentFolderPath(resourcePath);
        log.info("Path to JSON - {}", parentFolderPath);

        String name = getFolderName(parentFolderPath, resourcePath);
        log.info("Collected folder DTO: path - {}, name - {}, type - {}", parentFolderPath, name, ResourceType.DIRECTORY.name());
        return ResourceResponseDto.builder()
                .path(parentFolderPath)
                .name(name)
                .type(ResourceType.DIRECTORY.name())
                .build();
    }
}
