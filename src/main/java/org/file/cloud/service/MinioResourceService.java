package org.file.cloud.service;

import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.exception.path.InvalidOrEmptyPathException;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.OutputStream;
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
                Item item = result.get();
                String objectName = item.objectName();
                objects.add(new DeleteObject(objectName));
                log.info("Marked for deletion: {}", objectName);
            }
            Iterable<Result<DeleteError>> removed = minioStorageService.removeObjects(objects);
            for (Result<DeleteError> result : removed) {
                DeleteError error = result.get();
                log.warn("Error in deleting object {}; {}", error.objectName(), error.message());
            }
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

            // если пустая папка - пропускаю
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                if (item.isDir()) {
                    zipOutputStream.putNextEntry(new ZipEntry(objectName + "/"));
                    zipOutputStream.closeEntry();
                    log.info("Adding empty folder Entry name: {} ", objectName);
                    continue;
                }

                // определяю имя для зип файла
                log.info("objectName: {}", objectName);
                String zipEntryName = objectName.substring(fullPath.length());
                log.info("Adding file Entry name: {} ", zipEntryName);

                // записываю энтри в зип поток
                zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));

                // получаю каждый объект в виде потока и передаю в зип поток
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

    public void renameFile(String username, String from, String to) {
//        1) rename file

        // проверить существование папки КУДА (родительской папки)
        storageResourceValidator.validateParentFolderExistence(username, to);

        // проверить существует ли этот файл
        storageResourceValidator.validateFileExistence(username, to);

        // копировать

    }

}


