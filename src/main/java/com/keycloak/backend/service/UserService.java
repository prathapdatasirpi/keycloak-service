package com.keycloak.backend.service;

import com.keycloak.backend.dto.UpdateUser;
import org.springframework.http.ResponseEntity;

public interface UserService {
    public ResponseEntity<Object> getUserDetails(String userId, String accessToken);

    public ResponseEntity<Object> updateUserDetails(String userId, UpdateUser updateUser);
}
