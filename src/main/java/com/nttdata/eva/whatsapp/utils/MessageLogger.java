package com.nttdata.eva.whatsapp.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;



@Slf4j
@Service
public class MessageLogger {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    private boolean isValidURL(String url) {
        try {
            URI uri = new URI(url);
            uri.toURL();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }
    
    private void logMessage(ObjectNode payload, String sender, BrokerConfiguration brokerConfig, String phoneId) {
        Boolean messageLoggerEnable = brokerConfig.getMessageLogerConfig().getEnabled();
        if (messageLoggerEnable) {
            String messageLoggerURL = brokerConfig.getMessageLogerConfig().getUrl();

            // Validate the URL
            if (isValidURL(messageLoggerURL)) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                // Add sender to the payload
                payload.put("sender", sender);
                if (phoneId != null) {
                    payload.put("phoneId", phoneId);
                }

                HttpEntity<ObjectNode> requestEntity = new HttpEntity<>(payload, headers);

                try {
                    ResponseEntity<String> response = restTemplate.exchange(messageLoggerURL, HttpMethod.POST, requestEntity, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Message Logger response received successfully");
                    } else {
                        log.error("Failed to get Message Logger response: " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while getting Message Logger response", e);
                }
            }
        }
    }

    @Async
    public void recordWebhookIncomming(JsonNode webhookData, BrokerConfiguration brokerConfig) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("data", webhookData);
        logMessage(payload, "Client", brokerConfig, null);
        log.info("logging message from Client"); 

    }

    @Async
    public void recordAPIMessageOutgoing(ObjectNode whatsappMessagePayload, String phoneId,  BrokerConfiguration brokerConfig) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("data", whatsappMessagePayload);
        logMessage(payload, "Syntphony", brokerConfig, phoneId);
        log.info("logging message from Syntphony"); 
    }
}


