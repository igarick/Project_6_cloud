package org.file.cloud.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
//import org.file.cloud.validation.PasswordMatches;

@Getter
@Builder
@ToString
//@PasswordMatches
public class UserSignUpDto {
    private String username;
    private String password;
    private String confirmPassword;
}
