package com.keycloak.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.backend.config.KeycloakPropertyValues;
import com.keycloak.backend.dto.UserDetails;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class UserUtils {

    private static RestTemplate restTemplate = new RestTemplate();
    static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public static ResponseEntity<Object> getUserProfile(KeycloakPropertyValues keycloakPropertyValues, String userId, String accessToken) {
        String url = keycloakPropertyValues.getQueryUserUrl() + "/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
    }
    public static UserDetails getUserByUserField(KeycloakPropertyValues keycloakPropertyValues,String userField, String username, String accessToken) {
        try {
            UserDetails userDetails = new UserDetails();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            String url = keycloakPropertyValues.getQueryUserUrl() + "?" + userField + username;
            HttpEntity<String> entity = new HttpEntity<>("", headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode valuesNode = objectMapper.readTree(response.getBody());
            for (JsonNode node : valuesNode) {
                if (node.get("username").toString().replaceAll("\"", "").compareTo(username) == 0) {
                    userDetails = objectMapper.readValue(node.toString(), UserDetails.class);
                }
            }
            return userDetails;
        }
         catch (Exception e) {
             return new UserDetails();
        }
    }
    public static ResponseEntity<String> getUserProfileInStringFormat(KeycloakPropertyValues keycloakPropertyValues, String userId, String accessToken) {
        String url = keycloakPropertyValues.getQueryUserUrl() + "/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
    public static ResponseEntity<String> updateUserProfile(KeycloakPropertyValues keycloakPropertyValues, String userId, String accessToken, JsonNode payload) {
        String url = keycloakPropertyValues.getQueryUserUrl() + "/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }
}