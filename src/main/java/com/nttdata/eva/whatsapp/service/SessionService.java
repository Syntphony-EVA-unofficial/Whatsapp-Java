package com.nttdata.eva.whatsapp.service;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.SessionDestination;
import com.nttdata.eva.whatsapp.model.UserSessionData;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SessionService {

    @Autowired
    private RestTemplate restTemplate;

    private final CacheManager cacheManager;

    public SessionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

   

    private String userID;
    private UserSessionData sessionData;

    @Getter

    private BrokerConfiguration brokerConfig;


    public Boolean InitCacheHandover(String userID, BrokerConfiguration brokerConfig) {
        this.userID = userID;
        this.brokerConfig = brokerConfig;
        sessionData = validateUserHandover(userID);
        if (sessionData != null) {
            return true;
        }
        return false;
    }

    public void InitCache(String userID, BrokerConfiguration brokerConfig) {
        this.userID = userID;
        this.brokerConfig = brokerConfig;
        sessionData = cacheManager.getFromCache(userID);
    }


    public UserSessionData validateUserHandover(String userID) {

        if (cacheManager.containsUser(userID)) {
            UserSessionData sessionData = cacheManager.getFromCache(userID);

            if (SessionDestination.HUMAN_AGENT.equals(sessionData.getDestination())) {
                return sessionData;
            }   
        }
    return null;

    }

    public String getUserID() {
        return userID;
    }

    public String getEvaSessionCode() {
        return sessionData.getEvaSessionCode();
    }

    public void setWelcomeback(String welcomeback) {
        sessionData.setWelcomeBack(welcomeback);
    }

    public String getWelcomeback() {
        return sessionData.getWelcomeBack();
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

    public void deleteToken() {
        sessionData.setEvaToken(null);
    }

    private String generateToken() {
        log.info("Enter to function Token Gen TIME: {}", Instant.now());

        try {
            String keycloakUrl = brokerConfig.getEvaConfig().getOrganization().getKeycloak().trim();
            String organization = brokerConfig.getEvaConfig().getOrganization().getName().trim();
            String clientid = brokerConfig.getEvaConfig().getEnvironment().getClientId().trim();
            String secret = brokerConfig.getEvaConfig().getEnvironment().getSecret().trim();

            URI uri = new URI(
                    String.format("%s/auth/realms/%s/protocol/openid-connect/token", keycloakUrl, organization).trim());
            restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

            final MultiValueMap<String, String> formVars = new LinkedMultiValueMap<>();
            formVars.set("grant_type", "client_credentials");
            formVars.set("client_id", clientid);
            formVars.set("client_secret", secret);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            final Map<String, String> mapResult = restTemplate.postForObject(uri.toString(), formVars, Map.class);

            if (mapResult != null && mapResult.containsKey("access_token")) {
                log.info("Token generated successfully");
                sessionData.deleteSessionCode();
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

    public void setDestination(SessionDestination destination) {
        sessionData.setDestination(destination);
    }

    public void setExitWord(String exitWord) {
        sessionData.setExitWord(exitWord);
    }

    public String getExitWord() {
        return sessionData.getExitWord();
    }

    public SessionDestination getDestination() {
        return sessionData.getDestination();
    }

    @Override
    public String toString() {
        return String.format("SessionService{destination=%s, exitWord='%s', ID=%s, sessionCode=%s}",
                sessionData.getDestination(), sessionData.getExitWord(), userID, sessionData.getEvaSessionCode());
    }

}