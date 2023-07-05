package com.keycloak.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {

    private String title;
    private String message;
    private Object payload;

    public ApiResponse(String title, String message, Object payload) {
        this.title = title;
        this.message = message;
        this.payload = payload;
    }
}