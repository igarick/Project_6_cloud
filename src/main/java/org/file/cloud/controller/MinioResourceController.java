package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioDownloadFolderService;
import org.file.cloud.service.MinioShowInfoResourceService;
import org.file.cloud.validator.PathValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioResourceController {
    private final MinioShowInfoResourceService minioShowInfoResourceService;
    private final MinioDownloadFolderService minioDownloadFolderService;

    @GetMapping("/api/resource")
    public ResponseEntity<ResourceResponseDto> findFile(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        if (!PathValidator.isValid(path)) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }

        minioShowInfoResourceService.checkResourceExists(userDetails.getUsername(), path);
        ResourceResponseDto infoToResponse = minioShowInfoResourceService.buildDtoToResponse(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.OK).body(infoToResponse);
    }

    @GetMapping("api/resource/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        minioShowInfoResourceService.checkResourceExists(userDetails.getUsername(), path);

        Path fileName = Paths.get(path).getFileName();
        String contentDisposition;

        if (!path.endsWith("/")) {
//            Path fileName = Paths.get(path).getFileName();
            contentDisposition = String.format("attachment; filename=\"%s\"", fileName);

//            InputStream stream = minioShowInfoResourceService.downloadFile(userDetails.getUsername(), path);
//            log.info("File - {} was downloaded", path);


//            StreamingResponseBody streamingResponseBody = outputStream -> {
//                try (InputStream stream = minioShowInfoResourceService.getFileStream(userDetails.getUsername(), path)) {
//                    stream.transferTo(outputStream);
//                }
//            };


            StreamingResponseBody streamingResponseBody = outputStream -> {
                try (InputStream stream = minioShowInfoResourceService.getFileStream(userDetails.getUsername(), path)) {
                    stream.transferTo(outputStream);
                }
            };

            log.info("File - {} was downloaded", path);


            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(streamingResponseBody);
//                    .body(new InputStreamResource(stream));
        }
//        Path fileName = Paths.get(path).getFileName();
        contentDisposition = String.format("attachment; filename=\"%s.zip\"", fileName);

        StreamingResponseBody streamingResponseBody = outputStream -> {
            minioDownloadFolderService.downloadFolder(userDetails.getUsername(), path, outputStream);
        };
        log.info("Folder - {} was downloaded", path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(streamingResponseBody);

    }
}
