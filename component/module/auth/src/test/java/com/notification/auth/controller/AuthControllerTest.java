package com.notification.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.auth.controller.advice.AuthControllerAdvice;
import com.notification.auth.dto.LoginRequestDTO;
import com.notification.auth.exception.BadCredentialsException;
import com.notification.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setup() {
        AuthController controller = new AuthController(authService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new AuthControllerAdvice())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        when(authService.login("admin@company.com", "admin123"))
                .thenReturn("jwt-token");

        var request = new LoginRequestDTO("admin@company.com", "admin123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_shouldReturn400_whenBodyIsInvalid() throws Exception {
        var request = new LoginRequestDTO("", ""); // @NotBlank deve falhar

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void login_shouldReturn401_whenServiceThrowsBadCredentialsException() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        var request = new LoginRequestDTO("admin@company.com", "wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void me_shouldReturn200_whenAuthenticated() throws Exception {
        // Controller usa Authentication como argumento -> passamos um Authentication de verdade
        var auth = new UsernamePasswordAuthenticationToken("admin@company.com", null);

        // AuthService.me(Authentication) deve retornar o DTO
        when(authService.me(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.notification.auth.dto.UserMeDTO("admin@company.com"));

        mockMvc.perform(get("/auth/me")
                        .with(authentication(auth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("admin@company.com"));
    }

}
