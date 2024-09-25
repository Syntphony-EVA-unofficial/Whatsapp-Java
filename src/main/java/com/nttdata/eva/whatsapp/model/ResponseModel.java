package com.nttdata.eva.whatsapp.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResponseModel {
    private String text; // Allow null values
    private String sessionCode;
    private String userInteractionUUID;
    private Map<String, Object> userInput; // Allow null values
    private NlpResponse nlpResponse;
    private List<Answer> answers;
    private Map<String, Object> context;
    private Map<String, Object> contextReadOnly;

    @Data
    public static class Answer {
        private String content;
        private Map<String, Object> technicalText; // Can be a Map or a String
        private List<Map<String, Object>> buttons;
        private List<Map<String, Object>> quickReply;
        private String description; // Allow null values
        private String type;
        private String interactionId;
        private boolean evaluable;
        private Boolean masked; // Allow null values
    }

    @Data
    public static class NlpResponse {
        private String type;
        private String name;
        private float score;
        private List<Map<String, Object>> entities;
    }
}