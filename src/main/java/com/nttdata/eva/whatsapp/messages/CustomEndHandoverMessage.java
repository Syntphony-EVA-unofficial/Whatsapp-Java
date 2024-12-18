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
public class CustomEndHandoverMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to VideoModel
            CustomEndHandOverModel jsondata = objectMapper.readValue(jsonString, CustomEndHandOverModel.class);

            // Validate the VideoModel
            Set<ConstraintViolation<CustomEndHandOverModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<CustomEndHandOverModel> violation : violations) {
                    log.debug("Validation error: {}", violation.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            log.debug("Validation error: {}", e.getMessage());
            return false;
        }
    }

    public static CustomEndHandOverModel create(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to HandOverModel
            CustomEndHandOverModel jsondata = objectMapper.readValue(jsonString, CustomEndHandOverModel.class);

            // Convert HandOverModel back to ObjectNode
            return jsondata;
        } catch (Exception e) {
            log.debug("Error creating ObjectNode: {}", e.getMessage());
            return null;
        }
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomEndHandOverModel {
        @NotNull(message = "Type must not be null")
        @Pattern(regexp = "end_handover", message = "Type must be 'end_handover'")
        private String type;
    }
    
}

// {"type":"end_handover" }


