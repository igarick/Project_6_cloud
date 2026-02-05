package org.file.cloud.controller;

import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersStarter {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1");

    static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:RELEASE.2025-07-23T15-54-02Z");

    static {
        postgres.start();
        minIOContainer.start();
    }
}
