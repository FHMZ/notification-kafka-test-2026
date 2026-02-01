package com.notification.auth.service;

import com.notification.auth.dto.UserMeDTO;
import org.springframework.security.core.Authentication;

public interface AuthService {

    String login(String username, String password);

    UserMeDTO me(Authentication authentication);

}
