package org.file.cloud.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.exception.DaoException;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.util.resource.ResourceType;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioResourceService {
    private final UserRepository userRepository;
    private final MinioClient minioClient;

    private final String MAIN_BUCKET = "user-files";
    private final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    public void validateResourceExists(String username, String resourcePath) throws Exception {
        String userRootFolder = getUserRootFolder(username);
        String fullPath = userRootFolder + resourcePath;
        if (resourcePath.endsWith("/")) {
            if (!isFolderExists(fullPath)) {
                log.warn("Resource - {} not found", fullPath);
                throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
            }
            log.info("Resource - {} exists", fullPath);
        } else {
            getResourceAttributes(username, resourcePath);
            log.info("Resource - {} exists", fullPath);
        }
    }

    public ResourceResponseDto buildDtoToResponse(String username, String resourcePath) throws Exception {
        String parentFolderPath = getParentFolderPath(resourcePath);
        String name;
        Long size = null;
        String type;
        if (!resourcePath.endsWith("/")) {
            name = getFileName(parentFolderPath, resourcePath);
            size = getFileSize(username, resourcePath);
            type = ResourceType.FILE.name();
        } else {
            name = getFolderName(parentFolderPath, resourcePath);
            type = ResourceType.DIRECTORY.name();
        }
        log.info("Collected DTO: path - {}, name - {}, size - {}, type - {}", parentFolderPath, name, size, type);

        return ResourceResponseDto.builder()
                .path(parentFolderPath)
                .name(name)
                .size(size)
                .type(type)
                .build();
    }

    private String getParentFolderPath(String resourcePath) {
        String parentFolder;
        int lastSlash = resourcePath.lastIndexOf("/", resourcePath.length() - 2);
        if (lastSlash == -1) {
            parentFolder = "";
        } else {
            parentFolder = resourcePath.substring(0, lastSlash + 1);
        }
        log.info("Parent folder path - {}", parentFolder);
        return parentFolder;
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

    public String getFolderName(String parentFolderPath, String resourcePath) {
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

    private Long getFileSize(String username, String resourcePath) throws Exception {
        GetObjectAttributesResponse resourceAttributes = getResourceAttributes(username, resourcePath);
        Long objectSize = resourceAttributes.result().objectSize();
        log.info("File size = {}", objectSize);
        return Objects.requireNonNullElse(objectSize, 0L);
    }

    public GetObjectAttributesResponse getResourceAttributes(String username, String resourcePath) throws Exception {
        String userRootFolder = getUserRootFolder(username);
        String fullPath = userRootFolder + resourcePath;

        GetObjectAttributesResponse response = null;
        try {
            response = minioClient.getObjectAttributes(
                    GetObjectAttributesArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .object(fullPath)
                            .objectAttributes(
                                    new String[]{"ObjectSize"
//                                            "ETag", "Checksum", "ObjectParts", "StorageClass", "ObjectSize"
                                    })
                            .build());
        } catch (ErrorResponseException e) {
            handleErrorResponseException(e, fullPath);
        } catch (Exception e) {
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
        }
        return response;
    }

    private void handleErrorResponseException(ErrorResponseException e, String fullPath) {
        String code = e.errorResponse().code();
        if ("NoSuchKey".equals(code)) {
            log.warn("Resource - {} not found. ", fullPath);
            throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
        }
        log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
        throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
    }

    private String getUserRootFolder(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> {
            log.warn("User - {} not found", username);
            return new DaoException(ErrorInfo.USER_NOT_FOUND);
        });
        int id = user.getId();
        return String.format(USER_ROOT_FOLDER_TEMPLATE, id);
    }

    private boolean isFolderExists(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(MAIN_BUCKET)
                .prefix(path)
                .recursive(false)
                .build());
        return results.iterator().hasNext();
    }
}
