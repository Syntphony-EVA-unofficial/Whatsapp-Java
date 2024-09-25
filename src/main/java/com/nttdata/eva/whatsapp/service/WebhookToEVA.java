package com.nttdata.eva.whatsapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;
import com.nttdata.eva.whatsapp.model.ResponseModel;
import com.nttdata.eva.whatsapp.model.WebhookData;
import com.nttdata.eva.whatsapp.utils.AudioSTT;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.json.Json;
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static EVARequestTuple handleText(WebhookData.Message message) {
        log.info("Handling text message");
        Map<String, Object> textMap = message.getText();
        String EVA_content = (String) textMap.get("body");
        ObjectNode EVA_context = null;
        return new EVARequestTuple(EVA_content, EVA_context);
    }

    public static EVARequestTuple handleInteractive(WebhookData.Message message) {
        
        //String subtype = ((JsonNode) message.getInteractive().get("type")).asText();
        
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
                }
                else {
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
            }
            else {
                log.error("list_reply is not a Map");
                return null;
            }
        } else {
            log.warn("Interactive message subtype not supported: {}", subtype);
            return null;
        }
    }

    public static EVARequestTuple handleAudio(WebhookData.Message message) {
        log.info("Handling audio message");
        try {
            Map<String, Object> AudioMap = message.getAudio();
            String audioID = (String) AudioMap.get("id");
            String audioURL = AudioSTT.getAudioURL(audioID);
            if (audioURL != null) {
                byte[] downloadAudio = AudioSTT.getDownloadAudio(audioURL);
                if (downloadAudio != null) {
                    log.info("Audio message downloaded successfully");
                    log.info("The size of the binary data is {} bytes", downloadAudio.length);
                    String STT_Result = AudioSTT.transcribeFileV2("seu-whatsapp-api", downloadAudio);
                    if (STT_Result != null) {
                        String EVA_content = STT_Result;
                        ObjectNode EVA_context = null;
                        return new EVARequestTuple(EVA_content, EVA_context);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Audio message could not be processed", e);
        }
        return null;
    }

    public static EVARequestTuple handleLocation(WebhookData.Message message) {
        log.info("Handling location message");
        String EVA_content = " ";
        ObjectNode locationNode = objectMapper.createObjectNode();
        locationNode.put("latitude", message.getLocation().getLatitude());
        locationNode.put("longitude", message.getLocation().getLongitude());

        ObjectNode evaContextNode = objectMapper.createObjectNode();
        evaContextNode.set("location", locationNode);

        return new EVARequestTuple(EVA_content, evaContextNode);
    }

    public static EVARequestTuple convert(WebhookData webhookData, WebhookData.Message message) {
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
        log.info("Sending message to evaBroker (TIME) {}", Instant.now());

        String baseUrl = String.format("https://%s/eva-broker/org/%s/env/%s/bot/%s/channel/%s/v1/conversations",
                instance, orgUUID, envUUID, botUUID, channelUUID).trim();
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        String sessionCode = session.getEvaSessionCode();
        if (sessionCode!= null) {
            urlBuilder.append("/").append(sessionCode);
            log.info("Session code is "+ sessionCode);
        }else
        {
            log.info("Session code is null");
        }

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API-KEY", apiKey);
        headers.set("OS", "Linux");
        headers.set("USER-REF", session.getUserID());
        headers.set("LOCALE", "en-US");
        String evaToken = session.getEvaToken();
        Map<String, Object> data = new HashMap<>();
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
            URI uri = new URI(baseUrl);
            log.info("Sending message to EVA: {}", data);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(data, headers);
            Map<String, Object> response = restTemplate.postForObject(urlBuilder.toString(), requestEntity, Map.class);
            String jsonResponse = objectMapper.writeValueAsString(response);

            ResponseModel responseModel = objectMapper.readValue(jsonResponse, ResponseModel.class);
            session.setEvaSessionCode(responseModel.getSessionCode());
            session.saveSession();
            return responseModel;
        }

        catch (RestClientException e) {
            log.error("Error sending message to EVA: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending message to EVA", e);
        }
        return null;
    }

}