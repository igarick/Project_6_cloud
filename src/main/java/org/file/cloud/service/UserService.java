package org.file.cloud.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.UserSignInDto;
import org.file.cloud.dto.UserSignUpDto;
import org.file.cloud.dto.UsernameDto;
import org.file.cloud.exception.DuplicateUserException;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Transactional
    public UsernameDto signUp(UserSignUpDto userSignUpDto) {
        User user = User.builder()
                .username(userSignUpDto.getUsername())
                .password(passwordEncoder.encode(userSignUpDto.getPassword()))
                .build();
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Failed to save user = {}. User already exists", userSignUpDto.getUsername());
            throw new DuplicateUserException(ErrorInfo.USERNAME_DUPLICATE_ERROR, e);
        }
        log.info("User = {} saved", userSignUpDto.getUsername());
        return UsernameDto.builder()
                .username(user.getUsername())
                .build();
    }

    public UsernameDto signIn(UserSignInDto userSignInDto, HttpServletRequest request, HttpServletResponse response) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                userSignInDto.getUsername(), userSignInDto.getPassword());
        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResponse);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        log.info("User = {} is authenticated", userSignInDto.getUsername());

        return UsernameDto.builder()
                .username(userSignInDto.getUsername())
                .build();
    }
}
