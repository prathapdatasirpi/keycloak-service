package com.keycloak.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPassword {
    private String password;
    private String slug;
}
