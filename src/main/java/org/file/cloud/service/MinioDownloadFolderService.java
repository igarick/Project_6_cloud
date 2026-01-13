package org.file.cloud.service;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioDownloadFolderService {
    private final MinioStorageService minioStorageService;

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
}
