package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.govdoc.vo.GovdocTrackVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.track.bo.TrackAjaxTranObj;
import com.seeyon.ctp.common.track.po.CtpTrackMember;

/**
 * 公文跟踪接口
 * @author 唐桂林
 *
 */
public interface GovdocTrackManager {

	/**
	 * 获取公文跟踪对象
	 * @param summaryId
	 * @param affairId
	 * @return
	 * @throws BusinessException
	 */
	public GovdocTrackVO getTrackInfoBySummaryId(Long summaryId, Long affairId) throws BusinessException;
	
	/**
	 * 保存公文跟踪
	 * @param summaryId
	 * @param affairId
	 * @return
	 * @throws BusinessException
	 */
	public String saveGovdocTrack(TrackAjaxTranObj obj) throws BusinessException;
	
	/**
	 * 获取公文跟踪的信息
	 * @param modelAndView
	 * @param vobj
	 * @param smmaryId
	 * @throws BusinessException
	 */
	public void fillTrackInfo(GovdocNewVO vobj) throws BusinessException;
	
	/**
	 * 
	 * @param info
	 * @throws BusinessException
	 */
	public void saveTrackInfo(GovdocNewVO info) throws BusinessException;
	
	public void saveTrackInfoByDraft(GovdocNewVO newVo) throws BusinessException;
	
	public int saveTrackInfo(GovdocDealVO dealVO, CtpAffair affair) throws BusinessException;
	
	/**
	 * 根据affair获取跟踪人信息
	 * @param affair
	 * @throws BusinessException
	 */
	public void fillSummaryVoByTrack(GovdocSummaryVO summaryVo) throws BusinessException;
	
	public void deleteTrackMembers(Long objectId) throws BusinessException;
    public void deleteTrackMembers(Long objectId,Long affairId) throws BusinessException ;
    public void deleteTrackMembersByAffairIds(List<Long> ids)throws BusinessException;

	public void save(List<CtpTrackMember> list) throws BusinessException ;
	
	
}
