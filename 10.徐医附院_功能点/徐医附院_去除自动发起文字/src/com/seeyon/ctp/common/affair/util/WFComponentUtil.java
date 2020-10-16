package com.seeyon.ctp.common.affair.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.privilege.manager.PrivilegeManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class WFComponentUtil {
    
    private static Log log = LogFactory.getLog(WFComponentUtil.class);
    private static final String       ISADVANCEREMIND  = "1";
    private static final String       NOTADVANCEREMIND = "0";
    
    private static EnumManager        enumManagerNew;
    private static WorkTimeManager    workTimeManager;
    private static OrgManager         orgManager;
    private static PrivilegeManager   privilegeManager;
    private static AffairManager      affairManager;
    private static WorkflowApiManager wapi;
    
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
    
    private static OrgManager getOrgManager(){
        if(orgManager == null){
            orgManager = (OrgManager) AppContext.getBean("orgManager");
        }
        return orgManager;
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
    
    private static WorkflowApiManager getWorkflowApiManager() {
        if (wapi == null) {
        	wapi = (WorkflowApiManager) AppContext.getBean("wapi");
        }
        return wapi;
    }
    
  //流程期限
    public static String getDeadLineName(Date deadlineDatetime){
        if(null == deadlineDatetime){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }else{
            return Datetimes.formatDatetimeWithoutSecond(deadlineDatetime).toString();
        }
    }
    //节点期限
    public static String getDeadLineName(Long value){
        if(value == null || value == 0){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        EnumManager enumManager = getEnumManager();
        if(enumManager == null){
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        CtpEnumItem cei = enumManager.getEnumItem(EnumNameEnum.collaboration_deadline, value.toString());
        if(cei == null){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        return ResourceUtil.getString(cei.getLabel());
    }

    //获得流程期限--如果流程期限大于0且枚举没有该流程期限，则返回具体的流程期限时间（公文用）
    public static String getDeadLineNameForEdoc(Long value, Timestamp createTime){
        if(value == null || value == 0){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        EnumManager enumManager = getEnumManager();
        if(enumManager == null){
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        CtpEnumItem cei = enumManager.getEnumItem(EnumNameEnum.collaboration_deadline, value.toString());
        if(cei == null){//无
            if(value>0){
                return Functions.showDeadlineTime(createTime.toString(), value);
            }
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        return ResourceUtil.getString(cei.getLabel());
    }
    
    /**
     * 当有具体的处理期限的时间点则返回具体时间点，否则返回相对时间
     * @param affair
     * @param isShowToday
     * @return
     */
    public static String getDeadlineDisplayName(CtpAffair affair,boolean isShowToday){
        String deadlineDisplayName = "";
        if(affair.getExpectedProcessTime()!=null){
            if(isShowToday){
                deadlineDisplayName = getDateTime(affair.getExpectedProcessTime(),"yyyy-MM-dd HH:mm");
            }else{
                deadlineDisplayName = DateUtil.format(affair.getExpectedProcessTime(), "yyyy-MM-dd HH:mm");
            }
        }else{
            deadlineDisplayName = getDeadLineName(affair.getDeadlineDate());
        }
        return deadlineDisplayName ;
    }
    
    /**
     * 对日期进行转换，如果传入的日期与当前系统日期相同则显示成'今日'，时间不变，如果日期与系统日期不相同，则只显示日期<br>
     * 例如当前系统时间为'2013-01-31 12:12:12',传入的日期为'2013-01-31 13:13:13'，则显示成'今日 13:13:13' <br>
     * 例如当前系统时间为'2013-01-31 12:12:12',传入的日期为'2013-01-30 13:13:13'，则显示成'2013-01-30' <br>
     * @param srcDate
     * @param datetimeStyle
     * @return
     */
    public static String getDateTime(Date srcDate, String datetimeStyle){
        
        //找到客户端0点的相对服务器时间
        long todayFirstTime = Datetimes.getTodayFirstTime().getTime();
        
        long todayLastTime = todayFirstTime + 86400000;
        long tomorrowLastTime = todayLastTime + 86400000;
        if(srcDate == null){
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        long srcDateStamp = srcDate.getTime();
        //今天
        if(todayFirstTime <= srcDateStamp && srcDateStamp < todayLastTime){
            String dateTime = Datetimes.format(srcDate, datetimeStyle);
            return ResourceUtil.getString("menu.tools.calendar.today") + dateTime.substring(11);
        }
        //明天
        else if(todayLastTime < srcDateStamp && srcDateStamp < tomorrowLastTime){
            String dateTime = Datetimes.format(srcDate, datetimeStyle);
            return ResourceUtil.getString("menu.tools.calendar.tomorrow") + dateTime.substring(11);
        }
        else{
            return Datetimes.format(srcDate, "yyyy-MM-dd");
        }
    }
    
  //提醒
    public static String getAdvanceRemind(String value){
        if(value == null  || "0".equals(value)){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        EnumManager enumManager = getEnumManager();
        if(enumManager  == null){
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        CtpEnumItem cei = enumManager.getEnumItem(EnumNameEnum.common_remind_time, value);
        if(cei == null){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        return ResourceUtil.getString(cei.getLabel());
    }

    public static void deleteQuartzJob(Long summaryId){
        try {
            if(QuartzHolder.hasQuartzJob("ColProcessDeadLine" + summaryId)){
                QuartzHolder.deleteQuartzJob("ColProcessDeadLine" + summaryId);
                QuartzHolder.deleteQuartzJob("ColProcessRemind" + summaryId);
            }
            //流程超期循环提醒,增加了时间后缀,导致JobName不是固定的,增加通过groupName来删除
            QuartzHolder.deleteQuartzJobByGroup("ColProcessDeadLine" + summaryId);
            
            QuartzHolder.deleteQuartzJob("ColSupervise" + summaryId);
        } catch (Exception e) {
            log.info("调用定时任务出错：", e);
        }
    }
    public static void deleteQuartzJob(Long summaryId,String messageRuleId){
        try {
            if(QuartzHolder.hasQuartzJob("ColProcessDeadLine" + summaryId)){
                QuartzHolder.deleteQuartzJob("ColProcessDeadLine" + summaryId);
                QuartzHolder.deleteQuartzJob("ColProcessRemind" + summaryId);
            }
            if(Strings.isNotBlank(messageRuleId) && QuartzHolder.hasQuartzJob("ColProcessDeadLine" + summaryId+"_"+messageRuleId)){
            	QuartzHolder.deleteQuartzJob("ColProcessDeadLine" + summaryId+"_"+messageRuleId);
                QuartzHolder.deleteQuartzJob("ColProcessRemind" + summaryId);
            }
            //流程超期循环提醒,增加了时间后缀,导致JobName不是固定的,增加通过groupName来删除
            QuartzHolder.deleteQuartzJobByGroup("ColProcessDeadLine" + summaryId);
            
            QuartzHolder.deleteQuartzJob("ColSupervise" + summaryId);
        } catch (Exception e) {
            log.info("调用定时任务出错：", e);
        }
    }
  //重要程度
    public static String getImportantLevel(String value){
        if(value == null){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        EnumManager enumManager = getEnumManager();
        if(enumManager  == null){
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        CtpEnumItem cei = enumManager.getEnumItem(EnumNameEnum.common_importance, value);
        if(cei == null){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        return ResourceUtil.getString(cei.getLabel());
    }
    
    /**
     * 
     * @Title: getImportantLevel    
     * @Description:  获取affair的重要程度
     * @param affairId
     * @return      
     * @return: Integer  
     * @date:   2019年6月29日 下午1:57:05
     * @author: xusx
     * @since   V7.1SP1	       
     * @throws
     */
    public static Integer getImportantLevel(Long affairId) {
    	CtpAffair affair = null;
    	if(affairId!=null) {
    		try {
				affair = getAffairManager().get(affairId);
			} catch (BusinessException e) {
				log.error("", e);
			}
    	}
    	if(affair==null) {
    		return null;
    	}else {
    		return affair.getImportantLevel();
    	}
    }
    
    /**
     * 发送消息 控制
     * @param affair
     * @return
     */
    public static Integer getImportantLevel(CtpAffair affair){
        if(affair == null || affair.getImportantLevel() == null){
            return 1;
        }
        //模板协同
        if(affair.getTempleteId() != null) {
            switch (affair.getImportantLevel()) {
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
            return affair.getImportantLevel();
        }
        return 1;
    }
    
    /**
     * 检测是否是空字符串, 不允许空格 <br>
     * ColUtil.isBlank(null)        = true <br>
     * ColUtil.isBlank("")          = true <br>
     * ColUtil.isBlank(" ")         = true <br>
     * ColUtil.isBlank("null")      = true <br>
     * ColUtil.isBlank("undefined") = true <br>
     * ColUtil.isBlank("bob")       = false <br>
     * ColUtil.isBlank("  bob  ")   = false <br>
     * @param val
     * @return
     */
    public static boolean isBlank(String val){
        if("null".equals(val)){
            return true;
        }
        if("undefined".equals(val)){
            return true;
        }
        return Strings.isBlank(val);
    }
    /**
     * 检测是否是空字符串, 不允许空格 <br>
     * ColUtil.isNotBlank(null)        = false <br>
     * ColUtil.isNotBlank("")          = false <br>
     * ColUtil.isNotBlank(" ")         = false <br>
     * ColUtil.isNotBlank("null")      = false <br>
     * ColUtil.isNotBlank("undefined") = false <br>
     * ColUtil.isNotBlank("bob")       = true <br>
     * ColUtil.isNotBlank("  bob  ")   = true <br>
     * @param val
     * @return
     */
    public static boolean isNotBlank(String val){
        return !isBlank(val);
    }
    
    /**
     * 是否为数字
     * @param id
     * @return
     */
    public static boolean isLong(String id) {
        String regex= "[-]{0,1}[\\d]+?";
        return id.matches(regex);
    }
    
    public static void affairExcuteRemind(CtpAffair affair, Long summaryAccountId) {
        //不需要计算setExpectedProcessTime
        Date deadLineRunTime = affair.getExpectedProcessTime();
        Long remindTime = affair.getRemindDate();
        Date advanceRemindTime = null;
        if (remindTime != null && remindTime != -1 &&  remindTime != 0) {
            advanceRemindTime = getWorkTimeManager().getRemindDate(deadLineRunTime, remindTime);
        }
        affairExcuteRemind(affair,summaryAccountId,deadLineRunTime,advanceRemindTime);
    }


    public static void affairExcuteRemind4Node(CtpAffair affair, Long summaryAccountId,Date deadLineRunTime,Date advanceRemindTime) {
        if (affair.getApp() == ApplicationCategoryEnum.collaboration.key()
                || affair.getApp() == ApplicationCategoryEnum.edoc.key()
                || affair.getApp() == ApplicationCategoryEnum.edocRec.key()
                || affair.getApp() == ApplicationCategoryEnum.edocSend.key()
                || affair.getApp() == ApplicationCategoryEnum.edocSign.key()
                || affair.getApp() == ApplicationCategoryEnum.info.key()) {
            //超期提醒
            try {
                if (deadLineRunTime != null) {
                      Long affairId = affair.getId();
                      {
                          //先删除老的定时任务
                          String jobName = new StringBuilder("DeadLine").append(affair.getObjectId())
                                                                        .append("_")
                                                                        .append(affair.getActivityId())
                                                                        .toString();
                          QuartzHolder.deleteQuartzJob(jobName);
                          
                          String name = jobName;
                          Map<String, String> datamap = new HashMap<String, String>(2);
                          datamap.put("isAdvanceRemind", ISADVANCEREMIND);
                          datamap.put("activityId", String.valueOf(affair.getActivityId()));
                          datamap.put("objectId", String.valueOf(affair.getObjectId()));
                          datamap.put("affairId", String.valueOf(affairId));
                          //Long templateId= affair.getTempleteId();
                          //log.info("templateId:="+templateId);
                          //增加30秒随机数
                          int randomInOneMinte = (int)(Math.random()*30+1)*1000;
                          Date _runDate = new java.util.Date(deadLineRunTime.getTime()+randomInOneMinte);
                          Date now = DateUtil.newDate();
                          // 如果已经超时了,直接在当前时间加30s
                          if(deadLineRunTime.compareTo(now) < 0) {
                              _runDate = new java.util.Date(now.getTime() + 30*1000);
                          } else {
                        	  //执行时间增加30s的随机数
                              _runDate = new java.util.Date(deadLineRunTime.getTime()+randomInOneMinte);
                          }
                          //if(null!=templateId && templateId.longValue()==2447814604579751585l){
                             //log.info("指定模板调试用：3分钟触发超期自动跳过任务:"+templateId);
                            //_runDate = new java.util.Date(System.currentTimeMillis()+180000+randomInOneMinte);//3分钟
                          //}
                          QuartzHolder.newQuartzJob(name,_runDate, "affairIsOvertopTimeJob", datamap);
                          log.info("______创建定时任务：activityId:"+affair.getActivityId()+",objectId："+affair.getObjectId()+",事项名："+affair.getId()+",定时任务name:"+name+",预计执行时间:"+deadLineRunTime);
                      }

                      Long remindTime = affair.getRemindDate();
                      if (remindTime != null && !Long.valueOf(0).equals(remindTime) && !Long.valueOf(-1).equals(remindTime)
                              && advanceRemindTime != null  &&  advanceRemindTime.before(deadLineRunTime)) {
                          
                        //先删除老的定时任务
                          String jobName = new StringBuilder("Remind").append(affair.getObjectId())
                                  .append("_")
                                  .append(affair.getActivityId())
                                  .toString();
                          QuartzHolder.deleteQuartzJob(jobName);
                          
                          String name = "Remind" + affair.getObjectId() + "_" + affair.getActivityId();
                          Map<String, String> datamap = new HashMap<String, String>(2);
                          datamap.put("isAdvanceRemind", NOTADVANCEREMIND);
                          datamap.put("activityId", String.valueOf(affair.getActivityId()));
                          datamap.put("objectId", String.valueOf(affair.getObjectId()));
                          datamap.put("affairId", String.valueOf(affairId));
                          QuartzHolder.newQuartzJob(name, advanceRemindTime, "affairIsOvertopTimeJob", datamap);
                      }
                }
            } catch (Exception e) {
              log.error("获取定时调度器对象失败", e);
            }
        }
    }

    private static boolean checkApp(CtpAffair affair){
      return affair.getApp() == ApplicationCategoryEnum.collaboration.key()
      || affair.getApp() == ApplicationCategoryEnum.edoc.key()
      || affair.getApp() == ApplicationCategoryEnum.edocRec.key()
      || affair.getApp() == ApplicationCategoryEnum.edocSend.key()
      || affair.getApp() == ApplicationCategoryEnum.edocSign.key()
      || affair.getApp() == ApplicationCategoryEnum.info.key();
    }

    public static void affairExcuteRemind(CtpAffair affair, Long summaryAccountId,Date deadLineRunTime,Date advanceRemindTime) {
        if (checkApp(affair)) {
            //超期提醒
            try {
                if (deadLineRunTime != null) {
                      Long affairId = affair.getId();
                      {
                          String name = "DeadLine" + affairId;
                          Map<String, String> datamap = new HashMap<String, String>(2);
                          datamap.put("isAdvanceRemind", "1");
                          datamap.put("affairId", String.valueOf(affairId));
                          QuartzHolder.newQuartzJob(name,deadLineRunTime , "affairIsOvertopTimeJob", datamap);
                      }
                      Long remindTime = affair.getRemindDate();
                      if (remindTime != null && advanceRemindTime != null && remindTime !=-1 && remindTime != 0 && advanceRemindTime.before(deadLineRunTime)) {
                              String name = "Remind" + affairId;
                              Map<String, String> datamap = new HashMap<String, String>(2);
                              datamap.put("isAdvanceRemind", "0");
                              datamap.put("affairId", String.valueOf(affairId));
                              QuartzHolder.newQuartzJob(name, advanceRemindTime, "affairIsOvertopTimeJob", datamap);
                      }
                }
            } catch (Exception e) {
              log.error("获取定时调度器对象失败", e);
            }
        }
    }
    
    /**
     * 此方法针对 我的代理列表功能
     * @param affair
     * @param isProxy
     * @param length
     * @return
     */
    public static String showSubjectOfAffair(CtpAffair affair, Boolean isProxy, int length){
        User user = AppContext.getCurrentUser();
        if(affair == null){
            return null;
        }

        String subject = affair.getSubject();
        if(null != affair.getAutoRun() && affair.getAutoRun()){
    		subject = ResourceUtil.getString("collaboration.newflow.fire.subject",affair.getSubject());
    	}
        
        String proxyName = "";
        if(!affair.getMemberId().equals(user.getId())){
            proxyName = Functions.showMemberNameOnly(affair.getMemberId());
        }
        
		if (affair.getProxyMemberId() != null && !Long.valueOf(0).equals(affair.getProxyMemberId())) {
			try {
				V3xOrgMember m = null;
				if (user.getId().equals(affair.getProxyMemberId())) {//代理人显示(代理XX)
					m = getOrgManager().getMemberById(affair.getMemberId());
				} else {
					m = getOrgManager().getMemberById(affair.getProxyMemberId());//被代理人显示(XX代理)
				}
				if (m != null) {
					isProxy = true;
					proxyName = m.getName();
				}
			} catch (BusinessException e)   {
				log.error("", e);
			}
		}
        
        return showSubject(subject,affair.getForwardMember(),affair.getResentTime(),isProxy,length,proxyName,false);
    }

    public static String showSubject(String subject, String forwardMember,Integer resendTiem, Boolean isProxy, int length, String proxyName,Boolean isAgentDeal){
        if(Strings.isEmpty(proxyName)){
            return mergeSubjectWithForwardMembers(subject, length, forwardMember, resendTiem, null);
        }
        String colProxyLabel = "";
        if(Boolean.TRUE.equals(isProxy)){
            //我的代理列表(我代表别人)，我是代理人
            List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(AppContext.getCurrentUser().getId());
            //代理我的代理人列表，我是被代理人
            List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(AppContext.getCurrentUser().getId());
            boolean agentToFlag = false;
            if(_agentModelList != null && !_agentModelList.isEmpty()){
                agentToFlag = false;
            }else if(_agentModelToList != null && !_agentModelToList.isEmpty()){
                agentToFlag = true;
            }
            if(agentToFlag){
                //(XX)代理
                colProxyLabel = "(" + proxyName + ResourceUtil.getString("collaboration.proxy") + ")";
            }else{
                if(isAgentDeal){      //被代理人自己处理
                    //(XX)处理
                    colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy.deal", proxyName) + ")";
                }else{
                    //代理(XX)
                    colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy") + proxyName + ")";
                }
            }
            length -= colProxyLabel.getBytes().length;
        }
        return mergeSubjectWithForwardMembers(subject, length, forwardMember, resendTiem, null) + colProxyLabel;
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
        if(affair == null){
            return null;
        }
        V3xOrgMember member = null;
        String subject = "";
        String colProxyLabel = "";
        boolean isAgent = true;
        User user = AppContext.getCurrentUser();
        long userId = user.getId();
        if(affair.getTransactorId() != null){//当前登录人就是代理人的时候不需要显示代理信息
            Long memberId = affair.getTransactorId();
            if(memberId.longValue()==userId){
                memberId = affair.getMemberId();
            }else{
                isAgent = false;
            }
            try {
                member = getOrgManager().getMemberById(memberId);
                if(!member.getIsAdmin()){
                  if(member != null){
                    if(isAgent){
                      //代理(XX)
                      colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy") + member.getName() + ")";
                    }else {
                    	Map<String, Object> map = null;
                    	if(Integer.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(affair.getApp())){
                    		map = AffairUtil.getExtProperty(affair);
                    	}
                    	if(map != null && map.get(AffairExtPropEnums.dailu_pishi_mark.toString()) != null 
                    			&& !"".equals(map.get(AffairExtPropEnums.dailu_pishi_mark.toString()))){
                    		//(xx)代录
                     	   colProxyLabel = "(" + member.getName() + ResourceUtil.getString("govdoc.pishi") + ")";
                    	}else{
                            //(XX)代理
                            colProxyLabel = "(" + member.getName() + ResourceUtil.getString("collaboration.proxy") + ")";

                    	}
                    }
                    length -= colProxyLabel.getBytes().length;
                    //subject = mergeSubjectWithForwardMembers(affair.getSubject(), length, affair.getForwardMember(), affair.getResentTime(), null) + colProxyLabel;
                  }
                }
                  subject = mergeSubjectWithForwardMembers(affair.getSubject(), length, affair.getForwardMember(), affair.getResentTime(), null) + colProxyLabel;
                
            } catch (Exception e) {
                log.error("", e);
            }
          }else if(affair.getMemberId() != userId){//代理人已办查看被代理人处理的
            try{
                member = getOrgManager().getMemberById(affair.getMemberId());
                //(XX)处理
                colProxyLabel = "(" + ResourceUtil.getString("collaboration.proxy.deal", member.getName()) + ")";
                length -= colProxyLabel.getBytes().length;
                subject = mergeSubjectWithForwardMembers(affair.getSubject(), length, affair.getForwardMember(), affair.getResentTime(), null) + colProxyLabel;
            }catch(Exception e){
              log.error("", e);
            }
        }else{
            subject = mergeSubjectWithForwardMembers(affair.getSubject(), length, affair.getForwardMember(), affair.getResentTime(), null);
        }
        return subject;
    }
    
    /**
     * 分解转发人、转发次数，用于任务项分配往Affair表写
     *
     * @param subject
     * @param subjectLength
     * @param forwardMember
     * @param resentTime
     * @param orgManager
     * @param locale
     * @return
     */
    public static  String mergeSubjectWithForwardMembers(String subject, int subjectLength, String forwardMember,
            Integer resentTime, Locale locale) {
        StringBuffer sb = new StringBuffer();
        if(resentTime != null && resentTime > 0){
            sb.append(ResourceUtil.getString("collaboration.new.repeat.label", resentTime));
        }
        if(subject != null){
            if(subjectLength==-1 || subjectLength==0){
                sb.append(subject);
            }else{
                sb.append(Strings.getSafeLimitLengthString(subject, subjectLength, "..."));
            }
        }
        if(StringUtils.isNotBlank(forwardMember)) {
            String[] forwardMembers = forwardMember.split(",");
            for (String m : forwardMembers) {
                try {
                    try {
                        long memberId = Long.parseLong(m);
                        V3xOrgMember member =  getOrgManager().getEntityById(V3xOrgMember.class, memberId);
                        sb.append(ResourceUtil.getString("collaboration.forward.subject.suffix", member.getName()));
                    } catch(NumberFormatException nfe) {
                        sb.append(ResourceUtil.getString("collaboration.forward.subject.suffix", m));
                    }
                }
                catch (Exception e) {
                  log.info(e.getLocalizedMessage());
                }
            }
        }
        return sb.toString();
    }
    
    /**
     *  通栏：标题最长显示38个字（不包括图标）,超过就出现省略号
     *  1/2栏：标题最长显示26个字（不包括图标),超过就出现省略号
     *  1/3栏：标题最长显示18个字（不包括图标）,超过就出现省略号
     * @param subject
     * @param forwardMember
     * @param resentTime
     * @param locale
     * @param width -1 代表非首页调用
     * @return
     */
    public static String mergeSubjectWithForwardMembers(String subject, String forwardMember,
            Integer resentTime, Locale locale,int width){
        int length=-1;
        if(width==10){//通栏
            length=78;
        }else if(width==5){//两栏
            length=26*2+4;
        }else if(0<width&&width<5){//多栏
            length=18*2+4;
        }
        return mergeSubjectWithForwardMembers(subject, length, forwardMember, resentTime, locale);
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
    
    /**
     * 判断是否是表单
     * 
     * @param bodyType
     * @return
     *
     * @Since A8-V5 7.0
     * @Author      : xuqw
     * @Date        : 2017年11月1日下午8:23:23
     *
     */
    public static boolean isForm(int bodyType){
        
        return bodyType == MainbodyType.FORM.getKey();
    }
    
    
    /**
     * 判断是否是表单
     * 
     * @param bodyType
     * @return
     *
     * @Since A8-V5 7.0
     * @Author      : xuqw
     * @Date        : 2017年11月1日下午8:23:23
     *
     */
    public static boolean isForm(String bodyType){
        
        if(bodyType == null || !Strings.isDigits(bodyType)){
            return false;
        }
        
        return isForm(Integer.parseInt(bodyType));
    }
    
    public static void putImportantI18n2Session(){
        EnumManager enumManager = getEnumManager();
        List<CtpEnumItem> secretLevelItems =  enumManager.getEnumItems(EnumNameEnum.edoc_urgent_level);
        String i18nValue2 = ResourceUtil.getString("edoc.urgentlevel.pingji");
        if(Strings.isNotEmpty(secretLevelItems)){
            for(CtpEnumItem item : secretLevelItems){
                if("2".equals(item.getValue())){
                    i18nValue2 = ResourceUtil.getString(item.getLabel());
                    break;
                }
            }
        }
        AppContext.putRequestContext("i18nValue2",i18nValue2);
    }
    
    /**
     * 根据affair得到错误提示消息，回退，撤销，取回等
     * @param affair
     * @return
     */
    public static String getErrorMsgByAffair(CtpAffair affair) {
    	return AffairUtil.getErrorMsgByAffair(affair);
    }
    public static String parseCurrentNodesInfo(Date getFinishDate, String currentNodesInfo){
    	return parseCurrentNodesInfo(getFinishDate,currentNodesInfo,null);
    }
    
    public static String parseCurrentNodesInfo(Date getFinishDate, String currentNodesInfo, Integer state) {
        if (null == getFinishDate) {// 流程未结束
            String cninfo = currentNodesInfo;
            StringBuilder currentNodsInfo = new StringBuilder();
            String currentNodesId = "";
            int allCount = 0;
            int count = 0;
            if (Strings.isNotBlank(cninfo) && !"null".equalsIgnoreCase(cninfo)) {
                String[] nodeArr = cninfo.split(";");
                allCount = nodeArr.length;
                for (int index = 0; index < nodeArr.length; index++) {

                    String node = nodeArr[index];

                    if (Strings.isNotBlank(node) && count < 10) {// 显示两个处理人信息
                                                                 // id&nama;id&name
                        if (currentNodesId.indexOf(node) != -1) {// 去重复
                            count--;
                        } else {
                            String userIdStr2 = node;
                            if (node.indexOf("&") != -1) {
                                userIdStr2 = node.split("&")[0];
                            }
                            Long uid = Long.valueOf(userIdStr2);// 节点处理人id

                            String userName = Functions.showMemberName(uid);
                            if (userName != null) {

                                if (currentNodsInfo.length() != 0) {
                                    currentNodsInfo.append("、");
                                }
                                currentNodsInfo.append(userName);
                                currentNodesId += ";" + userIdStr2;
                            } else {
                                // 无效的用户名
                                count--;
                            }
                        }
                    }
                    count++;
                }
            } else {
                return "";
            }

            if ("null".equalsIgnoreCase(currentNodsInfo.toString())) {
                return "";
            }
            String currentStr = currentNodsInfo.toString();
            // 多于2个人拼接...
            if (allCount >= 10 && count != 1) {
                currentStr = currentStr + "...";
            }
            return currentStr;
        } else if(state!= null && state == 1){//流程终止
			return ResourceUtil.getString("collaboration.terminated");
		}
        else {// 流程结束
            return ResourceUtil.getString("collaboration.list.finished.label");
        }
    }
    
    public static Long getAgentMemberId(Long templateId, Long memberId, Date date) throws BusinessException {
        return MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), date, memberId, templateId);
    }
    
    public static Long getEdocAgentMemberId(Long templateId, Long memberId, Date date) throws BusinessException {
        return MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), date, memberId, templateId);
    }
    
    public static void webAlertAndClose(HttpServletResponse response,String msg) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("alert('"+ StringEscapeUtils.escapeEcmaScript(msg)+"');");
        out.println("if(window.parentDialogObj && window.parentDialogObj['dialogDealColl']){");
        out.println(" window.parentDialogObj['dialogDealColl'].close();");
        out.println("}else if(window.dialogArguments){"); //弹出
        out.println("  if(window.dialogArguments.parentDialogObj){");
        out.println("    try{window.dialogArguments.parentDialogObj.close();}catch(e){}");
        out.println("  }else{");
        out.println("    window.close();");
        out.println("  }");
        out.println("}else{");
        out.println(" window.close();");
        out.println("}");
        out.println("</script>");
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
                        
                        result = ResourceUtil.getString("supervise.validate.lable0");
                        
                        if(null != moduleType && null != user && null != m){
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
    
    public static void deleteQuartzJobForNode(CtpAffair affair) {
        boolean isDateLineDate = affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0
                && affair.getDeadlineDate() != -1;
        boolean isRemindDate = affair.getRemindDate() != null && affair.getRemindDate() != 0
                && affair.getRemindDate() != -1;

        if (isDateLineDate) {
            QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
            QuartzHolder.deleteQuartzJob("DeadLine" + affair.getObjectId() + "_" + affair.getActivityId());
        }

        if (isRemindDate && isDateLineDate) {
            QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
            QuartzHolder.deleteQuartzJob("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
        }
        
        //协同流程回退、指定回退操作后删除节点多次提醒任务
        deleteCycleRemindQuartzJob(affair, false);
    }
    
    public static void deleteQuartzJobForNodes(Collection<CtpAffair> affairs) {
        if(Strings.isEmpty(affairs)){
            return;
        }
        Set<String> jobNames = new HashSet<String>();
        for(CtpAffair affair: affairs){

            boolean isDateLineDate = affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0
                    && affair.getDeadlineDate() != -1;
            boolean isRemindDate = affair.getRemindDate() != null && affair.getRemindDate() != 0
                    && affair.getRemindDate() != -1;
            if (isDateLineDate) {
                jobNames.add("DeadLine" + affair.getObjectId() + "_" + affair.getActivityId());
            }
            
            if (isRemindDate && isDateLineDate) {
                jobNames.add("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
            }
            
            //协同流程回退、指定回退操作后删除节点多次提醒任务
            deleteCycleRemindQuartzJob(affair, false);
        }
        
        if(Strings.isNotEmpty(jobNames)){
            for(String jobName : jobNames){
                QuartzHolder.deleteQuartzJob(jobName);
            }
        }
    }
    
    public static void deleteCycleRemindQuartzJob(CtpAffair affair, boolean isFinishAll) {
        
        boolean isDateLineDate = affair.getDeadlineDate() != null && affair.getDeadlineDate().longValue() != 0 && affair.getDeadlineDate() != -1
                || affair.getExpectedProcessTime() != null;

        if (isDateLineDate) {
            String jobName = "CycleRemind_" + affair.getObjectId() + "_" + affair.getActivityId();

            try {
                boolean isDeleteJob = true;
                if (isFinishAll) {
                    List<CtpAffair> affairList = getAffairManager().getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
                    if (Strings.isNotEmpty(affairList)) {
                        for (CtpAffair bean : affairList) {
                            if (bean.getState() != null && bean.getState().intValue() == StateEnum.col_pending.key()
                            		&& !Integer.valueOf(SubStateEnum.col_pending_specialBack.key()).equals(bean.getSubState())
                            		&& !Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.key()).equals(bean.getSubState())) {
                                isDeleteJob = false;
                                break;
                            }
                        }
                    }
                }

                if (isDeleteJob) {

                    log.info("删除节点超期后多次提醒定时任务时间：" + DateUtil.get19DateAndTime() + " jobName=" + jobName);

                    QuartzHolder.deleteQuartzJobByGroup(jobName);
                }
            } catch (Exception e) {
                log.error("删除节点超期后多次提醒定时任务出错：" + DateUtil.get19DateAndTime() + " jobName=" + jobName + "," + e.getLocalizedMessage(), e);
            }
        }
    }

    public static String getStandardDuration(String value){
        if(value == null || "0".equals(value)){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        EnumManager enumManager = getEnumManager();
        if(enumManager  == null){
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        CtpEnumItem cei = enumManager.getEnumItem(EnumNameEnum.collaboration_deadline, value);
        if(cei == null){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        return ResourceUtil.getString(cei.getLabel());
    }
    
    
    public static String executeWorkflowBeforeEvent(CtpAffair affair,String eventId,String currentNodeLast,Comment comment) {
    	return executeWorkflowBeforeEvent(affair, eventId, currentNodeLast, comment,"","","");
    }
    
    public static String executeWorkflowBeforeEvent(CtpAffair affair,String eventId,String currentNodeLast,Comment comment,String selectTargetNodeId,String submitStyleAfterStepBack,String stepBackType) {
    	
    	WorkflowBpmContext context = new WorkflowBpmContext();
    	
    	context.setFormData(affair.getFormRecordid() == null ? null : String.valueOf(affair.getFormRecordid()));
    	context.setMastrid(affair.getFormRecordid() == null ? null : String.valueOf(affair.getFormRecordid()));
    	context.setFormAppId(affair.getFormAppId() == null ? null : String.valueOf(affair.getFormAppId()));
    	context.setFormViewOperation(affair.getMultiViewStr());
    	
    	context.setProcessTemplateId(affair.getProcessId() == null ? null : String.valueOf(affair.getProcessId()));
    	context.setCurrentActivityId(affair.getActivityId() == null ? null : String.valueOf(affair.getActivityId()));
    	context.setProcessId(affair.getProcessId());
    	context.setCaseId(affair.getCaseId() == null ? -1L : affair.getCaseId());
    	context.setCurrentNodeLast(currentNodeLast);
    	context.setCurrentWorkitemId(affair.getSubObjectId() == null ? -1L : affair.getSubObjectId());
    	
    	context.setBussinessId(affair.getObjectId() == null ? null : String.valueOf(affair.getObjectId()));
    	context.setAffairId(String.valueOf(affair.getId()));
    	context.setAppName(affair.getAppEnumStr());
    	context.setCurrentUserId(affair.getMemberId() == null ? null : String.valueOf(affair.getMemberId()));
    	
    	//指定回退相关参数
    	context.setSelectTargetNodeId(selectTargetNodeId);
    	context.setSubmitStyleAfterStepBack(submitStyleAfterStepBack);
    	
    	String attitude = "";
    	String content = "";
    	if (comment != null) {
            attitude = comment.getExtAtt1();
            content = comment.getContent();
        }
        context.setBusinessData(WorkflowEventData.WORKFLOW_EVENT_ATTRITUDE, attitude);
        context.setBusinessData(WorkflowEventData.WORKFLOW_EVENT_COMMENT_CONTENT, content);
        context.setBusinessData(WorkflowEventData.WORKFLOW_EVENT_STEP_BACK_TYPE, stepBackType);
    	String msg = getWorkflowApiManager().executeWorkflowBeforeEvent(eventId, context); //"BeforeFinishWorkitem"
    	return msg;
    }
    
}
