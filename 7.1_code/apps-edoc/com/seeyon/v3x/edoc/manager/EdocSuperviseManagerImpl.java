package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.quartz.QuartzListener;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.po.CtpSuperviseLog;
import com.seeyon.ctp.common.supervise.po.CtpSuperviseReceiver;
import com.seeyon.ctp.common.supervise.vo.CtpSuperviseVO;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.dao.EdocSuperviseDetailDao;
import com.seeyon.v3x.edoc.dao.EdocSuperviseLogDao;
import com.seeyon.v3x.edoc.dao.EdocSuperviseRemindDao;
import com.seeyon.v3x.edoc.dao.EdocSupervisorDao;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSuperviseRemind;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.EdocSuperviseDealModel;

public class EdocSuperviseManagerImpl implements EdocSuperviseManager {
    static Log LOGGER = LogFactory.getLog(EdocSuperviseManagerImpl.class);
    private EdocSuperviseDetailDao edocSuperviseDetailDao;
    private EdocSuperviseLogDao edocSuperviseLogDao;
    private EdocSupervisorDao edocSupervisorDao;
    private EdocSuperviseRemindDao edocSuperviseRemindDao;
    private EdocSummaryDao edocSummaryDao;
    private OrgManager orgManager;    
    private EnumManager enumManagerNew;
    public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

    private UserMessageManager userMessageManager;
    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    private AffairManager affairManager;

    

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    private SuperviseManager superviseManager;

 

    
    public void createSuperviseRemind(EdocSuperviseRemind edocSuperviseRemind){
        
        edocSuperviseRemindDao.save(edocSuperviseRemind);
        
    }

    public EdocSuperviseDetailDao getEdocSuperviseDetailDao() {
        return edocSuperviseDetailDao;
    }

    public void setEdocSuperviseDetailDao(
            EdocSuperviseDetailDao edocSuperviseDetailDao) {
        this.edocSuperviseDetailDao = edocSuperviseDetailDao;
    }

    public EdocSuperviseLogDao getEdocSuperviseLogDao() {
        return edocSuperviseLogDao;
    }

    public void setEdocSuperviseLogDao(EdocSuperviseLogDao edocSuperviseLogDao) {
        this.edocSuperviseLogDao = edocSuperviseLogDao;
    }

    public EdocSuperviseRemindDao getEdocSuperviseRemindDao() {
        return edocSuperviseRemindDao;
    }

    public void setEdocSuperviseRemindDao(
            EdocSuperviseRemindDao edocSuperviseRemindDao) {
        this.edocSuperviseRemindDao = edocSuperviseRemindDao;
    }

    public EdocSupervisorDao getEdocSupervisorDao() {
        return edocSupervisorDao;
    }

    public void setEdocSupervisorDao(EdocSupervisorDao edocSupervisorDao) {
        this.edocSupervisorDao = edocSupervisorDao;
    }
    
    
    /**
     * 更新督办集合
     * @throws BusinessException 
     */
    /*public void updateAllDetail(List list) throws BusinessException{
        if(list!=null){
            for(int i=0;i<list.size();i++){
                superviseDao.update(list[i]);
            }
        }
    }*/
    
    /**
     * 根据督办的id,查找该督办的所有日志
     * @throws BusinessException 
     */
    public List<CtpSuperviseLog> findLogById(FlipInfo flipInfo, Long superviseId) throws BusinessException{
    	
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("superviseId", superviseId);

        return (List<CtpSuperviseLog>) superviseManager.getLogByDetailId(flipInfo,map);
    }
    
    /**
     * 公文督办,参数由前台传入
     * @param remindMode 提醒方式
     * @param supervisorMemberId 督办人员
     * @param supervisorNames 督办人员的名称
     * @param superviseDate 督办的期限
     * @param SUMMARYID 公文的Id
     * @throws BusinessException 
     */
    @Override
    public void supervise(String title,String supervisorMemberId,String supervisorNames,String superviseDate,EdocSummary summary) throws BusinessException{
        User user = AppContext.getCurrentUser();
        
        long summaryId = summary.getId();

        if(null!=supervisorMemberId && !"".equals(supervisorMemberId)){
            
            boolean bool = false;  //处理时是否为第一次督办
            CtpSuperviseDetail detail = superviseManager.getSupervise(summaryId);
            String sNames = supervisorNames;
            List<MessageReceiver> deleteReceivers = new ArrayList<MessageReceiver>();
            StringBuilder orgMemberIds = new StringBuilder();
            if(null==detail){
                bool = true;  //第一次督办
                detail = new CtpSuperviseDetail();
                detail.setIdIfNew();
                detail.setRemindMode(1);
                detail.setCreateDate(new Date(System.currentTimeMillis()));
                detail.setSenderId(user.getId());
                detail.setStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_SUPERVISE_PROGRESSING);
                detail.setCount(0L);
                detail.setSupervisors(sNames);
                detail.setDescription(null);
                detail.setEntityId(summaryId);
            }else{
                List<CtpSupervisor> delSupervisors = superviseManager.getSupervisors(detail.getId());
                for(CtpSupervisor sor:delSupervisors){
                    orgMemberIds.append(sor.getSupervisorId().toString());
                    orgMemberIds.append(",");
                    boolean boo =supervisorMemberId.contains(sor.getSupervisorId().toString());
                    if(!boo){
                        MessageReceiver receiver = new MessageReceiver(detail.getId(), Long.valueOf(sor.getSupervisorId()));//因为是删除，不采取链接的方式
                        deleteReceivers.add(receiver);   
                        continue;
                    }
                }
                    String scheduleProp = detail.getScheduleProp();
                    if(null!=scheduleProp){
                        String[] scheduleProp_Array = scheduleProp.split("\\|");
                        if(null!=scheduleProp_Array && scheduleProp_Array.length > 0){
                            try{
                                Scheduler sched = QuartzListener.getScheduler();
                                QuartzHolder.deleteQuartzJobByGroupAndJobName(scheduleProp_Array[1], scheduleProp_Array[0]);
                                //删除队列中的消息提醒，下面将会重新生成。
                            }catch(Exception e){
                                LOGGER.error("删除督办期限提醒消息的计时器出错");
                            }
                        }
                    }
            }
            
            detail.setAwakeDate(Datetimes.parse(superviseDate,Datetimes.datetimeStyle));

            List<CtpSupervisor> supervisors = null;
            /*StringBuilder newSupervisorIds = new StringBuilder("");*/
           /*if(!bool){
                supervisors = superviseManager.getSupervisors(detail.getId());
            }*/
           
           if(null == supervisors){
               supervisors = new ArrayList<CtpSupervisor>();
           }
            
            if(supervisorMemberId.endsWith(",")){
                supervisorMemberId = supervisorMemberId.substring(0, supervisorMemberId.length()-1);
            }
            String[] spArray = supervisorMemberId.split(",");
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            
            String tempStr = orgMemberIds.toString();
            for(String s:spArray){
            	CtpSupervisor supervisor = new CtpSupervisor();
            	supervisor.setIdIfNew();
            	supervisor.setSuperviseId(detail.getId());
            	supervisor.setSupervisorId(Long.valueOf(s));
            	supervisor.setPermission(com.seeyon.v3x.edoc.util.Constants.EDOC_SUPERVISOR_PERMISSION_CHANGE);
            	supervisor.setEntityId(summaryId);
            	supervisors.add(supervisor);
            	boolean boo =tempStr.contains(s);
                if(!boo){
                    /*newSupervisorIds.append(s);
                    newSupervisorIds.append(",");*/
                    MessageReceiver receiverA = new MessageReceiver(detail.getId(), supervisor.getSupervisorId(),"message.link.edoc.supervise.detail",detail.getEntityId());
                    receivers.add(receiverA);
                }
            }
            
            try {
                superviseManager.deleteSupervisorsByDetailId(detail.getId());
            }
            catch (Exception e1) {
                LOGGER.error("", e1);
            }
            edocSupervisorDao.saveAll(supervisors);
            detail.setSupervisors(sNames);
            
            try {
                Scheduler sched = QuartzListener.getScheduler();
                Long jobId = UUIDLong.longUUID();
                String jobName = jobId.toString();
                Long groupId = UUIDLong.longUUID();
                String groupName = groupId.toString();
                Long triggerId = UUIDLong.longUUID();
                String triggerName = triggerId.toString();
                if(null  ==  detail.getAwakeDate()){
                    detail.setAwakeDate(new Date());
                }
                
                Map<String, String> parameterMap = new HashMap<String, String>();
                parameterMap.put("colSuperviseId", String.valueOf(detail.getId()));
                parameterMap.put("senderId", String.valueOf(user.getId()));
                parameterMap.put("supervisorMemberId", supervisorMemberId.toString());
                parameterMap.put("subject", summary.getSubject());
                QuartzHolder.newQuartzJob(groupName, jobName, detail.getAwakeDate(), "terminateColSuperviseJob", parameterMap);

                String scheduleProp = jobName + "|" + groupName;
                detail.setScheduleProp(scheduleProp);
            }catch(SchedulerException e) {
                LOGGER.error(e);
            }
  
            CtpAffair affair = affairManager.getSenderAffair(Long.valueOf(summaryId));
            
            if(affair != null){
                detail.setApp(affair.getApp());
                detail.setSubject(affair.getSubject());
                detail.setEntitySenderId(affair.getSenderId());
                detail.setEntityCreateDate(affair.getCreateDate());
                detail.setImportantLevel(affair.getImportantLevel());
                detail.setResentTime(affair.getResentTime());
                detail.setForwardMember(affair.getForwardMember());
                detail.setBodyType(affair.getBodyType());
                detail.setIdentifier(affair.getIdentifier());
                detail.setAffairId(affair.getId());
                detail.setExtProps(affair.getExtProps());
                detail.setCoverTime(affair.isCoverTime());
                detail.setTempleteId(affair.getTempleteId());
            }
            
            String forwardMemberId = affair != null ? affair.getForwardMember() : null;
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if(Strings.isNotBlank(forwardMemberId)){
                try {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                }
                catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
            try {
                ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.hasten", summary.getSubject(), user.getName(), forwardMemberFlag, forwardMember), app, user.getId(), receivers, EdocMessageFilterParamEnum.supervise.key);
                if (deleteReceivers.size() != 0) {
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.delete", summary.getSubject(), user.getName(), app.key()), app, user.getId(), deleteReceivers, EdocMessageFilterParamEnum.supervise.key);//给被删除的人发消息
                }
            } catch (Exception e) {
                LOGGER.error("给督办人发消息失败!");
            }
            detail.setTitle(title);
            detail.setEntityType(SuperviseEnum.EntityType.edoc.ordinal());
            if(bool){
                superviseManager.save(detail);
            }else{
            	superviseManager.update(detail);
            }
        }        
    }

//    /**
//     * 彻底删除督办项及所属的督办人
//     */
    public void deleteSuperviseDetailAndSupervisors(EdocSummary summary)throws EdocException{
            if(summary != null){
                long summaryId = summary.getId();
                CtpSuperviseDetail detail = null;
                try {
                    detail = superviseManager.getSupervise(summaryId);
                } catch (BusinessException e1) {
                    LOGGER.error("", e1);
                }
                if(null!=detail){
                    //给督办人发消息 
                    List<CtpSupervisor> supervisors = null;
                    supervisors = superviseManager.getSupervisors(detail.getId());
                    List<MessageReceiver> deleteReceivers = null;
                    if(supervisors != null) { 
                        deleteReceivers = new ArrayList<MessageReceiver>();
                        for(CtpSupervisor supervisor:supervisors) {
                            //删除督办人，不需要显示链接
                            MessageReceiver deleteReceiver = new MessageReceiver(detail.getId(), supervisor.getSupervisorId());
                            deleteReceivers.add(deleteReceiver);
                        }
                    }
                    if(detail.getId()!=null){
                        try {
                            superviseManager.deleteSupervised(detail.getId().longValue());
                        } catch (BusinessException e) {
                            LOGGER.error("", e);
                        }
                    }
                    if(Strings.isNotEmpty(deleteReceivers) && summary != null) {
                        ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                        try {
                            if(!summary.getHasArchive())
                                //{2,choice,1#协同|4#公文|19#发文|20#收文|21#签报}《{0}》的督办权限已被 {1} 撤销
                                userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.delete",summary.getSubject(),AppContext.getCurrentUser().getName(),app.ordinal()), app, AppContext.getCurrentUser().getId(), deleteReceivers,EdocMessageFilterParamEnum.supervise.key);
                            else//{1,choice,1#协同|4#公文|19#发文|20#收文|21#签报}《{0}》已归档,督办权限被撤销
                                userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.pigeonholed",summary.getSubject(),app.ordinal()), app, AppContext.getCurrentUser().getId(), deleteReceivers,EdocMessageFilterParamEnum.supervise.key);
                        }catch(Exception e) {
                            LOGGER.error("",e);
                        }
                    }
                }
            }
    }
    
    /**
     * 更新督办记录
     */
    public void changeSuperviseDetail(CtpSuperviseDetail detail){
    	superviseManager.update(detail);
    }

    public CtpSuperviseDetail getSuperviseById(Long id){
        try {
            return superviseManager.get(id);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        return null;
    }
    
    /**
     * 公文归档后对督办的处理
     */
    public void pigeonhole(EdocSummary summary)throws EdocException{
        
            this.deleteSuperviseDetailAndSupervisors(summary);
    }

    public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
        this.edocSummaryDao = edocSummaryDao;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public CtpSuperviseDetail getSuperviseBySummaryId(long summaryId){
        try {
            return superviseManager.getSupervise(summaryId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        return null;
    }

    public String checkColSupervisor(Long summaryId, CtpAffair senderAffair){
        /**
         * wangwei注解
         *由于affairManager中提供的getSenderAffair()是和协同公用一个方法，
         * 查询结果包含了假删除的数据，此处验证的数据是待发中的数据，此处验证恢复原来的样子
         */
        //if(senderAffair == null || senderAffair.getState() == StateEnum.col_waitSend.key()){
        if(senderAffair == null){
            return ResourceUtil.getString("edoc.delete.non.supervise.cancel");
        } else {
            
            boolean flag = !Integer.valueOf(SubStateEnum.col_pending_specialBack.key() ).equals(senderAffair.getSubState())
                           && !Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.key() ).equals(senderAffair.getSubState() )
                           && !Integer.valueOf(SubStateEnum.col_pending_specialBacked.key() ).equals(senderAffair.getSubState() );
            
            
            if(flag && Integer.valueOf(StateEnum.col_waitSend.key()).equals(senderAffair.getState())){
                String m =ResourceUtil.getString("edoc.delete.non.supervise");//公文被删除，不能进行督办
                return m;
            }
        } 
       
        boolean currentUserIdSupervisor = true;
        try {
            currentUserIdSupervisor = superviseManager.isSupervisor(AppContext.currentUserId(), summaryId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        
        if(!currentUserIdSupervisor){
            return ResourceUtil.getString("edoc.delete.non.supervise.nome");//公文取消了您的督办权限，不能进行督办
        }
        
        return null;
    }

    /**
     * Ajax前台调用。
     * @param summaryId
     * @return
     */
    public String isSupervisorOfOneSummary(String summaryId){
        Long edocSummaryId=0L;
        if(Strings.isNotBlank(summaryId)) 
            edocSummaryId=Long.parseLong(summaryId);
        CtpAffair affair = null;
        try {
            
            /**
             * wangwei注解
             * getSenderAffair()和协同公用一个方法，查询结果包含了假删除的数据，不适应迁移公文
             * getEdocSenderAffair()公文重写适应迁移功能能，查询结果过滤假删除的数据进行验证
             */
            //affair = affairManager.getSenderAffair(edocSummaryId);
            affair = affairManager.getSenderAffair(edocSummaryId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        String m = checkColSupervisor(edocSummaryId, affair);
        if(m==null)
            return "1";
        return "0";
    }
    
    //催办
    public void sendMessage(Long superviseId,String mode,String processId, String activityId, String additional_remark, long[] people,String summaryId){
        
        additional_remark = additional_remark == null ? "" : ResourceUtil.getString("sender.note.label") + ":" + additional_remark;
        additional_remark = "";
        
        User user = AppContext.getCurrentUser();

        Set<Long> memberIds = new HashSet<Long>();
        for (Long l : people) {
            memberIds.add(l);
        }
        
        List<CtpAffair> affairs = null;
        try {
            affairs = affairManager.getAffairs(Long.parseLong(summaryId));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        
        if(affairs != null && !affairs.isEmpty()){
            CtpAffair hastenAffair = affairs.get(0);
            Set<Long> existMemberIds = new HashSet<Long>();
            try {
                Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                for (CtpAffair affair : affairs) {
                    Long memberId = affair.getMemberId();
                    
                    if(!memberIds.contains(memberId) || existMemberIds.contains(memberId)){
                        continue;
                    }
                    //过滤掉待发送的公文。
                    if(affair.getApp()==22) continue; 
                    
                    existMemberIds.add(memberId);
                    
                    Integer hastenTimes = affair.getHastenTimes();
                    if (hastenTimes == null)
                        hastenTimes = 0;
                    hastenTimes += 1;
                    affair.setHastenTimes(hastenTimes);
                    affairManager.updateAffair(affair);
                    hastenAffair=affair;
        
                    receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(),"message.link.edoc.pending",affair.getId().toString()));
                }
                String subject = hastenAffair.getSubject();
                Integer importantLevel = hastenAffair.getImportantLevel();
                superviseManager.saveDbLog(superviseId);
                
                CtpSuperviseLog superviseLog = new CtpSuperviseLog();
                superviseLog.setIdIfNew();
                superviseLog.setSender(user.getId());
                superviseLog.setSendTime(new Date());
                superviseLog.setSuperviseId(superviseId);
                superviseLog.setType(SuperviseEnum.suerviseLogType.hasten.ordinal());
                superviseLog.setContent(additional_remark);
                Set<CtpSuperviseReceiver> set = new HashSet<CtpSuperviseReceiver>();
                for(long receiverId : people) {
                    CtpSuperviseReceiver receiver = new CtpSuperviseReceiver();
                    receiver.setIdIfNew();
                    receiver.setLogId(superviseLog.getId());
                    receiver.setReceiver(receiverId);
                    set.add(receiver);
                }
                superviseManager.save(superviseLog);                
                userMessageManager.sendSystemMessage(new MessageContent("edoc.hasten", subject, user.getName(), additional_remark,hastenAffair.getApp()),
                        ApplicationCategoryEnum.valueOf(hastenAffair.getApp()), user.getId(), receivers, EdocMessageFilterParamEnum.supervise.key);
            }catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        //SuperviseDetail 中的催办次数增1
    }
    
    @Override
    public void updateBySummaryId(long summaryId){
        try {
        	superviseManager.updateEdocStatusBySummaryId(summaryId);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public void superviseForTemplate(String remindMode,
            String supervisorMemberId, String supervisorNames,
            String superviseDate, EdocSummary summary, String title)//当发送公文时设置了督办人，传入affairId
                                                                                    //传入可变参数的原因是不影响其他方法调用该方法
            throws Exception {
        User user = AppContext.getCurrentUser();

        if(Strings.isBlank(supervisorMemberId)){
            
            boolean bool = false;  //处理时是否为第一次督办
            CtpSuperviseDetail detail = superviseManager.getSupervise(summary.getId());
            
            if(null==detail){
                bool = true;  //第一次督办
                detail = new CtpSuperviseDetail();
                detail.setIdIfNew();
                detail.setRemindMode(Integer.valueOf(remindMode));
                detail.setCreateDate(new Date(System.currentTimeMillis()));
                detail.setSenderId(user.getId());
                detail.setCount(0L);
                detail.setDescription(null);
                detail.setEntityId(summary.getId());
            }else{
                edocSuperviseDetailDao.delete(CtpSupervisor.class, new Object[][]{{"superviseId", detail.getId()}});
                String scheduleProp = detail.getScheduleProp();
                if(null!=scheduleProp){
                    String[] scheduleProp_Array = scheduleProp.split("\\|");
                    if(null!=scheduleProp_Array && scheduleProp_Array.length > 0){
                        try{
                            QuartzHolder.deleteQuartzJobByGroupAndJobName(scheduleProp_Array[1], scheduleProp_Array[0]);
                        }catch(Exception e){
                            LOGGER.error("删除督办期限提醒消息的计时器出错");
                        }
                    }
                }
            }
            
            detail.setAwakeDate(Datetimes.parse(superviseDate,Datetimes.datetimeStyle));
            detail.setSupervisors(supervisorNames);

            if(supervisorMemberId.endsWith(",")){
                supervisorMemberId = supervisorMemberId.substring(0, supervisorMemberId.length()-1);
            }
            String[] spArray = supervisorMemberId.split(",");;
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            StringBuilder names = new StringBuilder("");
            Long entityId = detail.getEntityId();
            for(String s:spArray){
                CtpSupervisor supervisor = new CtpSupervisor();
                supervisor.setIdIfNew();
                supervisor.setSuperviseId(detail.getId());
                supervisor.setSupervisorId(Long.valueOf(s));
                supervisor.setPermission(com.seeyon.v3x.edoc.util.Constants.EDOC_SUPERVISOR_PERMISSION_CHANGE);
                supervisor.setEntityId(entityId);
                superviseManager.save(supervisor);
                
                MessageReceiver receiver1 = null;
                //OA-16219 拟文，设置督办，督办人员登录收到督办提示，单击督办提示，报异常
                receiver1 = new MessageReceiver(detail.getId(), supervisor.getSupervisorId(),"message.link.edoc.supervise.detail",detail.getEntityId());
                receivers.add(receiver1);
                try{
                    V3xOrgMember member = this.orgManager.getMemberById(Long.valueOf(s));
                    if(member!=null){
                        names.append(member.getName());
                        names.append(",");
                    }    
                }catch(Exception e){
                    LOGGER.error("得到督办人实体错误 : ",e);
                    continue;
                }
            }
            
            String fNames = "";
            if(names.toString().endsWith(",")){
                fNames = names.substring(0, names.length()-1);
            }
            detail.setSupervisors(fNames);
            
            try {
                Scheduler sched = QuartzListener.getScheduler();
                String jobName = "EdocSupervise"+summary.getId();
                Long groupId = UUIDLong.longUUID();
                String groupName = groupId.toString();
                Long triggerId = UUIDLong.longUUID();
                String triggerName = triggerId.toString();
        
                Map<String, String> parameterMap = new HashMap<String, String>();
                parameterMap.put("colSuperviseId", String.valueOf(detail.getId()));
                parameterMap.put("senderId", String.valueOf(user.getId()));
                parameterMap.put("supervisorMemberId", supervisorMemberId.toString());
                parameterMap.put("subject", summary.getSubject());
                QuartzHolder.newQuartzJob(groupName, jobName, detail.getAwakeDate(), "terminateColSuperviseJob", parameterMap);

                
                String scheduleProp = jobName + "|" + groupName;
                detail.setScheduleProp(scheduleProp);
            }catch(Exception e) {
                LOGGER.error("",e);
            }
            
            try{
                ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.hasten",summary.getSubject(),user.getName(), 0,user.getName()), app, user.getId(), receivers,EdocMessageFilterParamEnum.supervise.key);
            }catch(Exception e){
                LOGGER.error("给督办人发消息失败!");
            }
            detail.setTitle(title);
            detail.setStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_SUPERVISE_PROGRESSING);
            detail.setEntityType(SuperviseEnum.EntityType.edoc.ordinal());
            
            if(bool){
                superviseManager.save(detail);
            }else{              
            	superviseManager.update(detail);
            }
        }    
        
    }

    
    
    @Override
    public boolean ajaxCheckIsSummaryOver(Long summaryId) {
        EdocSummary edocSummary = edocSummaryDao.get(summaryId);
        if(null!=edocSummary){
            if(edocSummary.getFinished()){
            return true;
        }
    }
        return false;
    }


    @Override
    public long saveForTemplate(int importantLevel, String summarySubject,String title,long senderId,String senderName,String supervisorNames
            ,long[] supervisorIds,long superviseDate,int entityType,long entityId,boolean sendMessage) {
        CtpSuperviseDetail detail = new CtpSuperviseDetail();
        detail.setIdIfNew();
        detail.setTitle(title);
        detail.setSenderId(senderId);
        detail.setStatus(EdocConstant.superviseState.supervising.ordinal());
        detail.setSupervisors(supervisorNames);
        detail.setCount(0l);
        detail.setTemplateDateTerminal(superviseDate);
        //detail.setCanModify(canModify);
        detail.setEntityType(entityType);
        detail.setEntityId(entityId);
        
        if(supervisorIds != null) {
            for(Long supervisorId:supervisorIds) {
                CtpSupervisor colSupervisor = new CtpSupervisor();
                colSupervisor.setIdIfNew();
                colSupervisor.setSuperviseId(detail.getId());
                colSupervisor.setSupervisorId(supervisorId);
                colSupervisor.setEntityId(entityId);
                superviseManager.save(colSupervisor);
            }
        }
        superviseManager.save(detail);
        return detail.getId();
    }
    public void saveSuperviseTemplateRole(long templateId, String supervisors){
        String[] strs = supervisors.split(",");
        for(String str : strs){
            if(!Strings.isBlank(str)){
            CtpSuperviseTemplateRole role = new CtpSuperviseTemplateRole();
            role.setIdIfNew();
            role.setSuperviseTemplateId(templateId);
            role.setRole(str);
            superviseManager.save(role);
            }
        }
    }
    public void updateForTemplate(int importantLevel, String summarySubject,String title,long senderId,String senderName
            ,String supervisorNames,long[] supervisorIds,long superviseDate,int entityType,long entityId,boolean sendMessage) throws BusinessException {
        CtpSuperviseDetail detail = superviseManager.getSupervise(entityType, entityId);
        if(detail != null) {
        	superviseManager.deleteSupervised(detail.getId());
        }
        this.saveForTemplate(importantLevel, summarySubject, title, senderId, senderName, supervisorNames, supervisorIds, superviseDate, entityType, entityId, sendMessage);
    }
    public void updateSuperviseTemplateRole(long templateId, String supervisors) throws BusinessException{
    	superviseManager.deleteAllTemplateRole(templateId);
        if(supervisors != null && !"".equals(supervisors))
            this.saveSuperviseTemplateRole(templateId, supervisors);
    }
    public int getHastenTimes(long superviseId) {
        try {
            return this.superviseManager.getHastenTimes(superviseId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        return 0;
    }
    
     /**
     * 督办人删除自己督办人权限的方法,只删除自己,督办及其他督办人保留
     */
    public void deleteSuperviseDetail(String superviseIds){
        User user = AppContext.getCurrentUser();
        if(superviseIds == null || "".equals(superviseIds))
            return;
        String[] ids = superviseIds.split(",");
        if(ids != null) {
            List<Long> longIds = new ArrayList<Long>();
            for(String id:ids) {
                longIds.add(Long.parseLong(id)) ;
            }
            Map<String, Object> nameParameters = new HashMap<String,Object>();
            nameParameters.put("superviseIds", longIds);
            try {
                superviseManager.deleteSupervised(user.getId(),nameParameters);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
        }
    }
    
    public List<EdocSuperviseDealModel> getAffairModel(long summaryId){
        EdocSummary summary = edocSummaryDao.get(summaryId);
        ApplicationSubCategoryEnum subApp = EdocUtil.getSubAppCategoryByEdocType(summary.getEdocType());
        if(null!=summary){
            Map conditions = new LinkedHashMap();
            conditions.put("objectId", summaryId);
            conditions.put("state", StateEnum.col_done.key());
            conditions.put("app", ApplicationCategoryEnum.edoc.key());
            conditions.put("subApp", subApp.key());
            conditions.put("isDelete", false);
            CtpEnumBean edocTypeEnum = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name());
            List<CtpAffair> affairs = null;
            try {
                affairs = affairManager.getByConditions(null, conditions);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
        if(affairs == null)
            return null;
        CtpEnumBean deadlineMeta = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name());
        List<CtpEnumItem> itms = deadlineMeta.getItems();
        Map<String,String> map = new HashMap<String,String>();
        for(CtpEnumItem item:itms)
            map.put(item.getValue(), item.getLabel());
        String bundleName = "com.seeyon.v3x.collaboration.resources.i18n.CollaborationResource";;
        String keyGood =  "col.supervise.dealline.good";
        String keyBad = "col.supervise.dealline.bad";
        List<EdocSuperviseDealModel> models = new ArrayList<EdocSuperviseDealModel>();
        String policyName = "";
        for(CtpAffair affair:affairs) {
            EdocSuperviseDealModel model = new EdocSuperviseDealModel();
            Date receiveDate = affair.getReceiveTime();
            Date computeDate = affair.getCompleteTime();
            Long deallineDate = affair.getDeadlineDate();
            model.setDealUser(affair.getMemberId());
            model.setReveiveDate(receiveDate);
            model.setDealDate(computeDate);
            model.setHastened(affair.getHastenTimes());
            if(deallineDate != null)
                model.setDealLine(map.get(deallineDate.toString()));
            model.setEfficiency(ResourceBundleUtil.getString(bundleName, keyGood));
            if(computeDate != null) {
                long[] dates = Datetimes.detailInterval(receiveDate, computeDate);
                model.setDealDays(this.timePatchwork(dates[0],dates[1],dates[2],dates[3],false));
                if(deallineDate != null && deallineDate>0 && computeDate.getTime()-receiveDate.getTime()>deallineDate*60000) {
                    model.setEfficiency(ResourceBundleUtil.getString(bundleName, keyBad));
                    model.setOverTime(true);
                }
            }else {
                Date today = new Date();
                if(deallineDate != null && deallineDate>0 && today.getTime()-receiveDate.getTime()>deallineDate*60000) {
                    model.setEfficiency(ResourceBundleUtil.getString(bundleName, keyBad));
                    model.setOverTime(true);
                }
            }
            model.setPolicyName(policyName);
            models.add(model);
        }
        return models;
        }else{
            return null;
        }
    }
    /**
     * 时间拼装
     *
     * @author jincm 2008-3-30
     * @param day
     * @param hour
     * @param minute
     * @param currentAction
     * @param second
     * @param filterFlag //过滤秒的标记
     * @return boolean
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
 public List<CtpSupervisor> listSupervies(Long detailId){
	 return superviseManager.getSupervisors(detailId);
 }
 public List<CtpAffair> getALLAvailabilityAffairList(ApplicationCategoryEnum app,Long objectId, boolean needPagination){
    return  edocSuperviseDetailDao.getALLAvailabilityAffairList(app,objectId,needPagination);
 }
 public List<CtpAffair> getALLAvailabilityAffairList(ApplicationCategoryEnum app,ApplicationSubCategoryEnum subApp,Long objectId, boolean needPagination) {
	 return  edocSuperviseDetailDao.getALLAvailabilityAffairList(app,subApp,objectId,needPagination);
 }
 
 public void updateStatusAndNoticeSupervisor(long entityId, int entityType, ApplicationCategoryEnum app, String summarySubject,
         long userId, String userName, int status, String messageKey, String repealComment, String forwardMemberIdStr) throws BusinessException{
     CtpSuperviseDetail detail = superviseManager.getSupervise(entityId);
     if(detail != null) { 
         if(Strings.isBlank(messageKey)){
             messageKey = "collaboration.msg.cancel";
         }
         detail.setStatus(status);
         detail.setCount(0l);
         detail.setDescription(null);
         detail.setScheduleProp(null);
         this.superviseManager.update(detail);
     }
 }
 public void update(CtpSuperviseVO ctpSuperviseVO,long summaryId) throws BusinessException {
        Long superviseId = ctpSuperviseVO.getId();
     if(superviseId == null){
         return ;
     }
     List<CtpSupervisor> superList = superviseManager.getSupervisors(ctpSuperviseVO.getId());
     User user = AppContext.getCurrentUser();
     CtpAffair affair = affairManager.getSenderAffair(Long.valueOf(summaryId));
     //保存督办人员
     Long[] supervisorIds = ctpSuperviseVO.getSupervisorIds();
     //替换后的人
     List<Long>  superMembers = new ArrayList<Long>();
     for (int i=0;i<supervisorIds.length;i++) {
         superMembers.add(supervisorIds[i]);
     }
     //被替换的督办人
     List<Long> members = new ArrayList<Long>();
     
     //给被替换的督办人发消息
     List<MessageReceiver> receiver = new ArrayList<MessageReceiver>();
     //给替换后，并且不给没有替换督办人发消息
     List<MessageReceiver> receiver1 = new ArrayList<MessageReceiver>();
     
     List<Long> membersNew = new ArrayList<Long>();
     //获取到被替换的督办人
     for (int i = 0;i<superList.size();i++) {
         long memberId = superList.get(i).getSupervisorId();
         if (!superMembers.contains(memberId)){
             members.add(memberId);
         }
         membersNew.add(memberId);
     }
     //获取到替换后，并和替换前不重复的
     for (int i=0;i<superMembers.size();i++) {
         //替换后的督办人，不在替换前中。并且不给自己发送消息
         if (!membersNew.contains(superMembers.get(i)) && !user.getId().equals(superMembers.get(i))){
             receiver1.add(new MessageReceiver(summaryId, superMembers.get(i),"message.link.edoc.supervise.detail",summaryId));
         }
     }
     for(int i = 0;i<members.size();i++) {
         if (!user.getId().equals(members.get(i))){
             receiver.add(new MessageReceiver(affair.getId(), members.get(i)));
         }
     }
     ApplicationCategoryEnum app = EdocUtil.valueOfApplicationCategoryEnum(affair.getApp());
     //给被删除的人发消息
     if (receiver != null && receiver.size() >0){
         userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.delete",affair.getSubject(),user.getName(),app.ordinal()), app, user.getId(), receiver,EdocMessageFilterParamEnum.supervise.key);
     }
     //给新的督办人发送消息
     if (receiver1!=null && receiver1.size() >0) {
         String forwardMemberId = affair.getForwardMember();
         String forwardMember = "";
         if (Strings.isNotBlank(forwardMemberId)) {
             forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
         }
         userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.hasten",affair.getSubject(),user.getName(),0,forwardMember), app, user.getId(), receiver1,EdocMessageFilterParamEnum.supervise.key);
     }
     
     CtpSuperviseDetail detail = new CtpSuperviseDetail();
     CtpSuperviseVO2CtpSuperviseDetail(detail,ctpSuperviseVO);
     
     if(affair != null){
         detail.setApp(affair.getApp());
         detail.setSubject(affair.getSubject());
         detail.setEntitySenderId(affair.getSenderId());
         detail.setEntityCreateDate(affair.getCreateDate());
         detail.setImportantLevel(affair.getImportantLevel());
         detail.setResentTime(affair.getResentTime());
         detail.setForwardMember(affair.getForwardMember());
         detail.setBodyType(affair.getBodyType());
         detail.setIdentifier(affair.getIdentifier());
         detail.setAffairId(affair.getId());
         detail.setExtProps(affair.getExtProps());
         detail.setCoverTime(affair.isCoverTime());
         detail.setTempleteId(affair.getTempleteId());
     }
     
     //更新督办事项
     superviseManager.update(detail);
     //删除督办人员
     superviseManager.deleteSupervisorsByDetailId(superviseId);
     
     for(Long supervisorId:supervisorIds){
         CtpSupervisor supervisor = new CtpSupervisor();
         supervisor.setIdIfNew();
         supervisor.setSuperviseId(superviseId);
         supervisor.setSupervisorId(supervisorId);
         supervisor.setEntityId(detail.getEntityId());
         superviseManager.save(supervisor);
     }
 }
 
 public void save(CtpSuperviseVO ctpSuperviseVO) throws BusinessException {
     
     CtpAffair affair = affairManager.getSenderAffair(Long.valueOf(ctpSuperviseVO.getEntityId()));
     User user = AppContext.getCurrentUser();
     
    CtpSuperviseDetail csd = new CtpSuperviseDetail();
    csd = this.CtpSuperviseVO2CtpSuperviseDetail(csd,ctpSuperviseVO);
    
    if(affair != null){
        csd.setApp(affair.getApp());
        csd.setSubject(affair.getSubject());
        csd.setEntitySenderId(affair.getSenderId());
        csd.setEntityCreateDate(affair.getCreateDate());
        csd.setImportantLevel(affair.getImportantLevel());
        csd.setResentTime(affair.getResentTime());
        csd.setForwardMember(affair.getForwardMember());
        csd.setBodyType(affair.getBodyType());
        csd.setIdentifier(affair.getIdentifier());
        csd.setAffairId(affair.getId());
        csd.setExtProps(affair.getExtProps());
        csd.setCoverTime(affair.isCoverTime());
        csd.setTempleteId(affair.getTempleteId());
    }
    
    this.superviseManager.save(csd);
    Long[] supervisorIds = ctpSuperviseVO.getSupervisorIds();
    Long entityId = csd.getEntityId();
    for(Long supervisorId:supervisorIds) {
            CtpSupervisor colSupervisor = new CtpSupervisor();
         colSupervisor.setIdIfNew();
         colSupervisor.setSuperviseId(csd.getId());
         colSupervisor.setSupervisorId(supervisorId);
         colSupervisor.setEntityId(entityId);
         this.superviseManager.save(colSupervisor);
     }
		ApplicationCategoryEnum app = null;
		if (affair != null) {
			app = EdocUtil.valueOfApplicationCategoryEnum(affair.getApp());
		}
		Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
		   //给督办人发消息
     List<MessageReceiver> receiver = new ArrayList<MessageReceiver>();
        for (int i=0;i<supervisorIds.length;i++) {
            if (!user.getId().equals(supervisorIds[i])){
                receiver.add(new MessageReceiver(affair.getObjectId(),supervisorIds[i],"message.link.edoc.supervise.detail",affair.getObjectId()));
            }
        }
        
        String forwardMemberId = affair.getForwardMember();
     int forwardMemberFlag = 0;
     String forwardMember = null;
     if(Strings.isNotBlank(forwardMemberId)){
         try {
             forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
             forwardMemberFlag = 1;
         }
         catch (Exception e) {
             LOGGER.error("", e);
         }
     }
     //给新的督办人发送消息
     if (receiver!=null && receiver.size() >0) {
         //userMessageManager.sendSystemMessage(new MessageContent("edoc.supervise.hasten",affair.getSubject(),user.getName(), forwardMemberFlag, forwardMember), app, user.getId(), receiver,systemMessageFilterParam);
     }
 }
 
 private CtpSuperviseDetail CtpSuperviseVO2CtpSuperviseDetail(CtpSuperviseDetail csd,CtpSuperviseVO ctpSuperviseVO) throws BusinessException{
     if(ctpSuperviseVO == null || csd == null){
         return null;
     }
     //督办事项id
     csd.setId(ctpSuperviseVO.getId());
     //督办主题
     csd.setTitle(ctpSuperviseVO.getTitle());
     //实体类型
     csd.setEntityType(ctpSuperviseVO.getEntityType());
     //实体ID
     csd.setEntityId(ctpSuperviseVO.getEntityId());
     //发起人ID
     csd.setSenderId(ctpSuperviseVO.getSenderId());
     //状态
     csd.setStatus(ctpSuperviseVO.getStatus());
     //督办人
     csd.setSupervisors(ctpSuperviseVO.getSupervisors());
     //描述
     csd.setDescription(ctpSuperviseVO.getDescription());
     //催办次数
     csd.setCount(ctpSuperviseVO.getCount());
     //督办提醒日期
     csd.setAwakeDate(ctpSuperviseVO.getAwakeDate());
     //提醒模式
     csd.setRemindMode(ctpSuperviseVO.getRemindMode());
     //超期提醒参数
     csd.setScheduleProp(ctpSuperviseVO.getScheduleProp());
     //完成时间
     csd.setCreateDate(ctpSuperviseVO.getCreateDate());
     //模版期限
     csd.setTemplateDateTerminal(ctpSuperviseVO.getTemplateDateTerminal());
     //督办日期类型
     csd.setSuperviseDateType(ctpSuperviseVO.getSuperviseDateType());
     return csd;
 }

}