package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.ResourceDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.MinioDirectoryService;
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

    private final String VALID_FOLDER_NAME_PATTERN = "^[^\\\\/:*?\"<>|]+$";


    @GetMapping("/api/directory")
    public void showFolderContents(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {

    }

    @PostMapping("/api/directory")
    public ResponseEntity<ResourceDto> getInfoDirectory(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        if (!path.endsWith("/") || !PathValidator.isValid(path)) {
            throw new InvalidOrEmptyPathException(ErrorInfo.NEW_FOLDER_PATH_ERROR);
        }
        minioDirectoryService.validateFolderExists(userDetails.getUsername(), path);
        ResourceDto folderDto = minioDirectoryService.createFolder(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderDto);

        // собрать родительский путь
//        StringBuilder parentPath = new StringBuilder();
//        if (pathSegments.length == 1) {
////            parentPath = new StringBuilder(path);
//            parentPath = new StringBuilder();
//        } else if (pathSegments.length >= 2) {
//            String[] parentFolders = Arrays.copyOfRange(pathSegments, 0, pathSegments.length - 1);
//            for (String parentFolder : parentFolders) {
//                parentPath.append(parentFolder).append("/");
//            }
//        }
//        log.info("Parent path = {}", parentPath);



//        String parentPath;
//        int lastSlash = path.lastIndexOf("/", path.length() - 2);
//        if (lastSlash == -1) {
//            parentPath = "";
//        } else {
//            parentPath = path.substring(0, lastSlash + 1);
//        }
//
//        log.info("Parent path = {}", parentPath);

//        boolean isParentFolderExists = minioService.checkParentFolderExists(userDetails.getUsername(), parentPath);

//        boolean folderAlreadyExists = minioService.isFolderAlreadyExists(userDetails.getUsername(), path);
//
//        if (!folderAlreadyExists) {
//            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
//        }

//        if (isParentFolderExists) {
//            minioService.createFolder(userDetails.getUsername(), path);
//        }

    }

}
