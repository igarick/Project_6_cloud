package org.file.cloud.configuration;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfiguration {

//    @Value("${spring.minio.endpoint}")
//    private String endpoint;
//
//    @Value("${spring.minio.access-key}")
//    private String accessKey;
//
//    @Value("${spring.minio.secret-key}")
//    private String secretKey;


    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
//                .endpoint("http://127.0.0.1:9000")
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }
}
