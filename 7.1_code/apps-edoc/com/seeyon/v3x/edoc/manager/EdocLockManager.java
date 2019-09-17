package com.seeyon.v3x.edoc.manager;

public interface EdocLockManager {

	/**
    * 能否获取锁，如果能获取锁，就返回true并枷锁，否则返回false;
    * @param lockId
    * @return
    */ 
	public Long canGetLock(Long lockId, Long userId);
	
    public void unlock(Long lockId);
	
}
