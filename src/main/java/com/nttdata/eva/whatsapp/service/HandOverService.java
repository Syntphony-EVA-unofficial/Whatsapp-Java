package com.nttdata.eva.whatsapp.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.messages.CustomHandoverMessage;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.EvaResponseModel;
import com.nttdata.eva.whatsapp.model.HandoverMessage;
import com.nttdata.eva.whatsapp.model.SessionDestination;
import com.nttdata.eva.whatsapp.utils.MessageLogger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HandOverService {
    @Autowired
    private SessionService sessionService; // Autowire the SessionService
    @Autowired
    private WebhookToEVA webhookToEVA; // Autowire the WebhookToEVA service
    @Autowired
    private EvaAnswerToWhatsapp evaAnswerToWhatsapp; // Autowire the EvaAnswerToWhatsapp service

    @Autowired
    MessageLogger messageLogger;

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public void processIncomingMessage(HandoverMessage handoverMessage, BrokerConfiguration brokerConfig) {
        try {
            // Extract the message
            String clientPhone = handoverMessage.getClientphone();  
            String phoneId = handoverMessage.getPhoneid();
            String composedUserID = clientPhone + "-" + phoneId;


            
            if (!sessionService.InitCacheHandover(composedUserID, brokerConfig)) {
                log.error("Session data not found for userID: {}", composedUserID);
                return;
            }

            EvaResponseModel responseModel = null;
            try {

                // Convert JsonNode to ResponseModel directly
                responseModel = objectMapper.treeToValue(handoverMessage.getEvaPayload(), EvaResponseModel.class);
            
            } catch (Exception e) {
                // Handle validation errors
                log.error("Error validating EVA style from handover", e);
                return;
            }
            
                    ArrayList<SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel>> whatsappAPICalls = evaAnswerToWhatsapp
                            .getWhatsappAPICalls(responseModel,clientPhone);

                    // Check for handover messages and update session
                    whatsappAPICalls.removeIf(call -> {
                        CustomHandoverMessage.CustomHandOverModel handover = call.getValue();
                        if (handover != null) {
                            if (handover.getAction() != null && handover.getAction().equals("disconnect")) {
                                // Update session destination to human agent
                                sessionService.setDestination(SessionDestination.BOT, composedUserID);
                                log.info("Human agent changes the destination to bot");

                                sessionService.saveSession(composedUserID);
                                log.info("session saved: {}", sessionService.toString(composedUserID));
            
                                return true; // Remove this message from the array
                            }
                        }
                        return false; // Do not remove this message
                    });

                    // Only send remaining messages to WhatsApp
                    if (!whatsappAPICalls.isEmpty()) {
                        evaAnswerToWhatsapp.sendListofMessagesToWhatsapp(whatsappAPICalls, phoneId, brokerConfig,
                                null, clientPhone);
                    }

            
        } catch (Exception e) {
            // Handle validation errors
            log.error("Error processing incoming Handover message", e);
        }
    }

}