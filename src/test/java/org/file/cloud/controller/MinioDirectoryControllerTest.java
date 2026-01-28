package org.file.cloud.controller;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.MinioDirectoryService;
import org.file.cloud.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MinioDirectoryControllerTest {
    private static final String BUCKET = "test-bucket";
    private final String TEST_FOLDER = "testFolder";
//    private static final String USERNAME = minIOContainer.getUserName();

    @Autowired
    MinioDirectoryService minioDirectoryService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    private String userName;
    private String userPassword;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1");

    @Container
//    @ServiceConnection
    static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:RELEASE.2025-07-23T15-54-02Z");

    @BeforeEach
    void setUp() throws Exception {
        userName = minIOContainer.getUserName();
        userPassword = minIOContainer.getPassword();

//        MinioClient minioClient = createClient();
//        createBucket(minioClient);
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(userName)
                .password(userPassword)
                .build();

        userService.signUp(requestUserDto);
    }

    @Test
    void check() {
        assertThat(userName.equals("popopo"));
        assertThat(userPassword.equals("popopo"));
        assertThat(minIOContainer.getHost().equals("asdasd"));
    }

//    MinioClient createClient() {
//        return MinioClient.builder()
////                .endpoint(minIOContainer.getS3URL())
//                .endpoint("http://" + minIOContainer.getHost() + ":" + minIOContainer.getMappedPort(9000))
//                .credentials(minIOContainer.getUserName(), minIOContainer.getPassword())
//                .build();
//    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", minIOContainer::getS3URL);
        registry.add("minio.access-key", minIOContainer::getUserName);
        registry.add("minio.secret-key", minIOContainer::getPassword);
        // Добавьте сюда те названия свойств, которые используются в вашем MinioDirectoryService
    }


//    @DynamicPropertySource
//    static void registerMinioProperties(DynamicPropertyRegistry registry) {
//        registry.add("minio.url", minIOContainer::getS3URL);
//        registry.add("minio.access-key", minIOContainer::getUserName);
//        registry.add("minio.secret-key", minIOContainer::getPassword);
//        registry.add("minio.bucket", () -> BUCKET);
//    }

    void createBucket(MinioClient minioClient) throws Exception {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(BUCKET)
                        .build());
    }

    @Test
    void shouldPersistUser() {
        Optional<User> userOptional = userRepository.findByUsername(userName);
        assertThat(userOptional.isPresent());
        assertThat(userOptional.get().getUsername().equals(userName));
    }

}