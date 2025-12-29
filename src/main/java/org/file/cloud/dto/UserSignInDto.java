package org.file.cloud.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class UserSignInDto {
    private String username;
    private String password;
}
