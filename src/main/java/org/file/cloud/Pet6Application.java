package org.file.cloud;

import org.file.cloud.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Pet6Application {

    public static void main(String[] args) {
        SpringApplication.run(Pet6Application.class, args);
    }

}
