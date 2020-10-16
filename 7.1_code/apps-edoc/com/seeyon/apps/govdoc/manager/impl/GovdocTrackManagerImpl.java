package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocTrackManager;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.govdoc.vo.GovdocTrackVO;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.track.bo.TrackAjaxTranObj;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 公文跟踪实现
 * @author 唐桂林
 *
 */
public class GovdocTrackManagerImpl implements GovdocTrackManager {

	private GovdocSummaryManager govdocSummaryManager;
	private AffairManager affairManager;
	private CtpTrackMemberManager trackManager;
	private OrgManager orgManager;
	
	@Override
	public GovdocTrackVO getTrackInfoBySummaryId(Long summaryId, Long affairId) throws BusinessException {
		String zdgzrStr = "";
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
    	CtpAffair affair = affairManager.get(affairId);
    	int trackType = affair.getTrack();//跟踪类型
    	Long startMemberId = summary.getStartUserId();//发起者ID
    	int state = summary.getState();//事务状态
    	if(trackType == 2) {//指定跟踪人的时候,查询回显数据
    		List<CtpTrackMember> trackList = trackManager.getTrackMembers(Long.valueOf(affairId));
    		StringBuilder sb=new StringBuilder();
    		for(int a = 0,j = trackList.size();a<j; a++) {
    			CtpTrackMember cm = trackList.get(a);
    			sb.append("Member|"+cm.getTrackMemberId()+",");
    		}
    		zdgzrStr = sb.toString();
    		if(Strings.isNotBlank(zdgzrStr)) {
    			zdgzrStr = zdgzrStr.substring(0, zdgzrStr.length()-1);
    		}
    	}
    	
    	GovdocTrackVO trackVo = new GovdocTrackVO();
    	trackVo.setSummaryId(summaryId);
    	trackVo.setAffairId(affairId);
    	trackVo.setTrackType(trackType);
    	trackVo.setState(state);
    	trackVo.setStartMemberId(startMemberId);
    	trackVo.setZdgzrStr(zdgzrStr);
    	return trackVo;
	}

	@Override
	public String saveGovdocTrack(TrackAjaxTranObj obj) throws BusinessException {
		try {
	        String oldTrackType = obj.getOldTrackType();
	        String newTrackType = obj.getNewTrackType();
	        String affairId = obj.getAffairId();
	        String objectId = obj.getObjectId();
	        String trackMemberIds = obj.getTrackMemberIds();
	        String senderId = obj.getSenderId();
	        String[] ids = null;
	        if (Strings.isNotBlank(trackMemberIds)) {
	            ids = trackMemberIds.split(",");
	        }
	
	        //trackType 0为不跟踪 1为全部跟踪 2为部分跟踪
	        if ("0".equals(oldTrackType) && "1".equals(newTrackType)) {
	            //修改affair表的track类型和summary表的是否跟踪
	            CtpAffair affair = new CtpAffair();
	            affair = affairManager.get(Long.parseLong(affairId));
	            affair.setTrack(Integer.parseInt(newTrackType));
	            affairManager.updateAffair(affair);
	        }
	        if ("0".equals(oldTrackType) && "2".equals(newTrackType)) {
	            //向跟踪表里面添加数据
	            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
	            if(null != ids){
	            	CtpTrackMember member = null;
	            	for (int i = 0; i < ids.length; i++) {
	            		member = new CtpTrackMember();
	            		member.setIdIfNew();
	            		member.setAffairId(Long.parseLong(affairId));
	            		member.setObjectId(Long.parseLong(objectId));
	            		member.setMemberId(Long.parseLong(senderId));
	            		member.setTrackMemberId(Long.parseLong(ids[i]));
	            		list.add(member);
	            	}
	            	trackManager.save(list);
	            }
	            //修改affair表的track类型和summary表的是否跟踪
	            CtpAffair affair = new CtpAffair();
	            affair = affairManager.get(Long.parseLong(affairId));
	            affair.setTrack(Integer.parseInt(newTrackType));
	            affairManager.updateAffair(affair);
	        }
	        if ("1".equals(oldTrackType) && "0".equals(newTrackType)) {
	            //修改affair表的track类型和summary表的是否跟踪
	            CtpAffair affair = new CtpAffair();
	            affair = affairManager.get(Long.parseLong(affairId));
	            affair.setTrack(Integer.parseInt(newTrackType));
	            affairManager.updateAffair(affair);
	        }
	        if ("1".equals(oldTrackType) && "2".equals(newTrackType)) {
	            //向跟踪表里面添加数据
	            //向跟踪表里面添加数据
	            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
	            if(null != ids){
	            	CtpTrackMember member = null;
	            	for (int i = 0; i < ids.length; i++) {
	            		member = new CtpTrackMember();
	            		member.setIdIfNew();
	            		member.setAffairId(Long.parseLong(affairId));
	            		member.setObjectId(Long.parseLong(objectId));
	            		member.setMemberId(Long.parseLong(senderId));
	            		member.setTrackMemberId(Long.parseLong(ids[i]));
	            		list.add(member);
	            	}
	            	trackManager.save(list);
	            }
	            //修改affair表的track类型和summary表的是否跟踪
	            CtpAffair affair = new CtpAffair();
	            affair = affairManager.get(Long.parseLong(affairId));
	            affair.setTrack(Integer.parseInt(newTrackType));
	            affairManager.updateAffair(affair);
	        }
	        if ("2".equals(oldTrackType) && "0".equals(newTrackType)) {
	            //删除跟踪表里面的数据
	            //修改affair表的track类型和summary表的是否跟踪
	            CtpAffair affair = new CtpAffair();
	            affair = affairManager.get(Long.parseLong(affairId));
	            affair.setTrack(Integer.parseInt(newTrackType));
	            affairManager.updateAffair(affair);
	        }
	        if ("2".equals(oldTrackType) && "1".equals(newTrackType)) {
	            //删除跟踪表里面的数据
	           
	            trackManager.deleteTrackMembers(Long.parseLong(objectId), Long.parseLong(affairId));
	            //修改affair表的track类型和summary表的是否跟踪
	            CtpAffair affair = new CtpAffair();
	            affair = affairManager.get(Long.parseLong(affairId));
	            affair.setTrack(Integer.parseInt(newTrackType));
	            affairManager.updateAffair(affair);
	        }
	        if ("2".equals(oldTrackType) && "2".equals(newTrackType)) {
	        	//先删除老的
	        	
	            trackManager.deleteTrackMembers(Long.parseLong(objectId), Long.parseLong(affairId));
	            //在插入新的
	            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
	            if(null != ids){
	            	CtpTrackMember member = null;
	            	for (int i = 0; i < ids.length; i++) {
	            		member = new CtpTrackMember();
	            		member.setIdIfNew();
	            		member.setAffairId(Long.parseLong(affairId));
	            		member.setObjectId(Long.parseLong(objectId));
	            		member.setMemberId(Long.parseLong(senderId));
	            		member.setTrackMemberId(Long.parseLong(ids[i]));
	            		list.add(member);
	            	}
	            	trackManager.save(list);
	            }
	        }
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return "false";
		}
        return "true";
    }
	
	@Override
	public void fillTrackInfo(GovdocNewVO vobj) throws BusinessException {
		if(vobj != null){
			Long smmaryId = vobj.getSummaryId();
			CtpAffair affairSent = affairManager.getSenderAffair(Long.valueOf(smmaryId));
			if ("waitSend".equals(vobj.getFrom()) && vobj.getAffairId()!=null && (vobj.getAffairId() != 0L)) {
				affairSent = affairManager.get(Long.valueOf(vobj.getAffairId()));
			}
			if (affairSent != null) {
				Integer trackType = affairSent.getTrack().intValue();
				vobj.setTrackType(trackType);
				List<CtpTrackMember> tList = trackManager.getTrackMembers(affairSent.getId());
				String trackNames = "";
				StringBuilder trackIds = new StringBuilder();
				if (tList.size() > 0) {
					for (CtpTrackMember ctpT : tList) {
						trackNames += "Member|" + ctpT.getTrackMemberId() + ",";
						trackIds.append(ctpT.getTrackMemberId() + ",");
					}
					if (trackNames.length() > 0) {
						vobj.setForGZShow(trackNames.substring(0, trackNames.length() - 1));
						vobj.setForGZIds(trackIds.substring(0, trackIds.length() - 1));
					}
				}
			}
		
		}
	}
	
	@Override
	public void saveTrackInfo(GovdocNewVO info) throws BusinessException {
		// 加入跟踪信息 当跟踪类型为2的时候(指定人),像跟踪表插入数据
		if (info.getTrackType() == 2 && Strings.isNotBlank(info.getTrackMemberId())) {
			// 跟踪的逻辑
			String trackMemberId = info.getTrackMemberId();
			String[] str = trackMemberId.split(",");
			List<CtpTrackMember> list = new ArrayList<CtpTrackMember>(str.length);
			/**
			 * OA-68807协同跟踪指定人后，回退、撤销给发起人，重新从待发列表编辑发送后，跟踪指定人显示重复
			 * 待发中重复发起，先删除再保存现有的跟踪设置
			 */
			trackManager.deleteTrackMembers(info.getSummary().getId());
			/**
			 * OA-68807协同跟踪指定人后，回退、撤销给发起人，重新从待发列表编辑发送后，跟踪指定人显示重复
			 */
			for (int count = 0; count < str.length; count++) {
				CtpTrackMember member = new CtpTrackMember();
				member.setIdIfNew();
				member.setAffairId(info.getSenderAffair().getId());
				member.setObjectId(info.getSummary().getId());
				member.setMemberId(info.getCurrentUser().getId());
				member.setTrackMemberId(Long.parseLong(str[count]));

				list.add(member);
			}

			this.trackManager.save(list);
		}
	}
	
	@Override
	public void saveTrackInfoByDraft(GovdocNewVO newVo) throws BusinessException {
		// 加入跟踪信息 当跟踪类型为2的时候(指定人),像跟踪表插入数据
		if (newVo.getTrackType() == 2) {
			CtpAffair affair = newVo.getAffair();
			if (affair == null) {
				affair = newVo.getSenderAffair();
			}
			this.deleteTrackMembers(newVo.getSummary().getId(), affair.getId());
			// 跟踪的逻辑
			String trackMemberId = newVo.getTrackMemberId();
			String[] str = trackMemberId.split(",");
			List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
			CtpTrackMember member = null;
			for (int count = 0; count < str.length; count++) {
				member = new CtpTrackMember();
				member.setIdIfNew();
				member.setAffairId(affair.getId());
				member.setObjectId(newVo.getSummary().getId());
				member.setMemberId(newVo.getCurrentUser().getId());//发起者
				member.setTrackMemberId(Long.parseLong(str[count]));
				list.add(member);
			}
			this.save(list);
		} else if (newVo.getTrackType() != 2) {// 不指定跟踪需要删除跟踪人员
			this.deleteTrackMembers(newVo.getSummary().getId());
		}
	}

	public int saveTrackInfo(GovdocDealVO dealVO, CtpAffair affair) throws BusinessException {
		Long affairId = affair.getId();
		Long summaryId = affair.getObjectId();
		int trackType = 0;
		Map<String, Object> trackPara = dealVO.getTrackPara();
		String trackIds = "";
		if (null != trackPara && trackPara.size() > 0) {
			// isTrack为"1"表示跟踪，其他情况表示不跟踪。
			String isTrack = (String) trackPara.get("isTrack");
			if ("1".equals(isTrack)) {
				trackType = 1;
				// 删除一次跟踪的消息，因为如果先暂存代办指定人 在打开的时候 选择全部的话
				// 数据没有被清理掉，为了避免数据错误，这里进行一次删除操作
				List<Long> affairIds = new ArrayList<Long>();
				affairIds.add(affairId);
				trackManager.deleteTrackMembersByAffairIds(affairIds);
				if (null != trackPara.get("trackRange_members")) {
					trackType = 2;
					trackIds = (String) trackPara.get("zdgzry");
				}
				if (trackType == 2) {
					String[] str = trackIds.split(",");
					List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
					if (Strings.isNotBlank(str[0])) {
						CtpTrackMember member = null;
						for (int count = 0; count < str.length; count++) {
							member = new CtpTrackMember();
							member.setIdIfNew();
							member.setAffairId(affairId);
							member.setObjectId(summaryId);
							member.setMemberId(affair.getMemberId());
							member.setTrackMemberId(Long.parseLong(str[count]));
							list.add(member);
						}
						trackManager.save(list);
					}
				}
				affair.setTrack(trackType);
			} else if (null == trackPara.get("isTrack")) {
				List<Long> affairIds = new ArrayList<Long>();
				affairIds.add(affairId);
				trackManager.deleteTrackMembersByAffairIds(affairIds);
				affair.setTrack(trackType);// 设置为不跟踪
			}
		}
		return trackType;
	}
	
	@Override
	public void fillSummaryVoByTrack(GovdocSummaryVO summaryVo) throws BusinessException {
		CtpAffair affair = summaryVo.getAffair();
		List<CtpTrackMember> trackInfo = trackManager.getTrackMembers(affair.getId());
    	String trackIds ="";
    	String zdgzry ="";
    	String trackNames="";
    	summaryVo.setTrackType(affair.getTrack());
    	if(null != affair.getTrack() && affair.getTrack() == 2){
    		if(trackInfo.size() > 0){
        		for(int a = 0 ; a < trackInfo.size(); a ++){
        			Long trackMemeberId=trackInfo.get(a).getTrackMemberId();
        			trackIds +="Member|"+trackMemeberId+",";
        			V3xOrgMember trackMember = orgManager.getMemberById(trackMemeberId);
        			zdgzry += trackMember.getId()+",";
        			trackNames+=trackMember.getName()+",";//指定跟踪人显示用人名
        		}
        		if(trackIds.length()>0){
        			trackIds = trackIds.substring(0,trackIds.length()-1);
        			zdgzry = zdgzry.substring(0,zdgzry.length()-1);
        			trackNames=trackNames.substring(0,trackNames.length()-1);
        			summaryVo.setTrackIds(trackIds);
        			summaryVo.setZdgzry(zdgzry);
        			summaryVo.setTrackNames(trackNames);
        		}
        	}
    	}	
	}
	
	public void deleteTrackMembers(Long objectId) throws BusinessException {
		trackManager.deleteTrackMembers(objectId);
	}
	
	public void deleteTrackMembers(Long objectId,Long affairId) throws BusinessException {
		trackManager.deleteTrackMembers(objectId, affairId);
	}

	 public void deleteTrackMembersByAffairIds(List<Long> ids)throws BusinessException {
		 trackManager.deleteTrackMembersByAffairIds(ids);
	 }
	
	public void save(List<CtpTrackMember> list) throws BusinessException {
		trackManager.save(list);
	}
	
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setTrackManager(CtpTrackMemberManager trackManager) {
		this.trackManager = trackManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	
	
}
