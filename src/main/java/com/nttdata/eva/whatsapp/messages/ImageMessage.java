package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.messages.ImageMessage.ImageModel;
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;

import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Set; // Import the Set type

@Slf4j
public class ImageMessage  {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static ImageModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to ImageModel
            jsondata = objectMapper.readValue(jsonString, ImageModel.class);

            // Validate the ImageModel
            Set<ConstraintViolation<ImageModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<ImageModel> violation : violations) {
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
        data.put("type", "image");
        ObjectNode imageNode = data.putObject("image");
        imageNode.put("link", jsondata.getLink());

        if (answer.getContent() != null && !answer.getContent().trim().isEmpty()) {
            imageNode.put("caption", answer.getContent());
        }

        return data;
    }

    @Data
    public static class ImageModel {

        @NotNull(message = "Type must not be null")
        @Pattern(regexp = "image", message = "Type must be 'image'")
        private String type;

        @NotNull(message = "Link must not be null")
        private String link;
    }
}




// https://developers.facebook.com/docs/whatsapp/cloud-api/messages/image-messages
// {
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "to": "<WHATSAPP_USER_PHONE_NUMBER>",
//   "type": "image",
//   "image": {
//     "id" : "<MEDIA_ID>", /* Only if using uploaded media */
//     "link": "<MEDIA_URL>", /* Only if linking to your media */
//     "caption": "<IMAGE_CAPTION_TEXT>"
//   }
// }