package org.file.cloud.controller;

import io.minio.GetObjectAttributesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.validator.PathValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ResourceDto> findFile(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        if (!PathValidator.isValid(path)) {
            log.warn("Invalid or empty path");
            throw new InvalidOrEmptyPathException(ErrorInfo.FILE_PATH_ERROR);
        }

        minioResourceService.validateFolderExists(userDetails.getUsername(), path);
        ResourceDto infoToResponse = minioResourceService.getInfoToResponse(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.OK).body(infoToResponse);

//        GetObjectAttributesResponse resourceAttributes = minioResourceService.getResourceAttributes(userDetails.getUsername(), path);
//        Long l = resourceAttributes.result().objectSize();
//        log.info("Size = {}", l);
    }
}
