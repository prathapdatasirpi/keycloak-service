package com.keycloak.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.keycloak")
@Getter
@Setter
public class KeycloakPropertyValues {

    private String url;
    private String authUrl;
    private String clientId;
    private String clientSecret;
    private String adminClientId;
    private String adminClientSecret;
    private String adminUrl;
    private String queryUserUrl;
    private String forgotPasswordRedirectUrl;
    private String userInviteRedirectUrl;

}
