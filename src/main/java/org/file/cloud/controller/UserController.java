package org.file.cloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.file.cloud.controller.swagger.UserSwagger;
import org.file.cloud.dto.UsernameDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController implements UserSwagger {

    @GetMapping("/me")
    public ResponseEntity<UsernameDto> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Current user = {}", userDetails.getUsername());
        return ResponseEntity.status(OK).body(new UsernameDto(userDetails.getUsername()));
    }
}
