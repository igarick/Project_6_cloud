package org.file.cloud.service.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.configuration.MinioProperties;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.exception.minio.ResourceStorageException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public void uploadFile(InputStream inputStream, String resourceFullPath, String contentType, Long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(resourceFullPath)
                            .stream(
                                    inputStream, size, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error while putting file: resourceFullPath = {}, error: {}", resourceFullPath, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }

    public void moveFile(String from, String to) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(to)
                            .source(
                                    CopySource.builder()
                                            .bucket(minioProperties.bucket())
                                            .object(from)
                                            .build())
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error while coping file: pathTo = {}, error: {}", to, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }

    public GetObjectAttributesResponse getFileAttributes(String fullPath) {
        try {
            return minioClient.getObjectAttributes(
                    GetObjectAttributesArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(fullPath)
                            .objectAttributes(
                                    new String[]{"ObjectSize"
//                                            "ETag", "Checksum", "ObjectParts", "StorageClass", "ObjectSize"
                                    })
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }

    public boolean isFolderExists(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioProperties.bucket())
                .prefix(path)
                .recursive(false)
                .build());
        return results.iterator().hasNext();
    }

    public void deleteFile(String fullPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(fullPath)
                            .build());

        } catch (Exception e) {
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public InputStream getObjectStream(String fullPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(fullPath)
                            .build());
        } catch (Exception e) {
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public Iterable<Result<Item>> getObjects(String fullPath, boolean recursive) {
        try {
            return minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.bucket())
                            .prefix(fullPath)
                            .recursive(recursive)
                            .build());
        } catch (Exception e) {
            log.warn("Unexpected error while getting list objects: path = {}, error: {}", fullPath, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }

    public Iterable<Result<Item>> getObjects(String fullPath) {
        return getObjects(fullPath, true);
    }

    public Iterable<Result<DeleteError>> removeObjects(List<DeleteObject> objects) {
        try {
            return minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(minioProperties.bucket())
                            .objects(objects)
                            .build());
        } catch (Exception e) {
            log.warn("Unexpected error while deleting: error - {}", e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }

    public boolean isFileExists(String fullPath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(fullPath)
                            .build());
            return true;
        } catch (ErrorResponseException e) {
            String code = e.errorResponse().code();
            if ("NoSuchKey".equals(code)) {
                return false;
            }
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR);
        } catch (Exception e) {
            log.warn("Unexpected error for - {}. Error: {}", fullPath, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR);
        }
    }

    public void createFolder(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(path)
                            .stream(InputStream.nullInputStream(), 0, -1)
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error while creating folder - {}. Error: {}", path, e.getMessage(), e);
            throw new ResourceStorageException(ErrorInfo.UNEXPECTED_ERROR, e);
        }
    }
}
