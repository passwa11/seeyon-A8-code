package com.seeyon.apps.govdoc.manager;

import java.util.Map;

import com.seeyon.apps.govdoc.bo.GovdocLockObject;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public interface GovdocLockManager {
	
	/**
	 * 公文单锁列表
	 * @param flipInfo
	 * @param query
	 * @return
	 * @throws BusinessException
	 */
    public FlipInfo getGovdocFormLockList(FlipInfo flipInfo, Map<String,String> query) throws BusinessException;
    
    /**
     * 公文单解锁
     * @param summaryId
     * @param fromRecordId
     * @param ownerId
     * @param ownerName
     * @return
     */
    public boolean unlockGovdocForm(String summaryId, String fromRecordId, String ownerId, String ownerName);
	
	/**
     * 修改完流程，解除流程同步锁
     * @param affairId
     * @throws BusinessException
     */
    public void colDelLock(Long affairId) throws BusinessException;
    
    public void colDelLock(EdocSummary summary,CtpAffair affair) throws BusinessException;
    
    public void ajaxColDelLock(Map<String,String> param) throws BusinessException ;


    /**
     * 能否获取锁，如果能获取锁，就返回true并枷锁，否则返回false;
     * @param affairId
     * @return
     */
     public boolean canGetLock(Long affairId) ;
     
     public void unlock(Long affairId);
     public void unlockAll(EdocSummary summary);
     public void unlockAll(EdocSummary summary, Long userId);
     public void unlockAll(EdocSummary summary, Long affairId, Long userId);
     
     /**
      * 加同步锁
      * @param affairId
      * @throws BusinessException
      */
     public GovdocLockObject formAddLock(Long affairId) throws BusinessException;
     
}
