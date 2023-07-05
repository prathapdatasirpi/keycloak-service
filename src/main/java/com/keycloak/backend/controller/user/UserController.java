package com.keycloak.backend.controller.user;

import com.keycloak.backend.config.KeycloakPropertyValues;
import com.keycloak.backend.dto.ApiResponse;
import com.keycloak.backend.dto.UpdateUser;
import com.keycloak.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import static com.keycloak.backend.utils.AuthUtils.getAdminAccessTokens;

@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private KeycloakPropertyValues keycloakPropertyValues;
    private static RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserService userService;


    @GetMapping(value = "/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable("userId") String userId) {
        try {
            String accessToken = getAdminAccessTokens(keycloakPropertyValues);
            if(accessToken == null) {
                return ResponseEntity.status(400).body(new ApiResponse("Cannot get user details",
                        "Cannot get user details", "Admin Access Token is null"));
            }
            ResponseEntity<Object> response = userService.getUserDetails(userId, accessToken);
            return ResponseEntity.status(response.getStatusCode()).body(new ApiResponse("User fetched successfully",
                        "User fetched successfully", response.getBody()));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(new ApiResponse("Cannot get user details",
                    "Cannot get user details", ex.getMessage()));
        }
    }
    @GetMapping
    public ResponseEntity<Object> getusers() throws Exception {
        try {
            String accessToken = getAdminAccessTokens(keycloakPropertyValues);
            if(accessToken == null) {
                return ResponseEntity.status(400).body(new ApiResponse("Cannot get users",
                        "Cannot get users", "Admin Access Token is null"));
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = keycloakPropertyValues.getQueryUserUrl();
            ResponseEntity<Object> response =  restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(new ApiResponse("Users fetched successfully",
                    "Users fetched successfully", response.getBody()));
        }
        catch (Exception ex) {
            return ResponseEntity.status(400).body(new ApiResponse("Internal Server Error",
                    "Internal Server Error", ex.getMessage()));
        }
    }

//    @PutMapping(value = "/update/{userId}")
//    public ResponseEntity<Object> updateUserD(@PathVariable("userId") String userId, @RequestBody UpdateUser updateUser) {
//        try {
//            System.out.println("Coming here");
//            ResponseEntity<Object> response = userService.updateUserDetails(userId, updateUser);
//            System.out.println(response);
//            return ResponseEntity.status(response.getStatusCode()).body(new ApiResponse("User fetched successfully",
//                    "User fetched successfully", response.getBody()));
//        } catch (Exception ex) {
//            System.out.println("Exception");
//            return ResponseEntity.status(400).body(new ApiResponse("Cannot update user details",
//                    "Cannot update user details", ex.getMessage()));
//        }
//    }

}
