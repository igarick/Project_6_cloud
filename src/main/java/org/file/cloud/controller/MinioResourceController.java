package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.builder.ResponseDtoBuilder;
import org.file.cloud.controller.swagger.ResourceSwagger;
import org.file.cloud.dto.folder.ResponseResourceDto;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class MinioResourceController implements ResourceSwagger {
    private final MinioResourceService minioResourceService;
    private final ResponseDtoBuilder responseDtoBuilder;
    private final StorageResourceValidator storageResourceValidator;

    @GetMapping
    public ResponseEntity<ResponseResourceDto> getResourceInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        if (!PathValidator.isValidPath(path)) {
            log.error("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), path);
        ResponseResourceDto responseResourceDto = responseDtoBuilder.buildResourceDto(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.OK).body(responseResourceDto);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), path);
        Path fileName = Paths.get(path).getFileName();
        String contentDisposition;
        if (!path.endsWith("/")) {
            contentDisposition = String.format("attachment; filename=\"%s\"", fileName);
        } else {
            contentDisposition = String.format("attachment; filename=\"%s.zip\"", fileName);
        }
        StreamingResponseBody streamingResponseBody = minioResourceService.getFileStream(userDetails.getUsername(), path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(streamingResponseBody);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        if (!PathValidator.isValidPath(path)) {
            log.error("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), path);
        minioResourceService.deleteResource(userDetails.getUsername(), path);
    }

    @GetMapping("/move")
    public ResponseEntity<ResponseResourceDto> moveResource(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestParam String from,
                                                            @RequestParam String to) {
        if (!PathValidator.isValidPath(from) || !PathValidator.isValidPath(to)) {
            log.error("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }
        PathValidator.validateForMove(from, to);
        storageResourceValidator.ensureResourceExists(userDetails.getUsername(), from);
        ResponseResourceDto responseResourceDto = minioResourceService.moveResource(userDetails.getUsername(), from, to);
        return ResponseEntity.ok().body(responseResourceDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseResourceDto>> searchResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String query) {
        if (!PathValidator.isValidPath(query)) {
            log.error("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.SEARCH_QUERY_ERROR);
        }
        List<ResponseResourceDto> responseResourceDtos = minioResourceService.searchResource(userDetails.getUsername(), query);
        return ResponseEntity.ok().body(responseResourceDtos);
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<ResponseResourceDto>> uploadResource(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestPart("object") List<MultipartFile> files,
//                               @RequestParam("path") String path) {
                               @RequestParam("path") String path) {
        if (!PathValidator.isValidPathOrRoot(path)) {    //  || !path.endsWith("/")
            log.error("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.REQUEST_BODY_ERROR);
        }
        List<ResponseResourceDto> responseDto = minioResourceService.uploadResource(userDetails.getUsername(), path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
