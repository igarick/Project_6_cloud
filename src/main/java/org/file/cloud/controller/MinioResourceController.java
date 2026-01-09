package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioShowInfoResourceService;
import org.file.cloud.validator.PathValidator;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioResourceController {
    private final MinioShowInfoResourceService minioShowInfoResourceService;

    @GetMapping("/api/resource")
    public ResponseEntity<ResourceResponseDto> findFile(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        if (!PathValidator.isValid(path)) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }

        minioShowInfoResourceService.validateResourceExists(userDetails.getUsername(), path);
        ResourceResponseDto infoToResponse = minioShowInfoResourceService.buildDtoToResponse(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.OK).body(infoToResponse);
    }

    @GetMapping("api/resource/download")
    public ResponseEntity<InputStreamResource> downloadResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        minioShowInfoResourceService.validateResourceExists(userDetails.getUsername(), path);
        InputStream stream = minioShowInfoResourceService.downloadFile(userDetails.getUsername(), path);
        log.info("File - {} was downloaded", path);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM).body(new InputStreamResource(stream));
    }
}
