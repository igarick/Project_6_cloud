package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioDirectoryService;
import org.file.cloud.service.StorageResourceValidator;
import org.file.cloud.validator.PathValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioDirectoryController {
    private final MinioDirectoryService minioDirectoryService;
    private final StorageResourceValidator storageResourceValidator;


    @GetMapping("/api/directory")
    public void showFolderContents(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {

    }

    @PostMapping("/api/directory")
    public ResponseEntity<ResourceResponseDto> getInfoDirectory(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        if (!path.endsWith("/") || !PathValidator.isValid(path)) {
            log.warn("Invalid or empty path to the new folder: path = {}", path);
            throw new InvalidOrEmptyPathException(ErrorInfo.NEW_FOLDER_PATH_ERROR);
        }
        storageResourceValidator.validateFolderExistence(userDetails.getUsername(), path);
        ResourceResponseDto folderDto = minioDirectoryService.createFolder(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderDto);
    }
}
