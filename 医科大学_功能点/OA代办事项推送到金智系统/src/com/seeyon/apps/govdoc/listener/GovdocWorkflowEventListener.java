package com.seeyon.apps.govdoc.listener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.ext.kypending.event.GovdocOperationEvent;
import com.seeyon.apps.govdoc.manager.*;
import com.seeyon.ctp.event.EventDispatcher;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.bo.SendGovdocResult;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocWorkflowTypeEnum;
import com.seeyon.apps.govdoc.event.GovdocEventDispatcher;
import com.seeyon.apps.govdoc.helper.GovdocAffairHelper;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocMessageHelper;
import com.seeyon.apps.govdoc.helper.GovdocWorkflowHelper;
//import com.seeyon.apps.govdoc.manager.GovdocOCIPExchangeManager;
import com.seeyon.apps.govdoc.service.GovdocApplicationHandler;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.ocip.OCIPConstants;
import com.seeyon.apps.ocip.util.OrgUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.trace.api.TraceWorkflowDataManager;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.common.trace.po.WorkflowTracePO;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ocip.common.org.OrgMember;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.Global;
import com.seeyon.ocip.online.OnlineChecker;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.workflow.event.EdocWorkFlowOldHelper;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;

import net.joinwork.bpm.engine.wapi.WorkItem;

public class GovdocWorkflowEventListener extends GovdocAbWorkflowEventListener {

    private static final Log LOGGER = LogFactory.getLog(GovdocWorkflowEventListener.class);
    private boolean hasMutiPlugin = Boolean.valueOf(AppContext.getSystemProperty(OCIPConstants.HAS_MUTI_PLUGIN));//是否开启多G6功能
    /**
     * 当前操作MEMBER
     */
    public static final String CURRENT_OPERATE_MEMBER_ID = "CURRENT_OPERATE_MEMBER_ID";
    public static final String CURRENT_OPERATE_SUMMARY_ID = "CURRENT_OPERATE_SUMMARY_ID";
    public static final String CURRENT_OPERATE_TRACK_FLOW_TYPE = "CURRENT_OPERATE_TRACK_FLOW_TYPE";
    public static final String CURRENT_OPERATE_TRACK_FLOW = "CURRENT_OPERATE_TRACK_FLOW";
    public static final String CURRENT_OPERATE_CIRCLE_BACK = "CURRENT_OPERATE_CIRCLE_BACK";
    public static final String CURRENT_OPERATE_COMMENT_CONTENT = "CURRENT_OPERATE_COMMENT_CONTENT";
    public static final String CURRENT_OPERATE_AFFAIR_ID = "CURRENT_OPERATE_AFFAIR_ID";
    public static final String CURRENT_OPERATE_COMMENT_ID = "CURRENT_OPERATE_COMMENT_ID";

    public static final String EDOCSUMMARY_CONSTANT = "EdocSummary";
    public static final String CTPAFFAIR_CONSTANT = "CtpAffair";
    public static final String CURRENTUSER_CONSTANT = "currentUser";
    public static final String AFFAIR_SUB_STATE = "subState";

    public static final Integer SpecialBackReRun = 100;//指定回退流程重走
    public static final Integer IS_XU_BAN = 8;//续办的formType

    public static final String WF_APP_GLOBAL = "WF_APP_GLOBAL"; //流程和应用穿透传参的KEY
    public static final String WF_APP_GLOBAL_REPEAT_AFFMAP = "WF_APP_GLOBAL_REPEAT_AFFMAP"; //重复跳过的相关数据
    public static final String WF_ALL_VALID_AFFAIRS = "WF_ALL_VALID_AFFAIRS"; //重复跳过的相关数据

    public static final String CANCELED_MIDTOAID_MAP = "WORKITEMCANCELED_MEMBERIDTOAFFAIRID";  //被权限的事项的人员ID-事项ID
    public static final String CANCELED_MIDTOARRAY_MAP = "WORKITEMCANCELED_MEMBERIDTOARRAY"; //被权限的事项的人员ID-数组


    private GovdocManager govdocManager;
    private GovdocSummaryManager govdocSummaryManager;
    private GovdocMessageManager govdocMessageManager;
    public TraceWorkflowDataManager govdocTraceWorkflowManager;
    private FormApi4Cap3 formApi4Cap3;
    private IOrganizationManager organizationManager;
    //    private GovdocOCIPExchangeManager govdocOCIPExchangeManager;
    private CollaborationApi collaborationApi;

    @Override
    public String getSubAppName() {
        return GovdocWorkflowTypeEnum.formedoc.name();
    }

    /**
     * 流程开启回调(暂未用)
     */
    @Override
    public boolean onProcessStarted(EventDataContext context) {
        return true;
    }

    /**
     * 流程结束回调
     * 1
     * 2 当流程中只有知会节点时，发送流程后会视为流程结束，调用该方法
     */
    @Override
    public boolean onProcessFinished(EventDataContext context) {
        try {
            EdocSummary summary = (EdocSummary) context.getAppObject();
            if (summary == null) {
                summary = govdocSummaryManager.getSummaryByProcessId(context.getProcessId());
            }
            if (summary == null) {
                return true;
            }
            Map<String, Object> businessData = context.getBusinessData();
            int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) == null ? COMMONDISPOSAL : (Integer) businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);
            String action = STETSTOP.equals(operationType) ? "stepstop" : "finish";

            //OA-168832
			/*if(StringUtils.isNotBlank(context.getGoNextMsg())){
				List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
			    List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
			    if(Strings.isNotEmpty(trackingAffairList)){
			        for(CtpAffair affair : trackingAffairList){
			            receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), ""));
			        }
			    }
			    if(Strings.isNotEmpty(receivers)){
			        userMessageManager.sendSystemMessage(new MessageContent(context.getGoNextMsg()), ApplicationCategoryEnum.edoc, AppContext.currentUserId(), receivers);
			    }
			
			}*/

            GovdocBaseVO baseVo = new GovdocBaseVO();
            baseVo.setAction(action);
            baseVo.setSummary(summary);
            baseVo.setAffair((CtpAffair) context.getBusinessData(GovdocWorkflowEventListener.CTPAFFAIR_CONSTANT));
            govdocManager.attachmentArchive(summary);
            govdocManager.transProcessFinishCallback(baseVo);

            //流程结束事件通知
            if (context.getBusinessData(CTPAFFAIR_CONSTANT) != null) {
                baseVo.setAffair((CtpAffair) context.getBusinessData(CTPAFFAIR_CONSTANT));
                GovdocEventDispatcher.fireEdocFinishEvent(this, baseVo);
            }

            //zhou:[医科大学代办消息推送]添加公文流程自定义事件 start
            GovdocOperationEvent operationEvent = new GovdocOperationEvent(this);
            operationEvent.setCurrentAffair((CtpAffair) context.getBusinessData(CTPAFFAIR_CONSTANT));
            operationEvent.setType("finish");//分配
            operationEvent.setSummaryId(summary.getId().longValue() + "");
            EventDispatcher.fireEvent(operationEvent);
            //zhou:[医科大学代办消息推送]添加公文流程自定义事件 end

        } catch (Exception e) {
            LOGGER.error("公文流程结束出错", e);
        }
//		try{
//			if(hasMutiPlugin&&OnlineChecker.isOnline())
//				govdocOCIPExchangeManager.onProcessFinished(context);
//		}catch(Exception e){
//			LOGGER.error("多G6出现异常", e);
//		}
        return true;
    }

    /**
     * 流程撤销回调
     */
    @Override
    public boolean onProcessCanceled(EventDataContext context) {
        try {
            Long affairId = null;
            if (context.getBusinessData(CURRENT_OPERATE_AFFAIR_ID) != null) {
                affairId = (Long) context.getBusinessData(CURRENT_OPERATE_AFFAIR_ID);
            }
            String repealComment = "";
            if (context.getBusinessData(CURRENT_OPERATE_COMMENT_CONTENT) != null) {
                repealComment = (String) context.getBusinessData(CURRENT_OPERATE_COMMENT_CONTENT);
            }
            Long summaryId = null;
            if (context.getBusinessData(CURRENT_OPERATE_SUMMARY_ID) != null) {
                summaryId = (Long) context.getBusinessData(CURRENT_OPERATE_SUMMARY_ID);
            }
            String isWFTrace = "";
            if (context.getBusinessData(CURRENT_OPERATE_TRACK_FLOW) != null) {
                isWFTrace = (String) context.getBusinessData(CURRENT_OPERATE_TRACK_FLOW);
            }
            String traceWorkflowType = "";
            if (context.getBusinessData(CURRENT_OPERATE_TRACK_FLOW_TYPE) != null) {
                traceWorkflowType = ((Integer) context.getBusinessData(CURRENT_OPERATE_TRACK_FLOW_TYPE)).toString();
            }
            if (affairId == null) {//没有值，这从workitem中来查
                if (null != context.getCurrentWorkitemId()) {
                    CtpAffair currentAffair = affairManager.getAffairBySubObjectId(context.getCurrentWorkitemId());
                    affairId = currentAffair.getId();
                    summaryId = currentAffair.getObjectId();
                }
            }
            CtpAffair affair = affairManager.get(affairId);

            Object _operationType = (Object) context.getBusinessData().get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);
            Integer _ioperationType = null;
            if (_operationType != null) {
                _ioperationType = (Integer) _operationType;
            }
            //是否是回退操作，回退操作就不需要重复保存意见，发消息，流程日志之类的了。
            boolean isSBoperation = "1".equals((String) context.getBusinessData("isSBoperation"));
            boolean isStepbackAction = isSBoperation || WITHDRAW.equals(_ioperationType) || SpecialBackReRun.equals(_ioperationType);
            GovdocBaseVO baseVo = new GovdocBaseVO();
            baseVo.setSummaryId(summaryId);
            baseVo.setAffairId(affairId);
            baseVo.setAffair(affair);
            baseVo.setCommentContent(repealComment);
            baseVo.setIsWFTrace(isWFTrace);
            baseVo.setTrackWorkflowType(traceWorkflowType);
            baseVo.setAction(isStepbackAction ? "stepback" : "cancel");
            baseVo.setCurrentUser((User) context.getBusinessData(GovdocWorkflowEventListener.CURRENTUSER_CONSTANT));
            govdocManager.transRepealCallback(baseVo);

            //zhou:[医科大学代办消息推送]添加公文流程自定义事件 start
            GovdocOperationEvent operationEvent = new GovdocOperationEvent(this);
            operationEvent.setCurrentAffair(affair);
            operationEvent.setType("cancel");//分配
            operationEvent.setSummaryId(summaryId.longValue()+"");
            EventDispatcher.fireEvent(operationEvent);
            //zhou:[医科大学代办消息推送]添加公文流程自定义事件 end
        } catch (Exception e) {
            LOGGER.error("", e);
        }
//		try{
//			if(hasMutiPlugin&&OnlineChecker.isOnline())
//				govdocOCIPExchangeManager.onProcessCanceled(context);;
//		}catch(Exception e){
//			LOGGER.error("多G6出现异常", e);
//		}
        return true;// 在controller中更新当前affair为待办状态，这里不需要做其他操作。
    }

    /**
     * 节点生成回调-单个
     * 1 发送生成新的节点
     * 2 处理生成下一新节点
     */
    @Override
    public boolean onWorkitemAssigned(EventDataContext context) {
        List<EventDataContext> contextList = new ArrayList<EventDataContext>();
        contextList.add(context);
        return onWorkflowAssigned(contextList);
    }

    /**
     * 节点生成回调-批量
     * 1 发送生成新的节点
     * 2 处理生成下一新节点
     */
    @Override
    public boolean onWorkflowAssigned(List<EventDataContext> contextList) {
        boolean isNotEmptyContextList = Strings.isNotEmpty(contextList);
        LOGGER.info(AppContext.currentUserName() + " in onWorkflowAssigned ,contextList.size:" + (isNotEmptyContextList ? contextList.size() : 0));
        if (!isNotEmptyContextList) {
            return true;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> global = (Map<String, Object>) contextList.get(0).getBusinessData(WF_APP_GLOBAL);
        if (global == null) {
            global = new HashMap<String, Object>();
        }
        Map<String, String> repeatMap = new HashMap<String, String>();
        EventDataContext firstContext = contextList.get(0);
        String processIdStr = firstContext.getProcessId();
        AffairData affairData = (AffairData) firstContext.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
        // 回退生成
        EdocSummary summary = null;
        //非内容组件封装事件执行
        if (affairData == null && Strings.isNotBlank(processIdStr) && Strings.isDigits(processIdStr)) {
            try {
                summary = govdocSummaryManager.getSummaryByProcessId(processIdStr);
                if (null != summary) {
                    affairData = GovdocHelper.getAffairData(summary, AppContext.getCurrentUser());
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        if (affairData == null) {
            return true;
        }

        Object summaryObj = firstContext.getBusinessData(EDOCSUMMARY_CONSTANT);
        if (summaryObj != null) {
            summary = (EdocSummary) summaryObj;
        } else if (summary == null) {
            try {
                summary = govdocSummaryManager.getSummaryById(affairData.getModuleId());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (summary.getGovdocType() == 0) {
            GovdocApplicationHandler handler = (GovdocApplicationHandler) AppContext.getBean("govdocApplicationHandler");
            try {
                GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler(GovdocWorkflowTypeEnum.oldedoc.name());
                return listener.onWorkflowAssigned(contextList);
            } catch (Exception e) {
                LOGGER.error("老公文onWorkflowAssigned出错", e);
                return false;
            }
        }

        Timestamp now = DateUtil.currentTimestamp();

        // 回退的情况下覆盖fromId，显示的时候通过subState来区分,设置回退的人员id
        int operationType = (Integer) ((Integer) firstContext.getBusinessData().get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) == null ? 12 : firstContext.getBusinessData().get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE));
        // 控制是否发送协同发起消息
        boolean isSendMessage = firstContext.isSendMessage();
        affairData.setIsSendMessage(isSendMessage);
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();

        Long currentMemberId = 0l;
        CtpAffair currentAffair = null;
        Object currentAffairObj = firstContext.getBusinessData(CTPAFFAIR_CONSTANT);
        if (currentAffairObj != null) {
            currentAffair = (CtpAffair) currentAffairObj;
        }
        Object obj = firstContext.getBusinessData(CURRENT_OPERATE_MEMBER_ID);
        if (obj != null) {
            currentMemberId = (Long) obj;
        }
        //zhou:[医科大学]当前代办事项 start
        CtpAffair ykdAffair = currentAffair;
        //zhou:[医科大学]当前代办事项 end
        try {
            //重复自动跳过的事项
            List<Long> isRepeatAutoSkipAffairIds = new ArrayList<Long>();
            //创建List存放affairid与skipAgentId的对应关系列表
            List<Map<String, String>> autoSkipArray = new ArrayList<Map<String, String>>();
            Set<Long> colDoneMemberIds = new HashSet<Long>();
            Set<Long> preDoneMemberIds = new HashSet<Long>();
            Map<Long, String> cantAutoSkipReson = new HashMap<Long, String>();
            List<CtpAffair> doneAffairList = new ArrayList<CtpAffair>();
            boolean processCanAnyDealMerge = GovdocWorkflowHelper.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE, summary);
            boolean processCanPreDealMerge = GovdocWorkflowHelper.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE, summary);
            boolean processCanStartMerge = GovdocWorkflowHelper.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE, summary);
            boolean processCanMerge = processCanAnyDealMerge || processCanPreDealMerge || processCanStartMerge;
            boolean hasLoadDoneAffair = false;
            boolean hasLoadPreDealAffair = false;
            //节点因为 什么条件符合合并处理
            String mergeDealType = "";
            if (processCanStartMerge) {
                List<StateEnum> states = new ArrayList<StateEnum>();
                states.add(StateEnum.col_sent);
                states.add(StateEnum.col_done);
                List<CtpAffair> affairList = affairManager.getAffairs(summary.getId(), states);
                for (CtpAffair ctpAffair : affairList) {
                    colDoneMemberIds.add(ctpAffair.getMemberId());
                }
            }
            //获取affair标题
            if (summary.getTempleteId() != null) {
                CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
                if (ctpTemplate != null) {
                    GovdocFormManager govdocFormManager = (GovdocFormManager) AppContext.getBean("govdocFormManager");
                    String affairSubject = govdocFormManager.makeSubject(ctpTemplate, summary);
                    summary.setDynamicSubject(affairSubject);
                }
            }
            for (EventDataContext context : contextList) {
                List<WorkItem> workitems = context.getWorkitemLists();
                Long deadline = null;
                Long remindTime = null;
                int dealTermType = 0;
                long dealTermUserId = -1;
                Date nodeDeaLineRunTime = null;
                if (WFComponentUtil.isNotBlank(context.getDealTerm())) {
                    if (context.getDealTerm().matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d$")) {
                        nodeDeaLineRunTime = DateUtil.parse(context.getDealTerm(), "yyyy-MM-dd HH:mm");
                        deadline = workTimeManager.getDealWithTimeValue(now, nodeDeaLineRunTime, affairData.getSummaryAccountId());
                        deadline = deadline / 1000 / 60;
                    } else if (context.getDealTerm().matches("^-?\\d+$")) {
                        deadline = Long.parseLong(context.getDealTerm());
                        if (null != deadline && 0 != deadline) {
                            nodeDeaLineRunTime = workTimeManager.getCompleteDate4Nature(now, deadline, affairData.getSummaryAccountId());
                        }
                    } else if (context.getDealTerm().startsWith("field")) {
                        //表单字段流程期限 trycatch是为了如果字段有为题也能发送
                        try {
                            Map<String, Object> formData = (Map<String, Object>) context.getBusinessData("CTP_FORM_DATA");
                            Object date = formData.get(context.getDealTerm());
                            if (date != null && StringUtils.isNotBlank(date.toString())) {
                                if (date instanceof Date) {
                                    nodeDeaLineRunTime = (Date) date;
                                } else if (date instanceof String) {
                                    nodeDeaLineRunTime = Strings.isBlank((String) date) ? null : Datetimes.parse(date.toString());
                                }
                                if (nodeDeaLineRunTime != null) {
                                    deadline = workTimeManager.getDealWithTimeValue(now, nodeDeaLineRunTime, affairData.getSummaryAccountId());
                                    deadline = deadline / 1000 / 60;
                                }
                            }
                        } catch (Exception e) {
                            deadline = null;
                            nodeDeaLineRunTime = null;
                            LOGGER.error("表单元素转换流程期限失败", e);
                        }
                    }
                    if (WFComponentUtil.isNotBlank(context.getDealTermType())) {
                        dealTermType = Integer.parseInt(context.getDealTermType().trim());
                    }
                    if (WFComponentUtil.isNotBlank(context.getDealTermUserId()) && WFComponentUtil.isLong(context.getDealTermUserId())) {
                        dealTermUserId = Long.parseLong(context.getDealTermUserId().trim());
                    }
                }
                if (WFComponentUtil.isNotBlank(context.getRemindTerm())) {
                    remindTime = Long.parseLong(context.getRemindTerm().trim());
                }
                //节点合并处理设置
                String nodeMergeDealType = context.getMergeDealType();
                boolean nodeMerge4Process = true;
                if (Strings.isNotBlank(nodeMergeDealType)) {
                    nodeMerge4Process = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.PROCESS_MERGE.getValue());
                }
                //默认不合并处理
                boolean canAnyDealMerge = false;
                boolean canPreDealMerge = false;
                boolean canStartMerge = false;
                boolean canMerge = false;

                //按照流程设置匹配是否合并处理
                if (nodeMerge4Process) {
                    canAnyDealMerge = processCanAnyDealMerge;
                    canPreDealMerge = processCanPreDealMerge;
                    canStartMerge = processCanStartMerge;
                    canMerge = processCanMerge;
                } else {//按照节点设置是否合并处理
                    boolean noMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.NO_MERGE.getValue());
                    if (!noMerge) {
                        canMerge = true;
                        canStartMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue());
                        canPreDealMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue());
                        canAnyDealMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue());
                    }

                }
                //设置与前面任一节点合并时读取已办事项
                if (canAnyDealMerge && !hasLoadDoneAffair) {
                    //已办事项
                    List<StateEnum> states = new ArrayList<StateEnum>();
                    states.add(StateEnum.col_done);
                    doneAffairList = affairManager.getAffairs(summary.getId(), states);
                    for (CtpAffair ctpAffair : doneAffairList) {
                        colDoneMemberIds.add(ctpAffair.getMemberId());
                    }
                    if (currentAffair != null
                            && (Integer.valueOf(StateEnum.col_pending.getKey()).equals(currentAffair.getState())
                            || Integer.valueOf(StateEnum.col_pending_repeat_auto_deal.getKey()).equals(currentAffair.getState())
                            || Integer.valueOf(StateEnum.col_done.getKey()).equals(currentAffair.getState()))) {
                        colDoneMemberIds.add(currentAffair.getMemberId());
                    }
                    hasLoadDoneAffair = true;
                }

                //设置了与上一步相同时合并处理并且没有读取数据时读取上一步处理人
                if (canPreDealMerge && !hasLoadPreDealAffair && currentAffair != null) {
                    //上一节点处理人的affair
                    Long activityId = currentAffair.getActivityId();
                    //前面已经查过的不再查询
                    if (Strings.isNotEmpty(doneAffairList)) {
                        for (CtpAffair ctpAffair : doneAffairList) {
                            if (activityId.equals(ctpAffair.getActivityId())) {
                                preDoneMemberIds.add(ctpAffair.getMemberId());
                            }
                        }
                    } else {//没查过重新查询一次
                        List<CtpAffair> preAffairList = affairManager.getAffairsByObjectIdAndNodeId(summary.getId(), activityId);
                        for (CtpAffair ctpAffair : preAffairList) {
                            if (Integer.valueOf(StateEnum.col_done.getKey()).equals(ctpAffair.getState())) {
                                preDoneMemberIds.add(ctpAffair.getMemberId());
                            }
                        }
                    }
                    //过滤发起人
                    if (null != activityId) {
                        preDoneMemberIds.add(currentAffair.getMemberId());
                    }
                    hasLoadPreDealAffair = true;
                }
                int affairCountOneNode = workitems.size();
                List<CtpAffair> affairs = new ArrayList<CtpAffair>(workitems.size());
                Set<Long> currentNodesInfoSet = new HashSet<Long>();

                for (WorkItem workitem : workitems) {
                    Long memberId = Long.parseLong(workitem.getPerformer());
                    // 知会// 节点不算待办
                    if (!"zhihui".equals(context.getPolicyId()) && !"inform".equals(context.getPolicyId())) {
                        if (currentNodesInfoSet.size() < 3) {//公文当前待办人只显示3个
                            currentNodesInfoSet.add(memberId);
                        }
                    }
                    //组装待办Affair
                    CtpAffair affair = new CtpAffair();
                    affair.setIdIfNew();
                    affair.setApp(ApplicationCategoryEnum.edoc.key());
                    affair.setSubApp(summary.getGovdocType());
                    affair.setState(affairData.getState());
                    affair.setSubState((operationType == WITHDRAW || operationType == SPECIAL_BACK_RERUN) ? SubStateEnum.col_pending_Back.key() : SubStateEnum.col_pending_unRead.key());
                    affair.setObjectId(affairData.getModuleId());
                    affair.setSubObjectId(Long.valueOf(workitem.getId()));
                    affair.setProcessId(context.getProcessId());
                    affair.setCaseId(context.getCaseId());
                    affair.setMemberId(memberId);
                    affair.setMatchDepartmentId(workitem.getMatchDepartmentId());
                    affair.setMatchAccountId(workitem.getMatchOrgAccountId());
                    affair.setMatchPostId(workitem.getMatchPostId());
                    affair.setMatchRoleId(workitem.getMatchRoleId());
                    String bodyType = affairData.getBodyType();
                    if (StringUtils.isBlank(bodyType)) {
                        String contentType = affairData.getContentType();
                        if (StringUtils.isNotBlank(contentType)) {
                            bodyType = String.valueOf(GovdocUtil.getContentType(contentType));
                        }
                    }
                    affair.setBodyType(bodyType);
                    affair.setSenderId(affairData.getSender());
                    affair.setSubject(Strings.isBlank(summary.getDynamicSubject()) ? summary.getSubject() : summary.getDynamicSubject());
                    affair.setOrgAccountId(summary.getOrgAccountId());
                    affair.setTrack(0);
                    Long currentUserId = Strings.isEmpty(firstContext.getCurrentUserId()) ? AppContext.currentUserId() : Long.parseLong(firstContext.getCurrentUserId());
                    affair.setPreApprover(currentAffair != null ? currentAffair.getMemberId() : currentUserId);
                    affair.setDeadlineDate(deadline);
                    affair.setDealTermType(dealTermType);
                    affair.setDealTermUserid(dealTermUserId);
                    affair.setRemindDate(remindTime);
                    affair.setReceiveTime(now);
                    affair.setCreateDate(affairData.getCreateDate() == null ? now : affairData.getCreateDate());
                    affair.setExpectedProcessTime(nodeDeaLineRunTime);
                    affair.setTempleteId(affairData.getTemplateId());
                    affair.setAutoRun(summary.getAutoRun());
                    if (summary.getImportantLevel() != null) {
                        affair.setImportantLevel(summary.getImportantLevel());
                    } else {
                        affair.setImportantLevel(formApi4Cap3.getImportantLevel(summary.getFormAppid(), summary.getFormRecordid()));
                    }
                    affair.setResentTime(affairData.getResentTime());
                    affair.setForwardMember(affairData.getForwardMember());
                    //设置待办事项节点权限
                    String _policyId = context.getPolicyId();
                    if (_policyId != null) {
                        _policyId = _policyId.replaceAll(new String(new char[]{(char) 160}), " ");
                    }
                    affair.setNodePolicy(_policyId);
                    if ("inform".equals(affair.getNodePolicy())) {
                        affair.setNodePolicy("zhihui");
                    }
                    //当前节点权限为空或为协同，手工修改为公文默认的发起节点权限
                    if (Strings.isBlank(affair.getNodePolicy())
                            || affair.getNodePolicy().equals(ApplicationCategoryEnum.collaboration.name())) {
                        if (summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_fawen.getKey()
                                || summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
                            affair.setNodePolicy("niwen");
                        } else {
                            affair.setNodePolicy("dengji");
                        }
                    }
                    // 设置加签、知会、会签的人员id
                    affair.setFromId(WFComponentUtil.isNotBlank(context.getAddedFromId()) ? Long.valueOf(context.getAddedFromId()) : null);
                    // 设置回退人的id
                    if (operationType == WITHDRAW || operationType == SpecialBackReRun) {
                        affair.setBackFromId(AppContext.getCurrentUser().getId());
                    }
                    // 设置多视图
//					affair.setMultiViewStr(context.getOperationm());
                    affair.setActivityId(Long.parseLong(workitem.getActivityId()));
                    if (WFComponentUtil.isNotBlank(context.getFormApp())) {
                        affair.setFormAppId(Long.valueOf(context.getFormApp()));
                    } else {
                        affair.setFormAppId(summary.getFormAppid());
                    }
//					if (ColUtil.isNotBlank(context.getForm())) {
//						affair.setFormId(Long.valueOf(context.getForm()));
//					} else {
//						affair.setFormId(summary.getFormId());
//					}
                    affair.setFormRecordid(summary.getFormRecordid());
//					if (ColUtil.isNotBlank(context.getOperationName())) {
//						affair.setFormOperationId(Long.valueOf(context.getOperationName()));
//					}
                    affair.setFormRelativeQueryIds(context.getQueryIds());
                    affair.setFormRelativeStaticIds(context.getStatisticsIds());
                    // 三个Boolean类型初始值，解决PostgreSQL插入记录异常问题
                    affair.setFinish(false);
                    //客开 项目名称： [修改功能：修复超期] 作者：fzc 修改日期：2018-5-23 start
                    if (null != affair.getExpectedProcessTime() && new Date().after(affair.getExpectedProcessTime())) {
                        affair.setCoverTime(true);
                    } else {
                        affair.setCoverTime(false);
                    }
                    //客开 项目名称： [修改功能：修复超期] 作者：fzc 修改日期：2018-5-23 end
                    affair.setDueRemind(false);
                    //设置待办事项附件等
                    AffairUtil.setHasAttachments(affair, affairData.getIsHasAttachment() == null ? false : affairData.getIsHasAttachment());
                    //AffairUtil.setFormReadonly(affair, "1".equals(context.getfR()));
                    AffairUtil.setFormReadonly(affair, false);//不受fr字段控制，默认编辑，受表单权限控制。

                    //设置加签、知会、会签的人员id
                    affair.setFromId(WFComponentUtil.isNotBlank(context.getAddedFromId()) ? Long.valueOf(context.getAddedFromId()) : null);
                    affair.setFromType(WFComponentUtil.isNotBlank(context.getAddedFromType()) ? Integer.valueOf(context.getAddedFromType()) : null);
                    if (context.getBusinessData().containsKey("isXuBan")) {
                        affair.setFromType(IS_XU_BAN);
                    }
                    Long proxyMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.getKey(), memberId, summary.getTempleteId(), affair.getSenderId());
                    affair.setProxyMemberId(proxyMemberId);

                    // 设置待办事项扩展字段
                    Map<String, Object> extParam = GovdocAffairHelper.createExtParam(summary);
                    extParam.put(AffairExtPropEnums.edoc_lastOperateState.name(), "");
                    // 设置流程期限
                    if (affairData.getProcessDeadlineDatetime() != null) {
                        extParam.put(AffairExtPropEnums.processPeriod.name(), affairData.getProcessDeadlineDatetime());
                    }
                    AffairUtil.setExtProperty(affair, extParam);

                    /**************************** 设置待办事项流程是否自动跳过 start  *******************************/
                    boolean isListPighole = String.valueOf(SubStateEnum.col_done_pighone.getKey()).equals(context.getBusinessData(AFFAIR_SUB_STATE));
                    boolean isListDelete = String.valueOf(SubStateEnum.col_done_delete.getKey()).equals(context.getBusinessData(AFFAIR_SUB_STATE));

                    // 设置事项为待办和协同待办未读
                    //1.MemberId
                    //2.加签
                    //3.回退
                    //4.是否要标志，WCW。
                    boolean isCompetition = PROCESS_MODE_COMPETITION.equals(context.getProcessMode()) && affairCountOneNode > 1;
                    boolean isSelectPeople = context.isSelectPeople();
                    boolean isSystemAdd = context.isSystemAdd();
                    boolean isNeedAutoSkip = !(isListPighole || isListDelete);
                    ;
                    boolean isCanAutoDeal = false;
                    boolean isBacked = affair.getBackFromId() != null;
                    boolean isAddNode = affair.getFromId() != null;
                    boolean isModifyWorkflowModel = context.isModifyWorkflowModel();
                    boolean isOrderExecuteAdd = String.valueOf(ChangeType.OrderExecuteAdd.getKey()).equals(context.getAddedFromType());
                    boolean isSubProcessSkipFSender = context.isSubProcessSkipFSender();
                    if (context.isSystemAdd()) {//子流程第一个节点是否能合并处理
                        if (context.isSubProcessSkipFSender()) {
                            isCanAutoDeal = true;
                        }
                    }
                    //定义一个代理id，代表此memberId的协同代理id
                    Long agentId = null;
                    boolean isAgentSkip = false;
                    if (canMerge) {// 如果有合并处理，根据下一个执行人获取其对该协同的代理人id
                        agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.getKey(), memberId, summary.getTempleteId());
                    }

                    //与发起人相同时合并处理
                    if (!isCanAutoDeal && canStartMerge) {
                        Long senderId = affairData.getSender();
                        if ((senderId.equals(memberId) || senderId.equals(agentId))) {
                            isAgentSkip = currentMemberId.equals(agentId);
                            isCanAutoDeal = true;
                            mergeDealType = BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue();
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    //上一节点处理过合并处理
                    if (!isCanAutoDeal && canPreDealMerge && !preDoneMemberIds.isEmpty()) {
                        isCanAutoDeal = preDoneMemberIds.contains(memberId);
                        if (!isCanAutoDeal && agentId != null) {//如果不能自动跳过，判断是否有代理
                            // 如果有代理，判断代理是否在前面处理人中
                            isCanAutoDeal = preDoneMemberIds.contains(agentId);
                            if (isCanAutoDeal) {// 如果因为前面有代理处理过而合并，设置标记为true
                                isAgentSkip = true;
                            }
                        }
                        if (isCanAutoDeal) {
                            mergeDealType = BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue();
                        }
                        sb.append("___________________preDoneMemberIds______contains:" + isCanAutoDeal);
                    }
                    //判定当前处理人员是否与在前面节点是否能查询到(前面已经处理过时合并处理)
                    if (!isCanAutoDeal && canAnyDealMerge && !colDoneMemberIds.isEmpty()) {
                        isCanAutoDeal = colDoneMemberIds.contains(memberId);
                        if (!isCanAutoDeal && agentId != null) {// 如果不能自动跳过，判断是否有代理
                            // 如果有代理，判断代理是否在前面处理人中
                            isCanAutoDeal = colDoneMemberIds.contains(agentId);
                            if (isCanAutoDeal) {// 如果因为前面有代理处理过而合并，设置标记为true
                                isAgentSkip = true;
                            }
                        }
                        if (isCanAutoDeal) {
                            mergeDealType = BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue();
                        }
                        sb.append("___________________colDoneMemberIds______contains:" + isCanAutoDeal);
                    }

                    if (!isCanAutoDeal && canMerge) {
                        sb.append("isCanAutoDeal：").append(isCanAutoDeal).append("\r\n");
                        sb.append("affairId:").append(affair.getId()).append("\r\n");
                        sb.append("PARAMETER:").append("\r\n");
                        sb.append("isAddNode:").append(isAddNode).append("\r\n");
                        sb.append("isSelectPeople:").append(isSelectPeople).append("\r\n");
                        sb.append("isSystemAdd:").append(isSystemAdd).append("\r\n");
                        sb.append("isBacked:").append(isBacked).append("\r\n");
                        sb.append("isCompetition:").append(isCompetition).append("\r\n");
                        sb.append("!isNeedAutoSkip:").append(!isNeedAutoSkip).append("\r\n");
                        sb.append("processAnyDealMerge:").append(processCanAnyDealMerge).append("\r\n");
                        sb.append("processPreDealMerge:").append(processCanPreDealMerge).append("\r\n");
                        sb.append("processMergeStart:").append(processCanStartMerge).append("\r\n");
                        sb.append("nodeAnyDealMerge:").append(canAnyDealMerge).append("\r\n");
                        sb.append("nodePreDealMerge:").append(canPreDealMerge).append("\r\n");
                        sb.append("nodeMergeStart:").append(canStartMerge).append("\r\n");
                        sb.append("isModifyWorkflowModel:").append(isModifyWorkflowModel).append("\r\n");
                        sb.append("isOrderExecuteAdd:").append(isOrderExecuteAdd).append("\r\n");
                        LOGGER.info(sb.toString());
                    }

                    if (canMerge || isCanAutoDeal) {
                        if (isCompetition) {
                            cantAutoSkipReson.put(affair.getId(), "isCompetition");
                        } else if (isSelectPeople) {
                            cantAutoSkipReson.put(affair.getId(), "isSelectPeople");
                        } else if (isSystemAdd && !isSubProcessSkipFSender) {
                            cantAutoSkipReson.put(affair.getId(), "isSystemAdd");
                        } else if (isBacked) {
                            cantAutoSkipReson.put(affair.getId(), "isBacked");
                        } else if (isAddNode) {
                            cantAutoSkipReson.put(affair.getId(), "isAddNode");
                        } else if (!isNeedAutoSkip) {
                            cantAutoSkipReson.put(affair.getId(), "isNeedAutoSkip");
                        } else if (isModifyWorkflowModel) {
                            cantAutoSkipReson.put(affair.getId(), "isModifyWorkflowModel");
                        } else if (isOrderExecuteAdd) {
                            cantAutoSkipReson.put(affair.getId(), "isOrderExecuteAdd");
                        }

                        if (isCanAutoDeal) {
                            // 创建jsonObject存放affairId 与skipAgentId的对应关系
                            isRepeatAutoSkipAffairIds.add(affair.getId());
                            Map<String, String> skipMap = new HashMap<String, String>();
                            skipMap.put("affairId", affair.getId().toString());
                            if (isAgentSkip) {
                                // 因代理处理而进行自动跳过
                                skipMap.put("skipAgentId", agentId.toString());
                            } else {// 非代理跳过，存入0
                                skipMap.put("skipAgentId", "0");
                            }
                            skipMap.put("mergeDealType", mergeDealType);
                            Long currentCommentId = 0l;
                            Object commentObj = contextList.get(0).getBusinessData(CURRENT_OPERATE_COMMENT_ID);
                            // 与前面节点重复处理的时候在定时任务中取意见，不在这个地方取意见。
                            if (commentObj != null && BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue().equals(mergeDealType)) {
                                currentCommentId = (Long) commentObj;
                            }
                            skipMap.put("_commentId", String.valueOf(currentCommentId));
                            // 将对应关系存入jsonArray
                            autoSkipArray.add(skipMap);
                        }
                    }
                    //回退操作不进行自动跳过操作
                    if (operationType == WITHDRAW || operationType == SpecialBackReRun) {
                        isCanAutoDeal = false;
                    }
                    affair.setState(isCanAutoDeal ? StateEnum.col_pending_repeat_auto_deal.getKey() : affairData.getState());

                    // 协同的ID


                    String dr = context.getDR();
                    affair.setRelationDataId(Strings.isBlank(dr) ? null : Long.valueOf(dr));


                    affair.setUpdateDate(now);
                    Long activetyId = Long.parseLong(workitem.getActivityId());

                    affair.setSummaryState(summary.getState());
                    affair.setProcessDeadlineTime(summary.getDeadlineDatetime());
                    affairs.add(affair);

                    if (isSendMessage && !isCanAutoDeal) {
                        getReceiver(isSendMessage, affair, affair.getApp(), receivers, receivers1);
                    }


                }//for-end workitemList

                if (affairData.getAffairList() != null) {
                    affairData.getAffairList().addAll(affairs);
                } else {
                    affairData.setAffairList(affairs);
                }

                DateSharedWithWorkflowEngineThreadLocal.setWorkflowAssignedAllAffairs(affairData.getAffairList());
                // 提前提醒，超期提醒
                CtpAffair affair = affairs.get(0);
                Date advanceRemindTime = null;
                if (remindTime != null && !Long.valueOf(-1).equals(remindTime) && affair.getExpectedProcessTime() != null) {
                    advanceRemindTime = workTimeManager.getRemindDate(nodeDeaLineRunTime, remindTime);
                }
                WFComponentUtil.affairExcuteRemind4Node(affair, affairData.getSummaryAccountId(), nodeDeaLineRunTime, advanceRemindTime);

            }//for-end contextList

            //保存公文下节点的Affair数据及发送消息
            saveListMap(affairData, now, summary.getCoverTime(), receivers, receivers1);

            // 工作流修改流程增加的节点，需要单独更新
            if (summaryObj == null) {
                GovdocHelper.updateCurrentNodesInfo(summary, false);
                govdocSummaryManager.updateEdocSummary(summary);
            }
            // 创建重复处理定时任务
            Long currentCommentId = 0l;
            Object commentObj = firstContext.getBusinessData(CURRENT_OPERATE_COMMENT_ID);
            if (commentObj != null && BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue().equals(mergeDealType)) {
                currentCommentId = (Long) commentObj;
            }
            if (Strings.isNotEmpty(isRepeatAutoSkipAffairIds)) {
                String policyName = "";
                if (currentAffair != null) {
                    policyName = collaborationApi.getPolicyByAffair(currentAffair).getName();
                }
//				DateSharedWithWorkflowEngineThreadLocal.addRepeatAffairs("_affairIds", Strings.join(isRepeatAutoSkipAffairIds, ","));
//				if(summary.getCanMergeDeal() != true){
//					DateSharedWithWorkflowEngineThreadLocal.addRepeatAffairs("_commentId", String.valueOf(currentCommentId));
//				}
//				DateSharedWithWorkflowEngineThreadLocal.addRepeatAffairs("_policyName", policyName);
//				DateSharedWithWorkflowEngineThreadLocal.addRepeatAffairs("count", "0");
//				DateSharedWithWorkflowEngineThreadLocal.addRepeatAffairs("_isAffairAutotSkip", "1");
//				//将jsonArray转换为字符串传递
//				String skipJsonString = JSONUtil.toJSONString(autoSkipArray);
//				DateSharedWithWorkflowEngineThreadLocal.addRepeatAffairs("skipJsonString", skipJsonString);
//				DateSharedWithWorkflowEngineThreadLocal.addRepeatAffairs("_cantSkipResonMap",JSONUtil.toJSONString(cantAutoSkipReson));
                String skipJsonString = JSONUtil.toJSONString(autoSkipArray);

                repeatMap.put("_policyName", policyName);
                repeatMap.put("count", "0");
                repeatMap.put("_isAffairAutotSkip", "1");
                repeatMap.put("skipJsonString", skipJsonString);//将jsonArray转换为字符串传递
                repeatMap.put("_cantSkipResonMap", JSONUtil.toJSONString(cantAutoSkipReson));

                global.put(WF_APP_GLOBAL_REPEAT_AFFMAP, repeatMap);
            }
            //zhou:[医科大学代办消息推送]添加公文流程自定义事件 start
            GovdocOperationEvent operationEvent = new GovdocOperationEvent(this);
            operationEvent.setCurrentAffair(ykdAffair);
            operationEvent.setAffairs(affairData.getAffairList());
            operationEvent.setType("assigned");//分配
            EventDispatcher.fireEvent(operationEvent);
            //zhou:[医科大学代办消息推送]添加公文流程自定义事件 end

        } catch (Exception e) {
            LOGGER.error(BPMException.EXCEPTION_CODE_DATA_FORMAT_ERROR, e);
        }
//		try{
//			if(hasMutiPlugin&&OnlineChecker.isOnline())
//				govdocOCIPExchangeManager.onWorkflowAssigned(affairData, summary);
//		}catch(Exception e){
//			LOGGER.error("多G6出现异常", e);
//		}
        //发送自动跳过的事件
        GovdocEventDispatcher.fireAutoSkipEvent(this, (Map<String, String>) global.get(WF_APP_GLOBAL_REPEAT_AFFMAP));
        return true;
    }

    /**
     * 节点处理回调
     */
    @Override
    public boolean onWorkitemFinished(EventDataContext context) {
        try {
            CtpAffair affair = (CtpAffair) context.getBusinessData(CTPAFFAIR_CONSTANT);
            if (affair == null) {
                affair = affairManager.getAffairBySubObjectId(context.getWorkItem().getId());
            }
            if (affair == null) {
                return false;
            }
            DBAgent.commit();//由于session问题 ，这里先提交一下，场景分办的时候
            boolean isSepicalBackedSubmit = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());

            User user = AppContext.getCurrentUser();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            logInfo4WorkItemFinished(affair, user, now);

            affair.setCompleteTime(now);
            affair.setUpdateDate(now);
            affair.setState(StateEnum.col_done.key());
            affair.setSubState(SubStateEnum.col_normal.key());
            // 判断代理人
            if (!affair.getMemberId().equals(AppContext.getCurrentUser().getId())) {
                affair.setTransactorId(AppContext.getCurrentUser().getId());
            }
            // 设置运行时长，超时时长等
            EdocSummary summary = null;
            if (null != context.getAppObject()) {
                summary = (EdocSummary) context.getAppObject();
            } else {
                summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
            }
            //防护processid为空，重新设置一下
            affair.setProcessId(summary.getProcessId());
            //老公文
            if (summary.getGovdocType() == null || summary.getGovdocType().intValue() == 0) {
                return EdocWorkFlowOldHelper.onWorkitemFinished(context);
            }
            setTime2Affair(affair, summary);
            affairManager.updateAffair(affair);

            Long summaryId = affair.getObjectId();
            //如果是节点跳过
            Object handleType = context.getBusinessData().get("handleType");
            if (ColHandleType.skipNode == handleType) {
                return true;
            }
            int operationType = context.getBusinessData().get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) == null ? -1 : (Integer) context.getBusinessData().get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);

            if (operationType == 8) {
                return true;
            } else {
                if (!isSepicalBackedSubmit) {// 指定回退提交在外层发消息
                    Comment c = (Comment) context.getBusinessData().get("comment");
                    if (null != c) {
                        govdocMessageManager.sendFinishMessage(c, affair, summaryId);
                    }
                }
            }

        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
//		try{
//			if(hasMutiPlugin&&OnlineChecker.isOnline())
//				govdocOCIPExchangeManager.onWorkitemFinished(context);
//		}catch(Exception e){
//			LOGGER.error("多G6出现异常", e);
//		}
        return true;
    }

    /**
     * 节点取消回调
     * 1回退至被回退或中间节点取消
     * 2
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean onWorkitemCanceled(EventDataContext context) {
        try {
            Map<String, Object> businessData = context.getBusinessData();
            int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE) == null ? AUTODELETE : (Integer) businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);
            WorkItem workitem = context.getWorkItem();
            if (workitem == null) {
                DateSharedWithWorkflowEngineThreadLocal.setOperationType(AUTODELETE);
                operationType = AUTODELETE;
            } else {
                if ((operationType == COMMONDISPOSAL || operationType == ZCDB) && !PROCESS_MODE_COMPETITION.equals(context.getProcessMode())) {
                    DateSharedWithWorkflowEngineThreadLocal.setOperationType(AUTODELETE);
                }
            }
            String processMode = context.getProcessMode();
            boolean isCompetition = (PROCESS_MODE_COMPETITION.equals(processMode) || Strings.isNotBlank(context.getGoNextMsg()));
            if (operationType == AUTOSKIP && isCompetition) {
                operationType = COMMONDISPOSAL;
            }
            if (Strings.isNotBlank(context.getGoNextMsg())) {
                operationType = COMMONDISPOSAL;
            }

            if (PROCESS_MODE_SINGLE.equals(processMode) && operationType == COMMONDISPOSAL) {
                operationType = AUTODELETE;
            }
            CtpAffair affair = eventData2ExistingAffair(context);
            if (affair == null)
                return false;

            if (GovdocHelper.isOldEdoc(affair.getSubApp())) {
                GovdocApplicationHandler handler = (GovdocApplicationHandler) AppContext.getBean("govdocApplicationHandler");
                try {
                    GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler(GovdocWorkflowTypeEnum.oldedoc.name());
                    return listener.onWorkitemCanceled(context);
                } catch (Exception e) {
                    LOGGER.error("老公文onProcessCanceled出错", e);
                    return false;
                }
            }

            List<Long> cancelAffairIds = new ArrayList<Long>();
            long workItemId = workitem != null ? workitem.getId() : null;
            Timestamp now = new Timestamp(System.currentTimeMillis());
            /****************** 正常处理、暂存待办、竞争执行 ********************/
            if ((operationType == COMMONDISPOSAL || operationType == ZCDB || operationType == SPECIAL_BACK_SUBMITTO)
                    && isCompetition) {
                List<CtpAffair> affairs = new ArrayList<CtpAffair>();
                List<CtpAffair> sendAffairs = affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
                if (!sendAffairs.isEmpty())
                    affairs.addAll(sendAffairs);

                if (!affairs.isEmpty()) {
                    StringBuilder hql = new StringBuilder();
                    hql.append("UPDATE "
                            + CtpAffair.class.getName()
                            + " SET state=:state,subState=:subState,updateDate=:updateDate "
                            + " WHERE app=:app AND objectId=:objectId AND activityId=:activityId "
                            + " AND subObjectId<>:subObjectId "
                            + " AND state <> :notState ");

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("state", StateEnum.col_competeOver.key());
                    params.put("subState", SubStateEnum.col_normal.key());
                    params.put("updateDate", now);
                    params.put("app", affair.getApp());
                    params.put("objectId", affair.getObjectId());
                    params.put("subObjectId", workItemId);
                    params.put("activityId", affair.getActivityId());
                    params.put("notState", StateEnum.col_done.key());

                    DBAgent.bulkUpdate(hql.toString(), params);
                    // 给在竞争执行中被取消的affair发送消息提醒

                    for (Iterator<CtpAffair> it = affairs.iterator(); it.hasNext(); ) {
                        CtpAffair a = it.next();
                        if (Integer.valueOf(StateEnum.col_done.key()).equals(a.getState())) {
                            it.remove();
                        }
                    }

                    GovdocMessageHelper.competitionCancel(affairManager, orgManager, userMessageManager, workitem, affairs, affair);
                }
                // 竞争执行，有人暂存待办，那么当前待办人就只有 暂存待办这个人了
                /** 与产品确认: 和协同同步.屏蔽这个功能**/
                /**if (operationType == ZCDB) {
                 StringBuilder hql = new StringBuilder();
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
                 }**/
            }
            /****************** 退回或是取回 ********************/
            if (operationType == TAKE_BACK || operationType == WITHDRAW || operationType == SPECIAL_BACK_RERUN) {
                int state = operationType == TAKE_BACK ? StateEnum.col_takeBack.key() : StateEnum.col_stepBack.key();
                List<WorkItem> workItems = context.getWorkitemLists();
                // 更新affair状态
                //AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
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
                Object currentNodeId = context.getBusinessData().get(CURRENT_OPERATE_AFFAIR_ID);

                int maxCommitNumber = 300;
                int length = workItems.size();
                int i = 0;

                List<Long> traceList = new ArrayList<Long>();
                List<CtpAffair> dynamicOldDataAffairList = new ArrayList<CtpAffair>();
                List<WorkflowTracePO> dynamicNewDataWorkflowTraceList = new ArrayList<WorkflowTracePO>();
                Map<Long, Long> canceledMemberIdToAffairId = new HashMap<Long, Long>();
                Map<Long, Long[]> canceledMemberIdToAarray = new HashMap<Long, Long[]>();

                for (WorkItem item : workItems) {
                    workItemList.add(item.getId());
                    if (m.keySet().contains(item.getId())) {
                        CtpAffair af = m.get(item.getId());
                        cancelAffairIds.add(af.getId());
                        cancelAffairs.add(af);
                        Long[] arr = new Long[2];
                        arr[0] = af.getId();
                        arr[1] = Long.valueOf(af.getState());

                        boolean isDoneNode = Integer.valueOf(StateEnum.col_done.getKey()).equals(af.getState());
                        boolean isZCDBNode = Integer.valueOf(StateEnum.col_pending.key()).equals(af.getState())
                                && Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(af.getSubState());
                        boolean isBackNode = af.getId().equals((Long) currentNodeId);
                        boolean isBackedNode = af.getActivityId() == null ? true : normalStepBackTargetNodes.contains(String.valueOf(af.getActivityId()));

                        if (operationType == WITHDRAW || operationType == SPECIAL_BACK_RERUN) {
                            //traceDao.deleteDynamicOldDataByAffair(af);
                            dynamicOldDataAffairList.add(af);
                        }

                        if ((operationType == WITHDRAW || operationType == SPECIAL_BACK_RERUN)
                                && "1".equals(context.getBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_TRACK_FLOW))
                                && !isBackedNode
                                && (isDoneNode || isBackNode || isZCDBNode)) {

                            String isCircleBack = (String) context.getBusinessData(CURRENT_OPERATE_CIRCLE_BACK);
                            WorkflowTraceEnums.workflowTrackType trackType = WorkflowTraceEnums.workflowTrackType.step_back_normal;
                            if (operationType == SPECIAL_BACK_RERUN) {//指定回退，将流程追溯类型修改为指定回退
                                trackType = WorkflowTraceEnums.workflowTrackType.special_step_back_normal;
                                if ("1".equals(isCircleBack)) {
                                    trackType = WorkflowTraceEnums.workflowTrackType.circle_step_back_normal;
                                }
                            }
                            WorkflowTracePO workflowTracePO = govdocTraceWorkflowManager.createWorkflowTracePO(af, af.getSenderId(), AppContext.getCurrentUser().getId(), trackType);
                            dynamicNewDataWorkflowTraceList.add(workflowTracePO);
                            //govdocTraceWorkflowManager.createStepBackTrackData(af, af.getSenderId(), AppContext.getCurrentUser().getId(), WorkflowTraceEnums.workflowTrackType.step_back_normal);

                            traceList.add(af.getId());
                        }
                        canceledMemberIdToAffairId.put(af.getMemberId(), af.getId());
                        canceledMemberIdToAarray.put(af.getMemberId(), arr);
                        // 加入threadlocal，发消息时使用
                        DateSharedWithWorkflowEngineThreadLocal.addToAllStepBackAffectAffairMap(af.getMemberId(), af.getId());
                    }

                    i++;
                    Map<String, Object> nameParameters = new HashMap<String, Object>();
                    nameParameters.put("updateDate", new Date());
                    nameParameters.put("state", state);
                    nameParameters.put("subState", SubStateEnum.col_normal.key());
                    nameParameters.put("backFromId", AppContext.getCurrentUser().getId());
                    if (i % maxCommitNumber == 0 || i == length) {
                        Object[][] wheres = new Object[][]{{"objectId", affair.getObjectId()}, {"subObjectId", workItemList}};
                        affairManager.update(nameParameters, wheres);
                        workItemList = new ArrayList<Long>();
                    }
                }
                context.setBusinessData(CANCELED_MIDTOAID_MAP, canceledMemberIdToAffairId);
                context.setBusinessData(CANCELED_MIDTOARRAY_MAP, canceledMemberIdToAarray);

                if (Strings.isNotEmpty(dynamicOldDataAffairList)) {
                    govdocTraceWorkflowManager.deleteBatch(dynamicOldDataAffairList);
                }

                if (Strings.isNotEmpty(dynamicNewDataWorkflowTraceList)) {
                    govdocTraceWorkflowManager.saveBatch(dynamicNewDataWorkflowTraceList);
                }


                // 更新当前处理人信息
                EdocSummary summary = (EdocSummary) context.getAppObject();
                if (summary == null) {
                    summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
                }
                GovdocHelper.deleteAffairsNodeInfo(summary, cancelAffairs);
            }
            /****************** 默认删除操作: 督办替换节点，自动流程复合节点的单人执 ********************/
            else if (operationType == AUTODELETE) {
                // 删除被替换的所有affair事项
                List<CtpAffair> affairs = new ArrayList<CtpAffair>();
                if (context.getWorkitemLists() != null) {
                    List<WorkItem> workitems = context.getWorkitemLists();
                    affairs = this.superviseCancel(workitems, now);
                }
                if (affairs.isEmpty()) {
                    affairs.add(affair);
                }
                // 2013-1-8给在督办中被删除的affair发送消息提醒
                // OA-50240 流程已经变更，原流程节点还能从消息进入处理页面，还能进行终止等操作
                govdocMessageManager.sendSuperviseDelete(workitem, affairs);
                // 替换节点后更新当前待办人信息
                EdocSummary summary = (EdocSummary) context.getAppObject();
                if (summary == null) {
                    summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
                }
                GovdocHelper.updateCurrentNodesInfo(summary, true);
            }
            /****************** 指定回退普通节点流程重走 ********************/
            else if (operationType == SPECIAL_BACK_RERUN) {
                EdocSummary summary = (EdocSummary) context.getAppObject();
                List<WorkItem> workItems = context.getWorkitemLists();
                Map<Long, Long> canceledMemberIdToAffairId = new HashMap<Long, Long>();
                Map<Long, Long[]> canceledMemberIdToAarray = new HashMap<Long, Long[]>();
                if (Strings.isNotEmpty(workItems)) {
                    CtpAffair firstAffair = affairManager.getAffairBySubObjectId(workItems.get(0).getId());
                    if (firstAffair == null) {
                        LOGGER.info("====firstAffair is null========workItems.get(0).getId():" + workItems.get(0).getId());
                        return false;
                    }

                    List<CtpAffair> cancelAffairs = new ArrayList<CtpAffair>();
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

                            canceledMemberIdToAffairId.put(af.getMemberId(), af.getId());
                            canceledMemberIdToAarray.put(af.getMemberId(), arr);
                        }

                        if (af != null) {
                            workitemIds1.add((long) workItem.getId());
                        }

                    }

                    context.setBusinessData(CANCELED_MIDTOAID_MAP, canceledMemberIdToAffairId);
                    context.setBusinessData(CANCELED_MIDTOARRAY_MAP, canceledMemberIdToAarray);

                    if (Strings.isNotEmpty(workitemIds1)) {
                        List<Long>[] subList = Strings.splitList(workitemIds1, 1000);
                        for (List<Long> list : subList) {

                            StringBuilder hql = new StringBuilder();
                            hql.append("update CtpAffair as affair set state=:state,subState=:subState,updateDate=:updateDate "
                                    + " where objectId=:objectId and subObjectId in (:subObjectIds) ");

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
                    GovdocHelper.deleteAffairsNodeInfo(summary, cancelAffairs);
                }
            }

//			OA-175115A人员将待办公文收藏后提交，B节点人员执行回退操作，回退到A的待办，A的收藏就没了
//			if (!CollectionUtils.isEmpty(cancelAffairIds) && AppContext.hasPlugin("doc")) {
//				docApi.deleteDocResources(AppContext.getCurrentUser().getId(), cancelAffairIds);
//			}
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
//		try{
//			if(hasMutiPlugin&&OnlineChecker.isOnline())
//				govdocOCIPExchangeManager.onWorkitemCanceled(context);
//		}catch(Exception e){
//			LOGGER.error("多G6出现异常", e);
//		}
        return true;
    }

    /**
     * 节点取回回调
     */
    @Override
    public boolean onWorkitemTakeBack(EventDataContext context) {
//		try{
//			if(hasMutiPlugin&&OnlineChecker.isOnline())
//				govdocOCIPExchangeManager.onWorkitemTakeBack(context);
//		}catch(Exception e){
//			LOGGER.error("多G6出现异常", e);
//		}
        return true;// 在controller中更新当前affair为待办状态，这里不需要做其他操作。
    }

    /**
     * 节点终止回调
     */
    @Override
    public boolean onWorkitemStoped(EventDataContext context) {
        WorkItem workitem = (WorkItem) context.getWorkItem();
        try {
            EdocSummary edocSummary = govdocSummaryManager.getSummaryByProcessId(context.getProcessId());
            if (edocSummary == null) {
                LOGGER.error("onWorkitemStoped中获取公文主表为空  processId =" + context.getProcessId());
                return false;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            edocSummary.setState(EdocConstant.flowState.terminate.ordinal());
            edocSummary.setCompleteTime(now);
            govdocSummaryManager.updateEdocSummary(edocSummary);

            CtpAffair affair = (CtpAffair) context.getBusinessData(CTPAFFAIR_CONSTANT);
            if (affair == null) {
                affair = affairManager.getAffairBySubObjectId(Long.valueOf(workitem.getId()));
            }
            //终止时不给待发送事项发消息
            List<CtpAffair> trackingAndPendingAffairs = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc, affair.getObjectId());
            // 指定回退
            List<CtpAffair> pendingAffairs = affairManager.getAffairs(edocSummary.getId(), StateEnum.col_pending);
            if (!CollectionUtils.isEmpty(pendingAffairs)) {
                for (CtpAffair ctpAffair : pendingAffairs) {
                    if (ctpAffair.getSubState() == SubStateEnum.col_pending_specialBack.getKey()
                            || ctpAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
                            || ctpAffair.getSubState() == SubStateEnum.col_pending_specialBackCenter.getKey()) {
                        trackingAndPendingAffairs.add(ctpAffair);
                    }
                }
            }
            //流程终止时发送消息
            Object businessData = context.getBusinessData("isAutoStop");
            //设置了超期自动终止的在workflowProcess*ColHanlder里面发了 消息 这里就不需要在发送消息了
            if (!(null != businessData && "1".equals((String) businessData))) {
                this.govdocMessageManager.sendStepstopMsg(workitem, affair, trackingAndPendingAffairs);
            }

            setTime2Affair(affair, edocSummary);
            Map<String, Object> columns = new HashMap<String, Object>();
            columns.put("state", StateEnum.col_done.key());
            columns.put("subState", SubStateEnum.col_done_stepStop.key());
            columns.put("completeTime", now);
            columns.put("updateDate", now);
            columns.put("finish", true);
            columns.put("summaryState", edocSummary.getState());
            columns.put("overWorktime", affair.getOverWorktime());
            columns.put("runWorktime", affair.getRunWorktime());
            columns.put("overTime", affair.getOverTime());
            columns.put("runTime", affair.getRunTime());
            //判断代理人
            if (!affair.getMemberId().equals(AppContext.getCurrentUser().getId())) {
                columns.put("transactorId", AppContext.getCurrentUser().getId());
            }

            List<CtpAffair> affairsByObjectIdAndState = affairManager.getAffairs(affair.getObjectId(), StateEnum.col_pending);
            // 根据app判断，避免终止时更新待发送事项的状态
            //affairManager.update(columns,new Object[][] {{"objectId", affair.getObjectId()}, {"state", StateEnum.col_pending.key()},{"app", affair.getApp()}});
            List<Long> allCtpAffairId = new ArrayList<Long>();
            for (int count = affairsByObjectIdAndState.size(), a = 0; a < count; a++) {
                CtpAffair ctpAffairM = affairsByObjectIdAndState.get(a);
                allCtpAffairId.add(ctpAffairM.getId());
                if (null == ctpAffairM.getDeadlineDate()) {
                    columns.put("overWorktime", 0L);
                    affairManager.update(columns, new Object[][]{{"id", ctpAffairM.getId()}, {"state", StateEnum.col_pending.key()}, {"app", ctpAffairM.getApp()}});
                }
                if (null != ctpAffairM.getDeadlineDate()) {
                    if (affair.getDeadlineDate().equals(ctpAffairM.getDeadlineDate())) {
                        columns.put("overWorktime", affair.getOverWorktime());
                    } else {
                        long ctpAffairMOverWorkTime = affair.getRunWorktime() - ctpAffairM.getDeadlineDate();
                        columns.put("overWorktime", ctpAffairMOverWorkTime > 0 ? ctpAffairMOverWorkTime : 0L);
                    }
                    affairManager.update(columns, new Object[][]{{"id", ctpAffairM.getId()}, {"state", StateEnum.col_pending.key()}, {"app", affair.getApp()}});
                }

            }
//			try {
//				if (AppContext.hasPlugin("ocip")) {
//					List<CtpAffair> list = new UniqueList<CtpAffair>();
//					Map<String, List<CtpAffair>> localAffair = new HashMap<String, List<CtpAffair>>();
//					List<String> objectList = new ArrayList<String>();
//					for (Long affairId : allCtpAffairId) {
//						CtpAffair affairData = affairManager.get(affairId);
//						if (affairData != null) {
//							V3xOrgMember dMember = orgManager.getMemberById(affairData.getMemberId());
//							if (OrgUtil.isPlatformEntity(dMember)) {
//								// 对于提醒发起人，标识是提醒发起人
//								affairData.setState(StateEnum.col_done.key());
//								affairData.setSubState(SubStateEnum.col_done_stepStop.key());
//								list.add(affairData);
//							} else {
//								objectList.add(dMember.getId().toString());
//								affairData.setState(StateEnum.col_done.key());
//								affairData.setSubState(SubStateEnum.col_done_stepStop.key());
//								if (localAffair.containsKey(dMember.getId().toString())) {
//									localAffair.get(dMember.getId().toString()).add(affairData);
//								} else {
//									List<CtpAffair> affList = new ArrayList<CtpAffair>();
//									affList.add(affairData);
//									localAffair.put(dMember.getId().toString(), affList);
//								}
//							}
//						}
//					}
//					if (!objectList.isEmpty()&&organizationManager!=null&&hasMutiPlugin&&OnlineChecker.isOnline()) {
//						String resource = Global.getConfig("sysCode");
//
//						Map<String, List<OrgMember>> mappingMember = organizationManager.getMemberByObjectIds(objectList, resource);
//
//						for (String objectId : objectList) {
//							List<OrgMember> members = mappingMember.get(objectId);
//							if (members != null && members.size() > 1) {
//								List<CtpAffair> affList = localAffair.get(objectId);
//								for (CtpAffair aff : affList) {
//									Map<String, Object> affairExtMap = AffairUtil.getExtProperty(aff);
//									affairExtMap.put("memberId", aff.getMemberId());
//									aff.setMemberId(Long.valueOf(members.get(0).getOrgPlatformUserId()));
//									AffairUtil.setExtProperty(aff, affairExtMap);
//									list.add(aff);
//								}
//							}
//						}
//					}
//					// 跨系统对超期提醒人员进行分发
//					if (list != null && list.size() > 0) {
//						AffairData affairData = new AffairData();
//						affairData.setAffairList(list);
//						affairData.setIsSendMessage(false);
//						affairData.setSubject(affair.getSubject());
//						affairData.setModuleId(affair.getObjectId());
//						EdocSummary doneSummary = govdocSummaryManager.getSummaryById(affair.getObjectId());
//						govdocOCIPExchangeManager.handCollaborationAffair(affairData, ColOperation.STOP, doneSummary);
//					}
//				}
//			} catch (Exception e) {
//				LOGGER.error("", e);
//			}

            //指定回退给发起人,在待发中
            Map<String, Object> columns2 = new HashMap<String, Object>();
            columns2.put("state", StateEnum.col_sent.key());
            columns.put("subState", SubStateEnum.col_normal.key());
            columns2.put("finish", true);
            columns2.put("updateDate", now);
            affairManager.update(columns2, new Object[][]{{"objectId", affair.getObjectId()}, {"state", StateEnum.col_waitSend.key()}, {"app", affair.getApp()}});
            if (edocSummary.getGovdocType() == ApplicationSubCategoryEnum.edoc_fawen.getKey()
                    || edocSummary.getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()
                    || edocSummary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()
                    || edocSummary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
                govdocSummaryManager.updateEdocSummaryState(edocSummary.getId(), edocSummary.getState());
            }

        } catch (BusinessException e) {
            LOGGER.error("更新affair事项出错  " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("更新edocSummary事项出错  " + e.getMessage(), e);
        }

        return true;
    }

    /**
     * 指定回退回调-将指定回退状态 17 改为 16(别人回退给我，这是我的状态)
     */
    @Override
    public boolean onWorkitemWaitToLastTimeStatus(EventDataContext context) {
        try {
            EdocSummary summary = (EdocSummary) context.getAppObject();
            if (summary == null) {
                return false;
            }

            //设置新的定时任务
            Long accountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary, templateManager);

            List<WorkItem> workItems = context.getWorkitemLists();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            //Set<MessageReceiver> targetReceivers = new HashSet<MessageReceiver>();
            for (WorkItem wi : workItems) {
                CtpAffair affair = affairManager.getAffairBySubObjectId(wi.getId());

                affair.setSubState(SubStateEnum.col_pending_specialBacked.key());
                affair.setState(StateEnum.col_pending.key());
                affair.setUpdateDate(now);
                //删除原来的定时任务（超期提醒、提前提醒）
                if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                }
                if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                }

                Long deadLine = affair.getDeadlineDate();
                Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                Date deadLineRunTime = null;
                try {
                    if (deadLine != null && deadLine != 0) {
                        deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, accountId);
                        affair.setExpectedProcessTime(deadLineRunTime);
                    }
                } catch (WorkTimeSetExecption e) {
                    LOGGER.error("", e);
                }
                affairManager.updateAffair(affair);
                WFComponentUtil.affairExcuteRemind(affair, accountId);

                String messageLink = "message.link.govdoc.pending";
                if (GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
                    messageLink = "message.link.edoc.pending";
                }
                /** 激活节点：消息接收人员  **/
                String senderName = orgManager.getMemberById(affair.getSenderId()).getName();
                MessageContent sendContent = new MessageContent("edoc.appointStepBack.send", senderName, affair.getSubject());
                sendContent.setImportantLevel(affair.getImportantLevel());
                ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), messageLink, affair.getId().toString());
                userMessageManager.sendSystemMessage(sendContent, app, affair.getSenderId(), receiver, GovdocMessageHelper.getSystemMessageFilterParam(affair).key);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return true;
    }

    /**
     * 指定回退回调-
     */
    @Override
    public boolean onWorkitemWaitToReady(EventDataContext context) {
        try {
            //提交时传递的summay
            EdocSummary summary = (EdocSummary) context.getAppObject();
            if (summary == null) {
                return false;
            }

            //发起指定回退人的nodeid
            //String activityId = context.getNodeId();
            //更新发起指定回退人的状态
            //List<CtpAffair> targetAffair = affairManager.getAffairsByActivityId(Long.parseLong(activityId));
            //Set<MessageReceiver> targetReceivers = new HashSet<MessageReceiver>();
            List<WorkItem> workitems = context.getWorkitemLists();
            for (WorkItem workItem : workitems) {
                /**OA-178171*/
                List<Long> workItemIds = new ArrayList<Long>();
                workItemIds.add(workItem.getId());
                List<CtpAffair> affairs = affairManager.getAffairBySubObjectIds(workItemIds);
                CtpAffair affair = null;
                for (CtpAffair affair1 : affairs) {
                    if (affair1.getSubState() != SubStateEnum.col_normal.getKey() && affair1.getSubState() != SubStateEnum.col_pending_specialBacked.getKey()) {
                        affair = affair1;
                        break;
                    }
                }
                /**OA-178171*/
                if (affair.getSubState() == SubStateEnum.col_pending_specialBack.getKey()) {
                    affair.setSubState(SubStateEnum.col_pending_unRead.getKey());
                    affair.setBackFromId(null);
                } else if (affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderReGo.getKey()) {
                    affair.setSubState(SubStateEnum.col_pending_ZCDB.getKey());
                } else {
                    continue;
                }
                try {
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    affair.setReceiveTime(now);
                    affair.setUpdateDate(now);
                    affair.setPreApprover(null != AppContext.getCurrentUser() ? AppContext.getCurrentUser().getId() : null);
                    affair.setState(StateEnum.col_pending.key());
                    //删除原来的定时任务（超期提醒、提前提醒）
                    if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                        QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                    }
                    if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                        QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                    }
                    //设置新的定时任务
                    Long accountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary, templateManager);
                    Long deadLine = affair.getDeadlineDate();
                    Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                    Date deadLineRunTime = null;
                    try {
                        if (deadLine != null && deadLine != 0) {
                            deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, accountId);
                            affair.setExpectedProcessTime(deadLineRunTime);
                            if (now.after(deadLineRunTime)) {
                                affair.setCoverTime(true);
                            } else {
                                affair.setCoverTime(false);
                            }
                        }
                    } catch (WorkTimeSetExecption e) {
                        LOGGER.error("", e);
                    }
                    affairManager.updateAffair(affair);
                    WFComponentUtil.affairExcuteRemind(affair, accountId);
                } catch (BusinessException e) {
                    LOGGER.error(e.getMessage(), e);
                }

                /** 激活节点：消息接收人员  **/
                String messageLink = "message.link.govdoc.pending";
                if (GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
                    messageLink = "message.link.edoc.pending";
                }
                String senderName = orgManager.getMemberById(affair.getSenderId()).getName();
                MessageContent messageContentSent = new MessageContent("edoc.appointStepBack.send", senderName, affair.getSubject());
                messageContentSent.setImportantLevel(affair.getImportantLevel());
                ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), messageLink, affair.getId().toString());
                userMessageManager.sendSystemMessage(messageContentSent, app, affair.getSenderId(), receiver, GovdocMessageHelper.getSystemMessageFilterParam(affair).key);

                break;
            }

        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        return true;
    }

    /**
     * 指定回退到普通/发起节点，退回人状态处理(待办->挂起)
     * 改状态; 不发消息;
     */
    @Override
    public boolean onWorkitemReadyToWait(EventDataContext context) {
        try {
            //提交时传递的summay
            EdocSummary summary = (EdocSummary) context.getAppObject();
            if (summary == null) {
                return false;
            }

            List<WorkItem> workitems = context.getWorkitemLists();
            String currentAffairID = (String) (context.getBusinessData("CURRENT_OPERATE_AFFAIR_ID") + "");
            for (WorkItem workItem : workitems) {
                CtpAffair affair = affairManager.getAffairBySubObjectId(workItem.getId());
                if (!String.valueOf(affair.getId()).equals(currentAffairID))
                    continue;
                affair.setState(StateEnum.col_stepBack.getKey());
                if (affair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()) { //16
                    affair.setSubState(SubStateEnum.col_pending_specialBackCenter.getKey());//17如果是被退回的数据。
                } else if (affair.getSubState() == SubStateEnum.col_pending_ZCDB.getKey()) {//13
                    affair.setSubState(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());//19暂存代办的数据
                } else {
                    affair.setSubState(SubStateEnum.col_pending_specialBack.getKey());//15
                }
                affair.setUpdateDate(new java.util.Date());
                //affair.setBackFromId(AppContext.getCurrentUser().getId());
                affairManager.updateAffair(affair);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    /**
     * 指定回退到普通节点时，被退回人的处理(已办-待办) 改状态; 删定时任务; 增定时任务; 不发消息;
     */
    @Override
    public boolean onWorkitemDoneToReady(EventDataContext context) {
        //提交时传递的summay
        EdocSummary summary = (EdocSummary) context.getAppObject();
        if (summary == null) {
            return false;
        }
        try {
            List<WorkItem> workitems = context.getWorkitemLists();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Long accountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary, templateManager);
            List<Long> memberList = new ArrayList<Long>();
            for (WorkItem workItem : workitems) {
                CtpAffair affair = affairManager.getAffairBySubObjectId(workItem.getId());

                //EdocSummary s = govdocSummaryManager.getSummaryById(affair.getObjectId(), false);
                memberList.add(affair.getMemberId());
                affair.setState(StateEnum.col_pending.key());
                affair.setSubState(SubStateEnum.col_pending_specialBacked.key());
                affair.setArchiveId(null);
                affair.setUpdateDate(now);
                affair.setCompleteTime(null);
                affair.setReceiveTime(now);
                affair.setBackFromId(AppContext.getCurrentUser().getId());

                //设置回退人的id用来在待办栏目显示
                affair.setBackFromId(AppContext.getCurrentUser().getId());
                //将超期状态置为不超期，设置新的定时任务来重新计算超期状态
                affair.setCoverTime(false);
                affair.setDelete(false);
                //超期时间
                Long deadLine = affair.getDeadlineDate();
                Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                Date deadLineRunTime = null;
                try {
                    if (deadLine != null && deadLine != 0) {
                        deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, accountId);
                        affair.setExpectedProcessTime(deadLineRunTime);
                    }
                } catch (WorkTimeSetExecption e) {
                    LOGGER.error("", e);
                }
                affairManager.updateAffair(affair);
                //删除原来的定时任务（超期提醒、提前提醒）
                if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                }
                if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                }
                //设置新的定时任务
                WFComponentUtil.affairExcuteRemind(affair, accountId);
            }
            context.setBusinessData("pointMemberIds", memberList);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    /**
     * 保存公文下节点的Affair数据及发送消息
     *
     * @param affairData
     * @param receiveTime
     * @param isCover
     * @param receivers
     * @param receivers1
     */
    private void saveListMap(AffairData affairData, Date receiveTime, Boolean isCover, List<MessageReceiver> receivers, List<MessageReceiver> receivers1) {
        if (affairData == null) {
            return;
        }
        try {
            List<CtpAffair> affairList = affairData.getAffairList();
            Boolean isSendMessage = affairData.getIsSendMessage();
            if (affairList == null || affairList.isEmpty()) {
                return;
            }
            CtpAffair aff = affairList.get(0);
            if (affairList.size() <= 50) {
                affairManager.saveAffairs(affairList);
            } else {
                DBAgent.saveAllForceFlush(affairList);
            }

            // 生成事项消息提醒
            if (isSendMessage) {
                govdocMessageManager.sendMessage(affairData, receivers, receivers1, receiveTime);
            }

            // 发送流程超期消息
            if (isCover != null && isCover) {
                govdocMessageManager.sendMsg4ProcessOverTime(aff, receivers, receivers1);
            }

            // 在此调用CallBack
            if (affairList.size() == 0) {
                return;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public boolean onSubProcessStarted(EventDataContext context) {
        try {
            String processTemplateId = context.getProcessTemplateId();
            String sendId = context.getStartUserId();
            //处理人的事项数据
            AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
            /************************************ 老公文逻辑 start ************************************/
            if (affairData.getFormAppId() == null) {
                return false;
            }
            /************************************ 老公文逻辑   end ************************************/
            CtpTemplate templete = templateManager.getCtpTemplateByWorkFlowId(Long.parseLong(processTemplateId));
            if (templete == null) {
                LOGGER.error("发起新流程失败，原因：触发的表单模板已被删除。NewflowRunningId=" + processTemplateId);
                return false;
            }
            GovdocPubManager govdocPubManager = (GovdocPubManager) AppContext.getBean("govdocPubManager");
            String bodyType = affairData.getBodyType();
            if (StringUtils.isBlank(bodyType)) {
                bodyType = "20";
            }
            Long reciveAccountId = AppContext.currentAccountId();
            if (StringUtils.isNotBlank(sendId)) {
                V3xOrgMember sender = orgManager.getMemberById(Long.parseLong(sendId));
                if (sender != null) {
                    reciveAccountId = sender.getOrgAccountId();
                }
            }
            Long oldSummaryId = affairData.getModuleId();
            //查看发起人发起的附件
            List<Attachment> attachmentlist = attachmentManager.getByReference(oldSummaryId, oldSummaryId);
            boolean hasAtts = false;
            if (attachmentlist != null && attachmentlist.size() > 0) {
                hasAtts = true;
            }
            AppContext.putThreadContext(EventDataContext.CTP_WORKFLOW_SUBPROCESS_SKIP_FSENDER, context.isSubProcessSkipFSender());
            SendGovdocResult sendGovdocResult = govdocPubManager.transSendColl(EdocConstant.SendType.child, templete.getId(),
                    Long.parseLong(sendId), affairData.getFormRecordId(), oldSummaryId, null, reciveAccountId, Integer.valueOf(bodyType), hasAtts, null, null);
            //子流程的已发事项
            CtpAffair childSenderAffair = sendGovdocResult.getSentAffair();
            EdocSummary newSummary = sendGovdocResult.getSummary();

            //进行一下排序
            if (!attachmentlist.isEmpty()) {
                Collections.sort(attachmentlist, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        Attachment stu1 = (Attachment) o1;
                        Attachment stu2 = (Attachment) o2;
                        return Integer.valueOf(stu1.getSort()).compareTo(Integer.valueOf(stu2.getSort()));
                    }
                });
            }
            Long[] fileIds = new Long[attachmentlist.size()];
            int i = 0;
            for (Attachment att : attachmentlist) {
                Long newFileId = fileManager.copyFileBeforeModify(att.getFileUrl());
                if (newFileId != -1) {
                    fileIds[i++] = newFileId;
                }
            }
            // 创建附件
            if (fileIds.length > 0) {
                attachmentManager.create(fileIds, ApplicationCategoryEnum.edoc, newSummary.getId(), newSummary.getId());
                List<Attachment> list = attachmentManager.getByReference(newSummary.getId());
                // 修改affair
                List<CtpAffair> aff = affairManager.getAffairs(newSummary.getId());
                for (CtpAffair a : aff) {
                    AffairUtil.setHasAttachments(a, list.size() > 0);
                    affairManager.updateAffair(a);
                }
            }
            wapi.updateSubProcessRunning(context.getSubProcessRunningId(), newSummary.getProcessId(), newSummary.getCaseId(), context.getStartUserId(), context.getStartUserName());
            //触发新流程，发送系统消息 ： 来自《主流程标题》的子流程《子流程标题》已经发起
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            //主流程的已发事项
            CtpAffair senderAffair = affairManager.getSenderAffair(affairData.getModuleId());
            Integer importantLevel = WFComponentUtil.getImportantLevel(senderAffair);
            boolean isCanViewByMainFlow = false;
            Object canViewByMainFlowObject = context.getBusinessData(EventDataContext.CTP_SUB_WORKFLOW_CAN_VIEW_BY_MAIN_FLOW);
            if (null != canViewByMainFlowObject) {
                isCanViewByMainFlow = (Boolean) canViewByMainFlowObject;
            }
            List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelToList(senderAffair.getSenderId());//得到谁代理我了

            if (isCanViewByMainFlow) {//能被主流程查看，就能打开连接
                Object relativeProcessId = context.getBusinessData(EventDataContext.CTP_WORKFLOW_MAIN_PROCESSID);
                Long relativeProcessIdl = relativeProcessId == null ? 0l : Long.valueOf(relativeProcessId.toString());
                receivers.add(new MessageReceiver(senderAffair.getId(), senderAffair.getSenderId(), "message.link.govdoc.done.newflow", childSenderAffair.getId(), 0, affairData.getModuleId(), newSummary.getProcessId(), relativeProcessIdl));
                //给代理人发消息
                for (AgentModel am : agentModelList) {
                    Long agentId = am.getAgentId();//代理人id
                    receivers.add(new MessageReceiver(senderAffair.getId(), agentId, "message.link.govdoc.done.newflow", childSenderAffair.getId(), 0, affairData.getModuleId(), newSummary.getProcessId(), relativeProcessIdl));
                }
            } else {
                receivers.add(new MessageReceiver(senderAffair.getId(), senderAffair.getSenderId()));
                //给代理人发消息
                for (AgentModel am : agentModelList) {
                    Long agentId = am.getAgentId();//代理人id
                    receivers.add(new MessageReceiver(senderAffair.getId(), agentId));
                }
            }
            //重要程度图标，不需要转换
            userMessageManager.sendSystemMessage(new MessageContent("collaboration.msg.workflow.new.start", affairData.getSubject(), newSummary.getSubject()).setImportantLevel(senderAffair.getImportantLevel()), ApplicationCategoryEnum.edoc, Long.parseLong(sendId), receivers, importantLevel);
            return true;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean onSubProcessCanceled(EventDataContext context) {
        long subCaseId = context.getCaseId();
        User user = AppContext.getCurrentUser();
        String operationType = (String) context.getBusinessData(OPERATION_TYPE);
        try {
            govdocManager.recallNewflowSummary(subCaseId, user, operationType);
        } catch (Throwable e) {
            LOGGER.error("子流程撤销发生异常", e);
        }
        return true;
    }

    public void setGovdocManager(GovdocManager govdocManager) {
        this.govdocManager = govdocManager;
    }

    public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
        this.govdocSummaryManager = govdocSummaryManager;
    }

    public void setGovdocMessageManager(GovdocMessageManager govdocMessageManager) {
        this.govdocMessageManager = govdocMessageManager;
    }

    public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

    public void setGovdocTraceWorkflowManager(TraceWorkflowDataManager govdocTraceWorkflowManager) {
        this.govdocTraceWorkflowManager = govdocTraceWorkflowManager;
    }

    public void setOrganizationManager(IOrganizationManager organizationManager) {
        this.organizationManager = organizationManager;
    }
//	public void setGovdocOCIPExchangeManager(GovdocOCIPExchangeManager govdocOCIPExchangeManager) {
//		this.govdocOCIPExchangeManager = govdocOCIPExchangeManager;
//	}

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    public Long getModuleIdByProcessId(String processId) {

        if (Strings.isDigits(processId)) {
            try {
                EdocSummary s = govdocSummaryManager.getSummaryByProcessId(processId);
                return s.getId();
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
        }
        return null;
    }
}
