package com.nttdata.eva.whatsapp.service;


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
        if (this.containsUser(userKey)) {
            return cacheUser.get(userKey);
        } else {
            UserSessionData newSessionData = new UserSessionData();
            return newSessionData;
        }
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
}