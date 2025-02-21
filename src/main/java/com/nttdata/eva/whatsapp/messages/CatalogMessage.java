package com.nttdata.eva.whatsapp.messages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.nttdata.eva.whatsapp.messages.ProductMessage.ProductModel;
import com.nttdata.eva.whatsapp.model.EvaResponseModel.Answer;
import jakarta.validation.ConstraintViolation;
import java.util.Set;


import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


@Slf4j
public class CatalogMessage {


    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static CatalogModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to IL_ListMessage
            jsondata = objectMapper.readValue(jsonString, CatalogModel.class);

            // Validate the IL_ListMessage
            Set<ConstraintViolation<CatalogModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<CatalogModel> violation : violations) {
                    log.debug("Validation Constraing error: {}", violation.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            log.debug("Validation Exception error: {}", e.getMessage());
            return false;
        }
    }

    public static ObjectNode create(ObjectNode data, Answer answer) {
        data.put("type", "interactive");
        ObjectNode interactiveNode = data.putObject("interactive");
        interactiveNode.setAll(objectMapper.convertValue(jsondata.getInteractive(), ObjectNode.class));
        return data;
    }


//InteractiveReplyButtonMessage  : Validation error: Unrecognized field "catalog_id" (class com.nttdata.eva.whatsapp.messages.InteractiveReplyButtonMessage$InteractiveReplyButtonModel$Action), not marked as ignorable (one known property: "buttons"])

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public static class CatalogModel {


    @NotNull(message = "Type must not be null")
    @Pattern(regexp = "interactive", message = "Type must be 'interactive'")
    private String type;

    @NotNull(message = "Interactive must not be null")
    @Valid
    private Interactive interactive;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Interactive {

        @NotNull(message = "Type must not be null")
        @Pattern(regexp = "catalog_message", message = "Type must be 'catalog_message'")
        private String type;

        @NotNull(message = "Body must not be null")
        @Valid
        private Body body;

        @Valid
        private Footer footer;

        @NotNull(message = "Action must not be null")
        @Valid
        private Action action;

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Body {
            @NotNull(message = "Text must not be null in body")
            @Size(max = 1024, message = "Text must not exceed 1024 characters")
            private String text;
        }

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Footer {
            @Size(max = 60, message = "Text must not exceed 60 characters")
            private String text;
        }

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Action {
            @NotNull(message = "Action name must not be null")
            @Pattern(regexp = "catalog_message", message = "Action name must be 'catalog_message'")
            private String name;

            @Valid
            private Parameters parameters;

            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Parameters {
                private String thumbnail_product_retailer_id;
            }
        }
    }
}


}

//https://developers.facebook.com/docs/whatsapp/cloud-api/guides/sell-products-and-services/share-products
// {
//     "messaging_product": "whatsapp",
//     "recipient_type": "individual",
//     "to": "+16505551234",
//     "type": "interactive",
//     "interactive": {
//       "type": "catalog_message",
//       "body": {
//         "text": "Hello! Thanks for your interest. Ordering is easy. Just visit our catalog and add items to purchase."
//       },
//       "action": {
//         "name": "catalog_message",
//         "parameters": {
//           "thumbnail_product_retailer_id": "2lc20305pt"
//         }
//       },
//       "footer": {
//         "text": "Best grocery deals on WhatsApp!"
//       }
//     }
//   }'