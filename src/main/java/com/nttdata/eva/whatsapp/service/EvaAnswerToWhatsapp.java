package com.nttdata.eva.whatsapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.messages.ImageMessage;
import com.nttdata.eva.whatsapp.messages.InteractiveListMessage;
import com.nttdata.eva.whatsapp.messages.InteractiveReplyButtonMessage;
import com.nttdata.eva.whatsapp.messages.TextMessage;
import com.nttdata.eva.whatsapp.messages.VideoMessage;
import com.nttdata.eva.whatsapp.messages.LocationMessage;
import com.nttdata.eva.whatsapp.messages.LocationRequestMessage;
import com.nttdata.eva.whatsapp.messages.TemplateMessage;
import com.nttdata.eva.whatsapp.model.ResponseModel;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class EvaAnswerToWhatsapp {

    @Autowired
    private RestTemplate restTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${facebook.accesstoken}")
    private String facebookAccessToken;

    @Value("${facebook.phoneid}")
    private String facebookPhoneId;

    public ArrayList<ObjectNode> getWhatsappAPICalls(ResponseModel evaResponse, String from)
    {
        ArrayList<ObjectNode> messages = new ArrayList<>();
        for (ResponseModel.Answer answer : evaResponse.getAnswers()) 
            {
            ObjectNode message = prepareMessages(answer, from);
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
            }
    
            if (modelFound) {
                System.out.println(data.toString());
            } else {
                System.out.println("No valid message model found.");
            }
        }

        return data;
    }

    public void sendListofMessagesToWhatsapp(ArrayList<ObjectNode> whatsappAPICalls) {
        for (ObjectNode bodyAPIcall : whatsappAPICalls) {
            sendToWhatsappAPI(bodyAPIcall);
            try {
                Thread.sleep(1500); // Wait for 1500 milliseconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                log.error("Thread was interrupted", e);
            }
        }
    }

    public void sendToWhatsappAPI(ObjectNode bodyAPIcall) {
        log.debug("Sending message to WhatsApp API");
        log.debug("Body Data sended to Whatsapp: {}", bodyAPIcall.toPrettyString());

        HttpHeaders headers = new HttpHeaders();

        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + facebookAccessToken);


        HttpEntity<ObjectNode> request = new HttpEntity<>(bodyAPIcall, headers);

        String url = String.format("https://graph.facebook.com/v18.0/%s/messages", facebookPhoneId);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode().value() != 200) {
            log.error("Response status: {}", response.getStatusCode());
            log.error("Response headers: {}", response.getHeaders());
            log.error("Response body: {}", response.getBody());
            throw new RuntimeException("Failed to send message to WhatsApp API");
        }
    }

}

