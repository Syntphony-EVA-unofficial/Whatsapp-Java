package com.nttdata.eva.whatsapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class WhatsappMediaUtils {
    private List<String> supportedLanguages;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${messages.audio.STTEnabled:false}")
    private String audioSTTEnabled; 

    @Value("${messages.audio.STTUrl:null}")
    private String audioSTTServer;

    @Value("${facebook.accesstoken}")
    private String audioSTTToken;

    @Autowired
    private RestTemplate restTemplate;


    private boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public String getAudioURL(String audioID) {
        String url = String.format("https://graph.facebook.com/v19.0/%s", audioID);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + audioSTTToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {});
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to get audio URL: {}", response);
                return null;
            }
            Map<String, Object> responseData = response.getBody();
            log.info("Data Response: {}", responseData);
            return (String) responseData.get("url");
        } catch (Exception e) {
            log.error("An error occurred while getting audio URL: {}", e.getMessage());
            return null;
        }
    }

    public EVARequestTuple getSTTFromAudio(String audioID)
    {
        String audioURL = getAudioURL(audioID);
        if (Boolean.parseBoolean(audioSTTEnabled)) {
            if ((audioSTTServer!=null)&& (isValidURL(audioURL)))
            {
                String Transcription = callSTTServer(audioURL);
                if (Transcription != null && !Transcription.isEmpty()) {
                    return new EVARequestTuple(Transcription, null);
                }
            }
        }
        ObjectNode context = objectMapper.createObjectNode();
        ObjectNode audioMessageNode = context.putObject("audiomessage");
        audioMessageNode.put("audiourl", audioURL);
        return new EVARequestTuple(null, context);
    }

     private String callSTTServer(String audioURL) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("url", audioURL);
        payload.put("token", audioSTTToken);

        String requestBody;
            try {
                requestBody = objectMapper.writeValueAsString(payload);
            } catch (Exception e) {
                log.error("Failed to create JSON payload", e);
                return null;
            }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);


        try {
            ResponseEntity<String> response = restTemplate.exchange(audioSTTServer, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Failed to get STT response: " + response.getStatusCode());
                return null;
            }
        } 
        catch (RestClientException e) {
            log.error("Error during REST call to STT server", e);
            return null;
        }
    }
        
}