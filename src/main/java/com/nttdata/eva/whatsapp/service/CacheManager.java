package com.nttdata.eva.whatsapp.service;

import com.nttdata.eva.whatsapp.model.SessionDestination;
import com.nttdata.eva.whatsapp.model.UserSessionData;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private final Map<String, UserSessionData> cacheUser = new HashMap<>();

    public void addToCache(String userKey, UserSessionData contextUserDTO) {
        if (this.containsUser(userKey))
            this.removeFromCache(userKey);
        cacheUser.put(userKey, contextUserDTO);
    }

    public UserSessionData getFromCache(String userKey) {
        UserSessionData sessionData = cacheUser.get(userKey);

        if (sessionData == null) {
            return createNewUser();
        }

        // Check if session is too old (e.g., 24 hours)
        long currentTime = System.currentTimeMillis();
        long lastInteraction = sessionData.getLastInteractionTime();
        long timeThreshold = 15 * 60 * 1000; // 15min in milliseconds

        // refresh by inactivity only if destination is human agent
        if (currentTime - lastInteraction > timeThreshold
                && SessionDestination.HUMAN_AGENT.equals(sessionData.getDestination())) {
            return createNewUser();
        }

        sessionData.setLastInteractionTime(currentTime);

        return sessionData;
    }

    public void removeFromCache(String userKey) {
        cacheUser.remove(userKey);
    }

    public boolean containsUser(String userKey) {
        return cacheUser.containsKey(userKey);
    }

    public int getSize() {
        return cacheUser.size();
    }

    private UserSessionData createNewUser() {
        UserSessionData newSessionData = new UserSessionData();
        return newSessionData;
    }
}