package org.file.cloud.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class RequestUserDto {
    private String username;
    private String password;
}
