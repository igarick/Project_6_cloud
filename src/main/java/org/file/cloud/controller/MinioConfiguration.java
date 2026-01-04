package org.file.cloud.controller;


import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
//                .endpoint("http://127.0.0.1:9000")
                .credentials("Mminio", "Mpassword")
                .build();
    }
}
