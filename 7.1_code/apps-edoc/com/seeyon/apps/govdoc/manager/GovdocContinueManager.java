package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;

/**
 * 新公文续办接口
 * @author 唐桂林
 *
 */
public interface GovdocContinueManager {

	/**
	 * 保存续办相关信息
	 * @param info
	 * @throws BusinessException
	 */
	public void setCustomDealWith(GovdocNewVO info) throws BusinessException;
	
	/**
	 * 
	 * @param affair
	 * @throws BusinessException
	 */
	public void setCustomAffairExt(GovdocDealVO finishVO) throws BusinessException;
	
	/**
	 * 公文处理-续办
	 * @param summaryVO
	 * @throws BusinessException
	 */
	public void fillSummaryVoByXuban(GovdocSummaryVO summaryVO) throws BusinessException;
	public void fillSummaryVoByCustomDealWithForM3(GovdocSummaryVO summaryVO) throws BusinessException;
	
	/**
	 * 取回的时候删除续办人员
	 * @throws BusinessException
	 */
	public void deleteCustomDealWidth(CtpAffair affair) throws BusinessException;
	
	/**
	 * 删除公文所有续办节点，用于撤销
	 * @param summaryId
	 * @throws BusinessException
	 */
	public void deleteAllCustomDealWidth(Long summaryId) throws BusinessException;
	
	/**
	 * 
	 * @param dealVo
	 * @param affairss
	 * @param result
	 * @param type
	 */
	public void deleteCustomDealWithStepBack(GovdocDealVO dealVo,List<CtpAffair> affairss,String result,String type);
	
}
