package org.file.cloud.controller;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.configuration.MinioProperties;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.MinioDirectoryService;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.service.UserRootFolderManager;
import org.file.cloud.service.UserService;
import org.file.cloud.service.minio.MinioStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MinioDirectoryControllerTest {
    //    private static final String BUCKET = "test-bucket";
    private final String TEST_FOLDER = "testFolder";

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioResourceService minioResourceService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRootFolderManager userRootFolderManager;

    @Autowired
    MinioProperties minioProperties;

    @Autowired
    MinioStorageService minioStorageService;

    @Autowired
    MinioDirectoryService minioDirectoryService;

    private String minioUserName;
    private String minioUserPassword;
    private String s3URL;

    private List<MultipartFile> files;

    private static final String TEST_USERNAME = "TestName";
    private static final String TEST_USERNAME_PASSWORD = "TestPassword";
    private static Long userId;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1");

    @Container
    static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:RELEASE.2025-07-23T15-54-02Z");

    @DynamicPropertySource
    static void overrideMinioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", minIOContainer::getS3URL);
        registry.add("minio.access-key", minIOContainer::getUserName);
        registry.add("minio.secret-key", minIOContainer::getPassword);
    }

    @BeforeEach
    void setUp() throws Exception {
        minioUserName = minIOContainer.getUserName();
        log.info("userName = {}", minioUserName);
        minioUserPassword = minIOContainer.getPassword();
        log.info("userPassword = {}", minioUserPassword);
        s3URL = minIOContainer.getS3URL();
        log.info("s3URL = {}", s3URL);

        createBucket(minioClient);

        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(TEST_USERNAME)
                .password(TEST_USERNAME_PASSWORD)
                .build();

        userId = createUserAndGetId(requestUserDto);
        userService.createUserRootFolder(requestUserDto);

        files = createFile();
    }

    @BeforeEach
    void cleanupDatabase() {
        userRepository.deleteAll();
    }

    @AfterEach
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

    @Test
    void check() {
        assertThat(minioUserName).isEqualTo("minioadmin");
        log.info("in test: userName = {}", minioUserName);
        assertThat(minioUserPassword).isEqualTo("minioadmin");
        log.info("in test:userPassword = {}", minioUserPassword);
    }

    private MinioClient createClient() {
        return MinioClient.builder()
                .endpoint(minIOContainer.getS3URL())
                .credentials(minioUserName, minioUserPassword)
                .build();
    }

    void createBucket(MinioClient minioClient) throws Exception {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(minioProperties.bucket())
                        .build());
        log.info("BUCKET = {}", minioProperties.bucket());
    }

    Long createUserAndGetId(RequestUserDto requestUserDto) {
        userService.signUp(requestUserDto);
        return userRepository.findIdByUsername(requestUserDto.getUsername());
    }

    List<MultipartFile> createFile() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile(
                        "file",
                        "hello.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello my friend!".getBytes()
                )
        );
        return files;
    }

    @Test
    void shouldCreateOneFile() {
        String uploadedPath = "";
        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(TEST_USERNAME, uploadedPath, files);
        StreamingResponseBody fileStream = minioResourceService.getFileStream(TEST_USERNAME, "hello.txt");

        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        String fullUploadedPath = userRootFolder + uploadedPath;
        String fullFilePath = userRootFolder + "hello.txt";
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
    void shouldCreateDirectory() {
//        String uploadedPath = "";
        String folderName = "snow";
        String directoryPath = folderName + "/";

        ResponseResourceDto folder = minioDirectoryService.createFolder(TEST_USERNAME, directoryPath);
        log.info("folder name from dto = {}", folder.getName());

        assertThat(folderName).isEqualTo(folder.getName());
    }
}




















