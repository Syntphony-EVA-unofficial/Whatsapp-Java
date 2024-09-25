package com.nttdata.eva.whatsapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSessionData implements Cloneable {
    String evaSessionCode;
    String evaToken;
    Instant evaTokenTimestamp;

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
}