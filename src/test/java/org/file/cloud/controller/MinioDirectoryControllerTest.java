package org.file.cloud.controller;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.configuration.MinioProperties;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.service.UserRootFolderManager;
import org.file.cloud.service.UserService;
import org.file.cloud.service.minio.MinioStorageService;
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

    private String minioUserName;
    private String minioUserPassword;
    private String s3URL;

    private List<MultipartFile> files;

    private static final String TEST_NAME = "TestName";
    private static final String TEST_PASSWORD = "TestPassword";
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

//        MinioClient minioClient = createClient();
        createBucket(minioClient);

        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(TEST_NAME)
                .password(TEST_PASSWORD)
                .build();

        userId = createUserAndGetId(requestUserDto);
        userService.createUserRootFolder(requestUserDto);

        files = createFile();
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
        String path = "";
        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(TEST_NAME, path, files);
        StreamingResponseBody fileStream = minioResourceService.getFileStream(TEST_NAME, "hello.txt");


        String userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        String fullFilePath =  userRootFolder + "hello.txt";
        log.info("fullFilePath: {}", fullFilePath);


        List<String> paths = new ArrayList<>();
        Iterable<Result<Item>> objects = minioStorageService.getObjects(fullFilePath);

        for (Result<Item> object : objects) {
            String objectName = null;
            try {
                objectName = object.get().objectName();
                paths.add(objectName);
                log.info("objectName: {}", objectName);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        log.info("paths size: {}", paths.size());

        assertThat(paths.size()).isEqualTo(1);
        assertThat(paths.get(0)).isEqualTo(fullFilePath);


        assertThat(fileStream).isNotNull();
        assertThat(responseResourceDtos.size()).isEqualTo(1);
    }
}




















