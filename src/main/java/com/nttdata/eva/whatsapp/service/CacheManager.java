package com.nttdata.eva.whatsapp.service;
import org.springframework.stereotype.Service;

import com.nttdata.eva.whatsapp.model.UserSessionData;

import java.util.HashMap;

@Service
public class CacheManager {

    private final HashMap<String, UserSessionData> cacheUser;

    public CacheManager() {
        cacheUser = new HashMap<>();
    }

    public void addToCache(String userKey, UserSessionData contextUserDTO) {
        if (this.containsUser(userKey))
            this.removeFromCache(userKey);
        cacheUser.put(userKey, contextUserDTO);
    }

    public UserSessionData getFromCache(String userKey) {
        if (this.containsUser(userKey))
            return cacheUser.get(userKey);
        else return null;
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