package org.file.cloud.controller;

import org.file.cloud.dto.UserDto;
import org.file.cloud.validator.RequestValidator;
import org.springframework.web.bind.annotation.*;

public class SignUpController {

    @GetMapping("/auth/sign-up")
    public String showSignUpForm(@ModelAttribute UserDto userDto) {
        return "signUp";
    }

    @PostMapping("/auth/sign-up")
    public void registerUser(@RequestBody UserDto userDto) {
        RequestValidator.validate(userDto);


    }



}
