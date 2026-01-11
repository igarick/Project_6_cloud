package org.file.cloud.service;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioDownloadFolderService {
    private final MinioShowInfoResourceService minioShowInfoResourceService;
    private final MinioClient minioClient;

    private final String MAIN_BUCKET = "user-files";
    private final String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    public void downloadFolder(String username, String resourcePath,OutputStream outputStream) {
        String userRootFolder = minioShowInfoResourceService.getUserRootFolder(username);
        String fullPath = userRootFolder + resourcePath;
        // если заканчивается на "/"
        // получаю список объектов внутри папки
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .prefix(fullPath)
                            .recursive(true)
                            .build());

            // если пустая папка - пропускаю
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) continue;

                // определяю имя для зип файла
                String objectName = item.objectName();
                String zipEntryName = objectName.substring(fullPath.length());
                log.info("Zip Entry name: {} ", zipEntryName);

                // записываю энтри в зип поток
                zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));

                // получаю каждый объект в виде потока и передаю в зип поток
                try (InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(MAIN_BUCKET)
                                .object(objectName)
                                .build())) {
                    stream.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
                log.info("Stream for Entry - {} closed", objectName);
            }
            zipOutputStream.finish();
        } catch (Exception e) {
            log.error("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }
}
