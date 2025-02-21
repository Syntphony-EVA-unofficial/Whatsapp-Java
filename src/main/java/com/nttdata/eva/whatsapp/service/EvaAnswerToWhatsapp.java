package com.nttdata.eva.whatsapp.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.messages.CatalogMessage;
import com.nttdata.eva.whatsapp.messages.CustomHandoverMessage;
import com.nttdata.eva.whatsapp.messages.DocumentMessage;
import com.nttdata.eva.whatsapp.messages.EVAButtonMessage;
import com.nttdata.eva.whatsapp.messages.FlowMessage;
import com.nttdata.eva.whatsapp.messages.ImageMessage;
import com.nttdata.eva.whatsapp.messages.InteractiveListMessage;
import com.nttdata.eva.whatsapp.messages.InteractiveReplyButtonMessage;
import com.nttdata.eva.whatsapp.messages.LocationMessage;
import com.nttdata.eva.whatsapp.messages.LocationRequestMessage;
import com.nttdata.eva.whatsapp.messages.ProductMessage;
import com.nttdata.eva.whatsapp.messages.TextMessage;
import com.nttdata.eva.whatsapp.messages.VideoMessage;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.EvaResponseModel;
import com.nttdata.eva.whatsapp.utils.MessageLogger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EvaAnswerToWhatsapp {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MessageLogger messageLogger;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ArrayList<SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel>> getWhatsappAPICalls(
            EvaResponseModel evaResponse, String from) {
        ArrayList<SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel>> messages = new ArrayList<SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel>>();
        
        if (evaResponse == null) {
            log.error("EvaResponseModel is null");
            return messages; // Return an empty list or handle as needed
        }
        
        if (evaResponse.getAnswers() != null) {
            for (EvaResponseModel.Answer answer : evaResponse.getAnswers()) {
                SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel> result = prepareMessages(answer, from);
                if (result != null)
                    messages.add(result);
            }
        } else {
            log.error("No answers found in the response");
        }
        return messages;
    }

    public SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel> prepareMessages(
            EvaResponseModel.Answer answer, String from) {

        // Prepare data
        ObjectNode data = objectMapper.createObjectNode();
        CustomHandoverMessage.CustomHandOverModel customHandoverModel = null;
        data.put("messaging_product", "whatsapp");
        data.put("recipient_type", "individual");
        data.put("to", from);

        // Determine the message type
        // The only messages that do not have a technical text are text messages and
        // buttons
        if (answer.getTechnicalText() == null) {
            if (EVAButtonMessage.validate(answer)) {
                data = EVAButtonMessage.create(data, answer);
            } else {
                // since eva cannot send empty answers, at this point we at least have a text
                // message.
                data = TextMessage.create(data, answer);
            }
            return new SimpleEntry<>(data, customHandoverModel);
            // rest of the message types require a technical text
        } else {
            boolean modelFound = false;

            if (InteractiveReplyButtonMessage.validate(answer)) {
                data = InteractiveReplyButtonMessage.create(data, answer);
                modelFound = true;
            } else if (InteractiveListMessage.validate(answer)) {
                data = InteractiveListMessage.create(data, answer);
                modelFound = true;
            } else if (ImageMessage.validate(answer)) {
                data = ImageMessage.create(data, answer);
                modelFound = true;
            } else if (VideoMessage.validate(answer)) {
                data = VideoMessage.create(data, answer);
                modelFound = true;
            } else if (LocationRequestMessage.validate(answer)) {
                data = LocationRequestMessage.create(data, answer);
                modelFound = true;
            } else if (LocationMessage.validate(answer)) {
                data = LocationMessage.create(data, answer);
                modelFound = true;
            } else if (FlowMessage.validate(answer)) {
                data = FlowMessage.create(data, answer);
                modelFound = true;
            } else if (DocumentMessage.validate(answer)) {
                data = DocumentMessage.create(data, answer);
                modelFound = true;
            } else if (ProductMessage.validate(answer)) {
                data = ProductMessage.create(data, answer);
                modelFound = true;
            } else if (CatalogMessage.validate(answer)) {
                data = CatalogMessage.create(data, answer);
                modelFound = true;
            } else if (CustomHandoverMessage.validate(answer)) {
                customHandoverModel = CustomHandoverMessage.create(answer);
                data = null;
                modelFound = true;
            } else {
                log.error("No valid message model found.");
            }

            if (modelFound) {
                return new SimpleEntry<>(data, customHandoverModel);
            } else {
                log.error("No valid message model found.");
                return null;
            }
        }
    }

    public void sendListofMessagesToWhatsapp(
            ArrayList<SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel>> whatsappAPICalls,
            String facebookPhoneId,
            BrokerConfiguration brokerConfig,
            JsonNode incommingData,
            String clientPhone) {
        String facebookAccessToken = brokerConfig.getMetaConfig().getAccessToken();

        // Accumulate outgoing messages for bulk sending
        ArrayList<ObjectNode> outgoingMessagesList = new ArrayList<>();

        for (SimpleEntry<ObjectNode, CustomHandoverMessage.CustomHandOverModel> bodyAPIcall : whatsappAPICalls) {
            // Send each message to the WhatsApp API
            sendToWhatsappAPI(bodyAPIcall.getKey(), facebookPhoneId, facebookAccessToken);

            // Collect outgoing messages for bulk logging
            outgoingMessagesList.add(bodyAPIcall.getKey());

            try {
                Thread.sleep(1500); // Wait for 1500 milliseconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                log.error("Thread was interrupted", e);
            }
        }

        // Send all outgoing messages in one bulk operation
        if (!outgoingMessagesList.isEmpty()) {
            ObjectNode[] outgoingMessagesArray = outgoingMessagesList.toArray(new ObjectNode[0]);
            messageLogger.sendBulkMessage(incommingData, outgoingMessagesArray, facebookPhoneId, brokerConfig,
                    clientPhone);
        }
    }

    public void sendToWhatsappAPI(ObjectNode bodyAPIcall, String facebookPhoneId, String facebookAccessToken) {
        log.debug("Sending message to WhatsApp API");
        log.debug("Body Data sended to Whatsapp: {}", bodyAPIcall.toPrettyString());

        HttpHeaders headers = new HttpHeaders();

        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + facebookAccessToken);

        HttpEntity<ObjectNode> request = new HttpEntity<>(bodyAPIcall, headers);

        String url = String.format("https://graph.facebook.com/v21.0/%s/messages", facebookPhoneId);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().value() != 200) {
                log.error("Response status: {}", response.getStatusCode());
                log.error("Response headers: {}", response.getHeaders());
                log.error("Response body: {}", response.getBody());
                log.error("Failed to send message to WhatsApp API");
            } else {
                log.info("Message sent successfully");
            }
        } catch (Exception e) {
            log.error("Exception occurred while sending message to WhatsApp API", e);
        }
    }

}
