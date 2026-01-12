package org.file.cloud.service.minio;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {
    private final MinioClient minioClient;

    private final String MAIN_BUCKET = "user-files";

    public boolean isFolderExists(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(MAIN_BUCKET)
                .prefix(path)
                .recursive(false)
                .build());
        return results.iterator().hasNext();
    }

    public InputStream getObjectStream(String fullPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .object(fullPath)
                            .build());
        } catch (Exception e) {
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public Iterable<Result<Item>> getObjects(String fullPath) {
        try {
            return minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .prefix(fullPath)
                            .recursive(true)
                            .build());
        } catch (Exception e) {
            log.warn("Unexpected error while getting list objects: path = {}, error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public Iterable<Result<DeleteError>> removeObjects(List<DeleteObject> objects) {
        try {
            return minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .objects(objects)
                            .build());
        } catch (Exception e) {
            log.warn("Unexpected error while deleting: error - {}", e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public boolean isFileExists(String fullPath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .object(fullPath)
                            .build());
        } catch (ErrorResponseException e) {
            handleErrorResponseException(e, fullPath);
        } catch (Exception e) {
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR);
        }
        return true;
    }

    private void handleErrorResponseException(ErrorResponseException e, String fullPath) {
        String code = e.errorResponse().code();
        if ("NoSuchKey".equals(code)) {
            log.warn("Resource (FILE) not found: path = {}", fullPath);
            throw new ResourceException(ErrorInfo.RESOURCE_NOT_FOUND);
        }
        log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
        throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
    }

    public void createFolder(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(MAIN_BUCKET)
                            .object(path)
                            .stream(InputStream.nullInputStream(), 0, -1)
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error while creating folder - {}. Error: {}", path, e.getMessage(), e);
            throw new ResourceException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }
}
