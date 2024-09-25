package com.nttdata.eva.whatsapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonTypeInfo.None;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.nttdata.eva.whatsapp.model.UserSessionData;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.net.URISyntaxException;

@Slf4j
@Service
public class SessionService {

    private final CacheManager cacheManager;

    public SessionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


    @Value("${eva.env.client.id}")
    private String clientId;

    @Value("${eva.env.secret}")
    private String clientSecret;

    @Value("${eva.org.keycloak}")
    private String keycloakUrl;

    @Value("${eva.org.name}")
    private String organization;

    private String userID;
    private UserSessionData sessionData;

    public void InitCache(String userID) {
        this.userID = userID;
        sessionData = cacheManager.getFromCache(userID);
        log.info("UserID set to: {}", userID);
    }
    
    public String getUserID() {
        return userID;
    }

    public String getEvaSessionCode() {
        return sessionData.getEvaSessionCode();
    }

    public String getEvaToken() {
        if (sessionData.getEvaToken() == null
                || Instant.now().minusSeconds(850).isAfter(sessionData.getEvaTokenTimestamp())) {
            sessionData.setEvaToken(generateToken());
            updateTokenTimestamp();
        }

        return sessionData.getEvaToken();
    }

    public void setEvaSessionCode(String sessionCode) {

        sessionData.setEvaSessionCode(sessionCode);
    }

    private String generateToken() {
        log.info("Enter to function Token Gen TIME: {}", Instant.now());

        try {

            URI uri = new URI(
            String.format("%s/auth/realms/%s/protocol/openid-connect/token", keycloakUrl, organization).trim());
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

            final MultiValueMap<String, String> formVars = new LinkedMultiValueMap<>();
            formVars.set("grant_type", "client_credentials");
            formVars.set("client_id", clientId.trim());
            formVars.set("client_secret", clientSecret.trim());

            HttpHeaders headers = new HttpHeaders();    
            headers.set("Content-Type", "application/x-www-form-urlencoded");


            final Map<String, String> mapResult = restTemplate.postForObject(uri.toString(), formVars, Map.class);
                    

            if (mapResult != null && mapResult.containsKey("access_token")) {
                log.info("Token generated successfully");
                return mapResult.get("access_token");
            } else {
                log.error("Failed to generate token: No access token in response");
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to generate token", e);
            return null;
        }
    }

    void updateTokenTimestamp() {
        sessionData.setEvaTokenTimestamp(Instant.now());
    }

    
      
      public void saveSession() {
        cacheManager.addToCache(userID, sessionData.clone());
        log.info("Session saved for UserID: {}", userID);
      }
     

}