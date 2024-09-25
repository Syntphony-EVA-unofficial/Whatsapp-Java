package com.nttdata.eva.whatsapp.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;
import com.nttdata.eva.whatsapp.service.WebhookService;
import com.nttdata.eva.whatsapp.utils.WebhookUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private WebhookUtils webhookUtils;




    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(HttpServletRequest request){
        return webhookService.verify(request);
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handleIncomingUserMessage(@RequestBody String requestBody, HttpServletRequest request) {
        //String payload = getRequestBody(request);

        if (webhookUtils.checkSignature(request, requestBody)) {
            try {
                webhookService.processIncomingMessage(requestBody, request);
                return ResponseEntity.ok("Request processed successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("An error occurred while processing the request.");
            }
        } else {
            return ResponseEntity.ok("Invalid signature.");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("This is a test endpoint");
    }

    
}