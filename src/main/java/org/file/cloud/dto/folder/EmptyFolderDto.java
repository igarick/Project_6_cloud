package org.file.cloud.dto.folder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmptyFolderDto {
    private String path;
    private String name;
    private String type;
}
