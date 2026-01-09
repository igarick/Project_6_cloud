package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioShowInfoResourceService;
import org.file.cloud.validator.PathValidator;
import org.file.cloud.validator.RequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioDeleteResourceController {
    private final MinioShowInfoResourceService minioShowInfoResourceService;

    @DeleteMapping("/api/resource")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        if (!PathValidator.isValid(path)) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }

        minioShowInfoResourceService.validateResourceExists(userDetails.getUsername(), path);
        minioShowInfoResourceService.deleteResource(userDetails.getUsername(), path);
        log.info("Resource - {} deleted", path);
    }
}
