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
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;
import jakarta.validation.ConstraintViolation;
import java.util.Set;


import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


@Slf4j
public class ProductMessage {


    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static ProductModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            // Convert JSON string to IL_ListMessage
            jsondata = objectMapper.readValue(jsonString, ProductModel.class);

            // Validate the IL_ListMessage
            Set<ConstraintViolation<ProductModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<ProductModel> violation : violations) {
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


//InteractiveReplyButtonMessage  : Validation error: Unrecognized field "catalog_id" (class com.nttdata.eva.whatsapp.messages.InteractiveReplyButtonMessage$InteractiveReplyButtonModel$Action), not marked as ignorable (one known property: "buttons"])

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public static class ProductModel {


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
        @Pattern(regexp = "product|product_list", message = "Type must be 'product' or 'product_list'")
        private String type;

        @NotNull(message = "Body must not be null")
        @Valid
        private Body body;

        @Valid
        private Footer footer;

        @Valid
        private Header header;

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
        public static class Header {
            @NotNull(message = "Type must not be null in header")
            @Pattern(regexp = "text", message = "Header type must be 'text'")
            private String type;

            @NotNull(message = "Text must not be null in header")
            @Size(max = 60, message = "Text must not exceed 60 characters")
            private String text;
        }

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Action {
            @NotNull(message = "Catalog ID must not be null")
            private String catalog_id;

            private String product_retailer_id;

            @Valid
            private List<Section> sections;

            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Section {
                @NotNull(message = "Title must not be null in section")
                private String title;

                @NotEmpty(message = "Product items must not be empty")
                @Valid
                private List<ProductItem> product_items;

                @Data
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class ProductItem {
                    @NotNull(message = "Product retailer ID must not be null")
                    private String product_retailer_id;
                }
            }
        }
    }
}
}

//https://developers.facebook.com/docs/whatsapp/cloud-api/guides/sell-products-and-services/share-products
// {
//     "messaging_product": "whatsapp",
//     "recipient_type": "individual",
//     "to": "PHONE_NUMBER",
//     "type": "interactive",
//     "interactive": {
//       "type": "product",
//       "body": {
//         "text": "BODY_TEXT"
//       },
//       "footer": {
//         "text": "FOOTER_TEXT"
//       },
//       "action": {
//         "catalog_id": "CATALOG_ID",
//         "product_retailer_id": "ID_TEST_ITEM_1"
//       }
//     }
//   }