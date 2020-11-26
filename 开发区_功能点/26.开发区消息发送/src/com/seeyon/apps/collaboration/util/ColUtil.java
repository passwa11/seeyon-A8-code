/**
 * $Author$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.apps.collaboration.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.privilege.manager.PrivilegeManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.vo.BPMSeeyonPolicyVO;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class ColUtil {
    private static Log                log              = LogFactory.getLog(ColUtil.class);
    protected static final int        INENTIFIER_SIZE  = 20;
    private static final int          CURRENT_NODES_INFO_SIZE  = 10;//当前待办人数量
    private static DocApi             docApi;
    private static EnumManager        enumManagerNew;
    private static OrgManager         orgManager;
    private static WorkTimeManager    workTimeManager;
    private static WorkflowApiManager wapi;
    private static CollaborationApi   collaborationApi;
    private static PrivilegeManager   privilegeManager;
    private static AffairManager      affairManager;
    private static CAPFormManager capFormManager;
    
    public static boolean isFinshed(ColSummary summary) {
        return summary.getFinishDate() != null;
    }
    
    public static  boolean isHasAttachments(ColSummary summary) {
        return IdentifierUtil.lookupInner(summary.getIdentifier(),
                INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1');
    }

    public static void setHasAttachments(ColSummary summary,Boolean hasAttachments) {
        if(Strings.isBlank(summary.getIdentifier())){
            summary.setIdentifier(IdentifierUtil.newIdentifier(summary.getIdentifier(), INENTIFIER_SIZE,
                    '0'));
        }
        summary.setIdentifier(IdentifierUtil.update(summary.getIdentifier(),
               INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), hasAttachments ? '1' : '0'));
    }
    
    public static  boolean isWorkflowTrace(ColSummary summary) {
        return IdentifierUtil.lookupInner(summary.getIdentifier(),
                INENTIFIER_INDEX.IS_WORKFLOWTRACE.ordinal(), '1');
    }

    public static void setWorkflowTrace(ColSummary summary,Boolean hasAttachments) {
        if(Strings.isBlank(summary.getIdentifier())){
            summary.setIdentifier(IdentifierUtil.newIdentifier(summary.getIdentifier(), INENTIFIER_SIZE,
                    '0'));
        }
        summary.setIdentifier(IdentifierUtil.update(summary.getIdentifier(),
               INENTIFIER_INDEX.IS_WORKFLOWTRACE.ordinal(), hasAttachments ? '1' : '0'));
    }
    /**
     * 标志位, 共100位，采用枚举的自然顺序
     */
    protected static enum INENTIFIER_INDEX {
        HAS_ATTACHMENTS, // 是否有附件
        HAS_FORMTRIGGER, // 是否有配置表单触发
        IS_WORKFLOWTRACE //是否追溯克隆数据
    }

    public static String getMemberName(Long id){
        return Functions.showMemberName(id);
    }
    
    //基准时长
    public static String getStandardDuration(String value){
        return WFComponentUtil.getStandardDuration(value);
    }
    
    public static String getImportantLevel(String value){
        return WFComponentUtil.getImportantLevel(value);
    }

    public static String showSubjectOfSummary(ColSummary summary, Boolean isProxy, int length, String proxyName){
        if(summary == null){
            return null;
        }
        String subject = summary.getSubject();
        if(null != summary.getAutoRun() && summary.getAutoRun()){
        	subject = ResourceUtil.getString("collaboration.newflow.fire.subject",subject);
        }
        return WFComponentUtil.showSubject(subject,summary.getForwardMember(),summary.getResentTime(),isProxy,length,proxyName,false);
    }
    public static String showSubjectOfAffair(CtpAffair affair, Boolean isProxy, int length){
        return WFComponentUtil.showSubjectOfAffair(affair, isProxy, length);
    }

    /**
     * 用于协同已办的显示
     * @param summary
     * @param isProxy
     * @param length
     * @param proxyName
     * @param isAgentDeal
     * @return
     */
    public static String showSubjectOfSummary4Done(CtpAffair affair, int length){
        return WFComponentUtil.showSubjectOfSummary4Done(affair, length);
    }
    /**
     * 分解转发人、转发次数，用于任务项分配往Affair表写
     *
     * @param subject
     * @param forwardMember
     * @param orgManager
     * @param locale 语言，如果是null，则采用当前登录者的语言
     * @return 转发人姓名
     */
    public static String mergeSubjectWithForwardMembers(ColSummary summary, OrgManager orgManager, Locale locale) {
        return WFComponentUtil.mergeSubjectWithForwardMembers(summary.getSubject(), -1,
                summary.getForwardMember(), summary.getResentTime(), locale);
    }
    /**
     * 时间拼装
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @param filterFlag 秒 过滤
     * @return
     */
    public static String timePatchwork(long day, long hour, long minute, long second, boolean filterFlag){
        String timeStr = "";
        if(second != 0){
            timeStr = second + ResourceUtil.getString("common.time.second");
        }
        if(minute != 0){
            timeStr = minute + ResourceUtil.getString("common.time.minute");
            if(!filterFlag)
                timeStr += second + ResourceUtil.getString("common.time.second");
        }
        if (hour != 0){
            timeStr = hour + ResourceUtil.getString("common.time.hour")
            + minute + ResourceUtil.getString("common.time.minute");
            if(!filterFlag)
                timeStr += second + ResourceUtil.getString("common.time.second");
        }
        if (day != 0){
            timeStr = day + ResourceUtil.getString("common.time.day") + hour + ResourceUtil.getString("common.time.hour")
            + minute + ResourceUtil.getString("common.time.minute");
            if(!filterFlag)
                timeStr += second + ResourceUtil.getString("common.time.second");
        }
        return timeStr;
    }

	public static String getErrorMsgByAffair(CtpAffair affair) {
		return WFComponentUtil.getErrorMsgByAffair(affair);
	}
	 
	public static Long getMinutesBetweenDatesByWorkTime(Date startDate,Date endDate ,Long orgAccountId ){
        if(startDate == null
            ||endDate == null
            ||orgAccountId == null){
            return 0L;
        }
        Long workTime = 0L;
        try {
            workTime = getWorkTimeManager().getDealWithTimeValue(startDate,endDate,orgAccountId);
            workTime = workTime/(60*1000);
        } catch (WorkTimeSetExecption e1) {
          log.error("获取工作时间分钟数异常",e1);
        }
        return workTime;
    }
	
	public static double getMinutesBetweenDatesByWorkTimehasDecimal(Date startDate,Date endDate ,Long orgAccountId ){
        if(startDate == null
            ||endDate == null
            ||orgAccountId == null){
            return 0L;
        }
        double workTime = 0L;
        try {
            workTime = getWorkTimeManager().getDealWithTimeValue(startDate,endDate,orgAccountId);
            workTime = workTime/60.0/1000.0;
        } catch (WorkTimeSetExecption e1) {
          log.error("获取工作时间分钟数异常",e1);
        }
        return workTime;
    }
	
    public static Long convert2WorkTime(Long time, Long accountId){
        return getWorkTimeManager().convert2WorkTime(time,accountId);
    }
    
    public static AffairData getAffairData(ColSummary summary) throws BusinessException {
        AffairData affairData = new AffairData();
        affairData.setForwardMember(summary.getForwardMember());
        affairData.setModuleType(ApplicationCategoryEnum.collaboration.key());
        if (summary.getId() != null) {
            affairData.setModuleId(summary.getId());
        }
        //容错处理
        int importantLevel = 1;
        if(summary.getImportantLevel() != null){
            importantLevel = summary.getImportantLevel();
        }else{
            summary.setImportantLevel(1);
        }

        affairData.setImportantLevel(importantLevel);
        affairData.setIsSendMessage(true); //是否发消息
        affairData.setResentTime(summary.getResentTime());//如协同colsummary
        affairData.setState(StateEnum.col_pending.key());//事项状态 - 协同业务中3为待办
        affairData.setSubject(summary.getSubject());//如协同colsummary
        affairData.setSubState(SubStateEnum.col_pending_unRead.key());//事项子状态 协同业务中11为协同-待办-未读
        affairData.setSummaryAccountId(summary.getOrgAccountId());//如协同colsummary.orgAccountId
        affairData.setTemplateId(summary.getTempleteId());//如协同colsummary
        affairData.setIsHasAttachment(ColUtil.isHasAttachments(summary));//是否有附件
        affairData.setContentType(summary.getBodyType());
        affairData.setSender(summary.getStartMemberId());
        affairData.setFormRecordId(summary.getFormRecordid());
        affairData.setFormAppId(summary.getFormAppid());
        affairData.setCreateDate(summary.getCreateDate());
        affairData.setProcessDeadlineDatetime(summary.getDeadlineDatetime());
        affairData.setCaseId(summary.getCaseId());
        affairData.setProcessId(summary.getProcessId());//设置主流程id
        affairData.setOrgAccountId(summary.getOrgAccountId());
        return affairData;
    }


    /**
     * 获取流程模板对应的单位
     *
     * @param defaultAccountId
     * @param summary
     * @param templateManager
     * @return
     * @throws BusinessException
     */
    public static Long getFlowPermAccountId(Long defaultAccountId, ColSummary summary) throws BusinessException{
    	Long flowPermAccountId = defaultAccountId;
        Long templeteId = summary.getTempleteId();
        if (templeteId != null) {
        	if(summary.getPermissionAccountId()!= null){
        		flowPermAccountId = summary.getPermissionAccountId();
        	}
        } else {
            if (summary.getOrgAccountId() != null) {
                flowPermAccountId = summary.getOrgAccountId();
            }
        }
        return flowPermAccountId;
    }
    
    public static Long getFlowPermAccountId(Long defaultAccountId, Long summaryOrgAccountId, Long templateOrgAccountId) throws BusinessException{
        Long flowPermAccountId = defaultAccountId;
        if(templateOrgAccountId != null){
                flowPermAccountId = templateOrgAccountId;
        }
        else{
            if(summaryOrgAccountId != null){
                flowPermAccountId = summaryOrgAccountId;
            }
        }
        return flowPermAccountId;
    }

    /**
     * 产生最终标题
     *
     * @param template
     * @param summary
     * @param sender
     * @return
     * @throws BusinessException
     */
    public static String makeSubject4NewWF(CtpTemplate template, ColSummary summary, User sender) throws BusinessException{
        //String  subject  = ResourceUtil.getString("collaboration.newflow.fire.subject", template.getSubject() + "(" + sender.getName() + " " + Datetimes.formatDatetimeWithoutSecond(summary.getCreateDate()) + ")");
        String  subject  =  template.getSubject() + "(" + sender.getName() + " " + Datetimes.formatDatetimeWithoutSecond(summary.getCreateDate()) + ")";
        if(Strings.isBlank(template.getColSubject())){
            return subject;
        }

        if(Strings.isNotBlank(template.getColSubject())){
            WorkflowApiManager wapi = getWorkflowApiManager();
            CAPFormManager capFormManager = getCapFormManager();
            BPMSeeyonPolicyVO startPolicy  = wapi.getStartNodeFormPolicy(template.getWorkflowId());
            
            if(startPolicy != null){
                
                String subjectForm = capFormManager.getCollSubjuet(summary.getFormAppid(), template.getColSubject(), summary.getFormRecordid(), true);
                
                log.info(AppContext.currentUserLoginName()+"，表单接口,协同标题生成：Param:appid:"+summary.getFormAppid()+",template.getColSubject()："+template.getColSubject()+",recordId:"+summary.getFormRecordid());
                
                //转移换行符，标题中不能用换行符
                subjectForm = Strings.toText(subjectForm);
                
                if (Strings.isBlank(subjectForm)) {
                    subjectForm = "{" + ResourceUtil.getString("collaboration.subject.default") + "}";
                }
                
                subject = subjectForm;
                subject = Strings.toText(subject);
                if (subject.length() > 300) {
                    subject = subject.substring(0, 295) + "...";
                }
                log.info("最终标题："+subject);
            }
        }
        return subject;
    }
    
    public static String makeSubject(CtpTemplate template, ColSummary summary, User sender) throws BusinessException{
        if(template == null || Strings.isBlank(template.getColSubject())){
            return summary.getSubject();
        }
        String subject = summary.getSubject();
        if(Strings.isNotBlank(template.getColSubject())){
            WorkflowApiManager wapi = getWorkflowApiManager();
            CAPFormManager capFormManager = getCapFormManager();
            BPMSeeyonPolicyVO startPolicy = wapi.getStartNodeFormPolicy(template.getWorkflowId());
            if(startPolicy != null){
                
                log.info(AppContext.currentUserLoginName()+"，表单接口,协同标题生成：Param:appid:"+summary.getFormAppid()+",template.getColSubject()："+template.getColSubject()+",recordId:"+summary.getFormRecordid());
                subject = capFormManager.getCollSubjuet(summary.getFormAppid(), template.getColSubject(), summary.getFormRecordid(), false);
                log.info("协同标题生成成功, 协同ID=："+summary.getId());
                //转移换行符，标题中不能用换行符
                subject = Strings.toText(subject);
                if (subject.length() > 300) {
                    subject = subject.substring(0, 295) + "...";
                }
            }
        }
        return subject;
    }
    
    /**
     * 根据模板标题和流程ID查询模板标题
     * @param templateColSubject 模板标题
     * @param templateWorkflowId 模板流程ID
     * @param summary
     * @param sender
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("finally")
	public static String makeSubject(String templateColSubject,Long templateWorkflowId, ColSummary summary, User sender) throws BusinessException{
    	if(Strings.isBlank(templateColSubject)){
            return summary.getSubject();
        }
        String subject = summary.getSubject();
        if(Strings.isNotBlank(templateColSubject)){
        	try {
        		WorkflowApiManager wapi = getWorkflowApiManager();
        		CAPFormManager capFormManager = getCapFormManager();
                BPMSeeyonPolicyVO startPolicy = wapi.getStartNodeFormPolicy(templateWorkflowId);
                if(startPolicy != null){
                    
                    subject = capFormManager.getCollSubjuet(summary.getFormAppid(), templateColSubject, summary.getFormRecordid(), false);
                    
                    log.info(AppContext.currentUserLoginName()+"，表单接口,协同标题生成：Param:appid:"+summary.getFormAppid()+",template.getColSubject()："+ templateColSubject + ",recordId:"+summary.getFormRecordid());
                    //转移换行符，标题中不能用换行符
                    subject = Strings.toText(subject);
                    if (subject.length() > 300) {
                        subject = subject.substring(0, 295) + "...";
                    }
                    log.info("最终标题："+subject);
                }
			} catch (Exception e) {
				log.error("",e);
				subject = summary.getSubject();
			} finally {
				return subject;
			}
            
        }
        return subject;
    }
    
    /**
     * 获得节点权限名称
     * @param affair
     * @return
     * @throws BusinessException
     */
    public static SeeyonPolicy getPolicyByAffair(CtpAffair affair) throws BusinessException {
    	try{
    		SeeyonPolicy seeyonPolicy = getPolicy(affair);
    		if(seeyonPolicy == null){
    			return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
    		}else if(Strings.isBlank(seeyonPolicy.getId())){
    			return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
    		}
    		return seeyonPolicy;
    	}catch(Exception e){
    	  log.error("",e);
    		return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
    	}
    }
	private static SeeyonPolicy getPolicy(CtpAffair affair) throws BusinessException, BPMException {
		if (affair == null
        		|| affair.getState().equals(StateEnum.col_waitSend.getKey())
        		|| affair.getState().equals(StateEnum.col_sent.getKey())){
            return new SeeyonPolicy("newCol",ResourceUtil.getString("node.policy.newCol"));
        }
        if(Strings.isNotBlank(affair.getNodePolicy())){
            return new SeeyonPolicy(affair.getNodePolicy(), BPMSeeyonPolicy.getShowName(affair.getNodePolicy()));
        }
        WorkflowApiManager wapi = getWorkflowApiManager();
        CollaborationApi collaborationApi = getCollaborationApi();
        ColSummary summary = collaborationApi.getColSummary(affair.getObjectId());
        if(summary != null){
            String[] result = wapi.getNodePolicyIdAndName(ModuleType.collaboration.name(),summary.getProcessId(),String.valueOf(affair.getActivityId()));
            if(result==null)
                return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
            return new SeeyonPolicy(result[0],result[1]);
        }else{
            return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
        }
	}
    

    public static void deleteQuartzJobOfSummary(ColSummary summary){
        WFComponentUtil.deleteQuartzJob(summary.getId(),summary.getMessageRuleId());
    }

    /**
     * 发送消息 控制
     * @param affair
     * @return
     */
    public static Integer getImportantLevel(ColSummary summary){
        if(summary == null || summary.getImportantLevel() == null ){
            return 1;
        }
        //模板协同
        if(summary.getTempleteId() != null) {
            switch (summary.getImportantLevel()) {
                case 1:
                    return 4;
                case 2:
                    return 5;
                case 3:
                    return 6;
                default:
                    break;
            }
        }else{
            //自由协同
            return summary.getImportantLevel();
        }
        return 1;
    }
    /**
     * 将集团管理员账号和单位管理员账号 转换成'集团管理员'或者'单位管理员'
     * @return
     */
    public static String getAccountName(){
        User user = AppContext.getCurrentUser();
        return OrgHelper.showMemberNameOnly(Long.valueOf(user.getId()));
    }
    /**
     * 校验协同是否超期
     * @param startDate
     * @param endDate
     * @param deadline
     * @param summary
     * @return
     */
    public static boolean checkColSummaryIsOverTime(ColSummary summary){
       return summary.isCoverTime() == null ? false : summary.isCoverTime();
    }

    /**
     * 校验事项是否超期
     * @param affair
     * @return
     */
    public static boolean checkAffairIsOverTime(CtpAffair affair,ColSummary summary){
        
        //long accountId;
        Date edate = new Date();
        //如果未设置流程期限，则不存在超期
        /*if(affair == null || affair.getDeadlineDate() == null || "0".equals(affair.getDeadlineDate().toString())){
            return false;
        }*/
        if(affair == null || affair.getExpectedProcessTime() == null){
            return affair.isCoverTime();
        }
        
        edate = affair.getExpectedProcessTime();
        
        /*try {
            accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
            edate = getWorkTimeManager().getCompleteDate4Nature(affair.getReceiveTime(), affair.getDeadlineDate(),accountId);
        }catch (BusinessException e) {
          log.error("计算工作时间是否超期",e);
        }*/
        boolean isOverTime = false;
        Date finishDate = affair.getCompleteTime();
        if(finishDate == null){
            if(edate.after(new Date())){
                isOverTime = false;
            }else{
                isOverTime = true;
            }
        }else{
            if(edate.after(finishDate)){
                isOverTime = false;
            }else{
                isOverTime = true;
            }
        }
        return isOverTime;
    }
    /**
     * 验证affair是否是有效数据
     * @param affair 当前事项
     * @param isFilterDelete 是否过滤删除
     * @return
     */
    public static boolean isAfffairValid(CtpAffair affair,boolean isFilterDelete){
    	return AffairUtil.isAfffairValid(affair,isFilterDelete);
    }
    public static boolean isAfffairValid(CtpAffair affair){
        return AffairUtil.isAfffairValid(affair,false);
    }

    /**
     * 校验是否有资源菜单
     * @param resourceCode
     * @return
     * @throws BusinessException
     */
    public static boolean checkByReourceCode(String resourceCode){
        User user = AppContext.getCurrentUser();
        if(user != null){
            return user.hasResourceCode(resourceCode);
        }

        PrivilegeManager pc = getPrivilegeManager();
    	boolean isHave = false;
		try {
			isHave = pc.checkByReourceCode(resourceCode);
		} catch (BusinessException e) {
		  log.error("",e);
		}
    	return isHave;
    }
    
    public static String parseCurrentNodesInfo(ColSummary summary) {
        return WFComponentUtil.parseCurrentNodesInfo(summary.getFinishDate(), summary.getCurrentNodesInfo(),summary.getState());
    }

	public static boolean isChildrenWorkFlow(ColSummary summary){
		return Integer.valueOf(ColConstant.NewflowType.child.ordinal()).equals(summary.getNewflowType());
	}

	public static void setCurrentNodesInfoFromCache(ColSummary summary, Long needRemoveCurrentMemberId,List<CtpAffair> assignedAffairs)
			throws BusinessException {

		List<CtpAffair> affairs = assignedAffairs;
		
		int count = 0;
		List<String> _newCurrentInfos =  new ArrayList<String>();
		if (Strings.isNotEmpty(affairs)) {
			// 最多只存10个待办人
			for (int k = 0; k < affairs.size() && count < CURRENT_NODES_INFO_SIZE; k++) {
				CtpAffair cf = affairs.get(k);
				String policy = cf.getNodePolicy();
				// 知会节点不算待办
				if (Strings.isNotBlank(policy) && !"inform".equals(policy)) {
					long memberId = cf.getMemberId();
					_newCurrentInfos.add(String.valueOf(memberId));
					count++;
				}
			}
		}
		//如果已经新生成的》2了，就不用解析老的了，性能优化
		String oldInfos = summary.getCurrentNodesInfo();
		//OA-155635 有个人存在两个待办 处理一个 全部都移除当前待办人了
		List alreadyRemove =  new ArrayList();
		if(count < CURRENT_NODES_INFO_SIZE && oldInfos != null){
		    
		    int leftCount = CURRENT_NODES_INFO_SIZE - count;
			String[] _oldInfos = oldInfos.split("[;]");
			String oldMenberId = String.valueOf(needRemoveCurrentMemberId);
			for(int i = 0;i < _oldInfos.length && leftCount > 0; i++){
			    String s = _oldInfos[i];
			    if(Strings.isNotBlank(s) && (!s.equals(oldMenberId) || alreadyRemove.contains(s))){
			        _newCurrentInfos.add(s);
			        leftCount--;
			    }else{
			    	alreadyRemove.add(s);
			    }
			}
		}
		
		if(_newCurrentInfos.size() != 0){
		    String __str  = Strings.join(_newCurrentInfos, ";");
	        if(Strings.isBlank(__str)){
	            log.info("当前待办人为空,协同colsummary.id:"+summary.getId()+",时间：+"+new Date());
	        }
	        summary.setCurrentNodesInfo(__str);
		}else{
			
			boolean isFinished = Integer.valueOf(CollaborationEnum.flowState.terminate.ordinal()).equals(summary.getState())
					|| Integer.valueOf(CollaborationEnum.flowState.finish.ordinal()).equals(summary.getState());
			
			if(!isFinished ) {
				updateCurrentNodesInfo(summary);
			}
		}
		
	}

	/**
     * 重新更新当前待办人信息
     * @param summary
     * @param isUpdateToDB 是否直接更新到数据库
     * @throws BusinessException
     */
    public static void updateCurrentNodesInfo(ColSummary summary) throws BusinessException {
        updateCurrentNodesInfo(summary,null);
    }
    
    public static void updateCurrentNodesInfo(ColSummary summary,Long currentAffairId) throws BusinessException {
        updateCurrentNodesInfo(summary, currentAffairId, false);
    }
    
    public static void updateCurrentNodesInfo(ColSummary summary,Long currentAffairId, boolean includeSender) throws BusinessException{

        List<Integer> states = new ArrayList<Integer>();
        states.add( StateEnum.col_pending.key());
        states.add( StateEnum.col_pending_repeat_auto_deal.getKey());
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        map.put("objectId", summary.getId());
        map.put("state",states);
        map.put("delete", false);
        map.put("nodePolicy","inform");
        
        FlipInfo fi = new FlipInfo();
        fi.setNeedTotal(false);
        fi.setSize(CURRENT_NODES_INFO_SIZE);
        
//      List<CtpAffair> affairs = getAffairManager().getAffairsByObjectIdAndStates(fi, summary.getId(), states);
        List<CtpAffair> affairs = getAffairManager().getAffairsForCurrentUsers(fi, map);
        int count = 0;
        
        List<Long> memberIds = new ArrayList<Long>(affairs.size());
        
		/*   if(includeSender){
		    memberIds.add(summary.getStartMemberId());
		}*/
        
        if(Strings.isNotEmpty(affairs)){
            //最多只存10个待办人
            for(int k = 0;k < affairs.size() && count<CURRENT_NODES_INFO_SIZE;k++){
                CtpAffair cf = affairs.get(k);
                String policy=cf.getNodePolicy();
                //知会节点不算待办
                if (Strings.isNotBlank(policy) 
                        && !"inform".equals(policy) 
                        && !Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(cf.getSubState()) 
                        && !(cf.getId().equals(currentAffairId))){
                
                    /*if(current.indexOf(memberId+"")!=-1){//一个人只需要显示一次
                        continue;
                    }*/
                    memberIds.add(cf.getMemberId());
                    count++;
                }
            }
        }
        
        String current = Strings.join(memberIds, ";");
        
       // if(Strings.isBlank(current)){
       //    log.info("协同"+summary.getId()+",id="+summary.getId()+"!时间："+new Date());
       //}
        summary.setCurrentNodesInfo(current);
    
    }
    
    

	public static boolean checkAgent(CtpAffair affair,ColSummary summary,boolean isWebAlert) throws Exception {
		return checkAgent(affair.getMemberId(), affair.getSubject(), ModuleType.collaboration, isWebAlert, AppContext.getRawRequest());
	}
	
	public static String ajaxCheckAgent(Long affairMemberId,String subject,ModuleType moduleType){
		
		String result = "";
		User user = AppContext.getCurrentUser();
		
		if(user.getId().equals(affairMemberId)){
			result = "";
		}else if(user.isAdmin()){
			log.info("[代理校验]:类型："+moduleType.getKey()+",当前用户:"+user.getName()+"uerid:"+user.getId()+",管理员！");
			result = "";
		}else{
			List<Long> ownerIds = MemberAgentBean.getInstance().getAgentToMemberId(moduleType.getKey(),user.getId());
			
			V3xOrgMember m = null;
			
			try {
				m = getOrgManager().getMemberById(affairMemberId);
	       
				if(Strings.isNotEmpty(ownerIds) && ownerIds.contains(affairMemberId)) {
		            
					result = "";
		            
					log.info("[代理校验]:类型："+moduleType.getKey()+",当前用户:"+user.getName()+",代理:"+m.getName()+",处理业务数据："+subject);
		       
				}else {
		        	
		        	result = ResourceUtil.getString("coll.summary.validate.lable26");
		        	if(null != moduleType && null != m){
		        		log.info("[代理校验]:类型："+moduleType.getKey()+",你不是当前业务的所属人,也不是当前业务的代理人,无法处理该协同,请联系管理员,谢谢配合!param:当前用户:"+user.getName()+",代理:"+m.getName()+",业务数据:"+subject);
		        	}
		
		
		        }
			} catch (BusinessException e) {
				log.info("",e);
			}
		}
		
		return result;
	}
	
	public static boolean checkAgent(Long affairMemberId,String subject,ModuleType moduleType,boolean isWebAlert,HttpServletRequest request) throws Exception {

			String result = ajaxCheckAgent(affairMemberId, subject, moduleType);
			
			if(Strings.isNotBlank(result)){
				if(isWebAlert){
					webAlertAndClose(AppContext.getRawResponse(), result);
				}
				Enumeration<?> es= request.getHeaderNames();
				StringBuilder stringBuffer= new StringBuilder();
				if(es!=null){
					while(es.hasMoreElements()){
						Object name= es.nextElement();
						String header= request.getHeader(name.toString());
						stringBuffer.append(name+":="+header+",");
					}
					log.warn("request header---"+stringBuffer.toString());
				}
				return false;
			}else{
				return true;
			}

	}

	public static void webAlertAndClose(HttpServletResponse response,String msg) throws IOException {
        WFComponentUtil.webAlertAndClose(response, msg);
    }
	
	public static void webWriteMsg(HttpServletResponse response,String msg) throws IOException{
	    
	    response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(msg);
	}
	
	public static String getAdvancePigeonholeName(Long archiveId,String str){
        String dvancePigeonholeName = "";
        try {
            JSONObject jo = new JSONObject(str);
            String archiveFolder = jo.optString("archiveFieldName", "");
            if(Strings.isNotBlank(archiveFolder)){
                archiveFolder = "\\"+"{"+archiveFolder+"}";
            }
            //调用模板的时候显示的名字
              if(null == getDocApi().getDocResourceName(archiveId)){
                 return "wendangisdeleted";
            }
            dvancePigeonholeName = getArchiveAllNameById(archiveId) + archiveFolder;
        } catch (Exception e) {
            log.error("",e);
        }
        return dvancePigeonholeName;
    }
	
	/**
	 * 获取高级归档的归档路径
	 */
	public static String getAdvancePigeonholeName(Long archiveId,String str,String flag){
		String dvancePigeonholeName = "";
		try {
			JSONObject jo = new JSONObject(str);
			String archiveFolder = jo.optString("archiveFieldName", "");
			if(Strings.isNotBlank(archiveFolder)){
			    archiveFolder = "\\"+"{"+archiveFolder+"}";
			}
			//调用模板的时候显示的名字
			if("template".equals(flag)){
			    String docPath = ColUtil.getArchiveAllNameById(archiveId);
			  if(Strings.isBlank(docPath)){
			     return "wendangisdeleted";
			  }
				dvancePigeonholeName = docPath+archiveFolder;
			}else{
				String archiveFieldValue = jo.optString("archiveFieldValue", "");
			
				String docPath = getDocApi().getDocResourceName(archiveId);
				if(Strings.isBlank(docPath)){
				  dvancePigeonholeName = ResourceUtil.getString("collaboration.project.nothing.label");
				}else{
				  dvancePigeonholeName = docPath+"\\"+archiveFieldValue;
				}
			}
		} catch (Exception e) {
			log.error("",e);
		}
		return dvancePigeonholeName;
	}
	/**
	 * 获取高级归档的json格式
	 */
	public static String getAdvancePigeonhole(String archiveField,String archiveFieldName,String archiveFieldValue,String archiveIsCreate,String archiveKeyword){
		String dvancePigeonholeName = "";
		try {
			JSONObject jo = new JSONObject();
			jo.put("archiveField", archiveField);
			jo.put("archiveFieldName", archiveFieldName);
			jo.put("archiveIsCreate", archiveIsCreate);
			jo.put("archiveFieldValue", archiveFieldValue);
			jo.put("archiveKeyword",archiveKeyword);
			dvancePigeonholeName = jo.toString();
		} catch (JSONException e) {
			log.error("",e);
		}
		return dvancePigeonholeName;
	}
	
	public static ColSummary addOneReplyCounts(ColSummary summary){
		Integer replyCounts = summary.getReplyCounts();
		if(null != replyCounts){
			summary.setReplyCounts(replyCounts.intValue() +1);
		}else{
			summary.setReplyCounts(1);
		}
		return summary;
	}
	public static ColSummary removeOneReplyCounts(ColSummary summary){
		Integer replyCounts = summary.getReplyCounts();
		if(null != replyCounts && replyCounts.intValue() != 1){
			summary.setReplyCounts(replyCounts.intValue() - 1);
		}else{
			summary.setReplyCounts(0);
		}
		return summary;
	}
	
	public static void putImportantI18n2Session(){
		WFComponentUtil.putImportantI18n2Session();
    }
	
    public static boolean isColSummaryFinished(ColSummary summary) {
        if (summary.getState() == null) {
            return false;
        }
        int intValue = summary.getState().intValue();
        return CollaborationEnum.flowState.terminate.ordinal() == intValue || CollaborationEnum.flowState.finish.ordinal() == intValue;
    }

	public static void deleteQuartzJobForNode(CtpAffair affair) {
	    WFComponentUtil.deleteQuartzJobForNode(affair);
	}

	
	public static void deleteQuartzJobForNodes(Collection<CtpAffair> affairs) {
	    WFComponentUtil.deleteQuartzJobForNodes(affairs);
	}

	
	public static Long getAgentMemberId(Long templateId, Long memberId, Date date) throws BusinessException {
	    return WFComponentUtil.getAgentMemberId(templateId, memberId, date);
	}

	/**
	 * 根据文档ID获取归档全路径名称
	 * @param archiveId
	 * @return
	 */
	public static String getArchiveAllNameById(Long archiveId) throws BusinessException {
		String archiveAllName = ResourceUtil.getString("collaboration.project.nothing.label");
		if (AppContext.hasPlugin(ApplicationCategoryEnum.doc.name()) && archiveId != null) {
			DocResourceBO docResourceBO = getDocApi().getDocResource(archiveId);
	    	if(docResourceBO == null){
	    		archiveAllName = "";
	    	}else{
	    		archiveAllName = getDocApi().getPhysicalPath(docResourceBO.getLogicalPath(), "\\", false, 0);
	    	}
		}
		
		return archiveAllName;
	}
	
    public static void deleteCycleRemindQuartzJob(CtpAffair affair, boolean isFinishAll) {
        WFComponentUtil.deleteCycleRemindQuartzJob(affair, isFinishAll);
    }
	/**
     * 设置Affair的运行时长，超时时长，按工作时间设置的运行时长，按工作时间设置的超时时长。
     * @param affair
     * @throws BusinessException
     */
    public static void setTime2Affair(CtpAffair affair, ColSummary summary) throws BusinessException {
        // 工作日计算运行时间和超期时间。
        long runWorkTime = 0L;
        long orgAccountId = summary.getOrgAccountId();
        runWorkTime = getWorkTimeManager().getDealWithTimeValue(affair.getReceiveTime(), new Date(), orgAccountId);
        runWorkTime = runWorkTime / (60 * 1000);
        Long deadline = 0l;
        Long workDeadline = 0l;
        
        if (affair.getExpectedProcessTime() != null || (affair.getDeadlineDate() != null && !Long.valueOf(0).equals(affair.getDeadlineDate()))) {
            if (affair.getDeadlineDate() != null && !Long.valueOf(0).equals(affair.getDeadlineDate())) {
                deadline = affair.getDeadlineDate().longValue();
            }
            else {
                if (DateUtil.currentTimestamp().after(affair.getExpectedProcessTime())) {
                    deadline = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), affair.getExpectedProcessTime(), orgAccountId);
                    deadline = deadline / 1000 / 60;
                }
            }
            workDeadline = getWorkTimeManager().convert2WorkTime(deadline, orgAccountId);
        }
        // 超期工作时间
        Long overWorkTime = 0L;
        // 设置了处理期限才进行计算,没有设置处理期限的话,默认为0;
        if (workDeadline != null && !Long.valueOf(0).equals(workDeadline)) {
            long ow = runWorkTime - workDeadline;
            overWorkTime = ow > 0 ? ow : 0l;
        }

        // 自然日计算运行时间和超期时间
        long runTime = (System.currentTimeMillis() - affair.getReceiveTime().getTime()) / (60 * 1000);
        Long overTime = 0L;
        if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) ||
        		affair.getExpectedProcessTime() != null ) {
            Long o = runTime - deadline;
            overTime = o > 0 ? o : 0l;
        }

        // 避免时间到了定时任务还没有执行。暂时不需要考虑是否在工作时间，因为定时任务那边也没有考虑，先保持一致。
        if (affair.getExpectedProcessTime() != null) {
            if (new Date().after(affair.getExpectedProcessTime())) {
                affair.setCoverTime(true);
            }
        }

        if (affair.isCoverTime() != null && affair.isCoverTime()) {
            if (Long.valueOf(0).equals(overTime)) {
                overTime = 1l;
            }
            if (Long.valueOf(0).equals(overWorkTime)) {
                overWorkTime = 1l;
            }
        }
        affair.setOverTime(overTime);
        affair.setOverWorktime(overWorkTime);
        affair.setRunTime(runTime == 0 ? 1 : runTime);
        affair.setRunWorktime(runWorkTime == 0 ? 1 : runWorkTime);
    }
	/**
	 * 根据文档Id获取归档简称路径
	 * @return
	 * @throws BusinessException
	 */
	public static String getArchiveNameById(Long archiveId) throws BusinessException{
		String archiveName = ResourceUtil.getString("collaboration.project.nothing.label");
		if (AppContext.hasPlugin(ApplicationCategoryEnum.doc.name()) && archiveId != null) {
			archiveName = getDocApi().getDocResourceName(archiveId);
			String archiveAllName = getArchiveAllNameById(archiveId);
	    	if (archiveName != null && !archiveName.equals(archiveAllName)) {
	    		archiveName = "...\\"+archiveName;
	    	}
		}
    	return archiveName;
	}

	public static boolean isForm(int bodyType){
	    return WFComponentUtil.isForm(bodyType);
	}
	
	public static boolean isForm(String bodyType){
        return WFComponentUtil.isForm(bodyType);
    }

	public static boolean canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType dealType,ColSummary summary){
		String mergeDealType = "";
		if(summary!=null){
			mergeDealType = summary.getMergeDealType();
		}
		if(Strings.isNotBlank(mergeDealType)){
			String mergeDealTypeValue= dealType.getValue();
			Map<String,String> mergeDealTypeMap  = (Map<String, String>) JSONUtil.parseJSONString(mergeDealType);
			if(null!=mergeDealTypeMap && !mergeDealTypeMap.isEmpty()){
				return mergeDealTypeValue.equals(mergeDealTypeMap.get(dealType.name()));
			}
		}
		return false;
	}
	
	private static OrgManager getOrgManager(){
        if(orgManager == null){
            orgManager = (OrgManager) AppContext.getBean("orgManager");
        }
        return orgManager;
    }
	
    private static DocApi getDocApi() {
        if (docApi == null) {
            docApi = (DocApi) AppContext.getBean("docApi");
        }
        return docApi;
    }

    private static EnumManager getEnumManager() {
        if (enumManagerNew == null) {
            enumManagerNew = (EnumManager) AppContext.getBean("enumManagerNew");
        }
        return enumManagerNew;
    }
    
    private static WorkTimeManager getWorkTimeManager(){
        if(workTimeManager == null){
            workTimeManager = (WorkTimeManager)AppContext.getBean("workTimeManager");
        }
        return workTimeManager;
    }
    
    
    private static WorkflowApiManager getWorkflowApiManager() {
        if (wapi == null) {
            wapi = (WorkflowApiManager) AppContext.getBean("wapi");
        }
        return wapi;
    }

    private static CollaborationApi getCollaborationApi() {
        if (collaborationApi == null) {
            collaborationApi = (CollaborationApi) AppContext.getBean("collaborationApi");
        }
        return collaborationApi;
    }
    
    private static PrivilegeManager getPrivilegeManager() {
        if (privilegeManager == null) {
            privilegeManager = (PrivilegeManager) AppContext.getBean("privilegeManager");
        }
        return privilegeManager;
    }
    
    private static AffairManager getAffairManager() {
        if (affairManager == null) {
            affairManager = (AffairManager) AppContext.getBean("affairManager");
        }
        return affairManager;
    }
    
    private static CAPFormManager getCapFormManager(){
        if(capFormManager == null){
            capFormManager = (CAPFormManager)AppContext.getBean("capFormManager");
        }
        return capFormManager;
    }
    
    public static String executeWorkflowBeforeEvent(CtpAffair affair,String eventId,String currentNodeLast) {
        String msg = WFComponentUtil.executeWorkflowBeforeEvent(affair, eventId, currentNodeLast,null);
        		
        return msg;
    }
    public static String executeWorkflowBeforeEvent(CtpAffair affair,String eventId,String currentNodeLast,Comment comment) {
    	
    	
    	String msg = WFComponentUtil.executeWorkflowBeforeEvent(affair, eventId, currentNodeLast,comment);
    	return msg;
    }

    public static String getDeadLineName(Date deadlineDatetime) {
        return WFComponentUtil.getDeadLineName(deadlineDatetime);
    }

    public static String getDeadLineName(Long value) {
        return WFComponentUtil.getDeadLineName(value);
    }

    public static String getDeadLineNameForEdoc(Long value, Timestamp createTime) {
        return WFComponentUtil.getDeadLineNameForEdoc(value, createTime);
    }

    public static String getDeadlineDisplayName(CtpAffair affair, boolean isShowToday) {
        return WFComponentUtil.getDeadlineDisplayName(affair, isShowToday);
    }

    public static String getDateTime(Date srcDate, String datetimeStyle) {
        return WFComponentUtil.getDateTime(srcDate, datetimeStyle);
    }

    public static String getAdvanceRemind(String value) {
        return WFComponentUtil.getAdvanceRemind(value);
    }

    public static void deleteQuartzJob(Long summaryId) {
        WFComponentUtil.deleteQuartzJob(summaryId);
    }

    public static Integer getImportantLevel(CtpAffair affair) {
        return WFComponentUtil.getImportantLevel(affair);
    }

    public static boolean isBlank(String val) {
        return WFComponentUtil.isBlank(val);
    }

    public static boolean isNotBlank(String val) {
        return WFComponentUtil.isNotBlank(val);
    }

    public static boolean isLong(String id) {
        return WFComponentUtil.isLong(id);
    }

    public static void affairExcuteRemind(CtpAffair affair, Long summaryAccountId) {
        WFComponentUtil.affairExcuteRemind(affair, summaryAccountId);
    }

    public static void affairExcuteRemind4Node(CtpAffair affair, Long summaryAccountId, Date deadLineRunTime, Date advanceRemindTime) {
        WFComponentUtil.affairExcuteRemind4Node(affair, summaryAccountId, deadLineRunTime, advanceRemindTime);
    }

    public static void affairExcuteRemind(CtpAffair affair, Long summaryAccountId, Date deadLineRunTime, Date advanceRemindTime) {
        WFComponentUtil.affairExcuteRemind(affair, summaryAccountId, deadLineRunTime, advanceRemindTime);
    }

    public static String mergeSubjectWithForwardMembers(String subject, int subjectLength, String forwardMember, Integer resentTime, Locale locale) {
        return WFComponentUtil.mergeSubjectWithForwardMembers(subject, subjectLength, forwardMember, resentTime, locale);
    }

    public static String mergeSubjectWithForwardMembers(String subject, String forwardMember, Integer resentTime, Locale locale, int width) {
        return WFComponentUtil.mergeSubjectWithForwardMembers(subject, forwardMember, resentTime, locale, width);
    }

}
