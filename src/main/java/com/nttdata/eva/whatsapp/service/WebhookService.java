package com.nttdata.eva.whatsapp.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;
import com.nttdata.eva.whatsapp.model.ResponseModel;
import com.nttdata.eva.whatsapp.model.WebhookData;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;


@Slf4j
@Service
public class WebhookService {
    @Autowired
    private SessionService sessionService; // Autowire the SessionService
    @Autowired
    private WebhookToEVA webhookToEVA; // Autowire the WebhookToEVA service
    @Autowired 
    private EvaAnswerToWhatsapp evaAnswerToWhatsapp; // Autowire the EvaAnswerToWhatsapp service
    
    @Value("${webhook.verificationtoken}")
    private String verificationToken;

    public void processIncomingMessage(WebhookData webhookData, HttpServletRequest request, BrokerConfiguration brokerConfig, String phoneId) {
            try {
                // Convert JsonNode to WebhookData object
                // Extract the message
                WebhookData.Message message = webhookData.getEntry().get(0).getChanges().get(0).getValue().getMessages().get(0);
                
                log.info("Phone Number " + phoneId );
                
                // Convert WebhookData to EVA request
                EVARequestTuple evaRequest = webhookToEVA.convert(webhookData, message, brokerConfig);
                if (evaRequest != null) {
                    //Use to load values from cache and generate a new token if needed
                    String displayPhone = webhookData.getEntry().get(0).getChanges().get(0).getValue().getMetadata().getDisplay_phone_number();
                    String composedUserID = message.getFrom() + "-" + phoneId;
                    String userRef = message.getFrom() ;

                    sessionService.InitCache(composedUserID, brokerConfig);
                    ResponseModel evaResponse = webhookToEVA.sendMessageToEVA(evaRequest, sessionService, userRef, displayPhone);
                    ArrayList<ObjectNode> whatsappAPICalls = evaAnswerToWhatsapp.getWhatsappAPICalls(evaResponse, message.getFrom());
                    evaAnswerToWhatsapp.sendListofMessagesToWhatsapp(whatsappAPICalls, phoneId, brokerConfig);
                    //session.saveSession();
                } else {
                    log.warn("Data to send to EVA is empty");
                }
            } catch (Exception e) {
                // Handle validation errors
                
            }
    }

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