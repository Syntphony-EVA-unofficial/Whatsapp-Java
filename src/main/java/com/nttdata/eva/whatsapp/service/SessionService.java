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

   

    //private String userID;
    //private UserSessionData sessionData;

    @Getter
    private BrokerConfiguration brokerConfig;


    public Boolean InitCacheHandover(String userID, BrokerConfiguration brokerConfig) {
        this.brokerConfig = brokerConfig;

        return validateUserHandover(userID);
    }

    public void InitCache(BrokerConfiguration brokerConfig) {
        this.brokerConfig = brokerConfig;
    }


    public Boolean validateUserHandover(String userID) {

        if (cacheManager.containsUser(userID)) {
            UserSessionData sessionData = cacheManager.getFromCache(userID);

            if (SessionDestination.HUMAN_AGENT.equals(sessionData.getDestination())) {
                return true;
            }   
        }
        return false;

    }

    
    public String getEvaSessionCode(String userID) {
        return cacheManager.getFromCache(userID).getEvaSessionCode();
    }

    public void setWelcomeback(String welcomeback, String userID) {
        cacheManager.getFromCache(userID).setWelcomeBack(welcomeback);
    }

    public String getWelcomeback(String userID) {
        return cacheManager.getFromCache(userID).getWelcomeBack();
    }

    public String getEvaToken(String userID) {
        if (cacheManager.getFromCache(userID).getEvaToken() == null
                || Instant.now().minusSeconds(850).isAfter(cacheManager.getFromCache(userID).getEvaTokenTimestamp())) {
            cacheManager.getFromCache(userID).setEvaToken(generateToken(userID));
            updateTokenTimestamp(userID);
        }

        return cacheManager.getFromCache(userID).getEvaToken();
    }

    public void setEvaSessionCode(String sessionCode, String userID) {

        cacheManager.getFromCache(userID).setEvaSessionCode(sessionCode);
    }

    public void deleteToken(String userID) {
        cacheManager.getFromCache(userID).setEvaToken(null);
    }

    private String generateToken(String userID) {
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
                cacheManager.getFromCache(userID).deleteSessionCode();
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

    void updateTokenTimestamp(String userID) {
        cacheManager.getFromCache(userID).setEvaTokenTimestamp(Instant.now());
    }

    public void saveSession(String userID) {
        cacheManager.addToCache(userID, cacheManager.getFromCache(userID).clone());
        log.info("Session saved for UserID: {}", userID);
    }

    public void setDestination(SessionDestination destination, String userID) {
        cacheManager.getFromCache(userID).setDestination(destination);
    }

    public void setExitWord(String exitWord, String userID) {
        cacheManager.getFromCache(userID).setExitWord(exitWord);
    }

    public String getExitWord(String userID) {
        return cacheManager.getFromCache(userID).getExitWord();
    }

    public SessionDestination getDestination(String userID) {
        return cacheManager.getFromCache(userID).getDestination();
    }

    public String toString(String userID) {
        return String.format("SessionService{destination=%s, exitWord='%s', ID=%s, sessionCode=%s}",
                cacheManager.getFromCache(userID).getDestination(), cacheManager.getFromCache(userID).getExitWord(), userID, cacheManager.getFromCache(userID).getEvaSessionCode());
    }

}