package org.file.cloud.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFilesBucketInitializer {
    private final MinioClient minioClient;

    @EventListener(ApplicationReadyEvent.class)
    public void createBucketIfNotExists() throws Exception {
        String bucket = "user-files";

        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucket)
                .build())) {
            log.info("Creating bucket - {}", bucket);
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucket)
                    .build());
        }
    }
}
