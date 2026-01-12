package org.file.cloud.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.service.minio.MinioStorageService;
import org.file.cloud.util.resource.ResourceType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioShowInfoResourceService {
    private final MinioClient minioClient;
    private final MinioDownloadFolderService minioDownloadFolderService;
    private final MinioStorageService minioStorageService;

    private final UserService userService;

    private static final String MAIN_BUCKET = "user-files";
    private static final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    public void validateResourceExistence(String username, String resourcePath) {
        String fullPath = getFullPath(username, resourcePath);
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

    public ResourceResponseDto buildDtoToResponse(String username, String resourcePath) {
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

    private Long getFileSize(String username, String resourcePath) {
        String fullPath = getFullPath(username, resourcePath);

//        GetObjectAttributesResponse resourceAttributes = getResourceAttributes(username, resourcePath);
        GetObjectAttributesResponse resourceAttributes = getResourceAttributes(fullPath);
        Long objectSize = resourceAttributes.result().objectSize();
        log.info("File size = {}", objectSize);
        return Objects.requireNonNullElse(objectSize, 0L);
    }

    //    public GetObjectAttributesResponse getResourceAttributes(String username, String resourcePath) {
    public GetObjectAttributesResponse getResourceAttributes(String fullPath) {
//        String userRootFolder = getUserRootFolder(username);
//        String fullPath = userRootFolder + resourcePath;

        GetObjectAttributesResponse response = null;

        try {
            response = minioClient.getObjectAttributes(
//           return minioClient.getObjectAttributes(
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
            log.warn("File not found: path = {}", fullPath);
            throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
        }
        log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
        throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
    }

    public void deleteResource(String username, String resourcePath) {
        String fullPath = getFullPath(username, resourcePath);

        if (resourcePath.endsWith("/")) {
            deleteFolder(fullPath);
            log.info("Folder was deleted: path = {}", fullPath);
        } else {
            deleteFile(fullPath);
            log.info("File was deleted: path = {}", fullPath);
        }
    }

    public void deleteFile(String fullPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .object(fullPath)
                            .build());

        } catch (Exception e) {
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public void deleteFolder(String fullPath) {
        try {
            Iterable<Result<Item>> results = minioStorageService.getObjects(fullPath);

            List<DeleteObject> objects = new LinkedList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                objects.add(new DeleteObject(objectName));
                log.info("Marked for deletion: {}", objectName);
            }
            Iterable<Result<DeleteError>> removed = minioStorageService.removeObjects(objects);
            for (Result<DeleteError> result : removed) {
                DeleteError error = result.get();
                log.warn("Error in deleting object {}; {}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            log.warn("Unexpected error while deleting folder: path = {}, error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public StreamingResponseBody getFileStream(String username, String resourcePath) {
        String fullPath = getFullPath(username, resourcePath);

        if (!resourcePath.endsWith("/")) {
            return outputStream -> {
                try (InputStream stream = minioStorageService.getObjectStream(fullPath)) {
                    stream.transferTo(outputStream);
                }
            };
        } else {
            return outputStream -> {
                minioDownloadFolderService.downloadFolder(fullPath, outputStream);
            };
        }
    }

    public String getFullPath(String username, String resourcePath) {
        String userRootFolder = getUserRootFolder(username);
        return userRootFolder + resourcePath;
    }

    public String getUserRootFolder(String username) {
        Long id = userService.getUserId(username);
        return String.format(USER_ROOT_FOLDER_TEMPLATE, id);
    }
}


