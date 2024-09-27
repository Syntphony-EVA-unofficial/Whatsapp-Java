package com.nttdata.eva.whatsapp.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

@Slf4j
@Service
public class WebhookUtils {

    @Value("${facebook.appsecret}") 
    private String appSecret;
    
   
    public boolean validateSignature(String payload, String signature, String appSecret) {
        try {
            // Create a new Mac instance with HMAC SHA-256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.ISO_8859_1), "HmacSHA256");
            mac.init(secretKeySpec);

            // Compute the HMAC on the payload
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String expectedSignature = hexString.toString();

            // Compare the computed signature with the provided signature
            return MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error validating signature", e);
            return false;
        }
    }



    public boolean checkSignature(HttpServletRequest request,  String payload) {
    try {
        String signature = request.getHeader("X-Hub-Signature-256");
        if (signature != null && signature.startsWith("sha256=")) {
            signature = signature.substring(7); // Removing 'sha256='
        } else {
            log.error("Invalid signature format");
            return false;
        }

        if (validateSignature(payload, signature, appSecret)) {
            log.debug("Signature verification passed!");
            return true;
        } else {
            log.error("Signature verification failed!");
            return false;
        }
    } catch (Exception e) {
        log.error("An error occurred while validating the Signature request.", e);
        return false;
    }
    }

    public Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }
}