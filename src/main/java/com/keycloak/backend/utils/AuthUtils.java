package com.keycloak.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.backend.config.KeycloakPropertyValues;
import com.keycloak.backend.dto.*;
import net.minidev.json.JSONObject;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

import static com.keycloak.backend.utils.PayloadUtils.*;
import static com.keycloak.backend.utils.UserUtils.*;

public class AuthUtils {
    private static RestTemplate restTemplate = new RestTemplate();

    static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static String getAdminAccessTokens(KeycloakPropertyValues keycloakPropertyValues) {
        try {
            MultiValueMap<String, String> payload = getAdminDetails(keycloakPropertyValues);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Object> keycloakResponse = restTemplate.exchange(keycloakPropertyValues.getAuthUrl(), HttpMethod.POST, entity, Object.class);
            String responseBody = objectMapper.writeValueAsString(keycloakResponse.getBody());
            TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
            return tokenResponse.getAccessToken();
        } catch (HttpClientErrorException | JsonProcessingException ex) {
            return null;
        }
    }

    public static String getCredentialId(KeycloakPropertyValues keycloakPropertyValues, String userid, String accessToken) {
        try {
                String url = keycloakPropertyValues.getQueryUserUrl() + "/" + userid + "/credentials";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(accessToken);
                HttpEntity<String> entity = new HttpEntity<>("", headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                JsonNode valuesNode = objectMapper.readTree(response.getBody());
                    for (JsonNode node : valuesNode) {
                        if (node != null) {
                            return node.get("id").toString();
                        }
                }
            return null;
        }
        catch (Exception ex) {
            return null;
        }
    }
    public static ResponseEntity<Object> updatePassword(KeycloakPropertyValues keycloakPropertyValues, ResetPassword resetPassword, String credentialId, String accessToken, String userId) {
        try {
                String url = keycloakPropertyValues.getQueryUserUrl() + "/" + userId + "/reset-password";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(accessToken);
                JSONObject payload = acquireResetPasswordPayloadString(resetPassword, credentialId);
                HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
        }
        catch (Exception ex) {
            return ResponseEntity.status(400).body(new ApiResponse("Cannot update user", "Cannot update user", ex.getMessage()));
        }
    }
    public static ResponseEntity<Object> validatePasswordSecret(KeycloakPropertyValues keycloakPropertyValues, String slug) {
        try {
            String decodedSlug = new String(Base64.getDecoder().decode(slug));
            String[] slugArray = decodedSlug.split("&");
            String userId = slugArray[0];
            String passwordSecret = slugArray[1];
            String accessToken = getAdminAccessTokens(keycloakPropertyValues);
            ResponseEntity<Object> response = getUserProfile(keycloakPropertyValues, userId, accessToken);
            JsonNode userAttributes = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()));
            if (userAttributes.get("id") == null) {
                return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                        "User not found", "User not found"));
            }
            if (userAttributes.get("attributes").get("passwordSecretExpiry").get(0).asLong() < System.currentTimeMillis()) {
                return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                        "Url Expired", "Url Expired"));
            }
            if(!userAttributes.get("attributes").get("passwordSecret").get(0).toString().replace("\"", "").equals(passwordSecret)) {
                return ResponseEntity.status(400).body(new ApiResponse("Api Request Failed",
                        "Invalid Password Secret", "Invalid Password Secret"));
            }

            return ResponseEntity.ok().body(new ApiResponse("Password Change Url",
                    "Password Change Url Validated", "Password Change Url Validated"));
        }
        catch (Exception ex) {
            return ResponseEntity.status(400).body(new ApiResponse("Cannot validate password secret", "Cannot validate password secret", ex.getMessage()));
        }
    }

}
