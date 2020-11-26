package com.seeyon.apps.collaboration.manager;


public interface ColLockManager {

   /**
    * 能否获取锁，如果能获取锁，就返回true并枷锁，否则返回false;
    * @param affairId
    * @return
    */
    public boolean canGetLock(Long affairId) ;

 
    public void unlock(Long affairId);
    
    public boolean isLock(Long affairId);
    
}
