package com.nttdata.eva.whatsapp.messages;  
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
public class VideoMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static VideoModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to VideoModel
            jsondata = objectMapper.readValue(jsonString, VideoModel.class);

            // Validate the VideoModel
            Set<ConstraintViolation<VideoModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<VideoModel> violation : violations) {
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
        data.put("type", "video");
        ObjectNode videoNode = data.putObject("video");
        videoNode.put("link", jsondata.getLink());
        if (answer.getContent() != null && !answer.getContent().trim().isEmpty()) {
            videoNode.put("caption", answer.getContent());
        }
        return data;
    }


    
    @Data
    public static class VideoModel {

        @NotNull(message = "Type must not be null")
        @Pattern(regexp = "video", message = "Type must be 'video'")
        private String type;

        @NotNull(message = "Link must not be null")
        private String link;
    }

}



// https://developers.facebook.com/docs/whatsapp/cloud-api/messages/video-messages
// {
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "to": "{{wa-user-phone-number}}",
//   "type": "video",
//   "video": {
//     "id" : "<MEDIA_ID>", /* Only if using uploaded media */
//     "link": "<MEDIA_URL>", /* Only if linking to your media */
//     "caption": "<VIDEO_CAPTION_TEXT>"
//   }
// }