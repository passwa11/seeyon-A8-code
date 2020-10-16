package yh.app1.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import yh.app1.util.LocalSessions;

/**
 * @Title:GlobalSessionListener
 * @Description:会话监听器，监听会话的创建和销毁
 * @version 1.0
 */
public class LocalSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		LocalSessions.put(httpSession.getId(), httpSession);

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		LocalSessions.remove(event.getSession().getId());
	}

}
