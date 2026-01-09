package org.file.cloud.service;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
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

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioDirectoryService {
    private final MinioClient minioClient;
    private final UserRepository userRepository;

    private final String MAIN_BUCKET = "user-files";
    private final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";
    private final String RESOURCE_TYPE = "DIRECTORY";

    public ResourceResponseDto createFolder(String username, String resourcePath) throws Exception {
        String userRootFolder = getUserRootFolder(username);
        String fullPath = userRootFolder + resourcePath;
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(MAIN_BUCKET)
                        .object(fullPath)
                        .stream(InputStream.nullInputStream(), 0, -1)
                        .build());
        log.info("Folder - {} was created", resourcePath);

        return getInfoToResponse(resourcePath);
    }

    public ResourceResponseDto getInfoToResponse(String resourcePath) {
        String parentFolderPath = getParentFolderPath(resourcePath);
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


    public void validateFolderExists(String username, String resourcePath) {
        String userRootFolder = getUserRootFolder(username);
        String parentFolderPath = getParentFolderPath(resourcePath);

        String fullParentPath = userRootFolder + parentFolderPath;
        String fullPath = userRootFolder + resourcePath;

        if (!isFolderExists(fullParentPath)) {
            log.warn("Path to parent folder - {} does not exist", fullParentPath);
            throw new ResourceException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
        }
        log.info("The parent folder - {} exists", fullParentPath);

        if (isFolderExists(fullPath)) {
            log.warn("Folder - {} already exists", fullPath);
            throw new ResourceException(ErrorInfo.FOLDER_ALREADY_EXISTS);
        }
        log.info("Folder - {} does not exists", fullPath);
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
//        Optional<User> byUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(username);
//        User user = byUsernameIgnoreCase.get();

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

//    public boolean isFolderExists(String path) {
//
//        if (!isFolderExistsQQQQQQQ(path)) {
//            log.warn("The parent folder - {} does not exist", path);
//            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
//        }
//        log.warn("The parent folder - {} exists", path);
//        return true;

//    }

//    public boolean isParentFolderExists(String username, String pathToParentFolder) {
//        Optional<User> byUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(username);
//        User user = byUsernameIgnoreCase.get();
//        int id = user.getId();
//        String userRootFolder = String.format(USER_ROOT_FOLDER_TEMPLATE, id);
//        String path = userRootFolder + pathToParentFolder;
//
//        if (!isFolderExists(path)) {
//            log.warn("The parent folder - {} does not exist", path);
//            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
//        }
//        log.warn("The parent folder - {} exists", path);
//        return true;

//    }

//    public boolean isFolderAlreadyExists(String username, String mainPath) {
//        Optional<User> byUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(username);
//        User user = byUsernameIgnoreCase.get();
//        int id = user.getId();
//        String path = String.format(USER_ROOT_FOLDER_TEMPLATE, id);
//        String bucket = path + mainPath;
//
//        if (isFolderExistsQQQQQQQ(bucket)) {
//            log.warn("Folder - {} already exists", bucket);
//            return true;
////            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
//        }
//        log.warn("Folder - {} does not exists", bucket);
//        return false;
//    }

//    private boolean isExist(String bucket) {
//        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
//                .bucket(MAIN_BUCKET)
//                .prefix(bucket)
//                .recursive(false)
//                .build());
//        return results.iterator().hasNext();
//    }

}
