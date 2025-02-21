package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.EvaResponseModel.Answer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set; // Import the Set type

@Slf4j
public class InteractiveReplyButtonMessage  {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static InteractiveReplyButtonModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to ImageModel
            jsondata = objectMapper.readValue(jsonString, InteractiveReplyButtonModel.class);

            // Validate the ImageModel
            Set<ConstraintViolation<InteractiveReplyButtonModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<InteractiveReplyButtonModel> violation : violations) {
                    log.debug("Constrain Validation error: {}", violation.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            log.debug("Exception Validation error: {}", e.getMessage());
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
@NoArgsConstructor
@AllArgsConstructor
public static class InteractiveReplyButtonModel {

    @NotNull(message = "Type must not be null")
    @Pattern(regexp = "interactive", message = "Type must be 'interactive'")
    private String type;

    @Valid
    @NotNull
    private Interactive interactive;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interactive {

        @NotBlank
        @Pattern(regexp = "button", message = "interactive.type must be 'button'")
        private String type;

        @Valid
        private Header header;

        @Valid
        @NotNull
        private Body body;

        @Valid
        private Footer footer;

        @Valid
        @NotNull
        private Action action;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        @NotBlank(message = "Header type must not be blank")
        @Pattern(regexp = "document|image|text|video", message = "Header type must be one of 'document', 'image', 'text', 'video'")
        private String type;

        @Valid
        private Document document;

        @Valid
        private Image image;

        @Valid
        private Video video;

        @Valid
        private String text;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Document {
            private String id;

            private String link; // Optional URL link for document

            private String caption;

            private String filename;
        }

        @Data
        public static class Image {
            private String id;

            private String link; // Optional URL link for image

            private String caption;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Video {
            private String id;

            private String link; // Optional URL link for video

            private String caption;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {

        @NotBlank
        @Size(max = 1024, message = "Body text must not exceed 1024 characters")
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Footer {

        @Size(max = 60, message = "Footer text must not exceed 60 characters")
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {

        @Valid
        @Size(max = 3, message = "A maximum of 3 buttons is allowed")
        private List<Button> buttons;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Button {

        @NotBlank
        @Pattern(regexp = "^reply$", message = "Button type must be 'reply'")
        private String type;

        @Valid
        @NotNull
        private Reply reply;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reply {

        @NotBlank
        @Size(max = 256, message = "Button ID must not exceed 256 characters")
        private String id;

        @NotBlank
        @Size(max = 20, message = "Button label must not exceed 20 characters")
        private String title;
    }
}
}


// https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-reply-buttons-messages
// {
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "to": "<WHATSAPP_USER_PHONE_NUMBER>",
//   "type": "interactive",
//   "interactive": {
//     "type": "button",
//     "header": {<MESSAGE_HEADER>},
//     "body": {
//       "text": "<BODY_TEXT>"
//     },
//     "footer": {
//       "text": "<FOOTER_TEXT>"
//     },
//     "action": {
//       "buttons": [
//         {
//           "type": "reply",
//           "reply": {
//             "id": "<BUTTON_ID>",
//             "title": "<BUTTON_LABEL_TEXT>"
//           }
//         }
//       ]
//     }
//   }
// }