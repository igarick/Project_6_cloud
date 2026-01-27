package org.file.cloud.controller;

import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.UserSignUpDto;
import org.file.cloud.exception.DuplicateUserException;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class AuthControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1");

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void cleanupDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldPersistUser_whenCreatingNewUser() {
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username("Robert")
                .password("Robert")
                .build();
        userService.signUp(requestUserDto);
        User user = userRepository.findByUsername("Robert").orElseThrow();
        assertThat(user.getUsername()).isEqualTo("Robert");
    }

    @Test
    void shouldThrowDuplicateException_whenUserAlreadyExists() {
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username("Tom")
                .password("Tom")
                .build();
        userService.signUp(requestUserDto);
        assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(() -> userService.signUp(requestUserDto));
    }

    @Test
    void passwordShouldBeEncoded() {
        RequestUserDto userDto = RequestUserDto.builder()
                .username("Pol")
                .password("Pol")
                .build();
        userService.signUp(userDto);
        Optional<User> userOptional = userRepository.findByUsername(userDto.getUsername());
        assertThat(userOptional.isPresent());
        assertThat(userOptional.get().getPassword()).isNotEqualTo(userDto.getPassword());
    }

}






















