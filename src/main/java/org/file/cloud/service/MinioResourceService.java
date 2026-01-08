package org.file.cloud.service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.DaoException;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.FolderException;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioResourceService {
    private final UserRepository userRepository;
    private final MinioClient minioClient;

    private final String MAIN_BUCKET = "user-files";
    private final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    public void getResource() throws Exception {
        GetObjectAttributesResponse response =
                minioClient.getObjectAttributes(
                        GetObjectAttributesArgs.builder()
                                .bucket(MAIN_BUCKET)
                                .object("my-objectname")
                                .objectAttributes(
                                        new String[] {
                                                "ETag", "Checksum", "ObjectParts", "StorageClass", "ObjectSize"
                                        })
                                .build());
    }

    public void validateFolderExists(String username, String resourcePath) {
        String userRootFolder = getUserRootFolder(username);
//        String parentFolderPath = getParentFolderPath(resourcePath);
//        String fullParentPath = userRootFolder + parentFolderPath;
        String fullPath = userRootFolder + resourcePath;

//        if (!isFolderExists(fullParentPath)) {
//            log.warn("Path to parent folder - {} does not exist", fullParentPath);
//            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
//        }
//        log.info("The parent folder - {} exists", fullParentPath);

        if (!isResourceExists(fullPath)) {
            log.warn("Resource - {} does not exists", fullPath);
            throw new FolderException(ErrorInfo.RESOURCE_DOES_NOT_EXIST);
        }
        log.info("Folder - {} already exists", fullPath);
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
