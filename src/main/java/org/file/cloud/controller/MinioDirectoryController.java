package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioDirectoryController {
    private final MinioDirectoryService minioDirectoryService;
    private final StorageResourceValidator storageResourceValidator;


    @GetMapping("/api/directory")
    public ResponseEntity<List<ResponseResourceDto>> showFolderContent(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {
//        if (!path.endsWith("/") || !PathValidator.isValid(path)) {
//        if (!PathValidator.isValid(path)) {
//        if (!path.isEmpty()) {
//
//            log.warn("Invalid or empty path: path = {}", path);
//            throw new InvalidOrEmptyPathException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
//        }
        boolean folderExists = storageResourceValidator.isFolderExists(userDetails.getUsername(), path);
        if (!folderExists) {
            log.error("Folder does not exist: path = {}", path);
            throw new ResourceException(ErrorInfo.FOLDER_DOES_NOT_EXIST);
        }
        List<ResponseResourceDto> responseDto = minioDirectoryService.showFolderContent(userDetails.getUsername(), path);
        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping("/api/directory")
    public ResponseEntity<ResponseResourceDto> createFolder(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        if (!path.endsWith("/") || !PathValidator.isValid(path)) {
            log.warn("Invalid or empty path to the new folder: path = {}", path);
            throw new InvalidOrEmptyPathException(ErrorInfo.NEW_FOLDER_PATH_ERROR);
        }
        storageResourceValidator.validateFolderExistence(userDetails.getUsername(), path);
        ResponseResourceDto folderDto = minioDirectoryService.createFolder(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderDto);
    }
}
