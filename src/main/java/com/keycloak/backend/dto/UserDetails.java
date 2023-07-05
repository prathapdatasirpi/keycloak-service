package com.keycloak.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserDetails {
    @JsonAlias({"id"})
    private String id;
    @JsonAlias({"username"})
    private String username;
    @JsonAlias({"emailVerified"})
    private boolean emailVerified;
    @JsonAlias({"enabled"})
    private boolean enabled;
    @JsonAlias({"firstName"})
    private String firstName;
    @JsonAlias({"lastName"})
    private String lastName;
    @JsonAlias({"email"})
    private String email;
    @JsonAlias({"passwordSecret"})
    private String passwordSecret;

    @JsonAlias({"organizations"})
    private String organizations;

    @JsonAlias({"mobileNumber"})
    private String mobileNumber;

    @JsonAlias({"inviteSecret"})
    private String inviteSecret;

    private String role;

    @JsonProperty("attributes")
    private void unpackPasswordSecretFromNestedObject(Map<String, Object> attributes) {
        List<String> passwordSecret = (List<String>) attributes.get("passwordSecret");
        List<String> organizations = (List<String>) attributes.get("organizations");
        List<String> inviteSecret = (List<String>) attributes.get("inviteSecret");
        List<String> mobileNumber = (List<String>) attributes.get("mobileNumber");
        if (passwordSecret != null)
            this.setPasswordSecret(passwordSecret.get(0));
        if (organizations != null)
            this.setOrganizations(organizations.get(0));
        if (inviteSecret != null)
            this.setInviteSecret(inviteSecret.get(0));
        if (mobileNumber != null)
            this.setMobileNumber(mobileNumber.get(0));
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", emailVerified=" + emailVerified +
                ", enabled=" + enabled +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", passwordSecret='" + passwordSecret + '\'' +
                ", organizations=" + organizations +
                ", inviteSecret='" + inviteSecret + '\'' +
                '}';
    }
}

