package com.seeyon.ctp.login.server.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ClientStorage {
    INSTANCE;
    private Map<String, ArrayList<String>> map = new HashMap<>();

    public void set(String token, String url) {
        if (map.containsKey(token)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(url);
            map.put(token, list);
            return;
        }
        map.get(token).add(url);

    }

    public List<String> get(String token) {
        if (map.containsKey(token)) {
            return map.remove(token);
        }
        return null;
    }
}
