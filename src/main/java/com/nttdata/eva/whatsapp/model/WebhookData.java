package com.nttdata.eva.whatsapp.model;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class WebhookData {
    private String object;
    private List<Entry> entry;

    @Data
    public static class Entry {
        private String id;
        private List<Change> changes;
    }

    @Data
    public static class Change {
        private Value value;
        private String field;
    }

    
    @Data
    public static class Location {
        private double latitude;
        private double longitude;
    }

    @Data
    public static class Message {
        private String from;
        private String id;
        private String timestamp;
        private String type;

        private Map<String, Object> interactive;
        private Map<String, Object> text;
        private ImageData image;
        private Map<String, Object> audio;
        private Map<String, Object> document;
        private Location location;
        private Context context; 
        private Order order; 
        private Button button; // Added Button structure

        @Data
        public static class Button {
            private String payload;
            private String text;
        }

        @Data
        public static class Context {
            private String from;
            private String id;
            private ReferredProduct referred_product;

            @Data
            public static class ReferredProduct {
                private String catalog_id;
                private String product_retailer_id;
            }
        }
        @Data
        public static class Order {
            private String catalog_id;
            private String text;
            private List<ProductItem> product_items;

            @Data
            public static class ProductItem {
                private String product_retailer_id;
                private int quantity;
                private double item_price;
                private String currency;
            }
        }
    }

    @Data
    public class ImageData {
    private String caption;
    private String sha256;
    private String id;
    private String mime_type;
    }


    @Data
    public static class Value {
        private String messaging_product;
        private Metadata metadata;
        private List<Contact> contacts;
        private List<Message> messages;
        private List<Status> statuses;
    }

    @Data
    public static class Metadata {
        private String display_phone_number;
        private String phone_number_id;
    }

    @Data
    public static class Contact {
        private Profile profile;
        private String wa_id;
    }

    @Data
    public static class Profile {
        private String name;
    }

    @Data
    public static class Status {
        private String id;
        private String recipient_id;
        private String status;
        private String timestamp;
        private Conversation conversation;
        private Pricing pricing;
    }

    @Data
    public static class Conversation {
        private String id;
        private String expiration_timestamp;
        private Origin origin;
    }

    @Data
    public static class Origin {
        private String type;
    }

    @Data
    public static class Pricing {
        private String pricing_model;
        private boolean billable;
        private String category;
    }
}

//Payload reference
//https://developers.facebook.com/docs/whatsapp/webhooks/
// #region Sample payload Text
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//         {
//             "id": "396803506838931",
//             "changes": [
//                 {
//                     "value": {
//                         "messaging_product": "whatsapp",
//                         "metadata": {
//                             "display_phone_number": "443300270175",
//                             "phone_number_id": "377135318814227"
//                         },
//                         "contacts": [
//                             {
//                                 "profile": {
//                                     "name": "Jorge Alviarez"
//                                 },
//                                 "wa_id": "819046050746"
//                             }
//                         ],
//                         "messages": [
//                             {
//                                 "from": "819046050746",
//                                 "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAEhgWM0VCMDEwOEIyMjcxRTU2OEMzMEZDMgA=",
//                                 "timestamp": "1726505663",
//                                 "text": {
//                                     "body": "text"
//                                 },
//                                 "type": "text"
//                             }
//                         ]
//                     },
//                     "field": "messages"
//                 }
//             ]
//         }
//     ]
// }
// endregion

// #region Sample payload Image
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//         {
//             "id": "396803506838931",
//             "changes": [
//                 {
//                     "value": {
//                         "messaging_product": "whatsapp",
//                         "metadata": {
//                             "display_phone_number": "443300270175",
//                             "phone_number_id": "377135318814227"
//                         },
//                         "contacts": [
//                             {
//                                 "profile": {
//                                     "name": "Jorge Alviarez"
//                                 },
//                                 "wa_id": "819046050746"
//                             }
//                         ],
//                         "messages": [
//                             {
//                                 "from": "819046050746",
//                                 "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAEhggOTE4RjQwRDVBREY0OTY1MUM5REU4MUZFMEYwNEJERUMA",
//                                 "timestamp": "1726505863",
//                                 "type": "image",
//                                 "image": {
//                                     "mime_type": "image/jpeg",
//                                     "sha256": "XI5zVScjPljtTOamVAfjuFJDXI4CIJjQjpiZAjjsXlI=",
//                                     "id": "1519915665311104"
//                                 }
//                             }
//                         ]
//                     },
//                     "field": "messages"
//                 }
//             ]
//         }
//     ]
// }
// endregion 

// #region Sample payload Audio
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//         {
//             "id": "396803506838931",
//             "changes": [
//                 {
//                     "value": {
//                         "messaging_product": "whatsapp",
//                         "metadata": {
//                             "display_phone_number": "443300270175",
//                             "phone_number_id": "377135318814227"
//                         },
//                         "contacts": [
//                             {
//                                 "profile": {
//                                     "name": "Jorge Alviarez"
//                                 },
//                                 "wa_id": "819046050746"
//                             }
//                         ],
//                         "messages": [
//                             {
//                                 "from": "819046050746",
//                                 "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAEhggQ0VERjBFNDk2M0RGNEZCQkRFNzEzRTc2RkU5MjFFNjIA",
//                                 "timestamp": "1726506047",
//                                 "type": "audio",
//                                 "audio": {
//                                     "mime_type": "audio/ogg; codecs=opus",
//                                     "sha256": "UVX8nD2Obr8xOSBWzvjaaELN5zcfJiDFl/1/mTAvvl4=",
//                                     "id": "888403122625223",
//                                     "voice": true
//                                 }
//                             }
//                         ]
//                     },
//                     "field": "messages"
//                 }
//             ]
//         }
//     ]
// }
// endregion

// region Sample payload Document
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//         {
//             "id": "396803506838931",
//             "changes": [
//                 {
//                     "value": {
//                         "messaging_product": "whatsapp",
//                         "metadata": {
//                             "display_phone_number": "443300270175",
//                             "phone_number_id": "377135318814227"
//                         },
//                         "contacts": [
//                             {
//                                 "profile": {
//                                     "name": "Jorge Alviarez"
//                                 },
//                                 "wa_id": "819046050746"
//                             }
//                         ],
//                         "messages": [
//                             {
//                                 "from": "819046050746",
//                                 "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAEhggNzFFQUI5QkMxNjEzQTNCRkEzMjk3QTQ1RjMzRjhGNDcA",
//                                 "timestamp": "1726506116",
//                                 "type": "document",
//                                 "document": {
//                                     "filename": "1. PRESENTACION SUE\u00d1A MARIN..pdf",
//                                     "mime_type": "application/pdf",
//                                     "sha256": "pp8c2xMjXu3x0pMvljAzP11z96p9/+OdftnP1EfkTj0=",
//                                     "id": "8095888260519062"
//                                 }
//                             }
//                         ]
//                     },
//                     "field": "messages"
//                 }
//             ]
//         }
//     ]
// }
// endregion

// region Sample payload Location
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//         {
//             "id": "396803506838931",
//             "changes": [
//                 {
//                     "value": {
//                         "messaging_product": "whatsapp",
//                         "metadata": {
//                             "display_phone_number": "443300270175",
//                             "phone_number_id": "377135318814227"
//                         },
//                         "contacts": [
//                             {
//                                 "profile": {
//                                     "name": "Jorge Alviarez"
//                                 },
//                                 "wa_id": "819046050746"
//                             }
//                         ],
//                         "messages": [
//                             {
//                                 "from": "819046050746",
//                                 "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAEhggQjIzQTEyMEFGODIxNDdFNDFDMDUwOTBGMEJGOUJCN0UA",
//                                 "timestamp": "1726506199",
//                                 "location": {
//                                     "latitude": 35.6589903,
//                                     "longitude": 139.7959751
//                                 },
//                                 "type": "location"
//                             }
//                         ]
//                     },
//                     "field": "messages"
//                 }
//             ]
//         }
//     ]
// }
// endregion


//https://developers.facebook.com/docs/whatsapp/cloud-api/guides/sell-products-and-services/receive-responses
// region Sample payload Interactive List Reply
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//         {
//             "id": "396803506838931",
//             "changes": [
//                 {
//                     "value": {
//                         "messaging_product": "whatsapp",
//                         "metadata": {
//                             "display_phone_number": "443300270175",
//                             "phone_number_id": "377135318814227"
//                         },
//                         "contacts": [
//                             {
//                                 "profile": {
//                                     "name": "Jorge Alviarez"
//                                 },
//                                 "wa_id": "819046050746"
//                             }
//                         ],
//                         "messages": [
//                             {
//                                 "context": {
//                                     "from": "443300270175",
//                                     "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAERgSMkVEMDI2OEI2QzQ5RDM2NUIxAA=="
//                                 },
//                                 "from": "819046050746",
//                                 "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAEhggNzc3OENBOTMyRTdGMzI3MzI4OThDQkQyQzgzRTI0OTkA",
//                                 "timestamp": "1726506263",
//                                 "type": "interactive",
//                                 "interactive": {
//                                     "type": "list_reply",
//                                     "list_reply": {
//                                         "id": "SECTION_1_ROW_2_ID",
//                                         "title": "SECTION_1_ROW_2_TITLE",
//                                         "description": "SECTION_1_ROW_2_DESCRIPTION"
//                                     }
//                                 }
//                             }
//                         ]
//                     },
//                     "field": "messages"
//                 }
//             ]
//         }
//     ]
// }
// endregion

//region Sample payload Interactive Button Reply
// {
//     "context": {
//         "from": "443300270175",
//         "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAERgSMjFEMEY2MTE0RDMxMURBNEZDAA=="
//     },
//     "from": "819046050746",
//     "id": "wamid.HBgMODE5MDQ2MDUwNzQ2FQIAEhggMDk0REUzNzkxN0NFNDRBMzMwODM2RUUyOTgwMUVCMjUA",
//     "timestamp": "1726506605",
//     "type": "interactive",
//     "interactive": {
//         "type": "button_reply",
//         "button_reply": {
//             "id": "value2",
//             "title": "ButtonName2"
//         }
//     }
// }
//endregion


//region Sample payload Place order
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//       {
//         "id": "<WHATSAPP_BUSINESS_ACCOUNT_ID>",
//         "changes": [
//           {
//             "value": {
//               "messaging_product": "whatsapp",
//               "metadata": {
//                 "display_phone_number": "<BUSINESS_DISPLAY_PHONE_NUMBER>",
//                 "phone_number_id": "<BUSINESS_PHONE_NUMBER_ID>"
//               },
//               "contacts": [
//                 {
//                   "profile": {
//                     "name": "<WHATSAPP_USER_NAME>"
//                   },
//                   "wa_id": "<WHATSAPP_USER_ID>"
//                 }
//               ],
//               "messages": [
//                 {
//                   "from": "<WHATSAPP_USER_PHONE_NUMBER>",
//                   "id": "<WHATSAPP_MESSAGE_ID>",
//                   "timestamp": "<WEBHOOK_TIMESTAMP>",
//                   "type": "order",
//                   "order": {
//                     "catalog_id": "<CATALOG_ID>",
//                     "text": "<MESSAGE_TEXT>",
//                     "product_items": [
//                       {
//                         "product_retailer_id": "<PRODUCT_ID>",
//                         "quantity": <PRODUCT_QUANTITY>,
//                         "item_price": <PRODUCT_PRICE>,
//                         "currency": "<PRODUCT_CURRENCY>"
//                       }
  
//                      /* Additional items would follow, if order contains multiple items */
  
//                     ]
//                   }
//                 }
//               ]
//             },
//             "field": "messages"
//           }
//         ]
//       }
//     ]
//   }
//endregion

//region Sample payload Text message Inquire Product
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//       {
//         "id": "<WHATSAPP_BUSINESS_ACCOUNT_ID>",
//         "changes": [
//           {
//             "value": {
//               "messaging_product": "whatsapp",
//               "metadata": {
//                 "display_phone_number": "<BUSINESS_DISPLAY_PHONE_NUMBER>",
//                 "phone_number_id": "<BUSINESS_PHONE_NUMBER_ID>"
//               },
//               "contacts": [
//                 {
//                   "profile": {
//                     "name": "<WHATSAPP_USER_NAME>"
//                   },
//                   "wa_id": "<WHATSAPP_USER_ID>"
//                 }
//               ],
//               "messages": [
//                 {
  
//                   /* The `context` property is only included if the user tapped a button
//                      on the product details page to send you the message */
//                   "context": {
//                     "from": "<BUSINESS_DISPLAY_PHONE_NUMBER>",
//                     "id": "<CONTEXT_ID>",
//                     "referred_product": {
//                       "catalog_id": "<CATALOG_ID>",
//                       "product_retailer_id": "<PRODUCT_ID>"
//                     }
//                   },
  
//                   "from": "<WHATSAPP_USER_PHONE_NUMBER>",
//                   "id": "<WHATSAPP_MESSAGE_ID>",
//                   "timestamp": "<WEBHOOK_TIMESTAMP>",
//                   "text": {
//                     "body": "<MESSAGE_TEXT>"
//                   },
//                   "type": "text"
//                 }
//               ]
//             },
//             "field": "messages"
//           }
//         ]
//       }
//     ]
//   }
//endregion

//region Sample payload Message status
// {
//     "object": "whatsapp_business_account",
//     "entry": [
//       {
//         "id": "<WHATSAPP_BUSINESS_ACCOUNT_ID>",
//         "changes": [
//           {
//             "value": {
//               "messaging_product": "whatsapp",
//               "metadata": {
//                 "display_phone_number": "<BUSINESS_DISPLAY_PHONE_NUMBER>",
//                 "phone_number_id": "<BUSINESS_PHONE_NUMBER_ID>"
//               },
//               "statuses": [
//                 {
//                   "id": "<WHATSAPP_MESSAGE_ID>",
//                   "recipient_id": "<WHATSAPP_USER_ID>",
//                   "status": "<MESSAGE_STATUS>",
//                   "timestamp": "<WEBHOOK_TIMESTAMP>",
//                   "conversation": {
//                     "id": "<CONVERSATION_ID>",
//                     "expiration_timestamp": "<CONVERSATION_EXPIRATION_TIMESTAMP>",
//                     "origin": {
//                       "type": "<CONVERSATION_ORIGIN>"
//                     }
//                   },
//                   "pricing": {
//                     "pricing_model": "CBP",
//                     "billable": <IS_BILLABLE>,
//                     "category": "<CONVERSATION_CATEGORY>"
//                   }
//                 }
//               ]
//             },
//             "field": "messages"
//           }
//         ]
//       }
//     ]
//   }