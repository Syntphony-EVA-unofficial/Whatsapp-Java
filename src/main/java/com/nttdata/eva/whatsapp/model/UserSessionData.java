package com.nttdata.eva.whatsapp.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSessionData implements Cloneable {
    String welcomeBack;
    String evaSessionCode;
    String evaToken;
    Instant evaTokenTimestamp;
    SessionDestination destination = SessionDestination.BOT; // Default value
    private long lastInteractionTime;
    String exitWord;


}