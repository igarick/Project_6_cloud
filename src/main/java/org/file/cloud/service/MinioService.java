package org.file.cloud.service;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.FolderException;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private final UserRepository userRepository;

    private final String MAIN_BUCKET = "user-files";
    private final String USER_HOME_PREFIX_PATTERN = "user-%s-files/";

    public void createFolder(String username, String mainPath) throws Exception {
        Optional<User> byUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(username);
        User user = byUsernameIgnoreCase.get();
        int id = user.getId();
        String path = String.format(USER_HOME_PREFIX_PATTERN, id);
        String bucket = path + mainPath;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(MAIN_BUCKET)
                        .object(bucket)
                        .stream(InputStream.nullInputStream(), 0, -1)
//                        .contentType("video/mp4")
                        .build());
        log.info("Folder = {} was created", bucket);
    }

//    public void createFolder(String username, String mainPath) throws Exception {
//        Optional<User> byUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(username);
//        User user = byUsernameIgnoreCase.get();
//        int id = user.getId();
//        String path = String.format(USER_FILES_BUCKET, id);
//        String bucket = path + mainPath;
//
//        minioClient.putObject(
//                PutObjectArgs.builder()
//                        .bucket(MAIN_BUCKET)
//                        .object(bucket)
//                        .stream(InputStream.nullInputStream(), 0, -1)
////                        .contentType("video/mp4")
//                        .build());
//    }

    public boolean checkParentFolderExists(String username, String pathToParentFolder) {
        Optional<User> byUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(username);
        User user = byUsernameIgnoreCase.get();
        int id = user.getId();
        String userHomePrefix = String.format(USER_HOME_PREFIX_PATTERN, id);
        String path = userHomePrefix + pathToParentFolder;

        if (!isFolderExists(path)) {
            log.warn("The parent folder - {} does not exist", path);
            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
        }
        log.warn("The parent folder - {} exists", path);
        return true;
    }

    private boolean isFolderExists(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(MAIN_BUCKET)
                .prefix(path)
                .recursive(false)
                .build());
        return results.iterator().hasNext();
    }

    public boolean isFolderAlreadyExists(String username, String mainPath) {
        Optional<User> byUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(username);
        User user = byUsernameIgnoreCase.get();
        int id = user.getId();
        String path = String.format(USER_HOME_PREFIX_PATTERN, id);
        String bucket = path + mainPath;

        if (isExist(bucket)) {
            log.warn("Folder - {} already exists", bucket);
            return true;
//            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
        }
        log.warn("Folder - {} does not exists", bucket);
        return false;
    }

    private boolean isExist(String bucket) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(MAIN_BUCKET)
                .prefix(bucket)
                .recursive(false)
                .build());
        return results.iterator().hasNext();
    }

}
