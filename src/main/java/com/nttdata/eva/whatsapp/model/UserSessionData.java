package com.nttdata.eva.whatsapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import com.nttdata.eva.whatsapp.model.SessionDestination;

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

    @Override
    public UserSessionData clone() {
        try {
            UserSessionData cloned = (UserSessionData) super.clone();
            // Deep copy of mutable fields if necessary
            cloned.evaTokenTimestamp = (this.evaTokenTimestamp != null) ? Instant.from(this.evaTokenTimestamp) : null;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should never happen
        }
    }

    public void deleteSessionCode() {
        this.evaSessionCode = null;
    }

}