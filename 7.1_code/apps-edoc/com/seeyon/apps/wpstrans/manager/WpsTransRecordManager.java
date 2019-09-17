package com.seeyon.apps.wpstrans.manager;

import com.seeyon.apps.wpstrans.po.WpsTransRecord;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 转版记录管理接口
 * @author 唐桂林
 *
 */
public interface WpsTransRecordManager {

	/**
	 * 保存转版记录
	 * @param objectId
	 * @param sourceFileId
	 * @param app
	 * @param affairId
	 * @param memberId
	 * @throws BusinessException
	 */
	public WpsTransRecord saveEdocTransRecord(Long objectId, String subject, Long sourceFileId, Integer app, Long affairId, Long memberId) throws BusinessException;
	
	/**
	 * 记录转版失败信息
	 * @param objectId
	 * @param message
	 * @throws BusinessException
	 */
	public WpsTransRecord saveFairlureInfo(Long objectId, String message) throws BusinessException;
	
	/**
	 * 记录转版成功信息
	 * @param objectId
	 * @param destFileId
	 * @param message
	 * @throws BusinessException
	 */
	public WpsTransRecord saveSuccessInfo(Long objectId, String message, Long destFileId) throws BusinessException;
	
}
