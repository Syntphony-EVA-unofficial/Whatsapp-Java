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

import java.util.Map;
import java.util.Set;

@Slf4j
public class LocationMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static LocationMessageModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to LocationMessageModel
            jsondata = objectMapper.readValue(jsonString, LocationMessageModel.class);

            // Validate the LocationMessageModel
            Set<ConstraintViolation<LocationMessageModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<LocationMessageModel> violation : violations) {
                    log.info("Validation error: {}", violation.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            log.info("Validation error: {}", e.getMessage());
            return false;
        }
    }

    public static ObjectNode create(ObjectNode data, Answer answer) {
        data.put("type", "location");
        ObjectNode locationNode = data.putObject("location");
        locationNode.put("latitude", jsondata.getLocation().getLatitude());
        locationNode.put("longitude", jsondata.getLocation().getLongitude());
        locationNode.put("name", jsondata.getLocation().getName());
        locationNode.put("address", jsondata.getLocation().getAddress());

        return data;
    }

    @Data
    public static class LocationModel {
        @NotNull(message = "Latitude must not be null")
        private Double latitude;

        @NotNull(message = "Longitude must not be null")
        private Double longitude;

        @NotNull(message = "Name must not be null")
        private String name;

        @NotNull(message = "Address must not be null")
        private String address;
    }

    @Data
    public static class LocationMessageModel {
        @NotNull(message = "Type must not be null")
        private String type;

        @NotNull(message = "Location must not be null")
        private LocationModel location;
    }
}

// https://developers.facebook.com/docs/whatsapp/cloud-api/messages/location-messages
// {
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "to": "<WHATSAPP_USER_PHONE_NUMBER>",
//   "type": "location",
//   "location": {
//     "latitude": "<LOCATION_LATITUDE>",
//     "longitude": "<LOCATION_LONGITUDE>",
//     "name": "<LOCATION_NAME>",
//     "address": "<LOCATION_ADDRESS>"
//   }
// }