package com.nttdata.eva.whatsapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;
import com.nttdata.eva.whatsapp.model.ResponseModel;
import com.nttdata.eva.whatsapp.model.WebhookData;
import com.nttdata.eva.whatsapp.utils.WhatsappMediaUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
public class WebhookToEVA {

    @Value("${eva.env.instance}")
    private String instance;

    @Value("${eva.org.uuid}")
    private String orgUUID;

    @Value("${eva.env.uuid}")
    private String envUUID;

    @Value("${eva.bot.uuid}")
    private String botUUID;

    @Value("${eva.bot.channeluuid}")
    private String channelUUID;

    @Value("${eva.env.apikey}")
    private String apiKey;

    @Value("${facebook.accesstoken}")
    private String accessToken;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WhatsappMediaUtils whatsappMediaUtils;

    @Autowired
    private RestTemplate restTemplate;

    public EVARequestTuple handleText(WebhookData.Message message) {
        log.info("Handling text message");
        Map<String, Object> textMap = message.getText();
        String EVA_content = (String) textMap.get("body");
        ObjectNode EVA_context = null;
        return new EVARequestTuple(EVA_content, EVA_context);
    }

    public EVARequestTuple handleInteractive(WebhookData.Message message) {

        // String subtype = ((JsonNode) message.getInteractive().get("type")).asText();

        Map<String, Object> interactiveMap = message.getInteractive();
        String subtype = (String) interactiveMap.get("type");

        if ("button_reply".equals(subtype)) {
            log.info("Handling interactive button message");
            Object buttonReplyObj = interactiveMap.get("button_reply");
            if (buttonReplyObj instanceof Map) {
                JsonNode buttonReply = objectMapper.convertValue(buttonReplyObj, JsonNode.class);
                String EVA_content = buttonReply.get("id").asText();
                ObjectNode EVA_context = null;
                return new EVARequestTuple(EVA_content, EVA_context);
            } else {
                log.error("button_reply is not a Map");
                return null;
            }
        } else if ("list_reply".equals(subtype)) {
            log.info("Handling interactive list message");
            Object listReplyObj = interactiveMap.get("list_reply");
            if (listReplyObj instanceof Map) {
                JsonNode listReply = objectMapper.convertValue(listReplyObj, JsonNode.class);
                String EVA_content = listReply.get("id").asText();
                ObjectNode EVA_context = null;
                return new EVARequestTuple(EVA_content, EVA_context);
            } else {
                log.error("list_reply is not a Map");
                return null;
            }
        } else {
            log.warn("Interactive message subtype not supported: {}", subtype);
            return null;
        }
    }

    public EVARequestTuple handleAudio(WebhookData.Message message) {
        log.info("Handling audio message");
        try {
            Map<String, Object> AudioMap = message.getAudio();
            String audioID = (String) AudioMap.get("id");
            EVARequestTuple STTResult = whatsappMediaUtils.getSTTFromAudio(audioID);
            return STTResult;

        } catch (Exception e) {
            log.warn("Audio message could not be processed", e);
        }
        return null;
    }

    public EVARequestTuple handleLocation(WebhookData.Message message) {
        log.info("Handling location message");
        String EVA_content = " ";
        ObjectNode locationNode = objectMapper.createObjectNode();
        locationNode.put("latitude", message.getLocation().getLatitude());
        locationNode.put("longitude", message.getLocation().getLongitude());

        ObjectNode evaContextNode = objectMapper.createObjectNode();
        evaContextNode.set("location", locationNode);

        return new EVARequestTuple(EVA_content, evaContextNode);
    }

    public EVARequestTuple convert(WebhookData webhookData, WebhookData.Message message) {
        try {
            String type = message.getType();
            switch (type) {
                case "text":
                    return handleText(message);
                case "location":
                    return handleLocation(message);
                case "interactive":
                    return handleInteractive(message);
                case "audio":
                    return handleAudio(message);
                default:
                    log.warn("Message type not supported: {}", type);
                    log.info("Message data: {}",
                            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
                    return null;
            }
        } catch (Exception e) {
            log.error("Error in convert to EVA occurred", e);
            return null;
        }
    }

    public ResponseModel sendMessageToEVA(EVARequestTuple evaRequest, SessionService session) {

        String baseUrl = String.format("https://%s/eva-broker/org/%s/env/%s/bot/%s/channel/%s/v1/conversations",
                instance, orgUUID, envUUID, botUUID, channelUUID).trim();
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        String sessionCode = session.getEvaSessionCode();
        if (sessionCode != null) {
            urlBuilder.append("/").append(sessionCode);
            log.info("Session code is " + sessionCode);
        } else {
            log.info("Session code is null");
        }

        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API-KEY", apiKey);
        headers.set("OS", "Linux");
        headers.set("USER-REF", session.getUserID());
        headers.set("LOCALE", "en-US");

        Map<String, Object> data = new HashMap<>();
        boolean successCall = false;
        int retryCount = 0;
        int maxRetries = 2;

        while (!successCall && retryCount < maxRetries) {

            String evaToken = session.getEvaToken();
            if (evaToken != null) {
                headers.setBearerAuth(evaToken);
                data.put("text", evaRequest.getContent());
                if (evaRequest.getContext() != null) {
                    data.put("context", evaRequest.getContext());
                }
            } else {
                log.error("EVA Token is null");
                return null;
            }

            try {
                log.info("Sending message to EVA: {} at retry {}", data, retryCount);
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(data, headers);
                Map<String, Object> response = restTemplate.postForObject(urlBuilder.toString(), requestEntity,
                        Map.class);
                String jsonResponse = objectMapper.writeValueAsString(response);

                ResponseModel responseModel = objectMapper.readValue(jsonResponse, ResponseModel.class);
                session.setEvaSessionCode(responseModel.getSessionCode());
                session.saveSession();
                return responseModel;
            } catch (HttpClientErrorException.Unauthorized e) {
                log.warn("Unauthorized error, refreshing token and retrying: {}", e.getMessage());
                session.deleteToken();
                retryCount++;
            } catch (RestClientException e) {
                log.error("Error sending message to EVA: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error sending message to EVA", e);
            }
        }

        return null;
    }

}