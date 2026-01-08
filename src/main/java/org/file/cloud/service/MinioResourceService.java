package org.file.cloud.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceDto;
import org.file.cloud.exception.DaoException;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.util.resource.ResourceType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioResourceService {
    private final UserRepository userRepository;
    private final MinioClient minioClient;

    private final String MAIN_BUCKET = "user-files";
    private final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    public GetObjectAttributesResponse getResourceAttributes(String username, String resourcePath) throws Exception {
        String userRootFolder = getUserRootFolder(username);
        String fullPath = userRootFolder + resourcePath;

        GetObjectAttributesResponse response;
        try {
            response = minioClient.getObjectAttributes(
                    GetObjectAttributesArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .object(fullPath)
                            .objectAttributes(
                                    new String[]{
                                            "ETag", "Checksum", "ObjectParts", "StorageClass", "ObjectSize"
                                    })
                            .build());
        } catch (ErrorResponseException e) {
            log.warn("Resource - {} not found", fullPath);
            throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND, e);
        }
        return response;
    }

    public ResourceDto getInfoToResponse(String username, String resourcePath) throws Exception {
        String parentFolderPath = getParentFolderPath(resourcePath);
        log.info("Path to JSON - {}", parentFolderPath);

        String name;
        if (parentFolderPath.isEmpty()) {
            name = resourcePath.substring(0, resourcePath.length() - 1);
        } else {
            int length = parentFolderPath.length();
            name = resourcePath.substring(length, resourcePath.length() - 1);
        }
        log.info("Name to JSON - {}", name);

        Long size = null;
        String type = ResourceType.DIRECTORY.name();
        if (!resourcePath.endsWith("/")) {
            GetObjectAttributesResponse resourceAttributes = getResourceAttributes(username, resourcePath);
            size = resourceAttributes.result().objectSize();
            type = ResourceType.FILE.name();
        }
        return ResourceDto.builder()
                .path(parentFolderPath)
                .name(name)
                .size(size)
                .type(type)
                .build();
    }

    public void validateFolderExists(String username, String resourcePath) throws Exception {
        String userRootFolder = getUserRootFolder(username);
//        String parentFolderPath = getParentFolderPath(resourcePath);
//        String fullParentPath = userRootFolder + parentFolderPath;
        String fullPath = userRootFolder + resourcePath;

//        if (!isFolderExists(fullParentPath)) {
//            log.warn("Path to parent folder - {} does not exist", fullParentPath);
//            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
//        }
//        log.info("The parent folder - {} exists", fullParentPath);

        if (resourcePath.endsWith("/")) {
            if (!isResourceExists(fullPath)) {
                log.warn("Resource - {} not found", fullPath);
                throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
            }
            log.info("Resource - {} exists", fullPath);
        } else {
            getResourceAttributes(username, resourcePath);
            log.info("Resource - {} exists", fullPath);
        }
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

    private String getUserRootFolder(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> {
            log.warn("User - {} not found", username);
            return new DaoException(ErrorInfo.USER_NOT_FOUND);
        });
        int id = user.getId();
        return String.format(USER_ROOT_FOLDER_TEMPLATE, id);
    }

    private boolean isResourceExists(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(MAIN_BUCKET)
                .prefix(path)
                .recursive(false)
                .build());
        return results.iterator().hasNext();
    }
}
