package com.nttdata.eva.whatsapp.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.BrokerConfiguration;
import com.nttdata.eva.whatsapp.model.EVARequestTuple;
import com.nttdata.eva.whatsapp.model.TranscribeResponse;
import com.nttdata.eva.whatsapp.model.WebhookData;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;



@Slf4j
@Service
public class MessageLogger {

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
    
    @Async
    private void logMessage(ObjectNode payload, String sender, BrokerConfiguration brokerConfig) {
        Boolean messageLoggerEnable = brokerConfig.getMessageLogerConfig().getEnabled();
        if (messageLoggerEnable) {
            String messageLoggerURL = brokerConfig.getMessageLogerConfig().getUrl();

            // Validate the URL
            if (isValidURL(messageLoggerURL)) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                // Add sender to the payload
                payload.put("sender", sender);

                HttpEntity<ObjectNode> requestEntity = new HttpEntity<>(payload, headers);

                try {
                    ResponseEntity<String> response = restTemplate.exchange(messageLoggerURL, HttpMethod.POST, requestEntity, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Message Logger response received successfully");
                    } else {
                        log.error("Failed to get Message Logger response: " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while getting Message Logger response", e);
                }
            }
        }
    }

    public void recordWebhookIncomming(JsonNode webhookData, BrokerConfiguration brokerConfig) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("data", webhookData);
        logMessage(payload, "Client", brokerConfig);
    }

    public void recordAPIMessageOutgoing(ObjectNode whatsappMessagePayload, BrokerConfiguration brokerConfig) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("data", whatsappMessagePayload);
        logMessage(payload, "Syntphony", brokerConfig);
    }
}





//     public EVARequestTuple getAudioIDorSTT(String audioID, BrokerConfiguration brokerConfig)
//     {
//         String audioSTTServer = brokerConfig.getSTTConfig().getUrl();
//         Boolean audioSTTEnabled = brokerConfig.getSTTConfig().getEnabled();
//         String metaToken = brokerConfig.getMetaConfig().getAccessToken();

//         String audioURL = getAudioURL(audioID, metaToken);
//         log.info("Audio URL: {}", audioURL);
//         log.info("Audio STT Enabled: {}", audioSTTEnabled);
//         log.info("Audio STT Server: {}", audioSTTServer);


//         if (audioSTTEnabled) {
            
//             if (audioSTTServer != null) {
//                 log.info("audioSTTServer is not null");
//                 if (isValidURL(audioURL)) {
//                     log.info("audioURL is valid");            
//                     log.info("calling STT Server: {}", audioSTTServer);	
//                     TranscribeResponse Transcription = callSTTServer(audioURL, metaToken, audioSTTServer);
//                     if (Transcription != null && Transcription.isSuccess() && Transcription.getMessage() != null) {
                        
//                         return new EVARequestTuple(Transcription.getMessage(), null);
//                     }
//                 }
//             }
//         }
//         ObjectNode context = objectMapper.createObjectNode();
//         ObjectNode audioMessageNode = context.putObject("audiomessage");
//         audioMessageNode.put("audiourl", audioURL);
//         return new EVARequestTuple(null, context);
//     }



//      private TranscribeResponse callSTTServer(String audioURL, String metaToken, String audioSTTServer) {
        
//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Content-Type", "application/json");

        
//         ObjectNode payload = objectMapper.createObjectNode();
//         payload.put("mediaURL", audioURL);
//         payload.put("token", metaToken);

//         String requestBody;
//             try {
//                 requestBody = objectMapper.writeValueAsString(payload);
//             } catch (Exception e) {
//                 log.error("Failed to create JSON payload", e);
//                 return null;
//             }

//         HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

//             try {
//                 ResponseEntity<String> response = restTemplate.exchange(audioSTTServer, HttpMethod.POST, requestEntity, String.class);
//                 if (response.getStatusCode().is2xxSuccessful()) {
//                     log.info("STT response received successfully");
//                     return objectMapper.readValue(response.getBody(), TranscribeResponse.class);
//                 } else {
//                     log.error("Failed to get STT response: " + response.getStatusCode());
//                     return null;
//                 }
//             } catch (Exception e) {
//                 log.error("Exception occurred while getting STT response", e);
//                 return null;
//             }
//     }
        
// }