package com.keycloak.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {

    @JsonAlias({"access_token"})
    private String accessToken;

    @JsonAlias({"expires_in"})
    private String expiresIn;

    @JsonAlias({"refresh_token"})
    private String refreshToken;

    @JsonAlias({"refresh_expires_in"})
    private String refreshExpiresIn;

    private String error;

}