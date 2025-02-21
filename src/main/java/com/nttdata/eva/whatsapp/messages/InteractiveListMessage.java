package com.nttdata.eva.whatsapp.messages;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.nttdata.eva.whatsapp.model.EvaResponseModel.Answer;

import java.util.List;
import java.util.Set;

@Slf4j
public class InteractiveListMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static IL_ListMessage jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to IL_ListMessage
            jsondata = objectMapper.readValue(jsonString, IL_ListMessage.class);

            // Validate the IL_ListMessage
            Set<ConstraintViolation<IL_ListMessage>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<IL_ListMessage> violation : violations) {
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
        interactiveNode.setAll(objectMapper.convertValue(jsondata.getInteractive(), ObjectNode.class));
        return data;
    }

    @Data
    public static class IL_TextandType {
        private String type;
        @NotNull(message = "Text must not be null")
        private String text;
    }

    @Data
    public static class IL_TextSingle {
        @NotNull(message = "Text must not be null")
        private String text;
    }

    @Data
    public static class IL_Row {
        @NotNull(message = "ID must not be null")
        private String id;
        @NotNull(message = "Title must not be null")
        private String title;
        private String description;
    }

    @Data
    public static class IL_Section {
        @NotNull(message = "Title must not be null")
        private String title;
        @NotNull(message = "Rows must not be null")
        private List<IL_Row> rows;
    }

    @Data
    public static class IL_Action {
        @NotNull(message = "Button must not be null")
        private String button;
        @NotNull(message = "Sections must not be null")
        private List<IL_Section> sections;
    }

    @Data
    public static class IL_Interactive {
        @Pattern(regexp = "list", message = "Type must be 'list'")
        private String type;
        @NotNull(message = "Header must not be null")
        private IL_TextandType header;
        @NotNull(message = "Body must not be null")
        private IL_TextSingle body;
        private IL_TextSingle footer;
        @NotNull(message = "Action must not be null")
        private IL_Action action;
    }

    @Data
    public static class IL_ListMessage {
        @NotNull(message = "Interactive must not be null")
        private IL_Interactive interactive;
    }
}



// https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-list-messages
// {
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "to": "<WHATSAPP_USER_PHONE_NUMBER>",
//   "type": "interactive",
//   "interactive": {
//     "type": "list",
//     "header": {
//       "type": "text",
//       "text": "<MESSAGE_HEADER_TEXT"
//     },
//     "body": {
//       "text": "<MESSAGE_BODY_TEXT>"
//     },
//     "footer": {
//       "text": "<MESSAGE_FOOTER_TEXT>"
//     },
//     "action": {
//       "sections": [
//         {
//           "title": "<SECTION_TITLE_TEXT>",
//           "rows": [
//             {
//               "id": "<ROW_ID>",
//               "title": "<ROW_TITLE_TEXT>",
//               "description": "<ROW_DESCRIPTION_TEXT>"
//             }
//             /* Additional rows would go here*/
//           ]
//         }
//         /* Additional sections would go here */
//       ],
//       "button": "<BUTTON_TEXT>",
//     }
//   }
// }