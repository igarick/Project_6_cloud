package org.file.cloud.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class UserFilesBucketInitializer {
    private final MinioClient minioClient;
    private final String BUCKET = "user-files";

    @EventListener(ApplicationReadyEvent.class)
    public void createBucketIfNotExists() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(BUCKET)
                .build())) {
            log.info("Creating bucket - {}", BUCKET);
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(BUCKET)
                    .build());
        }
    }
}
