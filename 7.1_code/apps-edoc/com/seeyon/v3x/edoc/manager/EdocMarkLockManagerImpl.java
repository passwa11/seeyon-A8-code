package com.seeyon.v3x.edoc.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.seeyon.ctp.util.Strings;


public class EdocMarkLockManagerImpl implements EdocMarkLockManager {
	
	private static Map<String, String> docMarklocksMap = new ConcurrentHashMap<String, String>();
	private final Object lockObject = new Object();

	@Override
	public String canGetLock(String docMark,String subject) {
		synchronized (lockObject) {
			String lockSubject = getLockUserId(docMark);
			if(Strings.isNotBlank(lockSubject)) {
				return docMarklocksMap.get(docMark);
			}else{
				docMarklocksMap.put(docMark, subject);
				return null;
			}
		}
	}
	
	@Override
	public void unlock(String docMark) {
		synchronized (lockObject) {
			docMarklocksMap.remove(docMark);
		}
	}
	
	private String getLockUserId(String docMark) {
		synchronized (lockObject) {
			return docMarklocksMap.get(docMark);
		}
	}

}
