package org.file.cloud.controller;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
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

    private List<MultipartFile> files;

    @BeforeEach
    void setUp() throws Exception {
        createBucket(minioClient);

        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(USERNAME_1)
                .password(PASSWORD_1)
                .build();

        userService.signUp(requestUserDto);
        userService.createUserRootFolder(requestUserDto);
        files = createFirstFile();
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
                        fail("Failed to delete file", e);
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
                        "file",
                        FIRST_FILE_NAME,
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello my friend!".getBytes()
                )
        );
        log.info("Created multipartFile = {}", FIRST_FILE_NAME);
        return files;
    }

    List<MultipartFile> createSecondFile() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile(
                        "file",
                        SECOND_FILE_NAME,
                        MediaType.TEXT_PLAIN_VALUE,
                        "Second file!".getBytes()
                )
        );
        log.info("Created multipartFile = {}", SECOND_FILE_NAME);
        return files;
    }

    @Test
    void shouldCreateOneFileInUserRootFolder() {
        String uploadPath = "";
        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(USERNAME_1, uploadPath, files);
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
        String resourcePath = uploadPath + SECOND_FILE_NAME;
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(USERNAME_2)
                .password(PASSWORD_2)
                .build();

        Long userId = userRepository.findIdByUsername(requestUserDto.getUsername());
        userService.createUserRootFolder(requestUserDto);

        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        String fullUploadedPath = userRootFolder + uploadPath;

        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(requestUserDto.getUsername(), uploadPath, createSecondFile());
        responseResourceDtos.removeIf(dto -> dto.getName().equals(fullUploadedPath));

        assertThat(responseResourceDtos.size()).isEqualTo(1);
        assertThat(responseResourceDtos.get(0).getName()).isEqualTo(SECOND_FILE_NAME);
        assertThatExceptionOfType(ResourceException.class).isThrownBy(() -> storageResourceValidator.ensureResourceExists(USERNAME_1, resourcePath));
    }

    @Test
    void shouldMoveFileFromUserRootFolderToAnotherFolder() {
        // create new folder
        String rootDirectory = "";
        String testFolder = "gold/";

        ResponseResourceDto folder = minioDirectoryService.createFolder(USERNAME_1, testFolder);
        Long id = userRepository.findIdByUsername(USERNAME_1);
        String userRootFolder = userRootFolderManager.getUserRootFolder(id);
        String fullFolderPath = userRootFolder + testFolder;
        log.info("Created folder = {}", fullFolderPath);
        assertThat(minioStorageService.isFolderExists(fullFolderPath)).isTrue();

        // upload file to user root folder
        List<ResponseResourceDto> createdResourceDtos = minioResourceService.uploadResource(USERNAME_1, rootDirectory, files);
        assertThat(createdResourceDtos.size()).isEqualTo(1);
        assertThat(createdResourceDtos.get(0).getName()).isEqualTo(FIRST_FILE_NAME);
        log.info("Uploaded file: path = {}, name = {}", rootDirectory, FIRST_FILE_NAME);

        // move the file to the new folder
        String resourcePathTo = testFolder + FIRST_FILE_NAME;
        ResponseResourceDto movedResourceDto = minioResourceService.moveResource(USERNAME_1, FIRST_FILE_NAME, resourcePathTo);
        assertThat(movedResourceDto.getName()).isEqualTo(FIRST_FILE_NAME);
        assertThat(movedResourceDto.getPath()).isEqualTo(testFolder);


        String fullResourcePathFrom = userRootFolder + rootDirectory + FIRST_FILE_NAME;
        String fullResourcePathTo = fullFolderPath + FIRST_FILE_NAME;
        log.info("Full moved file path = {}", fullResourcePathTo);
        assertThat(minioStorageService.isFileExists(fullResourcePathTo)).isTrue();
        assertThat(minioStorageService.isFileExists(fullResourcePathFrom)).isFalse();
    }
}




















