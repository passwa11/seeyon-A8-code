package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.helper.GovdocAffairHelper;
import com.seeyon.apps.govdoc.manager.GovdocAffairManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public class GovdocAffairManagerImpl implements GovdocAffairManager {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocAffairManagerImpl.class);
	
	private GovdocWorkflowManager govdocWorkflowManager;
	private AffairManager affairManager;
	private WorkTimeManager workTimeManager;
	
	@SuppressWarnings("deprecation")
	@Override
	public void saveDraftAffair(GovdocNewVO newVo) throws BusinessException {
		if (newVo.isNew()) {
			DBAgent.saveOrUpdate(newVo.getSenderAffair());
		} else {
			DBAgent.update(newVo.getSenderAffair());
		}
		//指定回退时，同步数据到所有Affair
		if (newVo.isSpecialBacked()) {
			updateSpecialBackedAffair(newVo.getSummary());
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void saveSenderAffair(GovdocNewVO newVo) throws BusinessException {
		EdocSummary summary = newVo.getSummary();
		CtpAffair affair = newVo.getSenderAffair();
		
		boolean specialback = false;
		boolean isSpecialBackReturn = false;
		if (affair == null && newVo.getCurrentAffairId() != null) {
			affair = affairManager.get(newVo.getCurrentAffairId());
		}
		if (affair != null) {
			if (Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState())) {
				specialback = true;
			}
			if (Integer.valueOf(SubStateEnum.col_pending_specialBackToSenderCancel.getKey()).equals(affair.getSubState())) {
				isSpecialBackReturn = true;
			}
		}
		newVo.setSpecialBacked(specialback);
		newVo.setSpecialBackReturn(isSpecialBackReturn);
		if (affair == null) {
			affair = new CtpAffair();
		}
		affair.setIdIfNew();
		affair.setSubject(Strings.isBlank(summary.getDynamicSubject()) ? summary.getSubject() : summary.getDynamicSubject());
		affair.setReceiveTime(newVo.getCurrentDate());
		affair.setMemberId(newVo.getCurrentUser().getId());
		affair.setObjectId(summary.getId());
		affair.setSubObjectId(null);
		affair.setSenderId(newVo.getCurrentUser().getId());
		affair.setState(StateEnum.col_sent.key());
		affair.setSubState(SubStateEnum.col_normal.key());
		affair.setTempleteId(summary.getTempleteId());
		affair.setBodyType(summary.getBodyType());
		affair.setImportantLevel(summary.getImportantLevel());
		affair.setOrgAccountId(newVo.getSummary().getOrgAccountId());
		affair.setApp(ApplicationCategoryEnum.edoc.getKey());
		affair.setSubApp(summary.getGovdocType());
		if (summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.key()
				|| summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {
			affair.setNodePolicy("dengji");
		} else {
			affair.setNodePolicy("niwen");
		}			
		affair.setTrack(newVo.getTrackType());
		affair.setDelete(false);
		affair.setFinish(false);
		affair.setCoverTime(false);
		affair.setDueRemind(false);
		affair.setArchiveId(summary.getArchiveId());
		AffairUtil.setHasAttachments(affair, summary.isHasAttachments());// 设置已发事项 附件
		if (summary.getDeadlineDatetime() != null) {
			AffairUtil.addExtProperty(affair, AffairExtPropEnums.processPeriod, summary.getDeadlineDatetime());
		}
		affair.setFormAppId(summary.getFormAppid());
		affair.setFormId(summary.getFormId());
		affair.setFormRecordid(summary.getFormRecordid());
        affair.setRelationDataId(Strings.isBlank(newVo.getDR()) ? null : Long.valueOf(newVo.getDR()));

		// 触发流程时 设置默认权限和类型 先写死 陈祥
		/*try {
			FormBean formBeanTemp = govdocFormManager.getFormByTemplate4Govdoc(newVo.getTemplate());
			if (formBeanTemp != null) {
				if (formBeanTemp.getFormType() == FormType.govDocSendForm.getKey()) {
					affair.setNodePolicy("niwen");
					summary.setGovdocType(ApplicationSubCategoryEnum.edoc_fawen.getKey());
				} else if (formBeanTemp.getFormType() == FormType.govDocReceiveForm.getKey()) {
					affair.setNodePolicy("dengji");
					summary.setGovdocType(ApplicationSubCategoryEnum.edoc_shouwen.getKey());
				} else if (formBeanTemp.getFormType() == FormType.govDocExchangeForm.getKey()) {
					affair.setNodePolicy("dengji");
					summary.setGovdocType(ApplicationSubCategoryEnum.edoc_jiaohuan.getKey());
					summary.setOrgAccountId(formBeanTemp.getOwnerAccountId());
				} else if (formBeanTemp.getFormType() == FormType.govDocSignForm.getKey()) {
					affair.setNodePolicy("niwen");
					summary.setGovdocType(ApplicationSubCategoryEnum.edoc_qianbao.getKey());
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}*/
		if (specialback || isSpecialBackReturn) {
			Timestamp createDate = new Timestamp(affair.getCreateDate().getTime());
			summary.setCreateTime(createDate);
			summary.setStartTime(createDate);
		} else {
			affair.setCreateDate(newVo.getCurrentDate());
		}
		if (summary.getIsQuickSend()) {
			affair.setSummaryState(EdocConstant.flowState.finish.ordinal() );
			affair.setTrack(Integer.valueOf(TrackEnum.no.ordinal()));
			affair.setCompleteTime(affair.getCreateDate());
			affair.setFinish(Boolean.TRUE);
		} else {
			affair.setSummaryState(EdocConstant.flowState.run.ordinal());
		}
		
		//设置扩展字段
		Map<String, Object> extParam = GovdocAffairHelper.createExtParam(summary);
		AffairUtil.setExtProperty(affair, extParam);
		
		this.affairManager.save(affair);
		newVo.setSenderAffair(affair);
		
		LOGGER.info("保存发起人事项成功！");
	}

	@Override
	public void updateAffairStateWhenClick(CtpAffair affair) throws BusinessException {
		Integer sub_state = affair.getSubState();
		if (sub_state == null || sub_state.intValue() == SubStateEnum.col_pending_unRead.key()) {
			affair.setSubState(SubStateEnum.col_pending_read.key());
			// 更新第一次查看时间
			Date nowTime = new Date();
			long firstViewTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), nowTime, affair.getOrgAccountId());
			affair.setFirstViewPeriod(firstViewTime);
			affair.setFirstViewDate(nowTime);

			affairManager.updateAffair(affair);
			// 要把已读状态写写进流程
			if (affair.getSubObjectId() != null) {
				try {
					govdocWorkflowManager.readWorkItem(affair.getSubObjectId());
				} catch (BPMException e) {
					LOGGER.error("", e);
					throw new BusinessException(e);
				}
			}
		}
	}

	/**
	 * 指定回退到发起人-提交给我时，同步参数到所有Affair
	 */
	private void updateSpecialBackedAffair(EdocSummary summary) {
		if (summary == null) {
			return;
		}
		StringBuilder hql = new StringBuilder();
		hql.append("update CtpAffair as a set a.subject =:subject ,a.importantLevel =:importantLevel,a.bodyType =:bodyType where a.objectId =:objectId");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("subject", summary.getSubject());
		params.put("importantLevel", summary.getImportantLevel());
		params.put("objectId", summary.getId());
		params.put("bodyType", summary.getBodyType());
		DBAgent.bulkUpdate(hql.toString(), params);
	}
	
	/**
	 * 
	 * @param affairIdList
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairList(List<Long> affairIdList) throws BusinessException {
		if(Strings.isEmpty(affairIdList)) {
			return null;
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("id", affairIdList);
		return affairManager.getByConditions(null, paramMap);
	}
	
	@Override
	public CtpAffair getSummaryAffairWhenAffairIsNull(String openFrom, Long summaryId) throws BusinessException {
		CtpAffair affair = null;
		List<StateEnum> states = new ArrayList<StateEnum>();
		states.add(StateEnum.col_sent);
		states.add(StateEnum.col_waitSend);//已发、待发在督办下 都可以
		//督办消息打开时，需要查询已发的个人事项即可，防止流程中没有督办人，获取到其他状态的affair导致安全检查拦截
		if(!EdocOpenFrom.supervise.name().equals(openFrom)) {
			states.add(StateEnum.col_done);
			states.add(StateEnum.col_cancel);
		}
		//交换中需要查看  
		List<CtpAffair> affairs = affairManager.getAffairs(summaryId, states);
		if(affairs != null && affairs.size()>0) {
			affair = affairs.get(0);
		}
		return affair;
	}
	
	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}
	
}
