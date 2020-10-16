package yh.app1.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
/**
 * @Title:LocalSessions
 * @Description:本地会话缓存，保存所有用户与本服务器的会话id和会话关系
 * @version 1.0
 */
public class LocalSessions {
	private static Map<String, HttpSession> session_cache = new HashMap<>();
	
	public static void put(String sessionId,HttpSession session) {
		session_cache.put(sessionId, session);

	}
	public static HttpSession get(String sessionId) {
		return session_cache.get(sessionId);

	}
	public static void remove(String sessionId) {
		session_cache.remove(sessionId);

	}
}
