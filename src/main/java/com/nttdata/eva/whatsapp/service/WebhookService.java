package com.nttdata.eva.whatsapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;
import com.nttdata.eva.whatsapp.model.ResponseModel;
import com.nttdata.eva.whatsapp.model.WebhookData;
import com.nttdata.eva.whatsapp.service.WebhookToEVA;
//import com.nttdata.eva.whatsapp.utils.Session;
//import com.nttdata.eva.whatsapp.utils.EVARequestSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;

import java.io.IOException;

@Slf4j
@Service
public class WebhookService {

    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private SessionService sessionService; // Autowire the SessionService

    @Autowired
    private WebhookToEVA webhookToEVA; // Autowire the WebhookToEVA service
    
    @Autowired 
    private EvaAnswerToWhatsapp evaAnswerToWhatsapp; // Autowire the EvaAnswerToWhatsapp service

    public void processIncomingMessage(String requestBody, HttpServletRequest request) {
        log.info("Processing incoming message");
        
        try {
            // Parse the request body into a JsonNode
            JsonNode data = objectMapper.readTree(requestBody);
            try {
                // Convert JsonNode to WebhookData object
                WebhookData webhookData = objectMapper.treeToValue(data, WebhookData.class);
                log.info("Webhook incoming data: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
                
                // Extract the message
                WebhookData.Message message = webhookData.getEntry().get(0).getChanges().get(0).getValue().getMessages().get(0);
                
                // Convert WebhookData to EVA request
                EVARequestTuple evaRequest = WebhookToEVA.convert(webhookData, message);
                
                if (evaRequest != null) {
                    
                    //Use to load values from cache and generate a new token if needed
                    sessionService.InitCache(message.getFrom());
                    
                    ResponseModel evaResponse = webhookToEVA.sendMessageToEVA(evaRequest, sessionService);
                    
                    ArrayList<ObjectNode> whatsappAPICalls = evaAnswerToWhatsapp.getWhatsappAPICalls(evaResponse, message.getFrom());

                    evaAnswerToWhatsapp.sendListofMessagesToWhatsapp(whatsappAPICalls);

                    //session.saveSession();
                } else {
                    log.warn("Data to send to EVA is empty");
                }
                
            } catch (Exception e) {
                // Handle validation errors
                try {
                    String status = data.get("entry").get(0).get("changes").get(0).get("value").get("statuses").get(0).get("status").asText();
                    log.info("Message status: {}", status);
                } catch (Exception ex) {
                    log.warn("Received unexpected data: {}", data);
                }
            }
            
        } catch (IOException e) {
            log.error("An error occurred while processing the request.", e);
        }
    }
    
    private void getWhatsappAPICalls(ResponseModel evaResponse, String from) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWhatsappAPICalls'");
    }

    @Value("${facebook.verificationtoken}")
    private String verificationToken;
    public ResponseEntity<String> verify(HttpServletRequest request) {
        log.info("Verifying webhook");
        
        String mode = request.getParameter("hub.mode");
        String token = request.getParameter("hub.verify_token");
        String challenge = request.getParameter("hub.challenge");
        
        if (mode != null && token != null && mode.equals("subscribe") && token.equals(verificationToken)) {
            log.info("Webhook verified");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Webhook verification failed");
            return ResponseEntity.status(403).body("Verification failed");
        }
    }

}