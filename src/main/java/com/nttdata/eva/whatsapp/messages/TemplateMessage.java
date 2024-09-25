package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TemplateMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static TM_TemplateMessage jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to TM_TemplateMessage
            jsondata = objectMapper.readValue(jsonString, TM_TemplateMessage.class);

            // Validate the TM_TemplateMessage
            Set<ConstraintViolation<TM_TemplateMessage>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<TM_TemplateMessage> violation : violations) {
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
        data.put("type", "template");
        ObjectNode templateNode = data.putObject("template");
        templateNode.setAll(objectMapper.convertValue(jsondata.getTemplate(), ObjectNode.class));
        return data;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TM_Component {
        @NotNull(message = "Type must not be null")
        private String type;

        @NotNull(message = "Parameters must not be null")
        private List<Map<String, Object>> parameters;

        private String sub_type;
        private String index;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TM_Language {
        @NotNull(message = "Code must not be null")
        private String code;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TM_Template {
        @NotNull(message = "namespace must not be null")
        private String namespace;

        @NotNull(message = "Name must not be null")
        private String name;

        @NotNull(message = "Language must not be null")
        private TM_Language language;

        @NotNull(message = "Components must not be null")
        private List<TM_Component> components;
    }

    @Data
    public static class TM_TemplateMessage {
        @NotNull(message = "Type must not be null")
        private String type;

        @NotNull(message = "Template must not be null")
        private TM_Template template;
    }
}











// https://developers.facebook.com/docs/whatsapp/cloud-api/guides/send-message-templates
// curl -X  POST \
//  'https://graph.facebook.com/v20.0/FROM_PHONE_NUMBER_ID/messages' \
//  -H 'Authorization: Bearer ACCESS_TOKEN' \
//  -H 'Content-Type: application/json' \
//  -d '{
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "to": "PHONE_NUMBER",
//   "type": "template",
//   "template": {
//     "name": "TEMPLATE_NAME",
//     "language": {
//       "code": "LANGUAGE_AND_LOCALE_CODE"
//     },
//     "components": [
//       {
//         "type": "body",
//         "parameters": [
//           {
//             "type": "text",
//             "text": "text-string"
//           },
//           {
//             "type": "currency",
//             "currency": {
//               "fallback_value": "VALUE",
//               "code": "USD",
//               "amount_1000": NUMBER
//             }
//           },
//           {
//             "type": "date_time",
//             "date_time": {
//               "fallback_value": "DATE"
//             }
//           }
//         ]
//       }
//     ]
//   }
// }'