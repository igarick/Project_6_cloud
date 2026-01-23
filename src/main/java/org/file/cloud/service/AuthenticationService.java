package org.file.cloud.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.dto.RequestUserDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public Authentication authenticate(RequestUserDto requestUserDto, HttpServletRequest request, HttpServletResponse response) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                requestUserDto.getUsername(), requestUserDto.getPassword());

        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResponse);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
        log.info("User = {} is authenticated", requestUserDto.getUsername());
        return authenticationResponse;
    }
}
