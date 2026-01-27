package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.doc.DirectorySwagger;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioDirectoryService;
import org.file.cloud.service.StorageResourceValidator;
import org.file.cloud.validator.PathValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directory")
public class MinioDirectoryController implements DirectorySwagger {
    private final MinioDirectoryService minioDirectoryService;
    private final StorageResourceValidator storageResourceValidator;

    @GetMapping
    public ResponseEntity<List<ResponseResourceDto>> getFolderContents(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        if (!PathValidator.isValidPathOrRoot(path)) {
            log.warn("Invalid or empty path: path = {}", path);
            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }

        boolean folderExists = storageResourceValidator.isFolderExists(userDetails.getUsername(), path);
        if (!folderExists) {
            log.error("Folder does not exist: path = {}", path);
            throw new ResourceException(ErrorInfo.FOLDER_DOES_NOT_EXIST);
        }
        List<ResponseResourceDto> responseDto = minioDirectoryService.showFolderContent(userDetails.getUsername(), path);
        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> createFolder(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
        if (!path.endsWith("/") || !PathValidator.isValidPath(path)) {
            log.warn("Invalid or empty path to the new folder: path = {}", path);
            throw new InvalidOrEmptyPathException(ErrorInfo.NEW_FOLDER_PATH_ERROR);
        }
        storageResourceValidator.validateFolderExistence(userDetails.getUsername(), path);
        ResponseResourceDto folderDto = minioDirectoryService.createFolder(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderDto);
    }
}
