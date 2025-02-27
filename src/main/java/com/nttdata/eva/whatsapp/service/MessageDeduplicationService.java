package com.nttdata.eva.whatsapp.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageDeduplicationService {
    private final ConcurrentHashMap<String, Long> processedMessages = new ConcurrentHashMap<>();
    private static final long MESSAGE_EXPIRY_HOURS = 24;

    public boolean isMessageProcessed(String messageId) {
        cleanup();
        return processedMessages.containsKey(messageId);
    }

    public void markMessageAsProcessed(String messageId) {
        processedMessages.put(messageId, System.currentTimeMillis());
    }

    private void cleanup() {
        long expiryTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(MESSAGE_EXPIRY_HOURS);
        processedMessages.entrySet().removeIf(entry -> entry.getValue() < expiryTime);
    }
} 