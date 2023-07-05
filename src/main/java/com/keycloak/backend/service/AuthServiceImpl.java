package com.keycloak.backend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.backend.config.KeycloakPropertyValues;
import com.keycloak.backend.dto.ApiResponse;
import com.keycloak.backend.dto.ResetPassword;
import com.keycloak.backend.dto.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

import static com.keycloak.backend.utils.AuthUtils.*;
import static com.keycloak.backend.utils.PayloadUtils.getPasswordSecretPayload;
import static com.keycloak.backend.utils.UserUtils.*;

@Service
public class AuthServiceImpl implements AuthService{
    @Autowired
    private KeycloakPropertyValues keycloakPropertyValues;
    private RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public ResponseEntity<Object> resetPassword(ResetPassword resetPassword) {
        String accessToken =  getAdminAccessTokens(keycloakPropertyValues);
        if(accessToken == null){
            return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                    "Api Request Failed", "Admin Access Token is null"));
        }
        String decodedSlug = new String(Base64.getDecoder().decode(resetPassword.getSlug()));
        String[] slugArray = decodedSlug.split("&");
        String userId = slugArray[0];
        String credentials = getCredentialId(keycloakPropertyValues,userId, accessToken);
        if(credentials == null){
            credentials = java.util.UUID.randomUUID().toString();
        }
        return updatePassword(keycloakPropertyValues, resetPassword, credentials, accessToken, userId);
    }
    public ResponseEntity<Object> getPasswordChangeUrl(String username) {
        try {
            String accessToken = getAdminAccessTokens(keycloakPropertyValues);
            UserDetails userDetails = getUserByUserField(keycloakPropertyValues, "username", username, accessToken);
            if (userDetails.getUsername() == null) {
                return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                        "Api Request Failed", "User not found"));
            }
            String passwordSecret = java.util.UUID.randomUUID().toString();
            ResponseEntity<String> userStringResponse = getUserProfileInStringFormat(keycloakPropertyValues, userDetails.getId(), accessToken);
            JsonNode userAttributes = objectMapper.readTree(userStringResponse.getBody());
            if (userAttributes.get("id") == null) {
                return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                        "Api Request Failed", "User not found"));
            }
            JsonNode payload = getPasswordSecretPayload(userAttributes, passwordSecret);
            ResponseEntity<String> response = updateUserProfile(keycloakPropertyValues, userDetails.getId(), accessToken, payload);
            String slug = userDetails.getId() + "&" + passwordSecret;
            String encodedSlug = Base64.getEncoder().encodeToString(slug.getBytes());
            String payloadString = keycloakPropertyValues.getForgotPasswordRedirectUrl() + "?username=" + userDetails.getUsername() +"&slug=" + encodedSlug;

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok().body(new ApiResponse("Password Change Url",
                        "Password Change Url", payloadString));
            }
            return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                    "Api Request Failed", "Password Change Url Failed"));
        }
        catch (Exception e){
            return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                    "Api Request Failed", e.getMessage()));
        }
    }
    public ResponseEntity<Object> validateUrl(String slug) {
        try {
            return validatePasswordSecret(keycloakPropertyValues, slug);
        }
        catch (Exception e){
            return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                    "Api Request Failed", e.getMessage()));
        }
    }
}
