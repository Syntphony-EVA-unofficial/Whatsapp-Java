package com.nttdata.eva.whatsapp.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.messages.CustomHandoverMessage;
import com.nttdata.eva.whatsapp.messages.CustomHandoverMessage.CustomHandOverModel;
import com.nttdata.eva.whatsapp.messages.TextMessage;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;
import com.nttdata.eva.whatsapp.model.ResponseModel;
import com.nttdata.eva.whatsapp.model.SessionDestination;
import com.nttdata.eva.whatsapp.model.WebhookData;
import com.nttdata.eva.whatsapp.utils.MessageLogger;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap.SimpleEntry;
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

    @Autowired
    MessageLogger messageLogger;

    @Value("${webhook.verificationtoken}")
    private String verificationToken;

    public void processIncomingMessage(WebhookData webhookData, HttpServletRequest request,
            BrokerConfiguration brokerConfig, String phoneId, JsonNode incommingData) {
        try {
            // Convert JsonNode to WebhookData object
            // Extract the message
            WebhookData.Message message = webhookData.getEntry().get(0).getChanges().get(0).getValue().getMessages()
                    .get(0);
            String userPhone = message.getFrom();
            String composedUserID = message.getFrom() + "-" + phoneId;
            sessionService.InitCache(composedUserID, brokerConfig);
            log.info("Current STARTING session state: {}", sessionService.toString());

            // Check if user is in human agent mode
            if (SessionDestination.HUMAN_AGENT.equals(sessionService.getDestination())) {
                // Check if user wants to change the destination to bot again

                if (TextMessage.isTextMessageMatching(message, sessionService.getExitWord())) {
                    sessionService.setDestination(SessionDestination.BOT);
                    log.info("User wants to change the destination to bot again");
                    // evaAnswerToWhatsapp.sendListofMessagesToWhatsapp(

                    ArrayList<SimpleEntry<ObjectNode, CustomHandOverModel>> ar = new ArrayList<>();
                    ObjectNode textNode = TextMessage.create(sessionService.getWelcomeback(), userPhone);

                    ar.add(new SimpleEntry<>(textNode, new CustomHandOverModel()));

                    evaAnswerToWhatsapp.sendListofMessagesToWhatsapp(ar, phoneId, brokerConfig, incommingData,
                            userPhone);
                    sessionService.saveSession();
                    log.info("71 Current END session state: {}", sessionService.toString());

                    // TODO: send message to console to inform that the user wants to change the
                    // destination to bot again
                    // TODO: send message to eva to trigger message to user
                } else {
                    messageLogger.logIncomingMessage(incommingData, phoneId, brokerConfig, userPhone);
                    log.info("Human agent mode, sending message to human agent");

                }
            } else { // Bot mode

                // Convert WebhookData to EVA request
                EVARequestTuple evaRequest = webhookToEVA.convert(webhookData, message, brokerConfig);
                if (evaRequest != null) {
                    // Use to load values from cache and generate a new token if needed
                    String userRef = message.getFrom();
                    // Get current session data

                    ResponseModel evaResponse = webhookToEVA.sendMessageToEVA(evaRequest, sessionService, userRef);
                    String clientPhone = message.getFrom();
                    ArrayList<SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel>> whatsappAPICalls = evaAnswerToWhatsapp
                            .getWhatsappAPICalls(evaResponse,
                                    clientPhone);

                    // Check for handover messages and update session
                    whatsappAPICalls.removeIf(call -> {
                        CustomHandoverMessage.CustomHandOverModel handover = call.getValue();
                        if (handover != null) {
                            // Update session destination to human agent
                            sessionService.setDestination(SessionDestination.HUMAN_AGENT);
                            sessionService.setExitWord(handover.getExit_command());
                            sessionService.setWelcomeback(handover.getWelcomeback());
                            log.info("eva changes the destination to human agent");
                            return true; // Remove this message from the array
                        }
                        return false;
                    });

                    // Only send remaining messages to WhatsApp
                    if (!whatsappAPICalls.isEmpty()) {
                        evaAnswerToWhatsapp.sendListofMessagesToWhatsapp(whatsappAPICalls, phoneId, brokerConfig,
                                incommingData, clientPhone);
                    }
                    sessionService.saveSession();
                    log.info("115 Current END session state: {}", sessionService.toString());
                } else {
                    log.warn("Data to send to EVA is empty");
                }

            }
        } catch (Exception e) {
            // Handle validation errors
            log.error("Error processing incoming message", e);
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