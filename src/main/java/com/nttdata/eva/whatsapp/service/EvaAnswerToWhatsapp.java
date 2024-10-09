package com.nttdata.eva.whatsapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.messages.DocumentMessage;
import com.nttdata.eva.whatsapp.messages.FlowMessage;
import com.nttdata.eva.whatsapp.messages.ImageMessage;
import com.nttdata.eva.whatsapp.messages.InteractiveListMessage;
import com.nttdata.eva.whatsapp.messages.InteractiveReplyButtonMessage;
import com.nttdata.eva.whatsapp.messages.TextMessage;
import com.nttdata.eva.whatsapp.messages.VideoMessage;
import com.nttdata.eva.whatsapp.messages.LocationMessage;
import com.nttdata.eva.whatsapp.messages.LocationRequestMessage;
import com.nttdata.eva.whatsapp.messages.TemplateMessage;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.ResponseModel;
import java.util.ArrayList;
import java.util.concurrent.Flow;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class EvaAnswerToWhatsapp {

    @Autowired
    private RestTemplate restTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();




    public ArrayList<ObjectNode> getWhatsappAPICalls(ResponseModel evaResponse, String from)
    {
        ArrayList<ObjectNode> messages = new ArrayList<>();
        for (ResponseModel.Answer answer : evaResponse.getAnswers()) 
            {
            ObjectNode message = prepareMessages(answer, from);
            if (message != null)
                messages.add(message);
            }
        return messages;

    }

    private ObjectNode prepareMessages(ResponseModel.Answer answer, String from) {



        log.info("Preparing body message for WhatsApp API");



        // Prepare data
        ObjectNode data = objectMapper.createObjectNode();
        data.put("messaging_product", "whatsapp");
        data.put("recipient_type", "individual");
        data.put("to", from);

        // Determine the message type
        if (answer.getTechnicalText() == null) {
            if (InteractiveReplyButtonMessage.validate(answer)) {
                data = InteractiveReplyButtonMessage.create(data, answer);
            } else {
                data = TextMessage.create(data, answer);
            }
            return data;
        } else {
            boolean modelFound = false;

            if (InteractiveReplyButtonMessage.validate(answer)) {
                data = InteractiveReplyButtonMessage.create(data, answer);
                modelFound = true;
            } else if (TextMessage.validate(answer)) {
                data = TextMessage.create(data, answer);
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
            } else if (TemplateMessage.validate(answer)) {
                data = TemplateMessage.create(data, answer);
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
            }


    
            if (modelFound) {
                return data;
            } else {
                log.error("No valid message model found.");
                return null;
            }
        }
    }

    public void sendListofMessagesToWhatsapp(ArrayList<ObjectNode> whatsappAPICalls, String facebookPhoneId, BrokerConfiguration brokerConfig) {
        String facebookAccessToken = brokerConfig.getMetaConfig().getAccessToken();
        for (ObjectNode bodyAPIcall : whatsappAPICalls) {
            sendToWhatsappAPI(bodyAPIcall, facebookPhoneId, facebookAccessToken);
            try {
                Thread.sleep(1500); // Wait for 1500 milliseconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                log.error("Thread was interrupted", e);
            }
        }
    }

    public void sendToWhatsappAPI(ObjectNode bodyAPIcall, String facebookPhoneId, String facebookAccessToken) {
        log.info("Sending message to WhatsApp API");
        log.info("Body Data sended to Whatsapp: {}", bodyAPIcall.toPrettyString());

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

