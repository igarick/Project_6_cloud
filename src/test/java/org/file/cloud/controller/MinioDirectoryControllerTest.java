package org.file.cloud.controller;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.configuration.MinioProperties;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.MinioDirectoryService;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.service.StorageResourceValidator;
import org.file.cloud.service.UserRootFolderManager;
import org.file.cloud.service.minio.MinioStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class MinioDirectoryControllerTest extends BaseIntegrationTest {
//    private final String TEST_FOLDER = "testFolder";

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioProperties minioProperties;

    @Autowired
    MinioResourceService minioResourceService;

    @Autowired
    MinioStorageService minioStorageService;

    @Autowired
    MinioDirectoryService minioDirectoryService;

    @Autowired
    StorageResourceValidator storageResourceValidator;

    @Autowired
    UserRootFolderManager userRootFolderManager;

    @Autowired
    UserRepository userRepository;

    private static final String USERNAME_2 = "user_two";
    private static final String PASSWORD_2 = "password_two";

    private static final String FIRST_FILE_NAME = "first.txt";
    private static final String SECOND_FILE_NAME = "second.txt";
    private static final String THIRD_FILE_NAME = "third.pdf";

    private List<MultipartFile> testFile;
    protected List<MultipartFile> testFolder;

    @BeforeEach
    void setUp() throws Exception {
        createBucket(minioClient);

        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(USERNAME_1)
                .password(PASSWORD_1)
                .build();

        userService.signUp(requestUserDto);
        userService.createUserRootFolder(requestUserDto);
        testFile = createFirstFile();
        testFolder = createFolder();
    }

    @AfterEach
    void cleanup() {
        removeBucket();
    }

    void removeBucket() {
        minioStorageService.getObjects("")
                .forEach(itemResult -> {
                    try {
                        minioStorageService.deleteFile(itemResult.get().objectName());
                    } catch (Exception e) {
                        fail("Failed to delete file in test bucket", e);
                    }
                });

        try {
            minioClient.removeBucket(
                    RemoveBucketArgs.builder()
                            .bucket(minioProperties.bucket())
                            .build());
        } catch (Exception e) {
            fail("Failed to remove test bucket", e);
        }
    }

    void createBucket(MinioClient minioClient) throws Exception {
        boolean found =
                minioClient.bucketExists(
                        BucketExistsArgs.builder()
                                .bucket(minioProperties.bucket())
                                .build());
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioProperties.bucket())
                            .build());
            log.info("Created bucket = {}", minioProperties.bucket());
        }
    }

    List<MultipartFile> createFirstFile() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile(
                        "files",
                        FIRST_FILE_NAME,
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello my friend!".getBytes()
                )
        );
        log.info("Created multipartFile = {}", FIRST_FILE_NAME);
        return files;
    }

    List<MultipartFile> createFolder() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile(
                        "files",
                        FIRST_FILE_NAME,
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello my friend!".getBytes()
                ));
        files.add(
                new MockMultipartFile(
                        "files",
                        SECOND_FILE_NAME,
                        MediaType.APPLICATION_PDF_VALUE,
                        "I want pdf!".getBytes()
                ));
        files.add(
                new MockMultipartFile(
                        "files",
                        THIRD_FILE_NAME,
                        MediaType.APPLICATION_XML_VALUE,
                        "Take it, xml!".getBytes()
                )
        );
        log.info("Created folder with files: {}, {}, {}", FIRST_FILE_NAME, SECOND_FILE_NAME, THIRD_FILE_NAME);
        return files;
    }

    @Test
    void shouldCreateOneFileInUserRootFolder() {
        String uploadPath = "";
        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(USERNAME_1, uploadPath, testFile);
        StreamingResponseBody fileStream = minioResourceService.getFileStream(USERNAME_1, FIRST_FILE_NAME);

        Long userId = userRepository.findIdByUsername(USERNAME_1);
        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        String fullUploadedPath = userRootFolder + uploadPath;
        String fullFilePath = fullUploadedPath + FIRST_FILE_NAME;
        log.info("fullFilePath: {}", fullFilePath);

        List<String> paths = new ArrayList<>();
        minioStorageService.getObjects(fullUploadedPath)
                .forEach(result -> {
                    try {
                        String objectName = result.get().objectName();
                        paths.add(objectName);
                        log.info("objectName: {}", objectName);
                    } catch (Exception e) {
                        fail("Failed to read object from MinIO", e);
                    }
                });
        paths.removeIf(s -> s.equals(fullUploadedPath));

        assertThat(paths).hasSize(1).containsExactly(fullFilePath);
        assertThat(fileStream).isNotNull();
        assertThat(responseResourceDtos.size()).isEqualTo(1);
    }

    @Test
    void shouldCreateDirectoryInUserRootFolder() {
        String uploadPath = "";
        String folderName = "snow";
        String fullUploadPath = uploadPath + folderName + "/";

        ResponseResourceDto folder = minioDirectoryService.createFolder(USERNAME_1, fullUploadPath);
        assertThat(folderName).isEqualTo(folder.getName());
    }

    @Test
    void shouldDenyAccessToOtherUsersResources() {
        String uploadPath = "";
        String resourcePath = uploadPath + FIRST_FILE_NAME;
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(USERNAME_2)
                .password(PASSWORD_2)
                .build();

        userService.signUp(requestUserDto);
        Long userId = userRepository.findIdByUsername(requestUserDto.getUsername());
        userService.createUserRootFolder(requestUserDto);

        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        String fullUploadedPath = userRootFolder + uploadPath;

        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(requestUserDto.getUsername(), uploadPath, createFirstFile());
        responseResourceDtos.removeIf(dto -> dto.getName().equals(fullUploadedPath));

        assertThat(responseResourceDtos.size()).isEqualTo(1);
        assertThat(responseResourceDtos.get(0).getName()).isEqualTo(FIRST_FILE_NAME);
        assertThat(responseResourceDtos.get(0).getPath()).isEqualTo(uploadPath);
        assertThatExceptionOfType(ResourceException.class).isThrownBy(() -> storageResourceValidator.ensureResourceExists(USERNAME_1, resourcePath));
    }

    @Test
    void shouldMoveFileFromUserRootFolderToAnotherFolder() {
        // create new folder
        String rootDirectory = "";
        String newFolder = "gold/";

        ResponseResourceDto folder = minioDirectoryService.createFolder(USERNAME_1, newFolder);
        Long id = userRepository.findIdByUsername(USERNAME_1);
        String userRootFolder = userRootFolderManager.getUserRootFolder(id);
        String fullFolderPath = userRootFolder + newFolder;
        log.info("Created folder = {}", fullFolderPath);
        assertThat(minioStorageService.isFolderExists(fullFolderPath)).isTrue();
        assertThat(folder.getName() + "/").isEqualTo(newFolder);

        // upload file to user root folder
        List<ResponseResourceDto> createdResourceDtos = minioResourceService.uploadResource(USERNAME_1, rootDirectory, testFile);
        assertThat(createdResourceDtos.size()).isEqualTo(1);
        assertThat(createdResourceDtos.get(0).getName()).isEqualTo(FIRST_FILE_NAME);
        log.info("Uploaded file: path = {}, name = {}", rootDirectory, FIRST_FILE_NAME);

        // move the file to the new folder
        String resourcePathTo = newFolder + FIRST_FILE_NAME;
        ResponseResourceDto movedResourceDto = minioResourceService.moveResource(USERNAME_1, FIRST_FILE_NAME, resourcePathTo);
        assertThat(movedResourceDto.getName()).isEqualTo(FIRST_FILE_NAME);
        assertThat(movedResourceDto.getPath()).isEqualTo(newFolder);


        String fullResourcePathFrom = userRootFolder + rootDirectory + FIRST_FILE_NAME;
        String fullResourcePathTo = fullFolderPath + FIRST_FILE_NAME;
        log.info("Full moved file path = {}", fullResourcePathTo);
        assertThat(minioStorageService.isFileExists(fullResourcePathTo)).isTrue();
        assertThat(minioStorageService.isFileExists(fullResourcePathFrom)).isFalse();
    }

    @Test
    void shouldMoveFolderToAnotherFolder() {
        // create new folders
        String folderPathFrom = "sun/";
        String folderPathTo = "gold/sold/";
        String expectedCreatedFolderNameTo = "sold/";

        ResponseResourceDto folderTo = minioDirectoryService.createFolder(USERNAME_1, folderPathTo);
        Long id = userRepository.findIdByUsername(USERNAME_1);
        String userRootFolder = userRootFolderManager.getUserRootFolder(id);
        String fullPathFolderTo = userRootFolder + folderPathTo;
        assertThat(minioStorageService.isFolderExists(fullPathFolderTo)).isTrue();
        assertThat(folderTo.getName() + "/").isEqualTo(expectedCreatedFolderNameTo);

        ResponseResourceDto folderFrom = minioDirectoryService.createFolder(USERNAME_1, folderPathFrom);
        String fullPathFolderFrom = userRootFolder + folderPathFrom;
        assertThat(minioStorageService.isFolderExists(fullPathFolderFrom)).isTrue();
        assertThat(folderFrom.getName() + "/").isEqualTo(folderPathFrom);
        log.info("Created folder = {}", fullPathFolderFrom);

        // upload testFolder to pathFolderFrom

        List<ResponseResourceDto> createdResourceDtos = minioResourceService.uploadResource(USERNAME_1, folderPathFrom, testFolder);
        assertThat(createdResourceDtos.size()).isEqualTo(3);

        String fullFirstFilePath = userRootFolder + folderPathFrom + FIRST_FILE_NAME;
        String fullSecondFilePath = userRootFolder + folderPathFrom + SECOND_FILE_NAME;
        String fullThirdFilePath = userRootFolder + folderPathFrom + THIRD_FILE_NAME;
        log.info("Folder contents: {}, {}, {}", fullFirstFilePath, fullSecondFilePath, fullThirdFilePath);
        assertThat(minioStorageService.isFileExists(fullFirstFilePath)).isTrue();
        assertThat(minioStorageService.isFileExists(fullSecondFilePath)).isTrue();
        assertThat(minioStorageService.isFileExists(fullThirdFilePath)).isTrue();

        // move to the new folder
        String newFolder = folderPathTo + folderPathFrom;
        ResponseResourceDto movedResourceDto = minioResourceService.moveResource(USERNAME_1, folderPathFrom, newFolder);
        assertThat(movedResourceDto.getName()).isEqualTo(folderPathFrom);

        String fullNewFolderPath = userRootFolder + newFolder;
        List<String> result = new ArrayList<>();
        minioStorageService.getObjects(fullNewFolderPath, false)
                .forEach(itemResult -> {
                    try {
                        String objectName = itemResult.get().objectName();
                        log.info("Object name = {}", objectName);
                        result.add(objectName);
                    } catch (Exception e) {
                        fail("Failed to read object from MinIO", e);
                    }
                });

        log.info("--------------2-----------------------");
        result.removeIf(s -> s.equals(newFolder));
        log.info("--------------3-----------------------");

        assertThat(result).hasSize(3);

        log.info("--------------4-----------------------");


        String newFullFirstFilePath = userRootFolder + folderPathTo + folderPathFrom + FIRST_FILE_NAME;
        String newFullSecondFilePath = userRootFolder + folderPathTo + folderPathFrom + SECOND_FILE_NAME;
        String newFullThirdFilePath = userRootFolder + folderPathTo + folderPathFrom + THIRD_FILE_NAME;
        log.info("Folder contents: {}, {}, {}", newFullFirstFilePath, newFullSecondFilePath, newFullThirdFilePath);
        assertThat(minioStorageService.isFileExists(newFullFirstFilePath)).isTrue();
        assertThat(minioStorageService.isFileExists(newFullSecondFilePath)).isTrue();
        assertThat(minioStorageService.isFileExists(newFullThirdFilePath)).isTrue();


////        assertThat(movedResourceDto.getName()).isEqualTo(FIRST_FILE_NAME);
//        assertThat(movedResourceDto.getPath()).isEqualTo(folderPathTo);
//
//
//        String fullResourcePathFrom = userRootFolder + rootDirectory + FIRST_FILE_NAME;
//        String fullResourcePathTo = fullFolderPathTo + FIRST_FILE_NAME;
//        log.info("Full moved file path = {}", fullResourcePathTo);
//        assertThat(minioStorageService.isFileExists(fullResourcePathTo)).isTrue();
//        assertThat(minioStorageService.isFileExists(fullResourcePathFrom)).isFalse();
    }
}




















