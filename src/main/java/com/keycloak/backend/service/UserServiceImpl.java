package com.keycloak.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.backend.config.KeycloakPropertyValues;
import com.keycloak.backend.dto.ApiResponse;
import com.keycloak.backend.dto.UpdateUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.keycloak.backend.utils.AuthUtils.getAdminAccessTokens;
import static com.keycloak.backend.utils.PayloadUtils.getPayloadForUpdateUser;
import static com.keycloak.backend.utils.UserUtils.*;

@Service
public class UserServiceImpl implements UserService {
    private static RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private KeycloakPropertyValues keycloakPropertyValues;
    static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public ResponseEntity<Object> getUserDetails(String userId, String accessToken) {
        return getUserProfile(keycloakPropertyValues, userId, accessToken);
    }
    public ResponseEntity<Object> updateUserDetails(String userId, UpdateUser updateUser) {
        try {
        String accessToken = getAdminAccessTokens(keycloakPropertyValues);
        if(accessToken == null) {
            return ResponseEntity.status(400).body(new ApiResponse("Cannot update user details",
                    "Cannot update user details", "Admin Access Token is null"));
        }

        ResponseEntity<String> userStringResponse = getUserProfileInStringFormat(keycloakPropertyValues, userId, accessToken);
        JsonNode userAttributes = objectMapper.readTree(userStringResponse.getBody());
        System.out.println(userAttributes);
        if (userAttributes.get("id") == null) {
            return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed", "Api Request Failed", "User not found"));
        }
        JsonNode payload = getPayloadForUpdateUser(userAttributes, updateUser);
        System.out.println(payload);
        ResponseEntity<String> response = updateUserProfile(keycloakPropertyValues, userId, accessToken, payload);
        System.out.println(response);
        return ResponseEntity.status(response.getStatusCode()).body(new ApiResponse("User details updated successfully",
                "User details updated successfully", response.getBody()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ApiResponse("Cannot update user details",
                    "Cannot update user details", e.getMessage()));
        }
    }

}
