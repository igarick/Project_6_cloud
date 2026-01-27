package org.file.cloud.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.doc.AuthSwagger;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.UsernameDto;
import org.file.cloud.service.UserService;
import org.file.cloud.validator.RequestValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthSwagger {
    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UsernameDto> signUpUser(
            @RequestBody RequestUserDto requestUserDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestValidator.validateUserParams(requestUserDto);
        userService.signUp(requestUserDto);
        UsernameDto usernameDto = userService.signIn(requestUserDto, request, response);
        return ResponseEntity.status(CREATED).body(usernameDto);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UsernameDto> signInUser(
            @RequestBody RequestUserDto requestUserDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestValidator.validateUserParams(requestUserDto);
        UsernameDto usernameDto = userService.signIn(requestUserDto, request, response);
        return ResponseEntity.status(OK).body(usernameDto);
    }

    @PostMapping("/sign-out")
    public void signOut() {
        userService.signOut();
    }
}
