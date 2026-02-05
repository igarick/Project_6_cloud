package org.file.cloud.configuration;

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
    private final MinioProperties minioProperties;
//    private final String BUCKET = "user-files";

    @EventListener(ApplicationReadyEvent.class)
    public void createBucketIfNotExists() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.bucket())
//                .bucket(BUCKET)
                .build())) {
            log.info("Creating bucket - {}", minioProperties.bucket());
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.bucket())
                    .build());
        }
    }
}
