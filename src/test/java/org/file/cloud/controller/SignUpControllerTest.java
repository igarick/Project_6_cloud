package org.file.cloud.controller;

import org.file.cloud.dto.UserSignUpDto;
import org.file.cloud.exception.DuplicateUserException;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class SignUpControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1");

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldPersistUser() {
        UserSignUpDto userSignUpDto = UserSignUpDto.builder()
                .username("Robert")
                .password("Robert")
                .build();
        userService.signUp(userSignUpDto);
        User user = userRepository.findByUsername("Robert").orElseThrow();
        assertThat(user.getUsername()).isEqualTo("Robert");
    }

    @Test
    void shouldThrowException_whenUserAlreadyExists() {
        UserSignUpDto userSignUpDto = UserSignUpDto.builder()
                .username("Robert")
                .password("Robert")
                .build();
        userService.signUp(userSignUpDto);
        assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(() -> userService.signUp(userSignUpDto));
    }
}






















