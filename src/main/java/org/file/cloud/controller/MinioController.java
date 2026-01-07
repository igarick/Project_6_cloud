package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.folder.EmptyFolderDto;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.FolderException;
import org.file.cloud.exception.path.InvalidOrEmptyPathToNewFolderException;
import org.file.cloud.service.MinioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MinioController {
    private final MinioService minioService;


    private final String VALID_FOLDER_NAME_PATTERN = "^[^\\\\/:*?\"<>|]+$";

    @GetMapping("/api/resource")
    public void getInfo(@RequestParam String path) {
        System.out.println(path);
    }

    @GetMapping("/api/directory")
    public void showFolderContents(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) {

    }

    @PostMapping("/api/directory")
    public ResponseEntity<EmptyFolderDto> getInfoDirectory(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        // проверка на null, пустоту и отсутствие завершающего /
        if (path == null || path.startsWith("/") || !path.endsWith("/") || path.contains("//")) {
            log.warn("Invalid or empty path to the new folder");
            throw new InvalidOrEmptyPathToNewFolderException(ErrorInfo.INVALID_OR_EMPTY_PATH);
        }
        log.warn("PATH = [" + path + "]");

        // разбить путь
        String[] pathSegments = path.split("/");

        // проверка каждого сегмента
        for (String segment : pathSegments) {
            log.info("Path segment - {}", segment);
            if (!segment.matches(VALID_FOLDER_NAME_PATTERN) || segment.isBlank()) {
                log.warn("Invalid or empty path segment to the new folder");
                throw new InvalidOrEmptyPathToNewFolderException(ErrorInfo.INVALID_OR_EMPTY_PATH);
            }
        }

        String s = path.replace("/", "");
        if (s.length() >= 255) {
            log.warn("Invalid or empty path segment to the new folder");
            throw new InvalidOrEmptyPathToNewFolderException(ErrorInfo.CHARACTER_LIMIT_ERROR);
        }

        minioService.validateFolderExists(userDetails.getUsername(), path);
//        minioService.createFolder(userDetails.getUsername(), path);
        EmptyFolderDto infoToResponse = minioService.getInfoToResponse(userDetails.getUsername(), path);
        return ResponseEntity.status(HttpStatus.CREATED).body(infoToResponse);

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
