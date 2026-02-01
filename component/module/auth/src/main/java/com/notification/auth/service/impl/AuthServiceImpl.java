package com.notification.auth.service.impl;

import com.notification.auth.dto.UserMeDTO;
import com.notification.auth.exception.BadCredentialsException;
import com.notification.auth.security.JwtService;
import com.notification.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;

    @Value("${app.auth.user}")
    private String configuredUser;

    @Value("${app.auth.password}")
    private String configuredPassword;

    public AuthServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String login(String username, String password) {
        final String normalizedUsername = (username == null) ? "" : username.trim().toLowerCase();
        final String normalizedConfiguredUser = (configuredUser == null) ? "" : configuredUser.trim().toLowerCase();

        if (!normalizedConfiguredUser.equals(normalizedUsername) || !safeEquals(configuredPassword, password)) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return jwtService.generateToken(normalizedUsername);
    }

    @Override
    public UserMeDTO me(Authentication authentication) {
        final String name = (authentication == null || authentication.getName() == null)
                ? ""
                : authentication.getName();
        return new UserMeDTO(name);
    }

    private boolean safeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
