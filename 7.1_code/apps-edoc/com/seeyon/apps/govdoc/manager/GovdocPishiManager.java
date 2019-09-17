package com.seeyon.apps.govdoc.manager;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.EdocLeaderPishiNo;
import com.seeyon.apps.govdoc.po.EdocUserLeaderRelation;
import com.seeyon.apps.govdoc.po.GovdocLeaderSerialShortname;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * 领导批示相关部分入口
 * 领导批示编号，代领导批示等处理
 */
public interface GovdocPishiManager {
	/**
	 * 领导批示编号相关数据
	 * @param summaryVO
	 * @throws BusinessException
	 */
	public void fillSummaryVoByLeaderPishiNo(GovdocSummaryVO summaryVO) throws BusinessException;
	/**
	 * 领导批示编号保存
	 * @param summaryVO
	 * @throws BusinessException
	 */
	public void saveLeaderPishiNo(GovdocDealVO dealVo) throws BusinessException;
	/**
	 * 直接保存或修改批示编号
	 * @param edocLeaderPishiNo
	 * @throws BusinessException
	 */
	public void saveOrEditPishiNo(EdocLeaderPishiNo edocLeaderPishiNo) throws BusinessException;
	/**
	 * 通过affairId获取对应的批示编号
	 * @param affairIds
	 * @return
	 * @throws BusinessException
	 */
	public  Map<Long, EdocLeaderPishiNo> getLeaderPishiByAffairIds(List<Long> affairIds) throws BusinessException;
	/**
	 * 检查批示编号是否可以使用
	 * @param pishiNo
	 * @param pishiName
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public Boolean checkPishiNo(Integer pishiNo,String pishiName,String pishiYear,Long summaryId) throws BusinessException;
	/**
	 * 释放一条公文全部的批示编号
	 * @param summaryId
	 * @throws BusinessException
	 */
	public void emptyPishiNo(Long summaryId) throws BusinessException;
	/**
	 * 释放单个处理的批示编号
	 * @param affairId
	 * @param pishiName
	 */
	public void emptyPishiNoByAffairId(Long affairId,String pishiName)throws BusinessException;
	/**
	 * 查询本单位所有的领导批示简称
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public List<String> getAllLeaderName(Long accountId) throws BusinessException;
	
    /**
     * @author rz
     * 获取代领导批示权限人员
     * @param flipInfo
     * @param params
     * @return
     * @throws BusinessException
     */
    public FlipInfo leaderPishiUser(FlipInfo flipInfo,Map params)throws BusinessException;
    
    /**
     * @author rz
     * 选人界面保存人员
     * @param ids
     * @param value
     * @return
     * @throws BusinessException
     */
    public void updateLeaderPishi(Long[] ids,String value)throws BusinessException;

	/**
	 * @author rz
	 * 获取代领导批示列表数据
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getlistPishiList(FlipInfo flipInfo, Map params)throws BusinessException, ParseException;
	
    /**
     * @author rz
     * 检查代录人权限
     * @param userId
     * @param affairMemberId
     * @return
     * @throws Exception
     *
     */
	public String checkLeaderPishi(Long userId, Long affairMemberId);
	
	/**
	 * @author rz
	 * 获取当前领导的代录人
	 * @param userId
	 * @return
	 */
	public  List<EdocUserLeaderRelation> getEdocUserLeaderRelation(Long userId);
	public  List<Long> getEdocUserLeaderId(Long userId);
	
	/**
	 * 新增批示编号简称
	 * @param govdocLNA
	 */
	public void saveOrEditLeaderShortName(GovdocLeaderSerialShortname govdocLNA) throws BusinessException;
	/** 
	 * 检查领导是否已经设置编号简称
	 * @param leaerId
	 */
	public int checkLSS(String leaderName,Long leaderId,String shortName) throws BusinessException;
	/**
	 * 获取对应的批示编号简称数据
	 * @param flipInfo
	 * @param map
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	public List<GovdocLeaderSerialShortname> getGovdocLSSs(FlipInfo flipInfo,Map map) throws BusinessException;
	/**
	 * 删除对应的批示编号简称数据
	 * @param id
	 * @throws BusinessException
	 */
	public void deleteLSS(String strings) throws BusinessException;
	public void deleteLSSByLeaderId(Long leaderId,Long orgAccountId) throws BusinessException;
	
	public Object getLSS(Long leaderId) throws BusinessException;
	public List<EdocLeaderPishiNo> getAllLeaderPishi(Long summaryId) throws BusinessException;
	/********************************************************************************************************************/
	@SuppressWarnings("rawtypes")
	public FlipInfo getLeaderShortName(FlipInfo flipInfo,Map params) throws BusinessException;
}
