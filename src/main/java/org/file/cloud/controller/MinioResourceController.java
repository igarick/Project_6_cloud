package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.validator.PathValidator;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioResourceController {
    private final MinioResourceService minioResourceService;

    @GetMapping("/api/resource")
    public void findFile(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        if (path.endsWith("/")) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.RESOURCE_NOT_FOUND);
        }
        PathValidator.isValid(path);


    }
}
