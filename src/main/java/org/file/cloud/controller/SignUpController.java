package org.file.cloud.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.UserSignInDto;
import org.file.cloud.dto.UserSignUpDto;
import org.file.cloud.dto.UsernameDto;
import org.file.cloud.service.UserService;
import org.file.cloud.validator.RequestValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SignUpController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/auth/sign-up")
    public ResponseEntity<UsernameDto> signUpUser(@RequestBody UserSignUpDto userSignUpDto) {
        RequestValidator.validateSignUpInput(userSignUpDto);
        UsernameDto usernameDto = userService.signUp(userSignUpDto);
        return ResponseEntity.status(CREATED).body(usernameDto);
    }

    @PostMapping("/api/auth/sign-in")
    public ResponseEntity<UsernameDto> signInUser(
            @RequestBody UserSignInDto userSignInDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestValidator.validateSignInInput(userSignInDto);
        UsernameDto usernameDto = userService.signIn(userSignInDto, request, response);
        return ResponseEntity.status(OK).body(usernameDto);
    }

    @GetMapping("/authorised")
    public void authorised(@AuthenticationPrincipal UserDetails userDetails) {
         log.info(userDetails.toString());
    }
}
