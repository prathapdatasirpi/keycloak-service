package com.keycloak.backend.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

import static com.nimbusds.oauth2.sdk.http.HTTPRequest.Method.POST;
import static com.nimbusds.oauth2.sdk.http.HTTPRequest.Method.PUT;
import static jdk.dynalink.StandardOperation.GET;
import static org.keycloak.events.admin.OperationType.DELETE;



@Configuration
@ConfigurationProperties(prefix = "spring.keycloak")
@KeycloakConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({KeycloakSpringBootConfigResolver.class})
public class KeycloakSecurityConfiguration {

    private static final String CORS_ALLOWED_HEADERS = "origin,content-type,accept,x-requested-with,Authorization";

    private long corsMaxAge = 60;

    private static final Logger logger = LoggerFactory.getLogger(KeycloakSecurityConfiguration.class);

    @Autowired
    private KeycloakPropertyValues keycloakPropertyValues;

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception  {
        logger.info("Inside configure method");
            http.authorizeRequests(authz -> authz.requestMatchers("/sample/*")
                        .authenticated().requestMatchers("/auth/*").permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt().jwtAuthenticationConverter(jwtAuthenticationConverter()));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(keycloakPropertyValues.getUrl());
    }

    @Bean
    public CustomJwtAuthenticationConverter jwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter(keycloakPropertyValues.getClientId());
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        /* @formatter:off */
        return (web) -> web.ignoring()
                .requestMatchers("/js/**")
                .and()
                .ignoring()
                .requestMatchers("/css/**")
                .and()
                .ignoring()
                .requestMatchers("/images/**")
                .and()
                .ignoring()
                .requestMatchers("/html/**")
                .and()
                .ignoring()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .and()
                .ignoring()
                .requestMatchers("/web")
                .and()
                .ignoring()
                .requestMatchers("/")
                .and()
                .ignoring()
                .requestMatchers("/auth/**")
                .and()
                .ignoring()
                .requestMatchers("/");
        /* @formatter:on */
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(
                Arrays.asList(GET.name(), POST.name(), PUT.name(), DELETE.name()));
        configuration.setAllowedHeaders(Arrays.asList(CORS_ALLOWED_HEADERS.split(",")));
        configuration.setMaxAge(corsMaxAge);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

