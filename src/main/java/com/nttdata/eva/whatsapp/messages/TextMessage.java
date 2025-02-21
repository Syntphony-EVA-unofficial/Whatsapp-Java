package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.EvaResponseModel.Answer;
import com.nttdata.eva.whatsapp.model.WebhookData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectNode create(String text, String from) {
        // Prepare data
        ObjectNode data = objectMapper.createObjectNode();
        CustomHandoverMessage.CustomHandOverModel customHandoverModel = null;
        data.put("messaging_product", "whatsapp");
        data.put("recipient_type", "individual");
        data.put("to", from);
        data.put("type", "text");
        ObjectNode textNode = data.putObject("text");
        textNode.put("preview_url", true);
        textNode.put("body", text);
        return data;
    }

    public static ObjectNode create(ObjectNode data, Answer answer) {

        data.put("type", "text");
        ObjectNode textNode = data.putObject("text");
        textNode.put("preview_url", true);
        textNode.put("body", answer.getContent());
        return data;
    }

    /**
     * Checks if a message is of type text and matches the specified content
     * 
     * @param message     The WhatsApp message to check
     * @param textToMatch The text to compare against
     * @return true if message is text type and content matches
     */
    public static boolean isTextMessageMatching(WebhookData.Message message, String textToMatch) {
        if (message == null || textToMatch == null) {
            return false;
        }

        if (!"text".equals(message.getType())) {
            return false;
        }

        String messageText = extractTextContent(message);
        return textToMatch.equalsIgnoreCase(messageText);
    }

    /**
     * Extracts text content from a WhatsApp message
     * 
     * @param message The WhatsApp message
     * @return The text content or null if not found
     */
    public static String extractTextContent(WebhookData.Message message) {
        if (message == null || message.getText() == null) {
            return null;
        }

        return (String) message.getText().get("body");
    }
}

// https://developers.facebook.com/docs/whatsapp/cloud-api/messages/text-messages
// {
// "messaging_product": "whatsapp",
// "recipient_type": "individual",
// "to": "<WHATSAPP_USER_PHONE_NUMBER>",
// "type": "text",
// "text": {
// "preview_url": <ENABLE_LINK_PREVIEW>,
// "body": "<BODY_TEXT>"
// }
// }