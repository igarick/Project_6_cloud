package org.file.cloud.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.UsernameDto;
import org.file.cloud.exception.DaoException;
import org.file.cloud.exception.DuplicateUserException;
import org.file.cloud.exception.ErrorInfo;
import org.file.cloud.model.User;
import org.file.cloud.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRootFolderManager userRootFolderManager;
    private final AuthenticationService authenticationService;
    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    @Transactional
    public void signUp(RequestUserDto requestUserDto) {
        String username = requestUserDto.getUsername();
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(requestUserDto.getPassword()))
                .build();
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Failed to save user - {}. User already exists", username);
            throw new DuplicateUserException(ErrorInfo.USERNAME_DUPLICATE_ERROR, e);
        }
        Long id = user.getId();
        log.info("User id = {}", id);
        userRootFolderManager.createUserRootFolder(user.getId());
        log.info("Saved user = {}", username);
    }

    public UsernameDto signIn(RequestUserDto requestUserDto, HttpServletRequest request, HttpServletResponse response) {
        Authentication authenticate = authenticationService.authenticate(requestUserDto, request, response);

        return UsernameDto.builder()
                .username(authenticate.getName())
                .build();
    }

    public void signOut() {
        securityContextHolderStrategy.clearContext();
    }

    public Long getUserId(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("User - {} not found", username);
            return new DaoException(ErrorInfo.USER_NOT_FOUND);
        });
        return user.getId();
    }
}
