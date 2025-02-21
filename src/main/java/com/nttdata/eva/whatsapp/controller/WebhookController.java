package com.nttdata.eva.whatsapp.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.HandoverMessage;
import com.nttdata.eva.whatsapp.model.WebhookData;
import com.nttdata.eva.whatsapp.service.HandOverService;
import com.nttdata.eva.whatsapp.service.WebhookService;
import com.nttdata.eva.whatsapp.utils.ConfigLoader;
import com.nttdata.eva.whatsapp.utils.WebhookUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class WebhookController {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private HandOverService handOverService;
    @Autowired
    private WebhookUtils webhookUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfigLoader configLoader;

    @GetMapping("/meta-events")
    public ResponseEntity<String> verifyWebhook(HttpServletRequest request) {

        return webhookService.verify(request);
    }

    private String extractMessageStatus(JsonNode incommingData) {
        try {
            return incommingData.get("entry").get(0).get("changes").get(0).get("value").get("statuses")
                    .get(0).get("status").asText();
        } catch (NullPointerException e) {
            return null;
        }
    }

    @PostMapping("/handover-messages")
    public ResponseEntity<String> handleHandoverMessages(@RequestBody String requestBody,
            HttpServletRequest request) {
        BrokerConfiguration brokerConfig;

        HandoverMessage handoverMessage;
        try {
            handoverMessage = objectMapper.readValue(requestBody, HandoverMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse handover message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body");
        }
        log.info("Handover message received: {}", handoverMessage);
        
        Map<String, BrokerConfiguration> allBrokerConfigs = configLoader.getBrokerConfigs();

        String phoneId = handoverMessage.getPhoneid();
        if (allBrokerConfigs.containsKey(phoneId)) {
            brokerConfig = allBrokerConfigs.get(phoneId);

            handOverService.processIncomingMessage(handoverMessage, brokerConfig);

        } else {
            log.error("Phone ID {} does not exist in the map", phoneId);
            // Get the set of keys
            Set<String> keys = allBrokerConfigs.keySet();

            // Print all keys
            for (String key : keys) {
                log.debug(key);
            }

            return ResponseEntity.status(400).body("Invalid request: Phone ID does not exist.");
        }
        
        
        return ResponseEntity.ok("Handover messages received");
    }

    @PostMapping("/meta-events")
    public ResponseEntity<String> handleIncomingUserMessage(@RequestBody String requestBody,
            HttpServletRequest request) {
        WebhookData webhookData;
        BrokerConfiguration brokerConfig;
        String phoneId;
        JsonNode incommingData = null;

        try {
            // Parse the request body into a JsonNode
            incommingData = objectMapper.readTree(requestBody);
            log.info("incommingData: {}", incommingData);
            webhookData = objectMapper.treeToValue(incommingData, WebhookData.class);
        } catch (Exception e) {
            log.warn("Received unexpected data: {}", incommingData);
            return ResponseEntity.status(400).body("Invalid request body: Unable to parse JSON.");
        }

        String status = extractMessageStatus(incommingData);
        if (status != null) {
            log.info("Message status: {}", status);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        try {

            log.debug("Webhook incoming data: {}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(incommingData));

            phoneId = webhookData.getEntry().get(0).getChanges().get(0).getValue().getMetadata().getPhone_number_id();
            Map<String, BrokerConfiguration> allBrokerConfigs = configLoader.getBrokerConfigs();

            if (allBrokerConfigs.containsKey(phoneId)) {
                // Process the brokerConfig as needed
                brokerConfig = allBrokerConfigs.get(phoneId);
            } else {
                log.error("Phone ID {} does not exist in the map", phoneId);
                // Get the set of keys
                Set<String> keys = allBrokerConfigs.keySet();

                // Print all keys
                for (String key : keys) {
                    log.debug(key);
                }

                return ResponseEntity.status(400).body("Invalid request: Phone ID does not exist.");
            }

            String appSecret = brokerConfig.getMetaConfig().getAppSecret();
            // Validate the signature
            if (webhookUtils.checkSignature(request, requestBody, appSecret)) {
                // Process the incoming message
                webhookService.processIncomingMessage(webhookData, request, brokerConfig, phoneId, incommingData);
                return ResponseEntity.ok("Request processed successfully.");
            } else {
                return ResponseEntity.ok("Invalid signature.");
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to log incoming data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("This is a test endpoint");
    }

}