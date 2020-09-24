/**
 * 
 */
package com.seeyon.ctp.common.usermessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.seeyon.ctp.common.usermessage.pipeline.Message;
import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.usermessage.UserHistoryMessage;
import com.seeyon.ctp.common.usermessage.dao.UserMessageDAO;

/**
 *
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2011-5-18
 */
public class UserHistoryMessageTask {
	private static final Log log = CtpLogFactory.getLog(UserHistoryMessageTask.class);

	private static UserHistoryMessageTask instance = new UserHistoryMessageTask();

	private List<UserHistoryMessage> message = null;

	private UserHistoryMessageThread userHistoryMessageThread = null;
	
	private Object lock = new Object();
	
	private static UserMessageDAO userMessageDAO;

	private UserHistoryMessageTask() {
		userHistoryMessageThread = new UserHistoryMessageThread();
		message = new ArrayList<UserHistoryMessage>(100);
		userMessageDAO = (UserMessageDAO)AppContext.getBean("userMessageDAO");
	}

	public static UserHistoryMessageTask getInstance() {
		return instance;
	}
	
	public void start(){
		userHistoryMessageThread.start();
	}
	
	public void stop(){
		try {
			userHistoryMessageThread.interrupt();
		}
		catch (Exception e) {
		}
		userHistoryMessageThread.running = false;
	}

	public void add(List<UserHistoryMessage> msgs) {
		if(userHistoryMessageThread.running){
			synchronized (lock) {
				message.addAll(msgs);
			}
		}
	}
	
	/*
	 * 获取等待入库的队列长度
	 */
    public int getQueueLength(){
       return message.size();
    }

    public synchronized void batchSaveUserHistoryMessage(){
		if (!message.isEmpty()) {
			List<UserHistoryMessage> msg = message;
			message = new ArrayList<UserHistoryMessage>(300);
			userMessageDAO.savePatchHistory(msg);
			MessageState.getInstance().setHistoryTimestamp(msg);//更新时间戳
		}
	}
    
	class UserHistoryMessageThread extends Thread {
		boolean running = true;
		
		public UserHistoryMessageThread(){
			super.setName("UserHistoryMessageThread");
		}
		
		public synchronized void start() {
			super.start();
			log.info("异步历史消息守护进程启动");
		}

		public void run() {
			while (running) {
				try{
					UserHistoryMessageTask.getInstance().batchSaveUserHistoryMessage();
				}
				catch(Exception e){
					log.error("", e);
				}

				try {
					Thread.sleep(3 * 1000L);
				}
				catch (Exception e) {
				    log.error("", e);
				}
			}
		}
	}
}
