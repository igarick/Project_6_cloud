package org.file.cloud.controller;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.service.UserRootFolderManager;
import org.file.cloud.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MinioDirectoryControllerTest {
    private static final String BUCKET = "test-bucket";
    private final String TEST_FOLDER = "testFolder";

    @Autowired
    MinioResourceService minioResourceService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRootFolderManager userRootFolderManager;

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

    @BeforeEach
    void setUp() throws Exception {
        minioUserName = minIOContainer.getUserName();
        log.info("userName = {}", minioUserName);
        minioUserPassword = minIOContainer.getPassword();
        log.info("userPassword = {}", minioUserPassword);
        s3URL = minIOContainer.getS3URL();
        log.info("s3URL = {}", s3URL);

        MinioClient minioClient = createClient();
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
                        .bucket(BUCKET)
                        .build());
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
        String fullFilePath = BUCKET + "/" + userRootFolder + "/" + "hello.txt";


        assertThat(fileStream).isNotNull();
        assertThat(responseResourceDtos.size()).isEqualTo(1);
    }
}




















