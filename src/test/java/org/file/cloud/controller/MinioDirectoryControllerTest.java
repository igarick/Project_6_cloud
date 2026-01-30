package org.file.cloud.controller;

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
import org.file.cloud.service.*;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MinioDirectoryControllerTest {
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

    @Autowired
    StorageResourceValidator storageResourceValidator;

    private String minioUserName;
    private String minioUserPassword;

    private List<MultipartFile> files;

    private static final String TEST_USERNAME = "TestName";
    private static final String TEST_USERNAME_PASSWORD = "TestPassword";
    private Long userId;

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
        minioUserPassword = minIOContainer.getPassword();

        createBucket(minioClient);

        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(TEST_USERNAME)
                .password(TEST_USERNAME_PASSWORD)
                .build();

        userId = createUserAndGetId(requestUserDto);
        userService.createUserRootFolder(requestUserDto);

        files = createFile();
    }

    @AfterEach
    void cleanup() {
        cleanupDatabase();
        removeBucket();
    }

    void cleanupDatabase() {
        userRepository.deleteAll();
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

//    @Test
//    void check() {
//        assertThat(minioUserName).isEqualTo("minioadmin");
//        log.info("in test: userName = {}", minioUserName);
//        assertThat(minioUserPassword).isEqualTo("minioadmin");
//        log.info("in test:userPassword = {}", minioUserPassword);
//    }

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

    List<MultipartFile> createSecondFile() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile(
                        "file",
                        "second.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Second file!".getBytes()
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
        String fullFilePath = fullUploadedPath + "hello.txt";
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
        String folderName = "snow";
        String directoryPath = folderName + "/";

        ResponseResourceDto folder = minioDirectoryService.createFolder(TEST_USERNAME, directoryPath);
        assertThat(folderName).isEqualTo(folder.getName());
    }

    @Test
    void shouldNotAllowGettingOtherUsersResources() {
        String uploadedPath = "";
        String fictiveResourceName = "second.txt";
        String resourcePath = uploadedPath + fictiveResourceName;
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username("TestName-2")
                .password("TestPassword-2")
                .build();

        Long userId = createUserAndGetId(requestUserDto);
        userService.createUserRootFolder(requestUserDto);

        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        String fullUploadedPath = userRootFolder + uploadedPath;

        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(requestUserDto.getUsername(), uploadedPath, createSecondFile());
        responseResourceDtos.removeIf(dto -> dto.getName().equals(fullUploadedPath));

        assertThat(responseResourceDtos.size()).isEqualTo(1);
        assertThat(responseResourceDtos.get(0).getName()).isEqualTo(fictiveResourceName);
        assertThatExceptionOfType(ResourceException.class).isThrownBy(() -> storageResourceValidator.ensureResourceExists(TEST_USERNAME, resourcePath));
    }
}




















