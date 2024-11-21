package com.nttdata.eva.whatsapp.model;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;


@Data
public class HandoverMessage {
    private String phoneid;
    private String clientphone;
    private JsonNode evaPayload;
}
