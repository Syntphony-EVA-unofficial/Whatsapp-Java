package com.nttdata.eva.whatsapp.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

    // Class-level variable to store the incoming message temporarily

    private boolean isValidURL(String url) {
        try {
            URI uri = new URI(url);
            uri.toURL();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

    private void logMessage(ObjectNode payload, String messageLoggerURL) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<ObjectNode> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(messageLoggerURL, HttpMethod.POST, requestEntity,
                    String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Message Logger response sent successfully");
            } else {
                log.error("Failed to send Message Logger response: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Exception occurred while sending Message Logger response", e);
        }
    }

    @Async
    public void logIncomingMessage(JsonNode incomingData, String companyPhoneID,
            BrokerConfiguration brokerConfig, String clientPhone) {
        if (!isLoggingEnabled(brokerConfig)) {
            return;
        }

        ArrayNode messagesArray = objectMapper.createArrayNode();
        // ... rest of message creation ...

        ObjectNode incomingMessage = objectMapper.createObjectNode();
        incomingMessage.put("sender", "Client");
        incomingMessage.put("mode", "HANDOVER");
        incomingMessage.set("payload", incomingData);
        messagesArray.add(incomingMessage);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("messages", messagesArray);
        payload.put("clientPhone", clientPhone);
        payload.put("companyPhoneID", companyPhoneID);

        logMessage(payload, brokerConfig.getMessageLogerConfig().getUrl());
        log.info("Logged incoming message for phoneId: {}", companyPhoneID);
    }

    private boolean isLoggingEnabled(BrokerConfiguration brokerConfig) {
        Boolean enabled = brokerConfig.getMessageLogerConfig().getEnabled();
        String url = brokerConfig.getMessageLogerConfig().getUrl();
        return enabled && isValidURL(url);
    }

    @Async
    public void sendBulkMessage(JsonNode incomingData, ObjectNode[] outgoingMessages,
            String companyPhoneID, BrokerConfiguration brokerConfig, String clientPhone) {
        if (!isLoggingEnabled(brokerConfig)) {
            return;
        }

        // Create an array node to hold all messages
        ArrayNode messagesArray = objectMapper.createArrayNode();

        // Add the stored incoming message if present
        if (incomingData != null) {
            ObjectNode incomingMessage = objectMapper.createObjectNode();
            incomingMessage.put("sender", "Client");
            incomingMessage.put("mode", "BOT");
            incomingMessage.set("payload", incomingData);
            messagesArray.add(incomingMessage);

        }

        // Add each outgoing message with "Syntphony" as the sender
        for (ObjectNode outgoing : outgoingMessages) {
            ObjectNode outgoingMessage = objectMapper.createObjectNode();
            outgoingMessage.put("sender", "Syntphony");
            outgoingMessage.put("mode", "BOT");
            outgoingMessage.set("payload", outgoing);
            messagesArray.add(outgoingMessage);
        }

        // Create the payload to send
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("messages", messagesArray);
        payload.put("clientPhone", clientPhone);
        payload.put("companyPhoneID", companyPhoneID);

        // Send the payload to the logger service
        logMessage(payload, brokerConfig.getMessageLogerConfig().getUrl());
        log.info("Sent bulk message set including stored incoming and outgoing messages for phoneId: {}",
                companyPhoneID);
    }
}
