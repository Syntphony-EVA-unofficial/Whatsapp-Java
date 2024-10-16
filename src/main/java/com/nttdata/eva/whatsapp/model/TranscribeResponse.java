package com.nttdata.eva.whatsapp.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscribeResponse {
    private String lang;
    private String message;
    private boolean success;
}