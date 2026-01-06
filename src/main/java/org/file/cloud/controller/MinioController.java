package org.file.cloud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.FolderException;
import org.file.cloud.exception.path.InvalidOrEmptyPathToNewFolderException;
import org.file.cloud.service.MinioService;
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
    public void getInfoDirectory(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path) throws Exception {
        // проверка на null, пустоту и отсутствие завершающего /
        if (path == null || path.isEmpty() || !path.endsWith("/")) {
            throw new InvalidOrEmptyPathToNewFolderException(ErrorInfo.INVALID_OR_EMPTY_PATH);
        }
        log.warn("PATH = [" + path + "]");

        // разбить путь
        String[] folders = path.split("/");

        // проверка каждого сегмента
        for (String folder : folders) {
//            if (folder.isEmpty()) continue;
            if (!folder.matches(VALID_FOLDER_NAME_PATTERN)) {
                throw new InvalidOrEmptyPathToNewFolderException(ErrorInfo.INVALID_OR_EMPTY_PATH);
//                throw new FolderException(ErrorInfo.INVALID_FOLDER_NAME);
            }
        }

        log.info(Arrays.toString(folders));

        // собрать родительский путь
        StringBuilder parentPath = new StringBuilder();
        if (folders.length == 1) {
            parentPath = new StringBuilder(path);
        } else if (folders.length >= 2) {
            String[] parentFolders = Arrays.copyOfRange(folders, 0, folders.length - 1);
            for (String parentFolder : parentFolders) {
                parentPath.append(parentFolder).append("/");
            }
        }
        log.info("Parent path = {}", parentPath);

        boolean isParentFolderExists = minioService.checkFolderExists(userDetails.getUsername(), parentPath.toString());
        boolean folderAlreadyExists = minioService.isFolderAlreadyExists(userDetails.getUsername(), path);

        if (folderAlreadyExists) {
            throw new FolderException(ErrorInfo.PARENT_FOLDER_DOES_NOT_EXIST);
        }

        if (isParentFolderExists) {
            minioService.createFolder(userDetails.getUsername(), path);
        }


//        for (String folder : folders) {
//            if (folder.matches(FOLDER_NAME_PATTERN)) {
//                throw new IncorrectOrEmptyPathToNewFolderException(ErrorInfo.INCORRECT_OR_EMPTY_PATH);
//            }
//        }

//----------------------------------------
//        if (path == null || path.isBlank() || !path.endsWith("/")) {
//            throw new IncorrectOrEmptyPathToNewFolderException(ErrorInfo.INCORRECT_OR_EMPTY_PATH);
//        }
//
// -------------------------- CREATE FOLDER-----------------------------
//        minioService.createFolder(userDetails.getUsername(), path);
//        System.out.println(path);
    }

}
