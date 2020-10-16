package com.seeyon.v3x.edoc.workflow.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.joinwork.bpm.engine.wapi.WorkItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.common.trace.manager.TraceWorkflowDataManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.event.AbstractEventListener;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMessageHelper;
import com.seeyon.v3x.edoc.manager.EdocMessagerManager;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public class EdocWorkFlowOldHelper {
	private static final Log LOGGER = LogFactory.getLog(EdocWorkFlowOldHelper.class);

	/** 回退 **/
	public static final Integer WITHDRAW = 1;

	/** 取回 **/
	public static final Integer TAKE_BACK = 2;

	/** 知会 **/
	public static final Integer ADD_INFORM = 3;

	/** 会签 **/
	public static final Integer COL_ASSIGN = 4;

	/** 加签 **/
	public static final Integer INSERT = 5;

	/** 减签 **/
	public static final Integer DELETE = 6;

	/** 分配任务 **/
	public static final Integer ASSIGN = 7;

	/** 终止 **/
	public static final Integer STETSTOP = 8;

	/** 正常处理 **/
	public static final Integer COMMONDISPOSAL = 9;

	/** 撤销 **/
	public static final Integer CANCEL = 10;

	/** 暂存待办 **/
	public static final Integer ZCDB = 11;

	/** 默认删除操作: 督办替换节点，自动流程复合节点的单人执行 **/
	public static final Integer AUTODELETE = 12;

	/** 自动跳过 **/
	public static final Integer AUTOSKIP = 13;

	/** 指定回退流程重走 **/
	public static final Integer SPECIAL_BACK_RERUN = 100;

	/** 指定回退提交回退者 **/
	public static final Integer SPECIAL_BACK_SUBMITTO = 101;

	/** 操作类型 **/
	public static final String OPERATION_TYPE = "operationType";

	/** 执行模式: 竞争执行 **/
	public static final String PROCESS_MODE_COMPETITION = "competition";

	private static EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");

	private static WorkTimeManager workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");

	private static AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
	private static EdocMessagerManager edocMessagerManager = (EdocMessagerManager) AppContext.getBean("edocMessagerManager");
	private static OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
	private static UserMessageManager userMessageManager = (UserMessageManager) AppContext.getBean("userMessageManager");
	private static DocApi docApi = (DocApi) AppContext.getBean("docApi");
	private static TraceWorkflowDataManager edocTraceWorkflowManager = (TraceWorkflowDataManager) AppContext.getBean("edocTraceWorkflowManager");

	public static boolean onWorkitemFinished(EventDataContext context) {
		CtpAffair affair = null;
		try {
			DateSharedWithWorkflowEngineThreadLocal.setOperationType(COMMONDISPOSAL);
			affair = eventData2ExistingAffair(context);
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		// 设置运行时长，超时时长等
		EdocSummary summary = null;
		try {
			if (null != context.getAppObject()) {
				summary = (EdocSummary) context.getAppObject();
			} else {
				summary = edocManager.getEdocSummaryById(affair != null ? affair.getObjectId() : null, false);
			}
			if(affair != null){
				affair.setCompleteTime(now);
			}
			setTime2Affair(affair, summary);
		} catch (Exception e) {
			LOGGER.error("", e);
		}

		Map<String, Object> businessData = context.getBusinessData();
		int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) == null ? AUTODELETE : (Integer) businessData
				.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);

		// 更新当前处理人信息
		List<CtpAffair> affairs = new ArrayList<CtpAffair>();
		affairs.add(affair);
		EdocHelper.deleteAffairsNodeInfo(summary, affairs);
		try {
			affairManager.updateAffair(affair);
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}

		Long summaryId = affair != null ? affair.getObjectId() : null;
		if (operationType == STETSTOP) {// 终止
			return false;
		} else {
			boolean isHasAtt = false;
			if (context.getBusinessData("isAddAttachmentByOpinion") != null) {
				isHasAtt = Boolean.valueOf(String.valueOf(context.getBusinessData("isAddAttachmentByOpinion")));
			}
			boolean isSepicalBackedSubmit = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair != null ? affair.getSubState(): null);
			if (!isSepicalBackedSubmit) {
				// 发送完成事项消息提醒
				Boolean ok = EdocMessageHelper.workitemFinishedMessage(affairManager, orgManager, edocManager, userMessageManager, affair, summaryId, isHasAtt);
			}
		}

		return false;
	}

	// 事项取消
	public static boolean onWorkitemCanceled(EventDataContext context) {
		// 流程撤销、指定回退发起者流程重走：这块不需要处理，affair都在EdocController通过调用affairManager批量处理了。
		// 流程取回和流程回退、指定回退普通节点流程重走（注意：回退和指定回退是不一样的）这个方法要单独实现
		try {
			Map<String, Object> businessData = context.getBusinessData();
			int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) == null ? AUTODELETE : (Integer) businessData
					.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);
			WorkItem workitem = context.getWorkItem();
			if (workitem == null) {
				DateSharedWithWorkflowEngineThreadLocal.setOperationType(AUTODELETE);
			} else {
				if ((operationType == COMMONDISPOSAL || operationType == ZCDB) && !PROCESS_MODE_COMPETITION.equals(context.getProcessMode())) {
					DateSharedWithWorkflowEngineThreadLocal.setOperationType(AUTODELETE);
				}
			}
			String processMode = context.getProcessMode();
			if (operationType == AUTOSKIP && PROCESS_MODE_COMPETITION.equals(processMode)) {
				operationType = COMMONDISPOSAL;
			}
			CtpAffair affair = eventData2ExistingAffair(context);
			if (affair == null)
				return false;
			List<Long> cancelAffairIds = new ArrayList<Long>();
			long workItemId = workitem != null ? workitem.getId() : null;
			Timestamp now = new Timestamp(System.currentTimeMillis());
			/****************** 正常处理、暂存待办、竞争执行 ********************/
			if ((operationType == COMMONDISPOSAL || operationType == ZCDB || operationType == SPECIAL_BACK_SUBMITTO)
					&& PROCESS_MODE_COMPETITION.equals(processMode)) {
				List<CtpAffair> affairs = new ArrayList<CtpAffair>();
				List<CtpAffair> sendAffairs = affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
				if (!sendAffairs.isEmpty())
					affairs.addAll(sendAffairs);

				if (!affairs.isEmpty()) {
					StringBuffer hql = new StringBuffer();
					hql.append("UPDATE "
							+ CtpAffair.class.getName()
							+ " SET state=:state,subState=:subState,updateDate=:updateDate WHERE app=:app AND objectId=:objectId AND activityId=:activityId AND subObjectId<>:subObjectId");
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("state", StateEnum.col_competeOver.key());
					params.put("subState", SubStateEnum.col_normal.key());
					params.put("updateDate", now);
					params.put("app", affair.getApp());
					params.put("objectId", affair.getObjectId());
					params.put("subObjectId", workItemId);
					params.put("activityId", affair.getActivityId());
					DBAgent.bulkUpdate(hql.toString(), params);
					// 给在竞争执行中被取消的affair发送消息提醒
					EdocMessageHelper.competitionCancel(affairManager, orgManager, userMessageManager, workitem, affairs);
				}
				// 竞争执行，有人暂存待办，那么当前待办人就只有 暂存待办这个人了
				if (operationType == ZCDB) {
					StringBuffer hql = new StringBuffer();
					hql.append("from " + CtpAffair.class.getName()
							+ " WHERE app=:app AND objectId=:objectId AND activityId=:activityId AND subObjectId=:subObjectId");
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("app", affair.getApp());
					params.put("objectId", affair.getObjectId());
					params.put("subObjectId", workItemId);
					params.put("activityId", affair.getActivityId());
					List<CtpAffair> afs = DBAgent.find(hql.toString(), params);
					if (Strings.isNotEmpty(afs)) {
						CtpAffair zcdbAf = afs.get(0);
						EdocSummary summary = (EdocSummary) context.getAppObject();
						String currentNodesInfo = zcdbAf.getMemberId() + "";

						summary.setCurrentNodesInfo(currentNodesInfo);
					}
				}
			}
			/****************** 退回或是取回 ********************/
			else if (operationType == WITHDRAW || operationType == TAKE_BACK) {

				int state = operationType == TAKE_BACK ? StateEnum.col_takeBack.key() : StateEnum.col_stepBack.key();
				List<WorkItem> workItems = context.getWorkitemLists();
				// 更新affair状态
				AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
				List<CtpAffair> affairs = affairManager.getValidAffairs(ApplicationCategoryEnum.valueOf(affair.getApp()), affair.getObjectId());
				Map<Long, CtpAffair> m = new HashMap<Long, CtpAffair>();
				for (CtpAffair af : affairs) {
					if (af.getSubObjectId() != null) {
						m.put(af.getSubObjectId(), af);
					}
				}

				List<CtpAffair> cancelAffairs = new ArrayList<CtpAffair>();
				List<Long> workItemList = new ArrayList<Long>(workItems.size());

				List<String> normalStepBackTargetNodes = context.getNormalStepBackTargetNodes();
				Object currentNodeId = context.getBusinessData().get("currentAffairId");
				List<Long> _traceList = new ArrayList<Long>();
				int maxCommitNumber = 300;
				int length = workItems.size();
				int i = 0;
				for (WorkItem item : workItems) {
					workItemList.add(item.getId());
					if (m.keySet().contains(item.getId())) {
						CtpAffair af = m.get(item.getId());
						cancelAffairIds.add(af.getId());
						cancelAffairs.add(af);
						boolean isDoneNode = Integer.valueOf(StateEnum.col_done.getKey()).equals(af.getState());
						boolean isZCDBNode = Integer.valueOf(StateEnum.col_pending.key()).equals(af.getState())
								&& Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(af.getSubState());
						boolean isBackNode = af.getId().equals((Long) currentNodeId);
						boolean isBackedNode = af.getActivityId() == null ? true : normalStepBackTargetNodes.contains(String.valueOf(af.getActivityId()));

						/*if (operationType == WITHDRAW) {
							traceDao.deleteDynamicOldDataByAffair(af);
						}*/

						if (operationType == WITHDRAW && "1".equals(context.getBusinessData("isNeedTraceWorkflow")) && !isBackedNode
								&& (isDoneNode || isBackNode) || isZCDBNode) {
							edocTraceWorkflowManager.createStepBackTrackData(af, af.getSenderId(), AppContext.getCurrentUser().getId(),
									WorkflowTraceEnums.workflowTrackType.step_back_normal);
							_traceList.add(af.getId());
						}
						// 加入threadlocal，发消息时使用
						DateSharedWithWorkflowEngineThreadLocal.addToAllStepBackAffectAffairMap(af.getMemberId(), af.getId());
					}
					// if(operationType == TAKE_BACK) {//取回
					// this.updateAffairBySubObject(workItemList, state,
					// SubStateEnum.col_normal.key(),affair.getObjectId());
					// } else{
					// this.updateStepBackAffair(workItemList, state,
					// SubStateEnum.col_normal.key(),affair.getObjectId());
					// }
					i++;
					Map<String, Object> nameParameters = new HashMap<String, Object>();
					nameParameters.put("updateDate", new Date());
					nameParameters.put("state", state);
					nameParameters.put("subState", SubStateEnum.col_normal.key());
					if (i % maxCommitNumber == 0 || i == length) {
						Object[][] wheres = new Object[][] { { "objectId", affair.getObjectId() }, { "subObjectId", workItemList } };
						affairManager.update(nameParameters, wheres);
						workItemList = new ArrayList<Long>();
					}
				}
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_affair", _traceList);// 发消息用
				DateSharedWithWorkflowEngineThreadLocal
						.addToTraceDataMap("traceData_traceType", WorkflowTraceEnums.workflowTrackType.step_back_normal.getKey());// 发消息用
				// 更新当前处理人信息
				EdocSummary summary = (EdocSummary) context.getAppObject();
				EdocHelper.deleteAffairsNodeInfo(summary, cancelAffairs);

			}
			/****************** 默认删除操作: 督办替换节点，自动流程复合节点的单人执 ********************/
			else if (operationType == AUTODELETE) {
				// 删除被替换的所有affair事项
				List<CtpAffair> affairs = new ArrayList<CtpAffair>();
				if (context.getWorkitemLists() != null) {
					List<WorkItem> workitems = context.getWorkitemLists();
					affairs = superviseCancel(workitems, now);
				}
				if (affairs.isEmpty()) {
					affairs.add(affair);
				}
				// 2013-1-8给在督办中被删除的affair发送消息提醒
				// OA-50240 流程已经变更，原流程节点还能从消息进入处理页面，还能进行终止等操作
				edocMessagerManager.superviseDelete(workitem, affairs);
				// 替换节点后更新当前待办人信息
				EdocSummary summary = (EdocSummary) context.getAppObject();
				if (summary == null) {
					summary = edocManager.getEdocSummaryById(affair.getObjectId(), false);
				}
				EdocHelper.updateCurrentNodesInfo(summary, true);
			}
			/****************** 指定回退普通节点流程重走 ********************/
			else if (operationType == SPECIAL_BACK_RERUN) {
				EdocSummary summary = (EdocSummary) context.getAppObject();
				List<WorkItem> workItems = context.getWorkitemLists();
				if (Strings.isNotEmpty(workItems)) {
					CtpAffair firstAffair = affairManager.getAffairBySubObjectId(workItems.get(0).getId());
					if (firstAffair == null) {
						LOGGER.info("====firstAffair is null========workItems.get(0).getId():" + workItems.get(0).getId());
						return false;
					}

					int MaxCommitNumber = 300;
					int length = workItems.size();
					List<Long> workitemIds = new ArrayList<Long>();
					List<Long> activityIds = new ArrayList<Long>();
					List<CtpAffair> cancelAffairs = new ArrayList<CtpAffair>();
					int i = 0;
					int state = operationType == TAKE_BACK ? StateEnum.col_takeBack.key() : StateEnum.col_stepBack.key();
					ApplicationSubCategoryEnum subApp = EdocUtil.getSubAppCategoryByEdocType(summary.getEdocType());
					List<CtpAffair> affairs = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc, subApp, affair.getObjectId());
					Map<Long, CtpAffair> m = new HashMap<Long, CtpAffair>();
					for (CtpAffair af : affairs) {
						if (af.getSubObjectId() != null) {
							m.put(af.getSubObjectId(), af);
						}
					}

					List<Long> workitemIds1 = new ArrayList<Long>();
					for (WorkItem workItem : workItems) {
						CtpAffair af = m.get(workItem.getId());
						if (m.keySet().contains(workItem.getId())) {
							cancelAffairIds.add(af.getId());
							cancelAffairs.add(af);
							DateSharedWithWorkflowEngineThreadLocal.addToAllStepBackAffectAffairMap(af.getMemberId(), af.getId());
							Long[] arr = new Long[2];
							arr[0] = af.getId();
							arr[1] = Long.valueOf(af.getState());
							DateSharedWithWorkflowEngineThreadLocal.addToAllSepcialStepBackCanceledAffairMap(af.getMemberId(), arr);
						}

						if (af != null) {
							workitemIds1.add((long) workItem.getId());
						}

					}
					if (Strings.isNotEmpty(workitemIds1)) {
						List<Long>[] subList = Strings.splitList(workitemIds1, 1000);
						for (List<Long> list : subList) {

							StringBuffer hql = new StringBuffer();
							hql.append("update CtpAffair as affair set state=:state,subState=:subState,updateDate=:updateDate where objectId=:objectId and subObjectId in (:subObjectIds) ");
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("state", state);
							params.put("subState", SubStateEnum.col_normal.key());
							params.put("updateDate", now);
							params.put("objectId", affair.getObjectId());
							params.put("subObjectIds", list);
							DBAgent.bulkUpdate(hql.toString(), params);
						}

					}

					// 更新当前处理人信息
					EdocHelper.deleteAffairsNodeInfo(summary, cancelAffairs);
				}
			}
			if (!CollectionUtils.isEmpty(cancelAffairIds) && AppContext.hasPlugin("doc")) {
				docApi.deleteDocResources(AppContext.getCurrentUser().getId(), cancelAffairIds);
			}
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
		return true;
	}

	public static boolean onWorkitemAssigned(EventDataContext context) {
		/*
		 * context.getWorkItem().getId();//--->v3x_affair
		 * 
		 * System.out .println(
		 * "----------------------------------------------------------------------------------------------------------------"
		 * ); context.setAppName("test"); return true;
		 */
		AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
		// 非内容组件封装事件执行
		if (affairData == null)
			return true;
		// 首页栏目的扩展字段设置--公文文号、发文单位等--start
		Object docMarkObject = context.getBusinessData("edoc_send_doc_mark");
		Object sendUnitObject = context.getBusinessData("edoc_send_send_unit");
		// OA-40584新建公文，设置督办，督办人替换节点后查看首页待办的数据，没有将发文单位显示出来
		if (docMarkObject == null) {
			docMarkObject = affairData.getBusinessData("edoc_send_doc_mark");
		}
		if (sendUnitObject == null) {
			sendUnitObject = affairData.getBusinessData("edoc_send_send_unit");
		}
		Map<String, Object> extParam = new HashMap<String, Object>();
		extParam.put(AffairExtPropEnums.edoc_edocMark.name(), docMarkObject); // 公文文号
		extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), sendUnitObject);// 发文单位
		// OA-43885 首页待办栏目下，待开会议的主持人名字改变后，仍显示之前的名称
		EdocSummary summary = (EdocSummary) context.getAppObject();

		Boolean isCover = false;
		if (null == summary) {
			summary = edocManager.getSummaryByProcessId(context.getProcessId());
		}
		if (summary != null) {
			isCover = summary.getCoverTime();
		}
		extParam.put(AffairExtPropEnums.edoc_sendAccountId.name(), summary.getSendUnitId());// 发文单位ID
		// 首页栏目的扩展字段设置--公文文号、发文单位等--end
		List<WorkItem> workitems = context.getWorkitemLists();
		StringBuffer currentNodesInfo = new StringBuffer();
		Timestamp now = DateUtil.currentTimestamp();
		// CtpAffair affair = new CtpAffair();

		try {
			// 控制是否发送协同发起消息
			Boolean _isSendMessage = context.isSendMessage();
			Boolean isSendMessage = true;
			if (_isSendMessage != null && !_isSendMessage)
				isSendMessage = false;

			Long deadline = null;
			Long remindTime = null;
			String dealTermType = null;
			String dealTermUserId = null;
			Date deadLineRunTime = null;
			if (!EdocUtil.isBlank(context.getDealTerm())) {
				if (context.getDealTerm().matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d$")) {
					// 直接把处理的具体时间set到affair表中
					deadLineRunTime = DateUtil.parse(context.getDealTerm(), "yyyy-MM-dd HH:mm");
					// 当自定义时间时，不设置deadline值，区分超期时间是具体时间点和相对时间 两种不同的方式
					/*
					 * deadline = workTimeManager.getDealWithTimeValue(now,
					 * deadLineRunTime, affairData.getSummaryAccountId());
					 * deadline = deadline/1000/60;
					 */
				} else if (context.getDealTerm().matches("^-?\\d+$")) {
					deadline = Long.parseLong(context.getDealTerm());
					if (deadline != null && deadline != 0) {
						// 按工作日进行设置时间
						deadLineRunTime = workTimeManager.getCompleteDate4Nature(now, deadline, affairData.getSummaryAccountId());
					}
				}
				if (null != context.getDealTermType() && !"".equals(context.getDealTermType().trim())) {
					dealTermType = context.getDealTermType().trim();
				} else {
					dealTermType = "0";
				}
				if (null != context.getDealTermUserId() && !"".equals(context.getDealTermUserId().trim())) {
					dealTermUserId = context.getDealTermUserId();
				} else {
					dealTermUserId = "-1";
				}
			}

			if (Strings.isNotBlank(context.getRemindTerm()) && !("undefined").equals(context.getRemindTerm()) && !("null").equals(context.getRemindTerm())) {
				remindTime = Long.parseLong(context.getRemindTerm());
			}

			List<CtpAffair> affairs = new ArrayList<CtpAffair>(workitems.size());
			Long activityId = null;
			for (WorkItem workitem : workitems) {
				Long memberId;
				try {
					memberId = Long.parseLong(workitem.getPerformer());
				} catch (Exception e) {
					memberId = 123456L;
				}
				/************** 加上当前待办人 **************/
				if (currentNodesInfo.length() == 0) {
					currentNodesInfo.append(memberId);
				} else {
					currentNodesInfo.append(";" + memberId);
				}
				/************** 加上当前待办人 **************/

				CtpAffair affair = new CtpAffair();
				affair.setPreApprover(null != AppContext.getCurrentUser() ? AppContext.getCurrentUser().getId() : null);
				affair.setIdIfNew();

				ApplicationSubCategoryEnum subApp = EdocUtil.getSubAppCategoryByEdocType(summary.getEdocType());
				affair.setApp(ApplicationCategoryEnum.edoc.key());
				affair.setSubApp(subApp.key());
				// 设置subApp
				/*办件阅件TODO
				 * if (ApplicationCategoryEnum.edocRec.equals(app) && null != summary.getProcessType()) {
					if (2 == summary.getProcessType().intValue()) {
						affair.setSubApp(ApplicationSubCategoryEnum.edocRecRead.getKey());
					} else if (1 == summary.getProcessType().intValue()) {
						affair.setSubApp(ApplicationSubCategoryEnum.edocRecHandle.getKey());
					}
				}*/

				affair.setTrack(0);
				affair.setDelete(false);
				affair.setSubObjectId(Long.valueOf(workitem.getId()));
				affair.setMemberId(memberId);
				// 设置事项为待办和协同待办未读
				affair.setState(affairData.getState());
				affair.setSubState(SubStateEnum.col_pending_unRead.key());
				affair.setSenderId(affairData.getSender());
				affair.setSubject(affairData.getSubject());
				String nodePolicy = context.getPolicyId();
				if (nodePolicy != null) {
					nodePolicy = nodePolicy.replaceAll(new String(new char[] { (char) 160 }), " ");
				}
				affair.setNodePolicy(nodePolicy);
				AffairUtil.setHasAttachments(affair, affairData.getIsHasAttachment() == null ? false : affairData.getIsHasAttachment());
				// 协同的ID
				affair.setObjectId(affairData.getModuleId());
				affair.setDeadlineDate(deadline);
				try {
					affair.setDealTermType(Integer.parseInt(dealTermType));
				} catch (Throwable e) {
					affair.setDealTermType(0);
				}
				try {
					affair.setDealTermUserid(Long.parseLong(dealTermUserId));
				} catch (Throwable e) {
					affair.setDealTermUserid(-1l);
				}
				affair.setRemindDate(remindTime);
				if (null != remindTime && remindTime.equals(0L)) {
					affair.setRemindDate(null);
				}

				affair.setReceiveTime(now);
				affair.setApp(affairData.getModuleType());//
				affair.setSubApp(affairData.getSubModuleType());//
				affair.setCreateDate(affairData.getCreateDate() == null ? now : affairData.getCreateDate());
				// affair.sets(isSendMessage);
				affair.setTempleteId(affairData.getTemplateId());

				affair.setImportantLevel(affairData.getImportantLevel());
				affair.setResentTime(affairData.getResentTime());
				affair.setForwardMember(affairData.getForwardMember());

				affair.setProcessId(workitem.getProcessId());
				affair.setCaseId(workitem.getCaseId());

				affair.setOrgAccountId(summary.getOrgAccountId());

				// 设置加签、知会、会签的人员id
				affair.setFromId(Strings.isNotBlank(context.getAddedFromId()) ? Long.valueOf(context.getAddedFromId()) : null);

				// 回退的情况下覆盖fromId，显示的时候通过subState来区分,设置回退的人员id
				Map<String, Object> businessData = context.getBusinessData();
				int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) == null ? AbstractEventListener.AUTODELETE
						: (Integer) businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);

				// 回退、指定回退流程重走
				if (operationType == AbstractEventListener.WITHDRAW || operationType == AbstractEventListener.SPECIAL_BACK_RERUN) {
					affair.setBackFromId(AppContext.getCurrentUser().getId());
				}

				affair.setBodyType(affairData.getContentType());
				// affair.setNodePolicy(eventData.getN);
				activityId = Long.parseLong(workitem.getActivityId());
				affair.setActivityId(activityId);

				if (String.valueOf(MainbodyType.FORM.getKey()).equals(affairData.getContentType())) {
					affair.setFormAppId(Long.valueOf(null == context.getFormApp() ? "0" : context.getFormApp()));
//					affair.setFormId(Long.valueOf(null == context.getForm() ? "0" : context.getForm()));
//					affair.setFormOperationId(Long.valueOf(null == context.getOperationName() ? "0" : context.getOperationName()));
				}
				// 回退导致新生成的事项
				if (!isSendMessage) {
					DateSharedWithWorkflowEngineThreadLocal.addToAffairMap(memberId, affair.getId());
				}

				// 三个Boolean类型初始值，解决PostgreSQL插入记录异常问题
				affair.setFinish(false);
				affair.setCoverTime(false);
				affair.setDueRemind(true);
				// lijl添加if,OA-42474.开发---对外接口

				affair.setSummaryState(summary.getState());

				affair.setExpectedProcessTime(deadLineRunTime);
				AffairUtil.setExtProperty(affair, extParam);
				affairs.add(affair);
			}
			affairData.setAffairList(affairs);
			affairData.setIsSendMessage(isSendMessage);

			saveListMap(affairData, now, isCover);

			if (null != summary) {// 正常处理生成
				EdocHelper.setCurrentNodesInfo(summary, currentNodesInfo.toString());
			}

		} catch (Exception e) {
			LOGGER.error(BPMException.EXCEPTION_CODE_DATA_FORMAT_ERROR, e);
			// throw new
			// BPMException(BPMException.EXCEPTION_CODE_DATA_FORMAT_ERROR, e);
		}
		return true;
	}

	private static void saveListMap(AffairData affairData, Date receiveTime, Boolean isCover) {
		if (affairData == null)
			return;

		try {
			Long senderId = affairData.getSender();
			List<CtpAffair> affairList = affairData.getAffairList();
			Boolean isSendMessage = affairData.getIsSendMessage();

			if (affairList == null || affairList.isEmpty())
				return;

			String subject = affairData.getSubject();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(affairData.getForwardMember())) {
				forwardMember = affairData.getForwardMember();
				forwardMemberFlag = 1;
			}

			Integer importantLevel = affairData.getImportantLevel();
			String bodyContent = affairData.getBodyContent();
			String bodyType = affairData.getContentType();
			Date bodyCreateDate = affairData.getBodyCreateDate();

			List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
			List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
			CtpAffair aff = affairList.get(0);
			int app = aff.getApp();

			CtpAffair senderAffair = affairManager.getSenderAffair(aff.getObjectId());
			Long[] userInfoData = new Long[2];
			if (senderAffair != null) {
				userInfoData[0] = senderAffair.getMemberId();
				userInfoData[1] = senderAffair.getTransactorId();
			}
			DBAgent.saveAll(affairList);
			for (CtpAffair affair : affairList) {
				if (isSendMessage) {
					getReceiver(affairData.getIsSendMessage(), affair, app, receivers, receivers1);
				}
				// 提前提醒，超期提醒
				affairExcuteRemind(affair, affairData.getSummaryAccountId(), affair.getExpectedProcessTime());
			}
			// 生成事项消息提醒
			if (isSendMessage) {
				V3xOrgMember sender = null;
				try {
					sender = orgManager.getMemberById(senderId);
				} catch (Exception e1) {
					LOGGER.error("", e1);
					return;
				}
				// {1}发起协同:《{0}{2,choice,0|#1# (由{3}原发)}》
				Object[] subjects = new Object[] { subject, sender.getName(), forwardMemberFlag, forwardMember };
				sendMessage(aff, app, receivers, receivers1, sender, subjects, importantLevel, bodyContent, bodyType, bodyCreateDate, userInfoData);
			}
			// 发送流程超期消息
			if (isCover != null && isCover) {
				edocMessagerManager.transSendMsg4ProcessOverTime(aff, receivers, receivers1);
			}

			// 在此调用CallBack
			if (affairList == null || affairList.size() == 0)
				return;

			// if(DateSharedWithWorkflowEngineThreadLocal.isNeedIndex()) {
			// Affair affair0 = affairList.get(0);
			// if (affair0.getApp() ==
			// ApplicationCategoryEnum.collaboration.key()) {
			// CallbackHandler callback =
			// CallbackHandler.getCallbackHandler("ColIndex");
			// callback.invoke(affair0.getObjectId().toString());
			// }
			// }
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 设置Affair的运行时长，超时时长，按工作时间设置的运行时长，按工作时间设置的超时时长。
	 * 
	 * @param affair
	 * @throws BusinessException
	 */
	private static void setTime2Affair(CtpAffair affair, EdocSummary summary) throws BusinessException {
		// 工作日计算运行时间和超期时间。
		long runWorkTime = 0L;
		long orgAccountId = summary.getOrgAccountId();
		runWorkTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), new Date(), orgAccountId);
		runWorkTime = runWorkTime / (60 * 1000);
		Long deadline = 0l;
		Long workDeadline = 0l;
		if ((affair.getExpectedProcessTime() != null || affair.getDeadlineDate() != null) && !Long.valueOf(0).equals(affair.getDeadlineDate())) {
			if (affair.getDeadlineDate() != null) {
				deadline = affair.getDeadlineDate().longValue();
			} else {
				deadline = workTimeManager.getDealWithTimeValue(DateUtil.currentTimestamp(), affair.getExpectedProcessTime(), orgAccountId);
				deadline = deadline / 1000 / 60;
			}
			workDeadline = workTimeManager.convert2WorkTime(deadline, orgAccountId);
		}
		// 超期工作时间
		Long overWorkTime = 0L;
		// 设置了处理期限才进行计算,没有设置处理期限的话,默认为0;
		if (workDeadline != null && workDeadline != 0) {
			long ow = runWorkTime - workDeadline;
			overWorkTime = ow > 0 ? ow : 0l;
		}
		// 自然日计算运行时间和超期时间
		Long runTime = (System.currentTimeMillis() - affair.getReceiveTime().getTime()) / (60 * 1000);
		Long overTime = 0L;
		if (affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) {
			Long o = runTime - affair.getDeadlineDate();
			overTime = o > 0 ? o : null;
		}

		// 避免时间到了定时任务还没有执行。暂时不需要考虑是否在工作时间，因为定时任务那边也没有考虑，先保持一致。
		if (null != affair.getExpectedProcessTime() && new Date().after(affair.getExpectedProcessTime())) {
			affair.setCoverTime(true);
		}

		if (affair.isCoverTime() != null && affair.isCoverTime()) {
			if (Long.valueOf(0).equals(overTime))
				overTime = 1l;
			if (Long.valueOf(0).equals(overWorkTime))
				overWorkTime = 1l;
		}
		affair.setOverTime(overTime);
		affair.setOverWorktime(overWorkTime);
		affair.setRunTime(runTime);
		affair.setRunWorktime(runWorkTime);
	}

	private static void getReceiver(boolean isSendMessage, CtpAffair affair, int app, List<MessageReceiver> receivers, List<MessageReceiver> receivers1) {
		Long theMemberId = affair.getMemberId();
		if (app == ApplicationCategoryEnum.collaboration.key()) {
			if (isSendMessage) {
				Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), theMemberId,
						affair.getTempleteId());
				if (agentMemberId != null) {
					receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending", affair.getId().toString()));
					receivers1.add(new MessageReceiver(affair.getId(), agentMemberId, "message.link.col.pending", affair.getId().toString()));
				} else {
					receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending", affair.getId().toString()));
				}
			}
		} else if (app==ApplicationCategoryEnum.edoc.key() && 
        		(affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSend.key() 
        		|| affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocRec.key()
                || affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSign.key())) {
			if (isSendMessage) {
				Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), theMemberId);
				if (agentMemberId != null) {
					receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId().toString()));
					receivers1.add(new MessageReceiver(affair.getId(), agentMemberId, "message.link.edoc.pending", affair.getId().toString()));
				} else {
					receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId().toString()));
				}
			}
		}
	}

	private static void affairExcuteRemind(CtpAffair affair, Long summaryAccountId, Date deadLineRunTime) {
		if (affair.getApp() == ApplicationCategoryEnum.collaboration.key() || 
				(affair.getApp()==ApplicationCategoryEnum.edoc.key() && 
					(affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSend.key() 
						|| affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocRec.key()
						|| affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSign.key()))) {
			// 超期提醒
			try {
				/*
				 * Date createTime = affair.getReceiveTime() == null ?
				 * affair.getCreateDate() : affair.getReceiveTime(); Long
				 * deadLine = affair.getDeadlineDate();
				 */
				if (deadLineRunTime != null) {

					Long affairId = affair.getId();
					{
						String name = "DeadLine" + affairId;

						Map<String, String> datamap = new HashMap<String, String>(2);

						datamap.put("isAdvanceRemind", "1");
						datamap.put("affairId", String.valueOf(affairId));

						// 增加30秒随机数
						int randomInOneMinte = (int) (Math.random() * 30 + 1) * 1000;
						Date _runDate = new java.sql.Timestamp(deadLineRunTime.getTime() + randomInOneMinte);

						QuartzHolder.newQuartzJob(name, _runDate, "affairIsOvertopTimeJob", datamap);
					}

					Long remindTime = affair.getRemindDate();
					if (remindTime != null && !Long.valueOf(0).equals(remindTime) && !Long.valueOf(-1).equals(remindTime)) {
						Date advanceRemindTime = workTimeManager.getRemindDate(deadLineRunTime, remindTime);// .getCompleteDate4Nature(new
																											// Date(createTime.getTime()),
																											// deadLine
																											// -
																											// remindTime,
																											// summaryAccountId);

						String name = "Remind" + affairId;

						Map<String, String> datamap = new HashMap<String, String>(2);

						datamap.put("isAdvanceRemind", "0");
						datamap.put("affairId", String.valueOf(affairId));

						QuartzHolder.newQuartzJob(name, advanceRemindTime, "affairIsOvertopTimeJob", datamap);
					}
				}

			} catch (Exception e) {
				LOGGER.error("获取定时调度器对象失败", e);
			}
		}
	}

	private static void sendMessage(CtpAffair affair, int app, List<MessageReceiver> receivers, List<MessageReceiver> receivers1, V3xOrgMember sender,
			Object[] subjects, Integer importantLevel, String bodyContent, String bodyType, Date bodyCreateDate, Long[] userInfoData) {
		Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
		if (app == ApplicationCategoryEnum.collaboration.key()) {
			try {
				userMessageManager.sendSystemMessage(
						MessageContent.get("col.send", subjects).setBody(bodyContent, bodyType, bodyCreateDate).setImportantLevel(importantLevel),
						ApplicationCategoryEnum.edoc, sender.getId(), receivers, systemMessageFilterParam);
			} catch (Exception e) {
				LOGGER.error("发起协同消息提醒失败!", e);
			}
			if (receivers1 != null && receivers1.size() != 0) {
				try {
					userMessageManager.sendSystemMessage(
							MessageContent.get("col.send", subjects).setBody(bodyContent, bodyType, bodyCreateDate).add("col.agent")
									.setImportantLevel(importantLevel), ApplicationCategoryEnum.edoc, sender.getId(), receivers1, systemMessageFilterParam);
				} catch (Exception e) {
					LOGGER.error("发起协同消息提醒失败!", e);
				}
			}
		} else if (app==ApplicationCategoryEnum.edoc.key() && 
        		(affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSend.key() 
        		|| affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocRec.key()
                || affair.getSubApp().intValue() == ApplicationSubCategoryEnum.old_edocSign.key())) {

			ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
			if (userInfoData.length == 2 && userInfoData[1] != null) {// 登记的时候是代理人进行登记的。
				try {
					String agentToName = "";
					Long agentToId = userInfoData[1];
					try {
						agentToName = orgManager.getMemberById(agentToId).getName();
					} catch (Exception e) {
						LOGGER.error("获取代理人名字抛出异常", e);
					}
					userMessageManager.sendSystemMessage(
							MessageContent.get("edoc.send", subjects[0], agentToName, app).setBody(bodyContent, bodyType, bodyCreateDate)
									.setImportantLevel(importantLevel).add("edoc.agent.deal", sender.getName()), appEnum, agentToId, receivers,
							systemMessageFilterParam);
					if (receivers1 != null && receivers1.size() != 0) {
						userMessageManager.sendSystemMessage(
								MessageContent.get("edoc.send", subjects[0], agentToName, app).setBody(bodyContent, bodyType, bodyCreateDate)
										.add("edoc.agent.deal", sender.getName()).add("col.agent").setImportantLevel(importantLevel), appEnum, agentToId,
								receivers1, systemMessageFilterParam);
					}
				} catch (Exception e) {
					LOGGER.error("发起公文消息提醒失败!", e);
				}
			} else {
				try {
					userMessageManager.sendSystemMessage(
							MessageContent.get("edoc.send", subjects[0], sender.getName(), app).setBody(bodyContent, bodyType, bodyCreateDate)
									.setImportantLevel(importantLevel), appEnum, sender.getId(), receivers, systemMessageFilterParam);
				} catch (Exception e) {
					LOGGER.error("发起公文消息提醒失败!", e);
				}
				if (receivers1 != null && receivers1.size() != 0) {
					try {
						userMessageManager.sendSystemMessage(
								MessageContent.get("edoc.send", subjects[0], sender.getName(), app).setBody(bodyContent, bodyType, bodyCreateDate)
										.add("col.agent").setImportantLevel(importantLevel), appEnum, sender.getId(), receivers1, systemMessageFilterParam);
					} catch (Exception e) {
						LOGGER.error("发起公文消息提醒失败!", e);
					}
				}
			}
		}
	}

	// 通过workitemId得到affair
	private static CtpAffair eventData2ExistingAffair(EventDataContext eventData) throws BusinessException {
		WorkItem workitem = eventData.getWorkItem();
		int operationType = DateSharedWithWorkflowEngineThreadLocal.getOperationType();
		CtpAffair affair = affairManager.getAffairBySubObjectId(workitem.getId());
		if (affair != null) {
			switch (operationType) {
			case 1: // 回退
			case 2: // 取回
				affair.setState(StateEnum.col_stepBack.key());
				affair.setSubState(SubStateEnum.col_normal.key());
				break;
			case 10: // 撤销
				affair.setState(StateEnum.col_cancel.key());
				affair.setSubState(SubStateEnum.col_normal.key());
				break;
			case 9: // 正常处理
			case 13: // 自动跳过
				affair.setState(StateEnum.col_done.key());
				affair.setSubState(SubStateEnum.col_normal.key());
				break;
			case 8: // 终止
				affair.setState(StateEnum.col_done.key());
				affair.setSubState(SubStateEnum.col_done_stepStop.key());
				affair.setCompleteTime(new Timestamp(System.currentTimeMillis()));
				break;
			}
		}
		return affair;
	}

	private static List<CtpAffair> superviseCancel(List<WorkItem> workitems, Timestamp now) throws BusinessException {
		List<CtpAffair> affair4Message = new ArrayList<CtpAffair>();
		if (workitems == null || workitems.size() == 0)
			return affair4Message;
		List<Long> ids = new ArrayList<Long>();
		Map<String, Object> nameParameters = new HashMap<String, Object>();
		for (int i = 0; i < workitems.size(); i++) {
			ids.add((long) ((WorkItem) workitems.get(i)).getId());
			// 防止in超长，300个一更新，事务上会有问题
			if ((i + 1) % 300 == 0 || i == workitems.size() - 1) {
				nameParameters.put("subObjectId", ids);
				StringBuffer hql = new StringBuffer();
				hql.append("update CtpAffair as a set a.state=:state,a.subState=:subState,a.updateDate=:updateDate,a.delete=1 where a.subObjectId in (:subObjectIds)");
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("state", StateEnum.col_cancel.key());
				params.put("subState", SubStateEnum.col_normal.key());
				params.put("updateDate", now);
				params.put("subObjectIds", ids);
				DBAgent.bulkUpdate(hql.toString(), params);
				/*
				 * DBAgent.bulkUpdate("update " + CtpAffair.class.getName() +
				 * " set state=?,subState=?,updateDate=?,delete=1 where subObjectId in (?)"
				 * ,
				 * StateEnum.col_cancel.key(),SubStateEnum.col_normal.key(),now
				 * ,ids);
				 */
				List<CtpAffair> affairs = affairManager.getByConditions(null, nameParameters);
				affair4Message.addAll(affairs);
				ids.clear();
			}
		}
		return affair4Message;
	}

}
