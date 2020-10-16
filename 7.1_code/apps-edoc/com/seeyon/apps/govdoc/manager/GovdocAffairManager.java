package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;

public interface GovdocAffairManager {
	
	/**
	 * 保存待发时，设置待发的Affair数据
	 * @param affair
	 * @throws BusinessException
	 */
	public void saveDraftAffair(GovdocNewVO newVo) throws BusinessException;
	
	/**
	 * 保存待发时，设置已发的Affair数据
	 * @param info
	 * @throws BusinessException
	 */
	public void saveSenderAffair(GovdocNewVO info) throws BusinessException;
	
	/**
	 * 公文查看时，更新Affair查看状态
	 * @param affair
	 * @throws BusinessException
	 */
	public void updateAffairStateWhenClick(CtpAffair affair) throws BusinessException;
	
	/**
	 * 
	 * @param affairIdList
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairList(List<Long> affairIdList) throws BusinessException; 
	
	/**
	 * 公文查看时，当Affair为空时，查询需要的Affair
	 * @param openFrom
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public CtpAffair getSummaryAffairWhenAffairIsNull(String openFrom, Long summaryId) throws BusinessException;
	
}
