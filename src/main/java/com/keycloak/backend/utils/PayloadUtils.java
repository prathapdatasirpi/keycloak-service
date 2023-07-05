package com.keycloak.backend.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.keycloak.backend.config.KeycloakPropertyValues;
import com.keycloak.backend.dto.*;
import net.minidev.json.JSONObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PayloadUtils {
    static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static MultiValueMap<String, String>  getPayloadForLogin(KeycloakPropertyValues keycloakPropertyValues, String userName, String password) {
        MultiValueMap<String, String> loginDetailsMap = getClientDetails(keycloakPropertyValues) ;
        loginDetailsMap.add("username", userName);
        loginDetailsMap.add("password", password);
        return loginDetailsMap;
    }
    public static MultiValueMap<String, String> getClientDetails(KeycloakPropertyValues keycloakPropertyValues) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", keycloakPropertyValues .getClientId());
        map.add("client_secret", keycloakPropertyValues.getClientSecret());
        map.add("grant_type", Constants.GRANT_TYPE_PASSWORD);
        return  map;
    }


    public static MultiValueMap<String, String> getAdminDetails(KeycloakPropertyValues keycloakPropertyValues) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", keycloakPropertyValues.getAdminClientId());
        map.add("client_secret", keycloakPropertyValues.getAdminClientSecret());
        map.add("grant_type", Constants.GRANT_TYPE_CLIENT_CREDENTIALS);
        return map;
    }

    public static  JSONObject getPayloadForSignup(RegisterRequest registerRequest) {
        JSONObject payload = new JSONObject();
        JSONObject credentials = new JSONObject();
        List<JSONObject> credentialsList = new ArrayList<>();
        payload.put("emailVerified", true);
        payload.put("enabled", true);
        payload.put("email", registerRequest.getEmail());
        payload.put("username", registerRequest.getUserName());
        payload.put("firstName", registerRequest.getFirstName());
        payload.put("lastName", registerRequest.getLastName());
        credentials.put("value", registerRequest.getPassword());
        credentialsList.add(credentials);
        payload.put("credentials", credentialsList);
        return payload;
    }

    public static JSONObject acquireResetPasswordPayloadString(
            ResetPassword resetPassword,
            String credentialId) {
        JSONObject payload = new JSONObject();
        payload.put("id", credentialId.replaceAll("\"", ""));
        payload.put("type", Constants.GRANT_TYPE_PASSWORD);
        payload.put("temporary", false);
        payload.put("value", resetPassword.getPassword());
        return payload;
    }
    public static JsonNode getPasswordSecretPayload(JsonNode existingAttributes, String uuid) {
        ObjectNode payload = objectMapper.createObjectNode();
        JsonNode attributes = existingAttributes.get("attributes");
        if(attributes == null) {
            attributes = objectMapper.createObjectNode();
        }
        ((ObjectNode)attributes).put("passwordSecret", uuid);
        Long instant = Instant.now().toEpochMilli() + 1000 * 60 * 60;
        ((ObjectNode)attributes).put("passwordSecretExpiry", instant);
        payload.set("attributes", attributes);
        return payload;
    }
    public static JsonNode getPayloadForUpdateUser(JsonNode existingAttributes, UpdateUser updateUser) {
        ObjectNode payload = objectMapper.createObjectNode();
        JsonNode attributes = existingAttributes.get("attributes");
        if(attributes == null) {
            attributes = objectMapper.createObjectNode();
        }
        ((ObjectNode)attributes).put("firstName", updateUser.getFirstName());
        ((ObjectNode)attributes).put("lastName", updateUser.getLastName());
        payload.set("attributes", attributes);
        return payload;
    }
}
