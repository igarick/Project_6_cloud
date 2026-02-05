package org.file.cloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    protected static final String USERNAME_1 = "user_one";
    protected static final String PASSWORD_1 = "password_one";

    @DynamicPropertySource
    static void overrideMinioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", TestContainersStarter.minIOContainer::getS3URL);
        registry.add("minio.access-key", TestContainersStarter.minIOContainer::getUserName);
        registry.add("minio.secret-key", TestContainersStarter.minIOContainer::getPassword);

        registry.add("spring.datasource.url", TestContainersStarter.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", TestContainersStarter.postgres::getUsername);
        registry.add("spring.datasource.password", TestContainersStarter.postgres::getPassword);
    }

    @AfterEach
    void cleanDataBase() {
        userRepository.deleteAll();
    }
}
