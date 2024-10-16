package com.nttdata.eva.whatsapp.messages;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nttdata.eva.whatsapp.model.ResponseModel.Answer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextMessage {

    public static ObjectNode create(ObjectNode data, Answer answer) {

        data.put("type", "text");
        ObjectNode textNode = data.putObject("text");
        textNode.put("preview_url", true);
        textNode.put("body", answer.getContent());
        return data;
    }

    
}



// https://developers.facebook.com/docs/whatsapp/cloud-api/messages/text-messages
// {
//   "messaging_product": "whatsapp",
//   "recipient_type": "individual",
//   "to": "<WHATSAPP_USER_PHONE_NUMBER>",
//   "type": "text",
//   "text": {
//     "preview_url": <ENABLE_LINK_PREVIEW>,
//     "body": "<BODY_TEXT>"
//   }
// }