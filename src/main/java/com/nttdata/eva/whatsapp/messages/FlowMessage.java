package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;
import jakarta.validation.ConstraintViolation;
import java.util.Set;


@Slf4j
public class FlowMessage {


    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static FlowModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to IL_ListMessage
            jsondata = objectMapper.readValue(jsonString, FlowModel.class);

            // Validate the IL_ListMessage
            Set<ConstraintViolation<FlowModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<FlowModel> violation : violations) {
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
        data.put("type", "interactive");
        ObjectNode interactiveNode = data.putObject("interactive");
        interactiveNode.setAll(objectMapper.convertValue(jsondata.getInteractive(), ObjectNode.class));
        return data;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)  // Ensures nulls are not serialized
    public static class FlowModel {
    
      
        @Pattern(regexp = "interactive", message = "Type must be interactive")
        @NotNull(message = "Type must not be null")
        private String type;  // Mandatory
        
        @NotNull(message = "Interactive must not be null")
        private Interactive interactive;
    
        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Interactive {
    
            @Pattern(regexp = "flow", message = "Type must be flow")
            @NotNull(message = "Type must not be null")
            private String type;  // Mandatory
            
            private Header header;  // Optional
            
            @NotNull(message = "Body must not be null")
            private Body body;  // Mandatory
            
            private Footer footer;  // Optional
            
            @NotNull(message = "Action must not be null")
            private Action action;  // Mandatory
    
            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Header {
                private String type;  // Optional
                private String text;  // Optional, but required if 'header' is used
            }
    
            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Body {
                @NotNull(message = "Text must not be null in body")
                private String text;  // Mandatory
            }
    
            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Footer {
                private String text;  // Optional
            }
    
            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Action {
                @NotNull(message = "Action name must not be null")
                private String name;  // Mandatory
                
                @NotNull(message = "Parameters must not be null")
                private Parameters parameters;  // Mandatory
    
                @Data
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class Parameters {
                    @NotNull(message = "Flow message version must not be null")
                    private String flow_message_version;  // Mandatory
                    
                    @NotNull(message = "Flow token must not be null")
                    private String flow_token;  // Mandatory
                    
                    @NotNull(message = "Flow ID must not be null")
                    private String flow_id;  // Mandatory
                    
                    @NotNull(message = "Flow CTA must not be null")
                    private String flow_cta;  // Mandatory
                    
                        @NotNull(message = "Flow action must not be null")
                    private FlowActionType flow_action;  // Use enum for validation
                    
                    private FlowActionPayload flow_action_payload;  // Mandatory if 'flow_action' is 'navigate'

                    @Data
                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    public static class FlowActionPayload {
                        private String screen;  // Mandatory if 'flow_action' is 'navigate'
                        private Map<String, Object> data;  // Optional
                    }
                }
            }
        }
    }

    public enum FlowActionType {
    NAVIGATE("navigate"),
    DATA_EXCHANGE("data_exchange");

    private final String value;

    FlowActionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @JsonCreator
    public static FlowActionType fromValue(String value) {
        for (FlowActionType type : FlowActionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown enum type " + value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
}



//https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-flow-messages
// curl -X  POST \
//  'https://graph.facebook.com/v19.0/FROM_PHONE_NUMBER_ID/messages' \
//  -H 'Authorization: Bearer ACCESS_TOKEN' \
//  -H 'Content-Type: application/json' \
//  -d '{
//   "recipient_type": "individual",
//   "messaging_product": "whatsapp",
//   "to": "PHONE_NUMBER",
//   "type": "interactive",
//   "interactive": {
//     "type": "flow",
//     "header": {
//       "type": "text",
//       "text": "Flow message header"
//     },
//     "body": {
//       "text": "Flow message body"
//     },
//     "footer": {
//       "text": "Flow message footer"
//     },
//     "action": {
//       "name": "flow",
//       "parameters": {
//         "flow_message_version": "3",
//         "flow_token": "AQAAAAACS5FpgQ_cAAAAAD0QI3s.",
//         "flow_id": "1",
//         "flow_cta": "Book!",
//         "flow_action": "navigate",
//         "flow_action_payload": {
//           "screen": "<SCREEN_NAME>",
//           "data": { 
//             "product_name": "name",
//             "product_description": "description",
//             "product_price": 100
//           }
//         }
//       }
//     }
//   }
// }'