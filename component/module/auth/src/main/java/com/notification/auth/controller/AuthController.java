package com.notification.auth.controller;

import com.notification.auth.dto.LoginRequestDTO;
import com.notification.auth.dto.TokenResponseDTO;
import com.notification.auth.dto.UserMeDTO;
import com.notification.auth.service.AuthService;
import com.notification.auth.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String TOKEN_TYPE = "Bearer";
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(new TokenResponseDTO(token, TOKEN_TYPE));
    }

    /**
     * Quick endpoint to verify JWT is working and see the current authenticated user.
     * Requires Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication));
    }

}
