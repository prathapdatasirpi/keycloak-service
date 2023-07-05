package com.keycloak.backend.controller.auth;

import com.keycloak.backend.config.KeycloakPropertyValues;
import com.keycloak.backend.dto.*;
import com.keycloak.backend.service.AuthService;
import com.keycloak.backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import static com.keycloak.backend.utils.AuthUtils.getAdminAccessTokens;
import static com.keycloak.backend.utils.AuthUtils.validatePasswordSecret;
import static com.keycloak.backend.utils.PayloadUtils.*;


@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private KeycloakPropertyValues keycloakPropertyValues;
    private static RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest loginRequest) throws Exception {
        logger.info("Login in user details ={}", loginRequest);
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            MultiValueMap<String, String> payload = getPayloadForLogin(keycloakPropertyValues, username, password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Object> tokenResponse = restTemplate.exchange(keycloakPropertyValues.getAuthUrl(),
                    HttpMethod.POST,
                    entity,
                    Object.class);;
            if (tokenResponse.getStatusCode() != HttpStatus.OK) {
                ApiResponse apiResponse = (ApiResponse)tokenResponse.getBody();
                return ResponseEntity.status(tokenResponse.getStatusCode()).body(new ApiResponse(apiResponse.getTitle(), apiResponse.getMessage(), apiResponse.getPayload()));
            }
            return ResponseEntity.status(200).body(new ApiResponse("Login success!", "You have logged in successfully", tokenResponse.getBody()));
        } catch (Exception ex) {
            logger.error("While login throws exception", ex);
            return ResponseEntity.status(400).body(new ApiResponse("Login Failed!", "Login Failed", ex.getMessage()));
        }
    }

    @PostMapping(value = "/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest registerRequest) throws Exception {
        try {
            String accessToken = getAdminAccessTokens(keycloakPropertyValues);
            if(accessToken == null) {
                return ResponseEntity.status(400).body(new ApiResponse("User Registration Failed",
                        "User Registration Failed", "Admin Access Token is null"));
            }
            JSONObject payload = getPayloadForSignup(registerRequest);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
            String url = keycloakPropertyValues.getQueryUserUrl();
            ResponseEntity<Object> response =  restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(new ApiResponse("User Registered Successfully",
                    "User Registered Successfully", response.getBody()));
        }
        catch (Exception ex) {
            return ResponseEntity.status(400).body(new ApiResponse("User Registration Failed",
                    "User Registration Failed", ex.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Object> validateToken(@RequestBody TokenDto tokenDto ) throws Exception {
        try {
            MultiValueMap<String, String> payload = getAdminDetails(keycloakPropertyValues);
            payload.add("token", tokenDto.getToken());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Object> tokenResponse = restTemplate.exchange(keycloakPropertyValues.getAuthUrl(),
                    HttpMethod.POST,
                    entity,
                    Object.class);;
            return ResponseEntity.status(tokenResponse.getStatusCode()).body(new ApiResponse("Token Validated", "Token Validated", tokenResponse.getBody()));
        }
        catch (Exception ex) {
            return ResponseEntity.status(400).body(new ApiResponse("Token Invalid",
                    "Token Invalid", ex.getMessage()));
        }
    }

    @PostMapping(value = "/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ResetPassword resetPassword) throws Exception {
        try {
            ResponseEntity<Object> validateResponse = validatePasswordSecret(keycloakPropertyValues, resetPassword.getSlug());
            if(validateResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(validateResponse.getStatusCode()).body(new ApiResponse("Reset Password Failed",
                        "Reset Password Failed", validateResponse.getBody()));
            }
            ResponseEntity<Object> response = authService.resetPassword(resetPassword);
            if(response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.ok().body(new ApiResponse("Reset Password",
                        "Reset Password", response.getBody()));
            }
            return ResponseEntity.status(response.getStatusCode()).body(new ApiResponse("Reset Password",
                    "Reset Password", response));
        }
        catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ApiResponse("Reset Password Failed",
                    "Reset Password Failed", ex.getMessage()));
        }
    }

    @GetMapping(value = "/get-password-change-url")
    public ResponseEntity<Object> getPasswordChangeUrl(@RequestParam String username) throws Exception {
        try {
            ResponseEntity<Object> response = authService.getPasswordChangeUrl(username);
            return ResponseEntity.status(response.getStatusCode()).body(new ApiResponse("Password Change Url",
                    "Password Change Url", response));
        }
        catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ApiResponse("Password Change Url Failed",
                    "Password Change Url Failed", ex.getMessage()));
        }
    }

    @GetMapping(value = "/validate-url")
    public void validateUrl(HttpServletResponse httpServletResponse, @RequestParam String slug) throws Exception {
        try {
            ResponseEntity<Object> response = authService.validateUrl(slug);
            if(response.getStatusCode() == HttpStatus.OK) {
                httpServletResponse.sendRedirect("https://www.kingpin.global");
            }
            httpServletResponse.sendRedirect("Invalid Url");
        }
        catch (Exception ex) {
            httpServletResponse.sendRedirect("Invalid Url");
        }
    }
}
