package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.builder.ResponseDtoBuilder;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.service.StorageResourceValidator;
import org.file.cloud.validator.PathValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioResourceController {
    private final MinioResourceService minioResourceService;
    private final ResponseDtoBuilder responseDtoBuilder;
    private final StorageResourceValidator storageResourceValidator;

    @GetMapping("/api/resource")
    public ResponseEntity<ResourceResponseDto> showResourceInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        if (!PathValidator.isValid(path)) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), path);
        ResourceResponseDto resourceResponseDto = responseDtoBuilder.buildResourceDto(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.OK).body(resourceResponseDto);
    }

    @GetMapping("/api/resource/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), path);

        Path fileName = Paths.get(path).getFileName();
        String contentDisposition;

        if (!path.endsWith("/")) {
            contentDisposition = String.format("attachment; filename=\"%s\"", fileName);
//            log.info("File - {} was downloaded", path);
        } else {
            contentDisposition = String.format("attachment; filename=\"%s.zip\"", fileName);
//            log.info("Folder - {} was downloaded", path);
        }
        StreamingResponseBody streamingResponseBody = minioResourceService.getFileStream(userDetails.getUsername(), path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(streamingResponseBody);

    }

    @DeleteMapping("/api/resource")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        if (!PathValidator.isValid(path)) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), path);
        minioResourceService.deleteResource(userDetails.getUsername(), path);
    }

    @GetMapping("/api/resource/move")
    public void renameResource(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String from,
                               @RequestParam String to) {
        if (!PathValidator.isValid(from) || !PathValidator.isValid(to)) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        if (from.endsWith("/") && !to.endsWith("/") ||
            (!from.endsWith("/") && to.endsWith("/"))) {
            log.warn("Cannot move: source and target types do not match (from = {}, to = {})", from, to);
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        if (from.endsWith("/") && to.startsWith(from) && !to.equals(from)) {
            log.warn("Cannot move folder into its own subfolder: from = {}, to = {}", from, to);
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), from);
        minioResourceService.moveResource(userDetails.getUsername(), from, to);
    }
}
