package com.seeyon.v3x.edoc.manager;

public interface EdocMarkLockManager {
	/**
	    * 能否获取锁，如果能获取锁，就返回true并枷锁，否则返回false;
	    * @param lockId
	    * @return
	    */ 
		public String canGetLock(String docMark,String subject);
		
	    public void unlock(String docMark);
}
