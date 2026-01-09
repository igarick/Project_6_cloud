package org.file.cloud.dto.folder;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceResponseDto {
    private String path;
    private String name;
    private Long size;
    private String type;
}
