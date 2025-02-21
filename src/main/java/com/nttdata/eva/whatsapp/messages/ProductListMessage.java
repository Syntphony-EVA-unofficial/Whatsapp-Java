package com.nttdata.eva.whatsapp.messages;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

@Slf4j
public class ProductListMessage {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private static ProductListModel jsondata;

    public static boolean validate(Answer answer) {
        try {
            String jsonString = objectMapper.writeValueAsString(answer.getTechnicalText());
            jsondata = objectMapper.readValue(jsonString, ProductListModel.class);
            Set<ConstraintViolation<ProductListModel>> violations = validator.validate(jsondata);

            if (violations.isEmpty()) {
                return true;
            } else {
                for (ConstraintViolation<ProductListModel> violation : violations) {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductListModel {
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
            @Pattern(regexp = "product_list", message = "Type must be 'product_list'")
            private String type;

            @NotNull(message = "Header must not be null")
            @Valid
            private Header header;

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
                @NotNull(message = "Catalog ID must not be null")
                private String catalog_id;

                @NotEmpty(message = "Sections must not be empty")
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


// https://developers.facebook.com/docs/whatsapp/cloud-api/guides/sell-products-and-services/share-products
// {
//     "messaging_product": "whatsapp",
//     "recipient_type": "individual",
//     "to": "PHONE_NUMBER",
//     "type": "interactive",
//     "interactive": {
//       "type": "product_list",
//       "header":{
//         "type": "text",
//         "text": "HEADER_CONTENT"
//       },
//       "body": {
//         "text": "BODY_CONTENT"
//       },
//       "footer": {
//         "text": "FOOTER_CONTENT"
//       },
//       "action": {
//         "catalog_id": "CATALOG_ID",
//         "sections": [
//           {
//             "title": "SECTION_TITLE",
//             "product_items": [
//               { "product_retailer_id": "PRODUCT-SKU" },
//               { "product_retailer_id": "PRODUCT-SKU" },
//               ...
//             ]
//           },
//           {
//             "title": "SECTION_TITLE",
//             "product_items": [
//               { "product_retailer_id": "PRODUCT-SKU" },
//               { "product_retailer_id": "PRODUCT-SKU" },
//               ...
//             ]
//           }
//         ]
//       }
//     }
//   }