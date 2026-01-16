package org.file.cloud.service;

import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioResourceService {
    private final MinioStorageService minioStorageService;
    private final PathService pathService;
    private final StorageResourceValidator storageResourceValidator;

    public void deleteResource(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);
        if (resourcePath.endsWith("/")) {
            deleteFolder(fullPath);
            log.info("Folder was deleted: path = {}", fullPath);
        } else {
            minioStorageService.deleteFile(fullPath);
            log.info("File was deleted: path = {}", fullPath);
        }
    }

    public void deleteFolder(String fullPath) {
        try {
            Iterable<Result<Item>> results = minioStorageService.getObjects(fullPath);

            List<DeleteObject> objects = new LinkedList<>();
            for (Result<Item> result : results) {
                String objectName = result.get().objectName();
                objects.add(new DeleteObject(objectName));
                log.info("Marked for deletion: {}", objectName);
            }
            removeObjects(objects, fullPath);
            log.info("Removed folder: path = {}", fullPath);
        } catch (Exception e) {
            log.warn("Unexpected error while deleting folder: path = {}, error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public StreamingResponseBody getFileStream(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);

        if (!resourcePath.endsWith("/")) {
            return outputStream -> {
                try (InputStream stream = minioStorageService.getObjectStream(fullPath)) {
                    stream.transferTo(outputStream);
                }
            };
        } else {
            return outputStream -> {
                downloadFolder(fullPath, outputStream);
            };
        }
    }

    public void downloadFolder(String fullPath, OutputStream outputStream) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            Iterable<Result<Item>> results = minioStorageService.getObjects(fullPath);
            for (Result<Item> result : results) {
                String objectName = result.get().objectName();
                log.info("objectName: {}", objectName);

                if (fullPath.equals(objectName)) {
                    log.info("Skip directory folder: objectName = {}", objectName);
                    continue;
                }
                String zipEntryName = objectName.substring(fullPath.length());
                log.info("Adding file/folder Entry name: {} ", zipEntryName);
                zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));

                try (InputStream stream = minioStorageService.getObjectStream(objectName)) {
                    stream.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            log.info("ZIP completed");
        } catch (Exception e) {
            log.error("Failed to ZIP folder - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }

    public void moveFile(String username, String from, String to) {
//        1) rename file
        log.info("Start coping/renaming file: from = {}, to = {}", from, to);
        String parentFolderPathFrom = pathService.extractParentFolderPath(from);

        // проверить существование папки КУДА (родительской папки)
        storageResourceValidator.validateParentFolderExistence(username, to);
        String parentFolderPathTo = pathService.extractParentFolderPath(to);

        String fullPathFrom = pathService.getFullPath(username, from);
        log.info("Full path From = {}", fullPathFrom);

        // проверить существует ли этот файл ------------------------------=====================------
        storageResourceValidator.ensureFileDoesNotExist(username, to);
        String fullPathTo = pathService.getFullPath(username, to);
        log.info("Full path To = {}", fullPathTo);

        String fileNameFrom = Path.of(from).getFileName().toString();
        String fileNameTo = Path.of(to).getFileName().toString();

        if (!parentFolderPathTo.equals(parentFolderPathFrom) && !fileNameFrom.equals(fileNameTo)) {
            log.info("Relocatable file names are not equal: fileNameFrom = {}, fileNameTo = {}", fileNameFrom, fileNameTo);
            throw new ResourceException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }

        log.info("Copied/Moved file: from = {}, to = {}", fullPathFrom, fullPathTo);
        minioStorageService.renameFile(fullPathFrom, fullPathTo);
        log.info("Deleted file: path = {}", fullPathFrom);
        minioStorageService.deleteFile(fullPathFrom);
    }

    public void moveResource(String username, String from, String to) {
        String parentFolderPathFrom = pathService.extractParentFolderPath(from);

        storageResourceValidator.validateParentFolderExistence(username, to);
        String parentFolderPathTo = pathService.extractParentFolderPath(to);

        String fullPathFrom = pathService.getFullPath(username, from);
        log.info("Full path From = {}", fullPathFrom);

        if (from.endsWith("/") && to.endsWith("/")) {
            storageResourceValidator.ensureFolderDoesNotExist(username, to);
        } else {
            storageResourceValidator.ensureFileDoesNotExist(username, to);
        }

        String fullPathTo = pathService.getFullPath(username, to);
        log.info("Full path To = {}", fullPathTo);

    }

    public void renameFolder(String username, String from, String to) {
        log.info("Start coping folder: from = {}, to = {}", from, to);
        String parentFolderPathFrom = pathService.extractParentFolderPath(from);

        // проверить существование родит папки КУДА
        storageResourceValidator.validateParentFolderExistence(username, to);
        String parentFolderPathTo = pathService.extractParentFolderPath(to);

        String fullPathFrom = pathService.getFullPath(username, from);
        log.info("Full path From = {}", fullPathFrom);

        // проверить существует ли эта папка -------------------------------------==========-------------------
        storageResourceValidator.ensureFolderDoesNotExist(username, to);

        String fullPathTo = pathService.getFullPath(username, to);
        log.info("Full path To = {}", fullPathTo);

        String resourceNameFrom = Path.of(from).getFileName().toString();
        String resourceNameTo = Path.of(to).getFileName().toString();

        // равны ли род папки?

        if (!parentFolderPathTo.equals(parentFolderPathFrom) && !resourceNameFrom.equals(resourceNameTo)) {
            log.info("Relocatable file names are not equal: fileNameFrom = {}, fileNameTo = {}", resourceNameFrom, resourceNameTo);
            throw new ResourceException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
        }

//        if (parentFolderPathTo.equals(parentFolderPathFrom)) {

            // получить список обьектов папки ОТ КУДА
            Iterable<Result<Item>> results = minioStorageService.getObjects(fullPathFrom);

            try {
                List<DeleteObject> objects = new LinkedList<>();
                for (Result<Item> result : results) {
                    String objectName = result.get().objectName();
                    objects.add(new DeleteObject(objectName));
                    log.info("objectName: {}", objectName);

                    if (fullPathFrom.equals(objectName)) {
                        log.info("Skip directory folder: objectName = {}", objectName);
                        continue;
                    }

                    String fileName = objectName.substring(fullPathFrom.length());
                    String newPath = fullPathTo + fileName;
                    log.info("New file name: path = {}", newPath);

                    minioStorageService.renameFile(objectName, newPath);
                    log.info("Copied file: from = {}, to = {}", fullPathFrom, fullPathTo);
                }
                removeObjects(objects, fullPathFrom);
                log.info("Removed folder: path = {}", fullPathFrom);
            } catch (Exception e) {
                log.error("Failed to rename folder: path = {}, error: {}", fullPathFrom, e.getMessage(), e);
                throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
            }
//        } else {
//            log.info("Relocatable folder names are not equal: fileNameFrom = {}, fileNameTo = {}", resourceNameFrom, resourceNameTo);
//            throw new ResourceException(ErrorInfo.INVALID_OR_EMPTY_PATH_ERROR);
//        }



    }

//    public String buildNewPathFile() {
//
//    }

    public void removeObjects(List<DeleteObject> objects, String fullPathFrom) {
        try {
            Iterable<Result<DeleteError>> removed = minioStorageService.removeObjects(objects);
            for (Result<DeleteError> deleteErrorResult : removed) {
                DeleteError error = deleteErrorResult.get();
                log.warn("Error in deleting object {}; {}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            log.error("Failed to remove objects: path = {}, error: {}", fullPathFrom, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }


}


