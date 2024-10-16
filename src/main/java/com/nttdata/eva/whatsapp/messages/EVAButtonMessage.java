package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class EVAButtonMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean validate(Answer answer) {
        return answer.getButtons() != null && !answer.getButtons().isEmpty();
    }

    public static ObjectNode create(ObjectNode data, Answer answer) {
        data.put("type", "interactive");
        ObjectNode interactiveNode = data.putObject("interactive");
        interactiveNode.put("type", "button");

        ObjectNode bodyNode = interactiveNode.putObject("body");
        bodyNode.put("text", answer.getContent());

        ObjectNode actionNode = interactiveNode.putObject("action");
        
        ArrayNode buttons = generateButtons(answer.getButtons());
        actionNode.set("buttons", buttons);

        return data;
    }

    private static ArrayNode generateButtons(List<Map<String, Object>> EvaButtons) {
        ArrayNode AppleButtons = objectMapper.createArrayNode();
        for (int i = 0; i < EvaButtons.size() && i < 3; i++) { // Limit to 3 buttons
            ObjectNode buttonNode = objectMapper.createObjectNode();
            Map<String, Object> buttonMap = EvaButtons.get(i);
            
            buttonNode.put("type", "reply");
            ObjectNode replyNode = buttonNode.putObject("reply");
            replyNode.put("id", buttonMap.get("value").toString());
            replyNode.put("title", buttonMap.get("name").toString());

            AppleButtons.add(buttonNode);
        }
        return AppleButtons;
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