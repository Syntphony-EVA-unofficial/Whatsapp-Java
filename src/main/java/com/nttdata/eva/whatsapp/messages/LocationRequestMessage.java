package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class LocationRequestMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static LocationRequestModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to LocationRequestModel
            jsondata = objectMapper.readValue(jsonString, LocationRequestModel.class);

            // Validate the LocationRequestModel
            Set<ConstraintViolation<LocationRequestModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<LocationRequestModel> violation : violations) {
                    log.debug("Validation error: {}", violation.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            log.debug("Validation error: {}", e.getMessage());
            return false;
        }
    }

    public static ObjectNode create(ObjectNode data, Answer answer) {
        data.put("type", "interactive");
        ObjectNode interactiveNode = data.putObject("interactive");
        interactiveNode.put("type", "location_request_message");

        ObjectNode bodyNode = interactiveNode.putObject("body");
        bodyNode.put("text", jsondata.getText());

        ObjectNode actionNode = interactiveNode.putObject("action");
        actionNode.put("name", "send_location");

        return data;
    }

    @Data
    public static class LocationRequestModel {
        @NotNull(message = "Type must not be null")
        private String type;

        @NotNull(message = "Text must not be null")
        private String text;
    }
}

// https://developers.facebook.com/docs/whatsapp/cloud-api/guides/send-messages/location-request-messages
// {
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "type": "interactive",
//   "to": "<WHATSAPP_USER_PHONE_NUMBER>",
//   "interactive": {
//     "type": "location_request_message",
//     "body": {
//       "text": "<BODY_TEXT>"
//     },
//     "action": {
//       "name": "send_location"
//     }
//   }
// }