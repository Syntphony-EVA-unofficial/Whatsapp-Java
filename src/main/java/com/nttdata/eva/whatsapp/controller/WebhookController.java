package com.nttdata.eva.whatsapp.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.WebhookData;
import com.nttdata.eva.whatsapp.service.WebhookService;
import com.nttdata.eva.whatsapp.utils.ConfigLoader;
import com.nttdata.eva.whatsapp.utils.MessageLogger;
import com.nttdata.eva.whatsapp.utils.WebhookUtils;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class WebhookController {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private WebhookUtils webhookUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfigLoader configLoader;

    @Autowired
    private MessageLogger messageLogger;


    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(HttpServletRequest request){
        
        
        
        return webhookService.verify(request);
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handleIncomingUserMessage(@RequestBody String requestBody, HttpServletRequest request) {
        WebhookData webhookData;
        BrokerConfiguration brokerConfig;
        String phoneId;
        try {
            // Parse the request body into a JsonNode
            JsonNode data = objectMapper.readTree(requestBody);

            try {
                // Convert JsonNode to WebhookData object
                webhookData = objectMapper.treeToValue(data, WebhookData.class);
                log.debug("Webhook incoming data: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
                
                
                phoneId = webhookData.getEntry().get(0).getChanges().get(0).getValue().getMetadata().getPhone_number_id();
                Map<String, BrokerConfiguration> allBrokerConfigs = configLoader.getBrokerConfigs();
                
                if (allBrokerConfigs.containsKey(phoneId)) {
                    // Process the brokerConfig as needed
                    brokerConfig = allBrokerConfigs.get(phoneId);
                    
                    messageLogger.recordWebhookIncomming(data, brokerConfig);
                } else {
                    
                    log.error("Phone ID {} does not exist in the map", phoneId);
                    // Get the set of keys
                    Set<String> keys = allBrokerConfigs.keySet();
                    
                    // Print all keys
                    for (String key : keys) {
                        System.out.println(key);
                    }
                    
                    return ResponseEntity.status(400).body("Invalid request: Phone ID does not exist.");
                }
                
            } catch (JsonProcessingException e) {
                try {
                    String status = data.get("entry").get(0).get("changes").get(0).get("value").get("statuses").get(0).get("status").asText();
                    log.info("Message status: {}", status);
                    return ResponseEntity.ok("Request processed successfully.");
                } catch (Exception ex) {
                    log.warn("Received unexpected data: {}", data);
                    return ResponseEntity.status(400).body("Invalid request body: Unable to parse JSON.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse request body: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid request body.");
        }
        
        String appSecret = brokerConfig.getMetaConfig().getAppSecret();
        // Validate the signature
        if (webhookUtils.checkSignature(request, requestBody, appSecret)) {
            // Process the incoming message
            webhookService.processIncomingMessage(webhookData, request, brokerConfig, phoneId);
            return ResponseEntity.ok("Request processed successfully.");
        } else {
            return ResponseEntity.ok("Invalid signature.");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("This is a test endpoint");
    }

    
}