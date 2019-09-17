package com.seeyon.v3x.edoc.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class EdocLockManagerImpl implements EdocLockManager {

	private static Map<Long, Long> locksMap = new ConcurrentHashMap<Long, Long>();
	private final Object lockObject = new Object();

	@Override
	public Long canGetLock(Long lockId, Long userId) {
		synchronized (lockObject) {
			Long lockUserId = getLockUserId(lockId);
			if(lockUserId != null) {
				return locksMap.get(lockId);
			}else{
				locksMap.put(lockId, userId);
				return null;
			}
		}
	}
	
	@Override
	public void unlock(Long lockId) {
		synchronized (lockObject) {
			locksMap.remove(lockId);
		}
	}
	
	private Long getLockUserId(Long lockId) {
		synchronized (lockObject) {
			return locksMap.get(lockId);
		}
	}
	
}
