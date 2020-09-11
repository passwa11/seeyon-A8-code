package com.seeyon.ctp.login.server.listener;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class GlobalSessions {
    private static Map<String, HttpSession> globalSessions_cache = new HashMap<>();

    public static void put(String sessionId, HttpSession session) {
        globalSessions_cache.put(sessionId, session);
    }

    public static HttpSession get(String sessionId) {
        return globalSessions_cache.get(sessionId);
    }

    public static void remove(String sessionId) {
        globalSessions_cache.remove(sessionId);
    }
}
