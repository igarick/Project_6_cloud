package org.file.cloud.controller;

import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.exception.DuplicateUserException;
import org.file.cloud.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void shouldPersistUser_whenCreatingNewUser() {
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(USERNAME_1)
                .password(PASSWORD_1)
                .build();
        userService.signUp(requestUserDto);
        User user = userRepository.findByUsernameIgnoreCase(USERNAME_1).orElseThrow();
        assertThat(user.getUsername()).isEqualTo(USERNAME_1);
    }

    @Test
    void shouldThrowDuplicateException_whenUserAlreadyExists() {
        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(USERNAME_1)
                .password(PASSWORD_1)
                .build();
        userService.signUp(requestUserDto);

        assertThatExceptionOfType(DuplicateUserException.class).isThrownBy(() -> userService.signUp(requestUserDto));
    }

    @Test
    void passwordShouldBeEncoded() {
        RequestUserDto userDto = RequestUserDto.builder()
                .username(USERNAME_1)
                .password(PASSWORD_1)
                .build();
        userService.signUp(userDto);
        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(userDto.getUsername());

        assertThat(userOptional.isPresent());
        assertThat(passwordEncoder.matches(userDto.getPassword(), userOptional.get().getPassword()));
    }

}






















