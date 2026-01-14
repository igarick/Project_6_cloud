package org.file.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.builder.ResponseDtoBuilder;
import org.file.cloud.dto.folder.ResourceResponseDto;
import org.file.cloud.service.minio.MinioStorageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioDirectoryService {
    private final MinioStorageService minioStorageService;
    private final PathService pathService;
    private final ResponseDtoBuilder responseDtoBuilder;

    public ResourceResponseDto createFolder(String username, String resourcePath) {
        String fullPath = pathService.getFullPath(username, resourcePath);
        minioStorageService.createFolder(fullPath);
        return responseDtoBuilder.buildFolderDto(resourcePath);
    }





}
