package com.keycloak.backend.service;

import com.keycloak.backend.dto.ResetPassword;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<Object> resetPassword(ResetPassword resetPassword);

    ResponseEntity<Object> getPasswordChangeUrl(String username);

    ResponseEntity<Object> validateUrl(String slug);
}
