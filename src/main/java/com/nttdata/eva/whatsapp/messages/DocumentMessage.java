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
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.Set;


@Slf4j
public class DocumentMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static DocumentModel documentJsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to VideoModel
            documentJsondata = objectMapper.readValue(jsonString, DocumentModel.class);

            // Validate the VideoModel
            Set<ConstraintViolation<DocumentModel>> violations = validator.validate(documentJsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<DocumentModel> violation : violations) {
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
        data.put("type", "document");
        ObjectNode documentNode = data.putObject("document");
        documentNode.put("link", documentJsondata.getLink());
        if (answer.getContent() != null && !answer.getContent().trim().isEmpty()) {
            documentNode.put("caption", answer.getContent());
        }

        if (documentJsondata.getFilename() != null) 
        {
        documentNode.put("filename", documentJsondata.getFilename());
        }
        return data;
    }
// "document": {
//     "id" : "<MEDIA_ID>", /* Only if using uploaded media */
//     "link": "<MEDIA_URL>", /* Only if linking to your media */
//     "caption": "<DOCUMENT_CAPTION>",
//     "filename": "<DOCUMENT_FILENAME>"
//   }

    
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentModel {

        @NotNull(message = "Type must not be null")
        @Pattern(regexp = "document", message = "Type must be 'document'")
        private String type;

        @NotNull(message = "Link must not be null")
        private String link;

        private String filename;
    }

}


// {
//     "messaging_product": "whatsapp",
//     "recipient_type": "individual",
//     "to": "<WHATSAPP_USER_PHONE_NUMBER>",
//     "type": "document",
//     "document": {
//       "id" : "<MEDIA_ID>", /* Only if using uploaded media */
//       "link": "<MEDIA_URL>", /* Only if linking to your media */
//       "caption": "<DOCUMENT_CAPTION>",
//       "filename": "<DOCUMENT_FILENAME>"
//     }
//   }