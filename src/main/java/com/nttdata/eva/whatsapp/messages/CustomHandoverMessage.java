package com.nttdata.eva.whatsapp.messages;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomHandoverMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to VideoModel
            CustomHandOverModel jsondata = objectMapper.readValue(jsonString, CustomHandOverModel.class);

            // Validate the VideoModel
            Set<ConstraintViolation<CustomHandOverModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<CustomHandOverModel> violation : violations) {
                    log.debug("Validation error: {}", violation.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            log.debug("Validation error: {}", e.getMessage());
            return false;
        }
    }

    public static CustomHandOverModel create(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to HandOverModel
            CustomHandOverModel jsondata = objectMapper.readValue(jsonString, CustomHandOverModel.class);

            // Convert HandOverModel back to ObjectNode
            return jsondata;
        } catch (Exception e) {
            log.debug("Error creating ObjectNode: {}", e.getMessage());
            return null;
        }
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomHandOverModel {

        @NotNull(message = "Type must not be null")
        @Pattern(regexp = "handover", message = "Type must be 'handover'")
        private String type;

        @NotNull(message = "handover must not be null")
        private String handover;

        @NotNull(message = "exit_command must not be null")
        private String exit_command;

        private String welcomeback;
    }

}

// {
//     "type":"handover",
//     "handover": "SYNTPHONY_PORTAL",
//     "exit_command":"exit",
//     "welcomeback":"You are now connected with the Virtual Assistant"
//     }