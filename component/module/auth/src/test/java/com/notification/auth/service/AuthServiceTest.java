package com.notification.auth.service;

import com.notification.auth.dto.UserMeDTO;
import com.notification.auth.exception.BadCredentialsException;
import com.notification.auth.security.JwtService;
import com.notification.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setup() {
        authService = new AuthServiceImpl(jwtService);

        // campos @Value do AuthServiceImpl
        ReflectionTestUtils.setField(authService, "configuredUser", "admin@company.com");
        ReflectionTestUtils.setField(authService, "configuredPassword", "admin123");
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        when(jwtService.generateToken("admin@company.com")).thenReturn("token");

        String token = authService.login("ADMIN@company.com", "admin123");

        assertEquals("token", token);
        verify(jwtService).generateToken("admin@company.com");
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    void login_shouldThrowAuthException_whenCredentialsInvalid() {
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login("admin@company.com", "wrong")
        );

        assertEquals("Invalid username or password", ex.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    void me_shouldReturnUsername_fromAuthentication() {
        var authentication = new UsernamePasswordAuthenticationToken("admin@company.com", null);

        UserMeDTO me = authService.me(authentication);

        assertEquals("admin@company.com", me.username());
        verifyNoInteractions(jwtService);
    }

}
