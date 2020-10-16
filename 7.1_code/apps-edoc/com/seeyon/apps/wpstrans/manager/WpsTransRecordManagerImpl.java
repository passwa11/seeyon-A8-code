package com.seeyon.apps.wpstrans.manager;

import com.seeyon.apps.wpstrans.dao.WpsTransRecordDao;
import com.seeyon.apps.wpstrans.po.WpsTransRecord;
import com.seeyon.apps.wpstrans.util.WpsTransConstant;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.util.DateUtil;

/**
 * 转版记录管理实现
 * @author 唐桂林
 *
 */
public class WpsTransRecordManagerImpl implements WpsTransRecordManager {

	private WpsTransRecordDao wpsTransRecordDao;
	private UserMessageManager userMessageManager;

	@Override
	public WpsTransRecord saveEdocTransRecord(Long objectId, String subject, Long sourceFileId, Integer app, Long affairId, Long memberId) throws BusinessException {
		WpsTransRecord po = new WpsTransRecord();
		po.setIdIfNew();
		po.setObjectId(objectId);
		po.setSourceFileId(sourceFileId);
		po.setApp(app);
		po.setAffairId(affairId);
		po.setMemberId(memberId);
		po.setStatus(WpsTransConstant.WPSTRANS_STATUS_START);
		po.setCreateDate(DateUtil.currentTimestamp());
		po.setSubject(subject);
		wpsTransRecordDao.saveOrUpdate(po);
		return po;
	}

	@Override
	public WpsTransRecord saveFairlureInfo(Long objectId, String message) throws BusinessException {
		WpsTransRecord po = wpsTransRecordDao.getWpsTransRecordByObjectId(objectId);
		if(po != null) {
			po.setStatus(WpsTransConstant.WPSTRANS_STATUS_FAILURE);
			po.setUpdateDate(DateUtil.currentTimestamp());
			po.setMessage(message);
			wpsTransRecordDao.saveOrUpdate(po);

			MessageContent msgContent = new MessageContent("wpstrans.result.failure", po.getSubject(), message);
			MessageReceiver receiver = MessageReceiver.get(po.getObjectId(), po.getMemberId());
			userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, po.getMemberId(), receiver);
		}
		return po;
	}

	@Override
	public WpsTransRecord saveSuccessInfo(Long objectId, String message, Long destFileId) throws BusinessException {
		WpsTransRecord po = wpsTransRecordDao.getWpsTransRecordByObjectId(objectId);
		if(po != null) {
			po.setStatus(WpsTransConstant.WPSTRANS_STATUS_SUCCESS);
			po.setUpdateDate(DateUtil.currentTimestamp());
			po.setMessage(message);
			po.setDestFileId(destFileId);
			wpsTransRecordDao.saveOrUpdate(po);

			MessageContent msgContent = new MessageContent("wpstrans.result.success", po.getSubject());
			MessageReceiver receiver = MessageReceiver.get(po.getObjectId(), po.getMemberId());
			userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, po.getMemberId(), receiver);
		}
		return po;
	}

	public void setWpsTransRecordDao(WpsTransRecordDao wpsTransRecordDao) {
		this.wpsTransRecordDao = wpsTransRecordDao;
	}

	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}
}
