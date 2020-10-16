package com.seeyon.apps.govdoc.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.listener.GovdocWorkflowEventListener;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 公文待办事项辅助类
 */
public class GovdocAffairHelper extends GovdocHelper {
	
	/**
	 * 拟文保存待发获取Affair对象
	 * @param newVo
	 * @return
	 */
	public static CtpAffair getDraftSenderAffair(GovdocNewVO newVo) {
		CtpAffair senderAffair = newVo.getSenderAffair();
		EdocSummary summary = newVo.getSummary();
		Date nowTime = newVo.getCurrentDate();
		
		senderAffair.setIdIfNew();
		senderAffair.setApp(ApplicationCategoryEnum.edoc.key());
		senderAffair.setSubApp(summary.getTempleteId() == null ? ApplicationSubCategoryEnum.collaboration_self.key() : ApplicationSubCategoryEnum.collaboration_tempate.key());
		senderAffair.setNodePolicy("niwen");
		senderAffair.setApp(ApplicationCategoryEnum.edoc.getKey());
		senderAffair.setSubApp(summary.getGovdocType());
		if (summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.key() || summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {
			senderAffair.setNodePolicy("dengji");
		} else {
			senderAffair.setNodePolicy("niwen");
		}
		senderAffair.setSubject(summary.getSubject());
		senderAffair.setObjectId(summary.getId());
		senderAffair.setSubObjectId(null);
		senderAffair.setProcessId(summary.getProcessId());
		senderAffair.setCaseId(summary.getCaseId());
		senderAffair.setMemberId(summary.getStartUserId());
		senderAffair.setSenderId(summary.getStartUserId());
		senderAffair.setState(StateEnum.col_waitSend.key());
		if (!newVo.isSpecialBacked()) {
			senderAffair.setCreateDate(nowTime);
			senderAffair.setSubState(SubStateEnum.col_waitSend_draft.key());
		}
		senderAffair.setUpdateDate(nowTime);
		senderAffair.setDelete(false);
		senderAffair.setTempleteId(summary.getTempleteId());
		senderAffair.setTrack(newVo.getTrackType());
		senderAffair.setBodyType(summary.getBodyType());// 设置正文类型
		senderAffair.setImportantLevel(summary.getImportantLevel());
		senderAffair.setForwardMember(summary.getForwardMember());
		// 设置流程期限
		if (summary.getDeadlineDatetime() != null) {
			AffairUtil.addExtProperty(senderAffair, AffairExtPropEnums.processPeriod, summary.getDeadlineDatetime());
		}
		// 保存附件
		AffairUtil.setHasAttachments(senderAffair, summary.isHasAttachments());
		newVo.setSenderAffair(senderAffair);
		return senderAffair;
	}
	
	/**
	 * 公文发送流程回调时，Affair模型生成待办节点
	 * @param summary
	 * @return
	 * @throws BusinessException
	 */
	public static AffairData getNewAffairData(EdocSummary summary) throws BusinessException {
		AffairData affairData = new AffairData();
		affairData.setForwardMember(summary.getForwardMember());
		affairData.setModuleType(ApplicationCategoryEnum.edoc.key());
		if (summary.getId() != null) {
			affairData.setModuleId(summary.getId());
		}
		// 容错处理
		int importantLevel = -1;
		if (summary.getImportantLevel() != null) {
			importantLevel = summary.getImportantLevel();
		} else {
			summary.setImportantLevel(importantLevel);
		}

		affairData.setImportantLevel(importantLevel);
		affairData.setIsSendMessage(true); // 是否发消息
		// affairData.setResentTime(summary.getResentTime());// 如协同colsummary
		affairData.setState(StateEnum.col_pending.key());// 事项状态 - 协同业务中3为待办
		affairData.setSubject(Strings.isBlank(summary.getDynamicSubject()) ? summary.getSubject() : summary.getDynamicSubject());// 如协同colsummary
		affairData.setSubState(SubStateEnum.col_pending_unRead.key());// 事项子状态
																		// 协同业务中11为协同-待办-未读
		affairData.setSummaryAccountId(summary.getOrgAccountId());// 如协同colsummary.orgAccountId
		affairData.setTemplateId(summary.getTempleteId());// 如协同colsummary
		affairData.setIsHasAttachment(summary.isHasAttachments());// 是否有附件
		affairData.setContentType(summary.getGovdocType()+"");
		affairData.setSender(summary.getStartUserId());
		affairData.setFormRecordId(summary.getFormRecordid());
		affairData.setFormAppId(summary.getFormAppid());
		affairData.setFormId(summary.getFormId());
		affairData.setCreateDate(new Date(summary.getCreateTime().getTime()));
		affairData.setProcessDeadlineDatetime(summary.getDeadlineDatetime());
		affairData.setProcessId(summary.getProcessId());
		affairData.setCaseId(summary.getCaseId());
		affairData.setOrgAccountId(summary.getOrgAccountId());
		affairData.setBodyCreateDate(summary.getCreateTime());
		affairData.setBodyType(summary.getBodyType());
		return affairData;
	}
	
	/**
	 * 公文暂存待办流程回调时，Affair模型生成待办节点
	 * @param summary
	 * @return
	 * @throws BusinessException
	 */
	public static AffairData getZcdbAffairData(EdocSummary summary) throws BusinessException {
		AffairData affairData = new AffairData();
		affairData.setForwardMember(summary.getForwardMember());
		affairData.setModuleType(ApplicationCategoryEnum.collaboration.key());
		if (summary.getId() != null) {
			affairData.setModuleId(summary.getId());
		}
		// 容错处理
		int importantLevel = 1;
		if (summary.getImportantLevel() != null) {
			importantLevel = summary.getImportantLevel();
		} else {
			summary.setImportantLevel(1);
		}

		affairData.setImportantLevel(importantLevel);
		affairData.setIsSendMessage(true); // 是否发消息
		// affairData.setResentTime(summary.getResentTime());//如协同colsummary
		affairData.setState(StateEnum.col_pending.key());// 事项状态 - 协同业务中3为待办
		affairData.setSubject(Strings.isBlank(summary.getDynamicSubject()) ? summary.getSubject() : summary.getDynamicSubject());// 如协同colsummary
		affairData.setSubState(SubStateEnum.col_pending_unRead.key());// 事项子状态
																		// 协同业务中11为协同-待办-未读
		affairData.setSummaryAccountId(summary.getOrgAccountId());// 如协同colsummary.orgAccountId
		affairData.setTemplateId(summary.getTempleteId());// 如协同colsummary
		affairData.setIsHasAttachment(summary.isHasAttachments());// 是否有附件
		affairData.setContentType(summary.getBodyType());
		affairData.setBodyType(summary.getBodyType());
		affairData.setSender(summary.getStartMemberId());
		affairData.setFormRecordId(summary.getFormRecordid());
		affairData.setFormAppId(summary.getFormAppid());
		affairData.setFormId(summary.getFormId());
		affairData.setCreateDate(summary.getCreateTime());
		affairData.setProcessDeadlineDatetime(summary.getDeadlineDatetime());
		affairData.setProcessId(summary.getProcessId());
		affairData.setCaseId(summary.getCaseId());
		affairData.setOrgAccountId(summary.getOrgAccountId());
		return affairData;
	}

	/**
	 * 公文撤销流程回调时，被撤销节点生成新的Affair模型
	 * @param summaryId
	 * @param affairId
	 * @param repealVO
	 * @return
	 */
	public static AffairData getRepealAffairData(Long summaryId, Long affairId, GovdocRepealVO repealVO) {
		AffairData affairData = new AffairData();
		Map<String, Object> businessData = new HashMap<String, Object>();
		businessData.put(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, GovdocWorkflowEventListener.CANCEL);
		businessData.put(GovdocWorkflowEventListener.CURRENT_OPERATE_AFFAIR_ID, affairId);
		businessData.put(GovdocWorkflowEventListener.CURRENT_OPERATE_SUMMARY_ID, summaryId);
		businessData.put(GovdocWorkflowEventListener.CURRENT_OPERATE_COMMENT_CONTENT, repealVO.getRepealComment());
		//是否追溯流程
		businessData.put(GovdocWorkflowEventListener.CURRENT_OPERATE_TRACK_FLOW, repealVO.getIsWFTrace());
		businessData.put("currentUser", repealVO.getCurrentUser());
		affairData.setBusinessData(businessData);
		return affairData;
	}
	
	/**
	 * 公文回退流程回调时，被回退节点生成新的Affair模型
	 * @param summary
	 * @return
	 * @throws BusinessException
	 */
	public static AffairData getStepbackAffairData(EdocSummary summary) throws BusinessException {
		AffairData affairData = new AffairData();
		affairData.setForwardMember(summary.getForwardMember());
		affairData.setModuleType(ApplicationCategoryEnum.edoc.getKey());
		if (summary.getId() != null) {
			affairData.setModuleId(summary.getId());
		}
		// 容错处理
		int importantLevel = 1;
		//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-22 start
		if (summary.getUrgentLevel() != null) {
			importantLevel = Integer.parseInt(summary.getUrgentLevel());
			//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-22 end
		} else {
			summary.setImportantLevel(1);
		}

		affairData.setImportantLevel(importantLevel);
		affairData.setIsSendMessage(true); // 是否发消息
		// affairData.setResentTime(summary.getResentTime());//如协同colsummary//TODO
		affairData.setState(StateEnum.col_pending.key());// 事项状态 - 协同业务中3为待办
		affairData.setSubject(Strings.isBlank(summary.getDynamicSubject()) ? summary.getSubject() : summary.getDynamicSubject());// 如协同colsummary
		affairData.setSubState(SubStateEnum.col_pending_unRead.key());// 事项子状态
																		// 协同业务中11为协同-待办-未读
		affairData.setSummaryAccountId(summary.getOrgAccountId());// 如协同colsummary.orgAccountId
		affairData.setTemplateId(summary.getTempleteId());// 如协同colsummary
		affairData.setIsHasAttachment(summary.isHasAttachments());// 是否有附件
		affairData.setContentType(summary.getBodyType());
		affairData.setSender(summary.getStartMemberId());
		affairData.setFormRecordId(summary.getFormRecordid());
		affairData.setFormAppId(summary.getFormAppid());
		affairData.setFormId(summary.getFormId());
		affairData.setCreateDate(summary.getCreateTime());
		affairData.setProcessDeadlineDatetime(summary.getDeadlineDatetime());
		affairData.setProcessId(summary.getProcessId());
		affairData.setCaseId(summary.getCaseId());
		affairData.setBodyType(summary.getBodyType());
		return affairData;
	}
	
	/**
	 * 公文指定回退流程回调时，被回退节点生成新的Affair模型
	 * @param summary
	 * @return
	 * @throws BusinessException
	 */
	public static AffairData getAppointStepBackAffairData(EdocSummary summary) throws BusinessException {
		AffairData affairData = new AffairData();
		affairData.setForwardMember(summary.getForwardMember());
		affairData.setModuleType(ApplicationCategoryEnum.collaboration.key());
		if (summary.getId() != null) {
			affairData.setModuleId(summary.getId());
		}
		// 容错处理
		int importantLevel = 1;
		if (summary.getImportantLevel() != null) {
			importantLevel = summary.getImportantLevel();
		} else {
			summary.setImportantLevel(1);
		}

		affairData.setImportantLevel(importantLevel);
		affairData.setIsSendMessage(true); // 是否发消息
		// affairData.setResentTime(summary.getResentTime());//如协同colsummary
		affairData.setState(StateEnum.col_pending.key());// 事项状态 - 协同业务中3为待办
		affairData.setSubject(Strings.isBlank(summary.getDynamicSubject()) ? summary.getSubject() : summary.getDynamicSubject());// 如协同colsummary
		affairData.setSubState(SubStateEnum.col_pending_unRead.key());// 事项子状态
																		// 协同业务中11为协同-待办-未读
		affairData.setSummaryAccountId(summary.getOrgAccountId());// 如协同colsummary.orgAccountId
		affairData.setTemplateId(summary.getTempleteId());// 如协同colsummary
		affairData.setIsHasAttachment(summary.isHasAttachments());// 是否有附件
		affairData.setContentType(summary.getBodyType());
		affairData.setBodyType(summary.getBodyType());
		affairData.setSender(summary.getStartMemberId());
		affairData.setFormRecordId(summary.getFormRecordid());
		affairData.setFormAppId(summary.getFormAppid());
		affairData.setFormId(summary.getFormId());
		affairData.setCreateDate(summary.getCreateTime());
		affairData.setProcessDeadlineDatetime(summary.getDeadlineDatetime());
		affairData.setProcessId(summary.getProcessId());
		affairData.setCaseId(summary.getCaseId());
		affairData.setOrgAccountId(summary.getOrgAccountId());
		return affairData;
	}
	
	/**
	 * 公文触发交换数据、触发公文流程、触发子流程时生成交换Affair
	 * @param newVo
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static CtpAffair createExchangeAffair(GovdocNewVO newVo) {
		EdocSummary summary = newVo.getSummary();
		CtpAffair affair = new CtpAffair();
		affair.setIdIfNew();
		affair.setCreateDate(summary.getCreateTime());
        affair.setReceiveTime(summary.getCreateTime());
        affair.setSubject(summary.getDynamicSubject());
        affair.setObjectId(summary.getId());
        affair.setSubObjectId(null);
        affair.setProcessId(summary.getProcessId());
        affair.setCaseId(summary.getCaseId());
        affair.setMemberId(summary.getStartMemberId());
        affair.setSenderId(summary.getStartMemberId());
        affair.setState(StateEnum.col_sent.key()); 
        affair.setSubState(SubStateEnum.col_normal.key());
        affair.setTempleteId(summary.getTempleteId());
        affair.setBodyType(summary.getBodyType());
        affair.setImportantLevel(summary.getImportantLevel());
        affair.setForwardMember(summary.getForwardMember());
        affair.setApp(ApplicationCategoryEnum.edoc.key());
        affair.setSubApp(summary.getGovdocType());
        if(summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.key() 
				|| summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {
			affair.setNodePolicy("dengji");
		} else {
			affair.setNodePolicy("niwen");
		}
        affair.setSummaryState(summary.getState());//更新冗余状态
		
        affair.setTrack(TrackEnum.no.ordinal());
        affair.setDelete(false);
        if (affair.getSubApp() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
			affair.setDelete(true);
		}
		affair.setFinish(false);
        affair.setCoverTime(false);
        affair.setDueRemind(false);
        affair.setArchiveId(null);
        affair.setRelationDataId(Strings.isBlank(newVo.getDR()) ? null : Long.valueOf(newVo.getDR()));
        affair.setOrgAccountId(summary.getOrgAccountId());
        
        AffairUtil.setHasAttachments(affair, summary.isHasAttachments());//设置已发事项 附件
        if(summary.getDeadlineDatetime()!= null){
            AffairUtil.addExtProperty(affair, AffairExtPropEnums.processPeriod,summary.getDeadlineDatetime());
        }

        affair.setFormAppId(summary.getFormAppid());
        affair.setFormId(summary.getFormId());
        affair.setFormRecordid(summary.getFormRecordid());
        if(Strings.isNotBlank(newVo.getFormOperationId())) {
        	affair.setFormOperationId(Long.valueOf(newVo.getFormOperationId()));
        }
        return affair;
	}
	
	/**
	 * 验证Affair是否有效
	 * @param affairId
	 * @param isTraceValid
	 * @param nodePolicy
	 * @return
	 * @throws BusinessException
	 */
	public static String checkAffairValid(String affairId, boolean isTraceValid,String nodePolicy) throws BusinessException {
		CtpAffair affair = null;
		if (Strings.isNotBlank(affairId)) {
			try {
				affair = affairManager.getSimpleAffair(Long.valueOf(affairId));
				if (affair == null) {
					affair = affairManager.getByHis(Long.valueOf(affairId));
				}
			} catch (Exception e) {
				//LOG.error("", e);
			}
		}
		String errorMsg = "";
		if (!AffairUtil.isAfffairValid(affair)) {
			errorMsg = WFComponentUtil.getErrorMsgByAffair(affair);
				if(isTraceValid){
					if (affair != null ) {
						return "";
					}
				}
		} else {
			try {
				errorMsg = GovdocWorkflowHelper.checkNodePolicyChange(affair, nodePolicy);
			} catch (BusinessException e) {
				//LOGGER.error("", e);
			}
		}
		return errorMsg;
	}

	/**
	 * 获取公文所有Affair人员ID(用于公文查看界面-即时交流)
	 * @param summaryId
	 * @return
	 */
	public static List<Long> getColAllMemberId(String summaryId) {
        List<Long> memberIdList = new ArrayList<Long>();
        //添加已办和待办的。
        List<StateEnum> states = new ArrayList<StateEnum>();
        states.add(StateEnum.col_pending);
        states.add(StateEnum.col_done);
        states.add(StateEnum.col_sent);
        states.add(StateEnum.col_waitSend);
        try {
            List<CtpAffair> ctpAffair = affairManager.getAffairs(Long.valueOf(summaryId), states);
            for (int i=0;i<ctpAffair.size();i++) {
                if (!memberIdList.contains(ctpAffair.get(i).getMemberId())) {
                    memberIdList.add(ctpAffair.get(i).getMemberId());
                }
            }
        } catch (Exception e) {
            //LOG.error("获取当前事项的所有memberId异常", e);
        }
        return memberIdList;
    }
	
	/**
	 * 维护公文Affair的扩展字段
	 * @param edocSummary
	 * @return
	 */
	public static Map<String, Object> createExtParam(EdocSummary edocSummary) {
		Map<String, Object> extParam = new HashMap<String, Object>();
		if(edocSummary == null) {
			return extParam;
		}
		String docMark="";
		String sendUnit="";
		if(edocSummary.getDocMark() != null) {
			docMark = edocSummary.getDocMark();
		}
		if(edocSummary.getSendUnit() != null) {
			sendUnit = edocSummary.getSendUnit();
		}
		extParam.put(AffairExtPropEnums.edoc_edocMark.name(), docMark); //公文文号
        extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), sendUnit);//发文单位
        //OA-43885 首页待办栏目下，待开会议的主持人名字改变后，仍显示之前的名称
        if(edocSummary.getSendUnitId() != null) {
        	extParam.put(AffairExtPropEnums.edoc_sendAccountId.name(), edocSummary.getSendUnitId());//发文单位ID
        } 
        if(null != edocSummary.getDeadline() && edocSummary.getDeadline().longValue() > 0) {
            extParam.put(AffairExtPropEnums.processPeriod.name(), edocSummary.getDeadline());//流程期限
        }
		return extParam;
	}
	
	/**
	 * 维护公文Affair的扩展字段
	 * @param affair
	 * @param extParam
	 */
	public static void addAffairExtParam(CtpAffair affair, Map<String, Object> extParam) {
		if(Strings.isBlank(affair.getExtProps())) {
			AffairUtil.setExtProperty(affair, extParam);	
		} else {
			Map<String, Object> map = AffairUtil.getExtProperty(affair);
			for(String key : extParam.keySet()) {
				map.put(key, extParam.get(key));
			}
			AffairUtil.setExtProperty(affair, map);
		}
	}

	/**
	 * 首页栏目配置公文字段的数据保存，存入CTP_AFFAIR的extProperties字段。
	 * 当前保存的是：公文字段、发文单位
	 * @param edocSummary
	 * @return Map<String, Object> 
	 */
	public static Map<String, Object> createExtParam(String docMark, String sendUnit, String sendUnitId) {
		return createExtParam(docMark, sendUnit, sendUnitId);
	}
	public static Map<String, Object> createExtParam(String docMark, String sendUnit,String sendUnitId, Long exSendRetreat) {
		Map<String, Object> extParam = new HashMap<String, Object>();
		if(docMark == null) {
        	docMark="";
        }
        if(sendUnit == null) {
        	sendUnit="";
        }
		extParam.put(AffairExtPropEnums.edoc_edocMark.name(), docMark); //公文文号
        extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), sendUnit);//发文单位
        if(sendUnitId != null) {
        	extParam.put(AffairExtPropEnums.edoc_sendAccountId.name(), sendUnitId);//发文单位ID
        }
        if(exSendRetreat != null) {
        	extParam.put(AffairExtPropEnums.edoc_edocExSendRetreat.name(), exSendRetreat);//发文分发退件标识
        }
        return extParam;
	}
	
}
