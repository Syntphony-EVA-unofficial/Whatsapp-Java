package com.nttdata.eva.whatsapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;
import com.nttdata.eva.whatsapp.model.TranscribeResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


@Slf4j
@Service
public class WhatsappMediaUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    
    @Autowired
    private RestTemplate restTemplate;

    

private boolean isValidURL(String url) {
    try {
        URI uri = new URI(url);
        uri.toURL();
        return true;
    } catch (URISyntaxException | MalformedURLException e) {
        return false;
    }
}

    public String getAudioURL(String audioID, String metaToken) {
        String url = String.format("https://graph.facebook.com/v19.0/%s", audioID);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + metaToken);

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

    public EVARequestTuple getAudioIDorSTT(String audioID, BrokerConfiguration brokerConfig)
    {
        String audioSTTServer = brokerConfig.getSTTConfig().getUrl();
        Boolean audioSTTEnabled = brokerConfig.getSTTConfig().getEnabled();
        String metaToken = brokerConfig.getMetaConfig().getAccessToken();

        String audioURL = getAudioURL(audioID, metaToken);
        log.info("Audio URL: {}", audioURL);
        log.info("Audio STT Enabled: {}", audioSTTEnabled);
        log.info("Audio STT Server: {}", audioSTTServer);


        if (audioSTTEnabled) {
            
            if (audioSTTServer != null) {
                log.info("audioSTTServer is not null");
                if (isValidURL(audioURL)) {
                    log.info("audioURL is valid");            
                    log.info("calling STT Server: {}", audioSTTServer);	
                    TranscribeResponse Transcription = callSTTServer(audioURL, metaToken, audioSTTServer);
                    if (Transcription != null && Transcription.isSuccess() && Transcription.getMessage() != null) {
                        
                        return new EVARequestTuple(Transcription.getMessage(), null);
                    }
                }
            }
        }
        ObjectNode context = objectMapper.createObjectNode();
        ObjectNode audioMessageNode = context.putObject("audiomessage");
        audioMessageNode.put("audiourl", audioURL);
        return new EVARequestTuple(null, context);
    }



     private TranscribeResponse callSTTServer(String audioURL, String metaToken, String audioSTTServer) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("mediaURL", audioURL);
        payload.put("token", metaToken);

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
                    log.info("STT response received successfully");
                    return objectMapper.readValue(response.getBody(), TranscribeResponse.class);
                } else {
                    log.error("Failed to get STT response: " + response.getStatusCode());
                    return null;
                }
            } catch (Exception e) {
                log.error("Exception occurred while getting STT response", e);
                return null;
            }
    }
        
}