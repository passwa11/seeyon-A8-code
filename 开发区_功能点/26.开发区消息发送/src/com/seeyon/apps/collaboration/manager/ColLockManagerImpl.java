package com.seeyon.apps.collaboration.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;

public class ColLockManagerImpl implements ColLockManager {
	private static final Log LOG = CtpLogFactory.getLog(ColLockManagerImpl.class);
	private static Map<Long, Boolean> locksMap = new ConcurrentHashMap<Long, Boolean>();
	private final Object lockObject = new Object();
	@Override
	public boolean canGetLock(Long affairId) {
	    boolean canGet = false;
		synchronized (lockObject) {
			if (locksMap.get(affairId) == null) {
				locksMap.put(affairId, Boolean.TRUE);
				canGet = true;
			}
		}
		if(canGet){
		    LOG.info(AppContext.currentUserName()+",内存锁加锁,affairId:"+affairId );
		}
		return canGet;
	}

	@Override
	public void unlock(Long affairId) {
	    LOG.info(AppContext.currentUserName()+",内存锁移除,affairId:"+affairId );
		synchronized (lockObject) {
			locksMap.remove(affairId);
		}

	}

	@Override
	public boolean isLock(Long affairId) {
		synchronized (lockObject) {
			return locksMap.get(affairId) == null ? false : true;
		}
	}
}
