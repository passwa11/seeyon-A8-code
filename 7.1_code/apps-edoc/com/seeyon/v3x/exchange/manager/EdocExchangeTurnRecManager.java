package com.seeyon.v3x.exchange.manager;

import java.util.List;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;

public interface EdocExchangeTurnRecManager{
	
	public void save(EdocExchangeTurnRec edocExchangeTurnRec);
	
	public EdocExchangeTurnRec findEdocExchangeTurnRecByEdocId(long edocId);
	
	/**
	 * 根据下级收文id找关联的上级收文id
	 * @param distributeEdocId  下级收文id
	 * @return
	 */
	public Long findSupEdocId(long distributeEdocId);
	
	/**
	 * 下级单位进行意见汇报时，需要先通过ajax判断上级收文是否已经撤销了，如果撤销了给出一个提示
	 * 上级单位已将收文流程撤销，您填写的意见上级单位无法看到
	 * @param turnRec
	 */
	public String isSupEdocCanceled(String subEdocId);
	
	public void updateTurnRec(EdocExchangeTurnRec turnRec);
	
	public void delTurnRecByEdocId(long edocId);
	
	
	/**
	 * 删除下级单位对上级汇报的意见后，被退回的意见
	 * @param edocId  下级收文id
	 * @throws BusinessException
	 */
	public List<EdocOpinion> getDelStepBackSupOptions(long edocId,long supEdocId)throws BusinessException;
	
	public List<EdocOpinion> getDelStepBackSupOptions(long edocId)throws BusinessException;
	
	/**
	 * 转收文生成
	 * @param user
	 * @param summaryId
	 * @param exchangeType
	 * @param grantedDepartIdStr
	 * @param opinion
	 * @return
	 * @throws BusinessException
	 */
	public String transCreateSendDataByTurnRec(User user,long summaryId,int exchangeType,String grantedDepartIdStr,String opinion,
			String exchangeMemberId,String returnDeptId) throws BusinessException;
	
	/**
	 * ajax调用，是否已经转收文了
	 */
	public String isTurnReced(String edocId);
}
