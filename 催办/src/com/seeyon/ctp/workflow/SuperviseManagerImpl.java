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

package com.seeyon.ctp.common.supervise.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.ai.util.Constants;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseLog;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseReceiver;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.bo.SuperviseDealModel;
import com.seeyon.ctp.common.supervise.bo.SuperviseHastenParam;
import com.seeyon.ctp.common.supervise.bo.SuperviseLogWebModel;
import com.seeyon.ctp.common.supervise.bo.SuperviseParam;
import com.seeyon.ctp.common.supervise.bo.SuperviseQueryCondition;
import com.seeyon.ctp.common.supervise.dao.SuperviseDao;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.handler.SuperviseAppInfoBO;
import com.seeyon.ctp.common.supervise.handler.SuperviseHandler;
import com.seeyon.ctp.common.supervise.vo.CtpSuperviseVO;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseModelVO;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.space.manager.PortletEntityPropertyManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.workflow.manager.WorkFlowAppExtendInvokeManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;


/**
 * @author mujun
 *
 */
public class SuperviseManagerImpl implements SuperviseManager{
	
	private static Log log = LogFactory.getLog(SuperviseManagerImpl.class);
    private final Map<ModuleType, SuperviseHandler> superviseHandlerMap    = new ConcurrentHashMap<ModuleType, SuperviseHandler>();
	private SuperviseDao superviseDao;
	private AffairManager affairManager;
	private EnumManager enumManagerNew;
    private OrgManager orgManager;
    private UserMessageManager userMessageManager;
    private PortletEntityPropertyManager portletEntityPropertyManager;
    private PermissionManager permissionManager;
    private TemplateManager 	  templateManager;
    private WorkTimeManager workTimeManager;
    private EdocApi edocApi ;
    private CollaborationApi  collaborationApi ;

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public EnumManager getEnumManagerNew() {
        return enumManagerNew;
    }

    public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

    public AffairManager getAffairManager() {
        return affairManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public SuperviseDao getSuperviseDao() {
        return superviseDao;
    }

    public void setSuperviseDao(SuperviseDao superviseDao) {
        this.superviseDao = superviseDao;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public UserMessageManager getUserMessageManager() {
        return userMessageManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public PortletEntityPropertyManager getPortletEntityPropertyManager() {
        return portletEntityPropertyManager;
    }

    public void setPortletEntityPropertyManager(PortletEntityPropertyManager portletEntityPropertyManager) {
        this.portletEntityPropertyManager = portletEntityPropertyManager;
    }

    public EdocApi getEdocApi() {
        return edocApi;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    public CollaborationApi getCollaborationApi() {
        return collaborationApi;
    }

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public WorkTimeManager getWorkTimeManager() {
        return workTimeManager;
    }
    /**
     * 内容管理接口初始化，内容处理器接口加载
     */
    public void init() {
        Map<String, SuperviseHandler> superviseHandlers = AppContext.getBeansOfType(SuperviseHandler.class);
        for (String key : superviseHandlers.keySet()) {
            SuperviseHandler handler = superviseHandlers.get(key);
            superviseHandlerMap.put(handler.getModuleType(), handler);
        }
    }
    @Override
	public void deleteSuperviseAllAndSendMsgById(Long superviseId,long summaryId,SuperviseMessageParam smp) throws BusinessException{
        List<CtpSupervisor> superList = superviseDao.getSupervisors(superviseId);
        User user = AppContext.getCurrentUser();
        String forwardMemberId = null;
        Long affairId = null;
        String subject = "";
        Integer importantLevel =  0;
        if(smp!=null){
            forwardMemberId = smp.getForwardMember();
            affairId = smp.getAffairId();
            subject = smp.getSubject();
            importantLevel = smp.getImportantLevel();
        }
        else{
            CtpAffair affair = affairManager.getSenderAffair(Long.valueOf(summaryId));
            forwardMemberId = affair.getForwardMember();
            affairId = affair.getId();
            subject = affair.getSubject();
            importantLevel = affair.getImportantLevel();
        }

        int forwardMemberFlag = 0;
        String forwardMember = "";
        if(Strings.isNotBlank(forwardMemberId)){
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }
            catch (Exception e) {
                log.error("", e);
            }
        }
        List<MessageReceiver> receiver = new ArrayList<MessageReceiver>();
        for(CtpSupervisor colSupervisor:superList) {
            if (!user.getId().equals(colSupervisor.getSupervisorId())){
                receiver.add(new MessageReceiver(affairId, colSupervisor.getSupervisorId()));
            }
        }
        if (receiver != null && receiver.size() >0){
            userMessageManager.sendSystemMessage(new MessageContent("collaboration.msg.supervise.delete",
                    subject,user.getName(), forwardMemberFlag, forwardMember).setImportantLevel(importantLevel), ApplicationCategoryEnum.collaboration, user.getId(), receiver,7);//给被删除的人发消息
        }
        superviseDao.deleteSupervised(superviseId);
    }

    @Override
    public void deleteSuperviseAllById(Long superviseId) throws BusinessException {
        superviseDao.deleteSupervised(superviseId);
    }

	public CtpSuperviseDetail getSupervise(int entityType, long entityId) throws BusinessException{
		return (CtpSuperviseDetail)superviseDao.getSupervise(entityType,entityId);
	}
	
	private void update(SuperviseMessageParam smp, String title,
			long senderId, String senderName, String supervisorNames,
			List<Long> supervisorIds, Date awakeDate, int entityType,
			long entityId) throws BusinessException{

		CtpSuperviseDetail detail = this.getSupervise(entityType, entityId);
		if(detail == null) {
			this.save(smp, title, senderId, senderName, supervisorNames, supervisorIds, awakeDate, entityType, entityId, new HashMap<String, Object>());
			return;
		}
		//添加设置更新督办信息的标题
        if(smp != null && Strings.isNotEmpty(smp.getSubject())){
            detail.setSubject(smp.getSubject());
        }
		detail.setTitle(title);
		detail.setSenderId(senderId);
		detail.setSupervisors(supervisorNames);
		detail.setAwakeDate(awakeDate);
		if(!smp.isSaveDraft()){
		  detail.setStatus(SuperviseEnum.superviseState.supervising.ordinal());
		}
		detail.setEntityId(entityId);
		detail.setEntityType(entityType);

		List<Long> deletedPerson = new ArrayList<Long>();
		List<CtpSupervisor> add = new ArrayList<CtpSupervisor>();

		List<Long> deleteMembers = new ArrayList<Long>();

		Map<Long,Long> hash = new HashMap<Long,Long>();
		if(supervisorIds != null) {
			for(Long supervisorId:supervisorIds) {
				hash.put(supervisorId, supervisorId);
			}
		}

		List<Long> oldSuper = new ArrayList<Long>();
		List<CtpSupervisor> s_list = superviseDao.getSupervisors(detail.getId());
		if(s_list!=null && s_list.size()>0) {
			Iterator<CtpSupervisor> it = s_list.iterator();
			while(it.hasNext()) {
				CtpSupervisor supervisor = it.next();
				if(hash.get(supervisor.getSupervisorId())==null) {
					deletedPerson.add(supervisor.getId());
					deleteMembers.add(supervisor.getSupervisorId());
                }
				it.remove();
				oldSuper.add(supervisor.getSupervisorId());
			}
		}

		if(supervisorIds != null) {
			for(Long supervisorId:supervisorIds) {
				CtpSupervisor colSupervisor = new CtpSupervisor();
				colSupervisor.setIdIfNew();
				colSupervisor.setSuperviseId(detail.getId());
				colSupervisor.setSupervisorId(supervisorId);
				if(!oldSuper.contains(supervisorId)){
					add.add(colSupervisor);
					superviseDao.save(colSupervisor);
				}
			}
		}
		superviseDao.update(detail);

		//删除取消的督办人员记录
		for(Long supervisorId:deletedPerson){
			superviseDao.deleteSupervisor(supervisorId);
		}
		SuperviseParam sp = convertProperties2VO(smp.getImportantLevel(), smp.getSubject(), title, senderId, senderName,
	            supervisorNames, supervisorIds, awakeDate, entityType, entityId, SuperviseEnum.superviseState.supervising.ordinal(),
	            true, smp.getForwardMember(), new HashMap<String, Object>());

		this.sendMessageAndJob(sp,detail.getId(),deleteMembers,add);
	}
	@Override
    public CtpSuperviseDetail getSupervise(long entityId) throws BusinessException {
        return (CtpSuperviseDetail)superviseDao.getSupervise(entityId);
    }
    @SuppressWarnings("rawtypes")
    @Override
    public FlipInfo getSuperviseList4App(FlipInfo flipInfo,Map params) throws BusinessException{
        getEntityPropertyMap(params);//优化1 请求中去掉mehtod的参数
        
        EnumMap<SuperviseQueryCondition, List<String>> queryConditionMap =
                new EnumMap<SuperviseQueryCondition, List<String>>(SuperviseQueryCondition.class);
        Set set = params.keySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            SuperviseQueryCondition queryConditionEnum = SuperviseQueryCondition.valueOf2(key);
            if(queryConditionEnum != null){
                String pageName = queryConditionEnum.getName();
                List<String> list = new ArrayList<String>();
                if(queryConditionEnum != SuperviseQueryCondition.colSuperviseSendTime &&
                		queryConditionEnum != SuperviseQueryCondition.edocAwakeDate &&
                		queryConditionEnum != SuperviseQueryCondition.deadlineDatetime){
                		String pageNameValue = (String)params.get(pageName);
                    if(pageNameValue != null && !"".equals(pageNameValue)){
                        if("templeteCategorys".equals(key) && !"".equals(pageNameValue)){
                          list = Arrays.asList((pageNameValue).split(","));
                        }else if("templeteAll".equals(key)){
                          list.add("templeteAll");
                        }else if("edocType".equals(key)){
                          list.add(pageNameValue);
                        }else{
                          list.add(pageNameValue);
                        }
                    }
                }else{
                    if(params.get("begin_"+pageName) != null){
                        list.add(String.valueOf(params.get("begin_"+pageName)));
                    }
                    if(params.get("end_"+pageName) != null){
                        list.add(String.valueOf(params.get("end_"+pageName)));
                    }
               }
               if(list.size()!=0){
                   queryConditionMap.put(queryConditionEnum, list);
               }
            }
        }

        List<String> status = new ArrayList<String>(1);
        if(params.get("status")!=null){
            status.add(String.valueOf(params.get("status")));
        }else{
            status.add("0");
        }
        queryConditionMap.put(SuperviseQueryCondition.status, status);

        List<String> objectId = new ArrayList<String>(1);
        objectId.add((String) params.get("objectId"));
        queryConditionMap.put(SuperviseQueryCondition.moduleId, objectId);
        
        Integer app = Integer.valueOf(params.get("app").toString());
        
        boolean onlyCount = Boolean.valueOf(String.valueOf(params.get("onlyCount")));
        
        SuperviseHandler superviseHandler = getSuperviseHandler(ModuleType.getEnumByKey(app));
        superviseHandler.getSuperviseDataList(flipInfo,queryConditionMap,onlyCount);
		return flipInfo;
	}
	private SuperviseHandler getSuperviseHandler(ModuleType moduleType) throws BusinessException {
	    SuperviseHandler handler = superviseHandlerMap.get(moduleType);
        if (handler == null)
            throw new BusinessException("没有找到督办数据处理器：" + moduleType);
        return handler;
    }
   public Long save(
	   SuperviseMessageParam smp,
       String title,
       long senderId,
       String senderName,
       String supervisorNames,
       List<Long> supervisorIds,
       Date awakeDate,
       int entityType,
       long entityId,
       Map<String, Object> param
       ) throws BusinessException {
       
       
       int superviseState = SuperviseEnum.superviseState.supervising.ordinal();
       //如果发送后流程已结束(只有知会节点的情况)，且设置了督办，那么督办状态直接为已办结
       CtpAffair sendAffairs = affairManager.getSenderAffair(entityId);
       if(sendAffairs != null && sendAffairs.isFinish()){
    	    superviseState = SuperviseEnum.superviseState.supervised.ordinal();
       }
       if(sendAffairs==null ||  (Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(sendAffairs.getState())
           && !Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(sendAffairs.getSubState()))){
           superviseState = SuperviseEnum.superviseState.waitSupervise.ordinal();
       }

       SuperviseParam sp = convertProperties2VO(smp.getImportantLevel(), smp.getSubject(), title, senderId, senderName,
            supervisorNames, supervisorIds, awakeDate, entityType, entityId, superviseState, smp.isSendMessage(), smp.getForwardMember(), param);
       return save(sp);
   }
/**
 * @param importantLevel
 * @param summarySubject
 * @param title
 * @param senderId
 * @param senderName
 * @param supervisorNames
 * @param supervisorIds
 * @param awakeDate
 * @param entityType
 * @param entityId
 * @param state
 * @param sendMessage
 * @param forwardMemberIdStr
 * @return
 */
private SuperviseParam convertProperties2VO(Integer importantLevel, String summarySubject, String title, Long senderId,
        String senderName, String supervisorNames, List<Long> supervisorIds, Date awakeDate, Integer entityType, Long entityId,
        Integer state, Boolean sendMessage, String forwardMemberIdStr, Map<String, Object> param) {
       SuperviseParam sp = new SuperviseParam();
       sp.setImportantLevel(importantLevel);
       sp.setSubject(summarySubject);
       sp.setTitle(title);
       sp.setUerId(senderId);
       sp.setUserName(senderName);
       sp.setSupervisorIds(supervisorIds);
       sp.setSupervisorNames(supervisorNames);
       sp.setSuperviseDate(awakeDate);
       sp.setEntityId(entityId);
       sp.setSuperviseType(entityType);
       sp.setSuperviseState(state);
       sp.setIsSendMessage(sendMessage);
       sp.setBodyType(String.valueOf(param.get("contentType")));
       if(Strings.isNotBlank(forwardMemberIdStr)){
           sp.setForwardMemberId(Long.valueOf(forwardMemberIdStr));
       }
    return sp;
}
    public Long save(SuperviseParam sp) throws BusinessException {

        CtpSuperviseDetail detail = new CtpSuperviseDetail();
        CtpSuperviseDetail  oldDetail = getSupervise(sp.getEntityId());
        List<Long> oldSupervisorIds = new ArrayList<Long>();//老的督办人
        if(oldDetail!=null){
        	//获得老的督办人ID
        	List<CtpSupervisor> oldSupervisors=this.getSupervisors(oldDetail.getId());
        	if(oldSupervisors !=null && Strings.isNotEmpty(oldSupervisors)){
        		for(CtpSupervisor ctpSupervisor1:oldSupervisors){
        			oldSupervisorIds.add(ctpSupervisor1.getSupervisorId());
        		}
        	}
        	//删除老的督办人信息
            detail = oldDetail;
            deleteSupervisorsByDetailId(detail.getId());
        }
        //保存新的督办信息
        detail.setIdIfNew();
        detail.setTitle(sp.getTitle());
        detail.setSenderId(sp.getUerId());
        detail.setStatus(sp.getSuperviseState());
        detail.setSupervisors(sp.getSupervisorNames());
        detail.setCount(0L);
        detail.setAwakeDate(sp.getSuperviseDate());
        detail.setCreateDate(new Date());
        detail.setEntityType(sp.getSuperviseType());
        detail.setEntityId(sp.getEntityId());
        
        detail.setSubject(sp.getSubject());
        detail.setEntityCreateDate(sp.getSuperviseDate());
        detail.setEntitySenderId(sp.getUerId());
        detail.setBodyType(sp.getBodyType());
        CtpAffair senderAffair = affairManager.getSenderAffair(sp.getEntityId());
        if(senderAffair != null){
            detail.setApp(senderAffair.getApp());
            detail.setSubject(senderAffair.getSubject());
            detail.setEntitySenderId(senderAffair.getSenderId());
            detail.setEntityCreateDate(senderAffair.getCreateDate());
            detail.setImportantLevel(senderAffair.getImportantLevel());
            detail.setResentTime(senderAffair.getResentTime());
            detail.setForwardMember(senderAffair.getForwardMember());
            detail.setBodyType(senderAffair.getBodyType());
            detail.setIdentifier(senderAffair.getIdentifier());
            detail.setAffairId(senderAffair.getId());
            detail.setExtProps(senderAffair.getExtProps());
            detail.setCoverTime(senderAffair.isCoverTime());
            detail.setTempleteId(senderAffair.getTempleteId());
            
            sp.addToMap("senderId",senderAffair.getMemberId());
        }
        
        superviseDao.saveOrUpdateDetail(detail);
        //保存新的督办人
        List<CtpSupervisor> supervisors =  new ArrayList<CtpSupervisor>();
        List<CtpSupervisor> newSupervisors =  new ArrayList<CtpSupervisor>();
        //获得被删除的督办人
        List<Long> deletedPerson = new ArrayList<Long>();
        if(Strings.isNotEmpty(sp.getSupervisorIds())){
        	for(Long supervisorId:sp.getSupervisorIds()) {
        		CtpSupervisor colSupervisor = new CtpSupervisor();
        		colSupervisor.setIdIfNew();
        		colSupervisor.setSuperviseId(detail.getId());
        		colSupervisor.setSupervisorId(supervisorId);
        		supervisors.add(colSupervisor);
        		//取得新增的督办人（即之前没有现在新加的，用来发消息）
        		if(!oldSupervisorIds.contains(supervisorId)){
        			newSupervisors.add(colSupervisor);
        		}
        	}
        	DBAgent.saveAll(supervisors);
            for(Long sId:oldSupervisorIds){
            	if(!sp.getSupervisorIds().contains(sId)){
            		deletedPerson.add(sId);
            	}
            }
        }
        //发消息
        if(sp.getIsSendMessage()) {
            this.sendMessageAndJob(sp, detail.getId(),deletedPerson,newSupervisors);
        }
        return detail.getId();
    }
    /**
     * 给督办人发消息
     * @param sp
     * @param detailId
     * @param deletedPerson 删除的督办人IDs(之前有现在没有)
     * @param addPeople 增加的督办人(之前没有现在有)
     * @throws BusinessException
     */
    private void sendMessageAndJob(
            SuperviseParam sp,
            Long detailId,
            List<Long> deletedPerson,
            List<CtpSupervisor> addPeople) throws BusinessException {

    	Long summaryId = sp.getEntityId();
    	List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        List<MessageReceiver> deleteReceivers = new ArrayList<MessageReceiver>();
        User user = AppContext.getCurrentUser();
        String hastenType = "col.supervise.hasten";
        String deleteType = "col.supervise.delete";
        String linkType = "message.link.col.supervise";
        ApplicationCategoryEnum app = ApplicationCategoryEnum.collaboration;
        Long senderId = null;
        String subject = "";
        if(sp.getSuperviseType().intValue() == SuperviseEnum.EntityType.summary.ordinal()){
        	
	        senderId = null != sp.getExtraAttr("senderId") ? (Long)sp.getExtraAttr("senderId"): sp.getUerId();
	        subject = sp.getSubject();
	        
    	}
        if(sp.getSuperviseType().intValue() == SuperviseEnum.EntityType.edoc.ordinal()){
        	app = ApplicationCategoryEnum.edoc;
            hastenType = "edoc.supervise.hasten2";
            deleteType = "edoc.supervise.delete";
            linkType = "message.link.edoc.supervise.detail";
            EdocSummaryBO edocSummary = edocApi.getEdocSummary(summaryId);
            if(edocSummary!=null) {
                senderId = edocSummary.getStartUserId();
                subject = edocSummary.getSubject();
            }
        }

        List<Long> members = new ArrayList<Long>();

        MessageReceiver receiver = null;
        for(CtpSupervisor colSupervisor:addPeople) {
            if (!user.getId().equals(colSupervisor.getSupervisorId()) && !members.contains(colSupervisor.getSupervisorId())) {
                receiver = new MessageReceiver(detailId, colSupervisor.getSupervisorId(),linkType,summaryId);
                receivers.add(receiver);
                members.add(colSupervisor.getSupervisorId());
            }
        }
        if(deletedPerson != null) {
            for(Long personId:deletedPerson) {
                if (!user.getId().equals(personId) && !members.contains(personId)) {
                    receiver = new MessageReceiver(detailId,personId);
                    deleteReceivers.add(receiver);
                    members.add(personId);
                }
            }
        }

        int forwardMemberFlag = 0;
        String forwardMember = null;
        try {
            if(sp.getForwardMemberId()!=null){
                forwardMember = orgManager.getMemberById(sp.getForwardMemberId()).getName();
                forwardMemberFlag = 1;
            }
            Integer systemMessageFilterParam = 7;
            if (app == ApplicationCategoryEnum.edoc) {
                systemMessageFilterParam = 17; 
            }
            if(null!=receivers && !receivers.isEmpty()){
                if("col.supervise.hasten".equals(hastenType)){
                	log.info("打印督办的消息记录1："+user.getName());
                	String sName = user.getName();
                	if(Strings.isBlank(user.getName())){
                		V3xOrgMember memberById = orgManager.getMemberById(user.getId());
                		if(null != memberById){
                			sName = memberById.getName();
                		}
                	}
                    userMessageManager.sendSystemMessage(new MessageContent(hastenType,subject,sName, forwardMemberFlag, forwardMember).setImportantLevel(sp.getImportantLevel()), app, sp.getUerId(), receivers,systemMessageFilterParam);
                }else{
                	log.info("打印督办的消息记录2："+sp.getUserName());
                    userMessageManager.sendSystemMessage(new MessageContent(hastenType,sp.getSubject(),sp.getUserName(),app.getKey()).setImportantLevel(sp.getImportantLevel()), app, sp.getUerId(), receivers,systemMessageFilterParam);
                }
            }
            if(deleteReceivers.size()>0){
                if("col.supervise.delete".equals(deleteType)){
                	log.info("打印督办的消息记录3："+sp.getUserName());
                    userMessageManager.sendSystemMessage(new MessageContent(deleteType,sp.getSubject(),sp.getUserName(), forwardMemberFlag, forwardMember).setImportantLevel(sp.getImportantLevel()), app, sp.getUerId(), deleteReceivers,systemMessageFilterParam);//给被删除的人发消息
                }else{
                	log.info("打印督办的消息记录4："+sp.getUserName());
                    userMessageManager.sendSystemMessage(new MessageContent(deleteType,sp.getSubject(),sp.getUserName(), app.getKey()).setImportantLevel(sp.getImportantLevel()), app, sp.getUerId(), deleteReceivers,systemMessageFilterParam);//给被删除的人发消息
                }
            }
        }catch(BusinessException e) {
            log.error("",e);
        }
        if(Strings.isNotEmpty(deletedPerson) || Strings.isNotEmpty(addPeople)){ //都为空的话表示没有修改督办
        	StringBuffer supervisorMemberId = new StringBuffer();
        	if(Strings.isNotEmpty(sp.getSupervisorIds())){
            	for(Long supervisorId:sp.getSupervisorIds()) {
            		supervisorMemberId.append(supervisorId + ",");
            	}
            	if(supervisorMemberId.length()>0)
                    supervisorMemberId.deleteCharAt(supervisorMemberId.length()-1);
            }
            createQuarz4Supervise(summaryId, detailId, sp.getSuperviseDate(),senderId, supervisorMemberId.toString(), subject);
        }
    }
    public static void createQuarz4Supervise(long summaryId,
            Long detailId,
            Date superviseDate ,
            long senderId,
            String supervisorMemberId,
            String subject){
        String name = "ColSupervise" + summaryId;

        try{
            QuartzHolder.deleteQuartzJob(name);
        }
        catch(Exception e) {
            log.error("",e);
        }

        try {
            Map<String, String> p = new HashMap<String, String>(4);
            p.put("colSuperviseId", String.valueOf(detailId));
            p.put("senderId", String.valueOf(senderId));
            p.put("supervisorMemberId", supervisorMemberId.toString());
            p.put("subject", subject);
            QuartzHolder.newQuartzJob(name, superviseDate, "terminateColSuperviseJob", p);
        }
        catch(Exception e) {
            log.error("",e);
        }
    }
	@Override
	public int getHastenTimes(long superviseId) throws BusinessException{
		return superviseDao.getHastenTimes(superviseId);
	}
	
	@Override
	public void updateAwakeDate(long superviseId,Date awakeDate) throws BusinessException{
		CtpSuperviseDetail detail = superviseDao.getSuperviseDetail(superviseId);
    	if(detail != null) {
			if(awakeDate!=null){
			    detail.setAwakeDate(awakeDate);
			}
        	superviseDao.update(detail);
        	//更新定时任务
            String subject = "";
            Long senderId = 0l;
            if(Integer.valueOf(SuperviseEnum.EntityType.summary.ordinal()).equals(detail.getEntityType())){
     	        ColSummary summary = collaborationApi.getColSummary(detail.getEntityId());
     	        if(summary!=null){
     	        	senderId = summary.getStartMemberId();
     	        	subject = summary.getSubject();
     	        }
         	}else if(Integer.valueOf(SuperviseEnum.EntityType.edoc.ordinal()).equals(detail.getEntityType())){
         		EdocSummaryBO edocSummary = edocApi.getEdocSummary(detail.getEntityId());
         		if(edocSummary!=null) {
         			senderId = edocSummary.getStartUserId();
         			subject = edocSummary.getSubject();
         		}
         	}
        	createQuarz4Supervise(detail.getEntityId(), detail.getId(), awakeDate,senderId, String.valueOf(senderId), subject);
    	}

	}
	@Override
	public CtpSuperviseDetail get(long superviseId) throws BusinessException{
		return superviseDao.getSuperviseDetail(superviseId);
	}
	@Override
	public void updateContent(long superviseId, String content) throws BusinessException{
		superviseDao.updateContent(superviseId, content);
	}
	
	
	@Override
	public void deleteSupervisorsByDetailId(long superviseDetailId)throws BusinessException {
		superviseDao.deleteSupervisorsByDetailId(superviseDetailId);

	}
	@Override
	public List<CtpSuperviseTemplateRole> findRoleByTemplateId(long templateId)
	        throws BusinessException{
		return superviseDao.findRoleByTemplateId(templateId);
	}
	@Override
	public void deleteAllTemplateRole(long templateId) throws BusinessException {
		superviseDao.deleteAllTemplateRole(templateId);

	}

	public void deleteSupervisedAjax(String superviseIds) throws BusinessException{
	    deleteSupervised(AppContext.currentUserId(),superviseIds);
	}
	
	@Override
	public void deleteSupervised(long userId,String superviseIds) throws BusinessException {
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
    		superviseDao.deleteSupervised(userId,nameParameters);
    	}
	}
   

    public static void getEntityPropertyMap(Map params) {
    	
    	Iterator<Map.Entry<Integer, Integer>> entries = params.entrySet().iterator();
    	while (entries.hasNext()) {
    	    Map.Entry<Integer, Integer> entry = entries.next();
    	    if("method".equals(entry.getKey())){
    	    	entries.remove();
    	    }
    	}
    }


    public String checkColSupervisor(Long summaryId, CtpAffair senderAffair) throws BusinessException{
        
        Long userId = AppContext.currentUserId();

        boolean currentUserIdSupervisor = false;
        CtpSuperviseDetail detail = this.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summaryId,userId);
        
        if(detail == null || Integer.valueOf(SuperviseEnum.superviseState.waitSupervise.ordinal()).equals(detail.getStatus())){
        	 String m = ResourceUtil.getString("supervise.col.delete.non");
             return m;
        }
        if(detail != null){
            List<CtpSupervisor> supervisors = getSupervisors(detail.getId());

            if(supervisors != null && !supervisors.isEmpty()){
                for (CtpSupervisor supervisor : supervisors){
                    if(userId.equals(supervisor.getSupervisorId())){
                        currentUserIdSupervisor = true;
                        break;
                    }
                }
            }
        }

        if(!currentUserIdSupervisor){
            return ResourceUtil.getString("supervise.col.delete.non.nome");
        }

        return null;
    }

    public CtpSuperviseDetail getSupervise(int entityType,long entityId,long userId) throws BusinessException{
        return (CtpSuperviseDetail)superviseDao.getCurrentUserSupervise(entityType,entityId,userId);
    }



    public List<CtpSupervisor> getSupervisors(long superviseId) throws BusinessException{
        return superviseDao.getSupervisors(superviseId);
    }

    public List<CtpSupervisor> getSupervisorsByModuleId(Long moduleId) throws BusinessException{
    	CtpSuperviseDetail detail = getSupervise(moduleId);
    	return superviseDao.getSupervisors(detail.getId());
    }
    public FlipInfo getLogByDetailId(FlipInfo flipInfo,Map params) throws BusinessException{
        if(params == null || params.get("superviseId") == null){
            return flipInfo;
        }
        //进行类型转换
        Long superviseId = Long.parseLong(params.get("superviseId").toString());
        List<CtpSuperviseLog> logList = superviseDao.getLogByDetailId(flipInfo,superviseId);
        List<SuperviseLogWebModel> modelList  = transSuperviseLog2WebModel(logList);
        flipInfo.setData(modelList);
        return flipInfo;
    }
    private List<SuperviseLogWebModel> transSuperviseLog2WebModel(List<CtpSuperviseLog> logList) throws BusinessException {
        List<SuperviseLogWebModel> modelList = new ArrayList<SuperviseLogWebModel>();
        int number = 1;
        for(CtpSuperviseLog superviseLog : logList) {
        	SuperviseLogWebModel model = new SuperviseLogWebModel();
            model.setReceiverName(superviseDao.getReceivers(superviseLog.getId()));
            Long senderID = superviseLog.getSender();
            model.setSender(senderID);
            model.setModel(superviseLog.getModel());
            model.setContent(superviseLog.getContent());
            model.setSendTime(superviseLog.getSendTime());
            model.setSuperviseId(superviseLog.getSuperviseId());
            model.setType(superviseLog.getType());
            String senderName = Functions.showMemberName(senderID);
            if(Constants.AI_SENDER_ID.equals(senderID)) {//AI催办单独 处理
            	senderName = ResourceUtil.getString("ai.remind.sender");
            }
            model.setSenderName(senderName);
            model.setNumber((number++) +"");
        	modelList.add(model);
        }
		return modelList;
	}

	/**
     * 对查询的List集合进行特殊处理
     * @param logList
     * @return
     */
    private List<SuperviseLogWebModel> commonSuperviseLogWebModel(List logList) throws BusinessException{
        if(logList == null){
            return null;
        }
        List<SuperviseLogWebModel> modelList = new ArrayList<SuperviseLogWebModel>();
        for(int i=0;i<logList.size();i++){
            Object[] obj = (Object[])logList.get(i);
            int j=0;
            SuperviseLogWebModel model = new SuperviseLogWebModel();
            model.setReceiverName(superviseDao.getReceivers((Long)obj[j++]));
            Long senderID = (Long)obj[j++];
            model.setSender(senderID);
            model.setModel((Integer)obj[j++]);
            model.setContent((String)obj[j++]);
            model.setSendTime((Date)obj[j++]);
            model.setSuperviseId((Long)obj[j++]);
            model.setType((Integer)obj[j++]);
            j++;
            model.setSenderName(Functions.showMemberName(senderID));
            model.setNumber((i+1)+"");
            modelList.add(model);
        }
        return modelList;
    }

    @Override
    public FlipInfo getLogBySummaryId(FlipInfo flipInfo, Map params) throws BusinessException {
        if(params == null || params.get("summaryId") == null) {
            return flipInfo;
        }
        Long summaryId = Long.parseLong(params.get("summaryId").toString());
        CtpSuperviseDetail csd = this.getSupervise(summaryId);
        Map<String,Long> query = new HashMap<String,Long>();
        if(csd != null && csd.getId() != null){
            query.put("superviseId", csd.getId());
            flipInfo = this.getLogByDetailId(flipInfo, query);
        }
        return flipInfo;
    }


    @Override
    public List<SuperviseLogWebModel> getLogBySummaryId(Long summaryId) throws BusinessException {
        if(summaryId==null){
            return null;
        }
        CtpSuperviseDetail csd = this.getSupervise(summaryId);
        if(null == csd){
        	return null;
        }
        return this.getLogBySuperviseId(csd.getId());
    }


    @Override
    public List<SuperviseLogWebModel> getLogBySuperviseId(Long superviseId) throws BusinessException {
        List logList =  this.superviseDao.getLogBySuperviseId(superviseId);
        return commonSuperviseLogWebModel(logList);
    }
    public FlipInfo getAffairModel(FlipInfo flipInfo,Map params) throws BusinessException {
    	String objectId = String.valueOf(params.get("objectId"));
    	Long accountId = AppContext.currentAccountId();
    	//由于页面传递为字符串类型，此处进行转换
    	params.put("objectId", Long.valueOf(objectId));
    	params.put("delete", false);
    	Integer[] state = new Integer[]{2,3,4,30};
    	params.put("state", state);

       
    	Long affairId = Long.valueOf((String)params.get("affairId"));
    	params.remove("affairId");

    	CtpAffair ctpAffair = affairManager.get(affairId);
        int app = ctpAffair.getApp().intValue();
        Long orgAccountId = 0l;
        if(app == ApplicationCategoryEnum.collaboration.getKey()){
        	SuperviseHandler handler = getSuperviseHandler(ModuleType.collaboration);
            orgAccountId = handler.getFlowPermAccountId(ctpAffair.getObjectId());
        }
        else{
        	SuperviseHandler handler = getSuperviseHandler(ModuleType.edoc);
            orgAccountId = handler.getFlowPermAccountId(ctpAffair.getObjectId());
        }

        params.put("app", app);

    	 List<CtpAffair> list = affairManager.getByConditions(flipInfo,params);
    	 List<SuperviseDealModel> modelList = new ArrayList<SuperviseDealModel>();

    	 for(CtpAffair affair : list){
    		 SuperviseDealModel model = new SuperviseDealModel();
             //督辦人
    		 model.setDealUser(this.getUserName(affair.getMemberId()));
	         //節點權限
	         String policyName = "";

	         if(Strings.isBlank(affair.getNodePolicy())){
            	 if(app == ApplicationCategoryEnum.collaboration.getKey()){
            		 policyName = ResourceUtil.getString("node.policy.collaboration");
            	 }
            	 //发文收文第一个节点为空，不加拟文操作以及相关时间信息
             	 if(app == ApplicationCategoryEnum.edocSend.getKey() || app == ApplicationCategoryEnum.edocSign.getKey()){
             		 //发文,签报
                     policyName = ResourceUtil.getString("node.policy.niwen");
            	 }
	             if (app == ApplicationCategoryEnum.edocRec.getKey()) {
	                 //收文
	                 policyName = ResourceUtil.getString("node.policy.dengji" + Functions.suffix());
	             }
	        }else{
	        	//协同(默认)
	        	String category = EnumNameEnum.col_flow_perm_policy.name();
	        	//发文
	        	if(app == ApplicationCategoryEnum.edocSend.getKey()){
	        		category = EnumNameEnum.edoc_send_permission_policy.name();
	        	}else if(app == ApplicationCategoryEnum.edocSign.getKey()){
	        		//签报
	        		category = EnumNameEnum.edoc_qianbao_permission_policy.name();
	        	}else if(app == ApplicationCategoryEnum.edocRec.getKey()){
	        		//收文
	        		category = EnumNameEnum.edoc_rec_permission_policy.name();
	        	}
	             policyName = permissionManager.getPermissionName(category, affair.getNodePolicy(), accountId);
	        }
	         model.setPolicyName(policyName);
	    	 //处理期限
	    	 model.setDealLine(affair.getDeadlineDate()==null?ResourceUtil.getString("collaboration.deadline.no"):ColUtil.getDeadLineName(affair.getExpectedProcessTime()));
	    	 //催辦次數
	    	 model.setHastened(affair.getHastenTimes()==null?0:affair.getHastenTimes());
	    	 //是否超期
	    	 Date receiveDate = affair.getReceiveTime();
	    	 Date computeDate = affair.getCompleteTime();
	    	 Long deallineDate = affair.getDeadlineDate();
	    	 //收到時間
	    	 model.setReveiveDate(receiveDate);
	    	 //處理時間
	    	 model.setCompleteTime(computeDate);
	    	 String keyGood =  "supervise.col.dealline.good"; //正常
	    	 String keyBad = "supervise.col.dealline.bad";//超期
	    	 model.setEfficiency(ResourceUtil.getString(keyGood));
	    	 // 0表示正常 1表示超期
	    	 model.setIsefficiency(0);
	    	 Date startDate = null;
	    	 if(Integer.valueOf(StateEnum.col_sent.key()).equals(affair.getState())
	    			 || Integer.valueOf(StateEnum.col_waitSend.key()).equals(affair.getState())){
	    		 startDate = affair.getCreateDate();
	    	 }else{
	    		 startDate = affair.getReceiveTime();
	    	 }
	    	 if(computeDate != null) {
                 Long intervalTime = Functions.getMinutesBetweenDatesByWorkTime(startDate, computeDate, orgAccountId);
                 String dealTime = "";
                 if(intervalTime != null){
                 	dealTime = Functions.showDateByWork(Integer.parseInt(intervalTime.toString()));
                 }
	    	     //處理時長
	    	     model.setDealDays(dealTime);
	    	  }
	    	 //是否超期
    	     boolean isOverTime = affair.isCoverTime() == null ? false : affair.isCoverTime();
    	     model.setOverTime(affair.isCoverTime());
    	     if(isOverTime){
    	    	 model.setEfficiency(ResourceUtil.getString(keyBad));
    	    	 model.setIsefficiency(1);
    	     }else{
    	    	 model.setEfficiency(ResourceUtil.getString(keyGood));
    	    	 model.setIsefficiency(0);
    	     }
             modelList.add(model);
    	 }
    	 flipInfo.setNeedTotal(false);
         flipInfo.setData(modelList);
    	 return flipInfo;
    }
	  private String getUserName(long id){
		  String userName="";
		  try {
			V3xOrgMember orgMember=orgManager.getMemberById(id);
			if(null != orgMember){
				userName=Functions.showMemberName(orgMember.getId());
			}
		} catch (BusinessException e) {
			e.printStackTrace();
		}
		  return userName;

	  }
	public Map<ModuleType, SuperviseHandler> getSuperviseHandlerMap() {
		return superviseHandlerMap;
	}
	@Override
    public void deleteSupervisor(Long supervisorId) throws BusinessException{
    	superviseDao.deleteSupervisor(supervisorId);
    }

	@Override
	public  void updateStatus(long entityId, SuperviseEnum.EntityType superviseType ,SuperviseEnum.superviseState state,SuperviseMessageParam messageParam) throws BusinessException{
	    
		int status = state.ordinal();
		CtpSuperviseDetail detail = this.getSupervise(entityId);
		long userId = 0;
		StringBuffer supervisorMemberId = new StringBuffer();
		String summarySubject = "";
		if(detail != null) {
			detail.setStatus(status);
			this.superviseDao.update(detail);
			if(messageParam != null){//发送督办信息
				
				
				int importantLevel = messageParam.getImportantLevel();
			    summarySubject = messageParam.getSubject() ; 
			    userId = messageParam.getMemberId();
			    
			    if( messageParam.isSendMessage()){
				    
				    String userName = "";
				    V3xOrgMember member = orgManager.getMemberById(userId);
				    if(member != null){
				    	userName = member.getName();
				    }
				     
					String hastenType = "col.supervise.hasten";
					String linkType = "message.link.col.supervise";
					
					if( SuperviseEnum.EntityType.edoc.ordinal() == superviseType.ordinal()){
				
						hastenType = "edoc.supervise.hasten2";
						linkType = "message.link.edoc.supervise.detail";
						
					}
					
					List<CtpSupervisor> addPeople = this.getSupervisors(detail.getId());
					List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
					ApplicationCategoryEnum app = ApplicationCategoryEnum.collaboration;
					MessageReceiver receiver = null;
					for(CtpSupervisor colSupervisor:addPeople) {
						if (!Long.valueOf(userId).equals(colSupervisor.getSupervisorId())) {
							receiver = new MessageReceiver(detail.getId(), colSupervisor.getSupervisorId(),linkType,entityId);
							receivers.add(receiver);
							supervisorMemberId.append(colSupervisor.getSupervisorId() + ",");
						}
						
					}
					if(supervisorMemberId.length()>0){
						supervisorMemberId.deleteCharAt(supervisorMemberId.length()-1);
					}
					
					int forwardMemberFlag = 0;
					String forwardMember = null;
					try {
						if("col.supervise.hasten".equals(hastenType)){
							userMessageManager.sendSystemMessage(new MessageContent(hastenType,summarySubject,userName, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel), app,AppContext.getCurrentUser().getId(), receivers,7);
						}
						if("edoc.supervise.hasten2".equals(hastenType)){
							 userMessageManager.sendSystemMessage(new MessageContent(hastenType,summarySubject,userName,ApplicationCategoryEnum.edoc.key(),userName).setImportantLevel(importantLevel), app, AppContext.getCurrentUser().getId(), receivers,17);
						}
					}catch(BusinessException e) {
						log.error("",e);
					}
			    }
			}
			createQuarz4Supervise(entityId, detail.getId(),detail.getAwakeDate(),userId, supervisorMemberId.toString(),summarySubject);
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
	
	@Override
	public String hastenSummary(Long memberId, Long summaryId
            , Map<String, Object> params) throws BusinessException{
	    
		String subject = "";
		boolean isEdoc = false;
		String processId = null;
		if(params.get("app") != null && "4".equals(String.valueOf(params.get("app")))) {
			EdocSummaryBO summary = edocApi.getEdocSummary(summaryId);	
			if(summary != null){
			    processId = summary.getProcessId();
			    subject = summary.getSubject();
			}
			isEdoc = true;
		} else {
			ColSummary summary = collaborationApi.getColSummary(summaryId);	
			if(summary != null){
                processId = summary.getProcessId();
                subject = summary.getSubject();
            }
		}
	    
	    
        SuperviseHastenParam param = new SuperviseHastenParam();
        
        param.setMemberId(memberId);
        param.setContent((String)params.get("content"));
        param.setSummaryId(summaryId);
        
        CtpSuperviseDetail detail = this.getSupervise(summaryId); 
        //即使superviseId为空，也要从数据库中查询一次，当CtpSuperviseDetail有值时赋值，没值则不是督办人，传入空值
        if (detail!=null){
            param.setSuperviseId(detail.getId().toString());
        }
        boolean isAllhasten = "true".equals(params.get("isAllHasten"));
        List<Long> personIds = (List<Long>)params.get("personIds");
        if (isAllhasten) {
        	if(personIds.size()>0){
        		param.setPersonIds(personIds);
        	}else{
        		List<Long> members = new ArrayList<Long>();
        		//待办事项列表（根据协同id和事项状态获取事项列表）
        		List<CtpAffair> affairList = affairManager.getAffairs(summaryId,StateEnum.col_pending);
        		
        		boolean _isNotNull = !"".equals(params.get("activityId")) && !"null".equals(params.get("activityId")) && params.get("activityId") != null  ; 
        		Long  _activityId =  _isNotNull ? Long.parseLong((String) params.get("activityId")) : null;
        		
        		for(CtpAffair affair : affairList) {
        			if(_activityId != null){
        				if(_activityId.equals(affair.getActivityId())){
        					members.add(affair.getMemberId());
        				}
        			}else{
        				members.add(affair.getMemberId());//处理人列表一键催办
        			}
        		}
        		param.setPersonIds(members);
        	}
        } else {
            param.setPersonIds(personIds);
            param.setActivityId((String)params.get("activityId"));
        }
        
        //检查是否要校验人员，移动和PC逻辑不一样， 这里主要是移动的逻辑
        if(processId != null && "true".equals(params.get("checkUser"))){
            
            String appName = null;
            if(isEdoc){
                appName = ApplicationCategoryEnum.edoc.name();
            }else {
                appName = ApplicationCategoryEnum.collaboration.name();
            }
            WorkFlowAppExtendManager hastenManager = WorkFlowAppExtendInvokeManager.getAppManager(appName);
            if(hastenManager != null){
                List<Long> memberIds = param.getPersonIds();
                int memberLength = memberIds.size();
                int invalidCount = 0;
                for (Long mId : memberIds) {
                    com.seeyon.ctp.workflow.vo.User user = hastenManager.getUser(String.valueOf(mId), processId, appName);
                    if(user == null){
                        invalidCount++;
                    }
                }
                if(memberLength == invalidCount){
                    //全部无效，给前端提示: 没有后续节点
                    return ResourceUtil.getString("workflow.deletePeople.noChildren");
                }
            }
        }
        
        
        param.setIsAllHasten(isAllhasten);
        param.setTitle(subject);
        
        boolean transRet = transHasten(param);
        String ret = ResourceUtil.getString("supervise.hasten.success.label"); //催办成功
        if(!transRet){
            ret = ResourceUtil.getString("supervise.hasten.fail.label"); //催办失败
        }
        
        return ret;
	}
	
	public boolean saveHastenLog(SuperviseHastenParam hastenParam) throws BusinessException{
		//保存督办人员，记录催办日志
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean transHasten(SuperviseHastenParam hastenParam) throws BusinessException{
		
		Long memberId = hastenParam .getMemberId();
		Long summaryId = hastenParam.getSummaryId();
		List<Long> personIds = hastenParam.getPersonIds();
		String superviseId = hastenParam.getSuperviseId();
		String content = hastenParam.getContent() ; 
		String title = hastenParam.getTitle() ; 
		
		//催办结果
		boolean ret = true;
		
		Long _superviseId = null;
        if(Strings.isNotEmpty(personIds)){
            //如果不存在督办的话，保存督办
            if(Strings.isBlank(superviseId)) {
                _superviseId = save(new SuperviseMessageParam(), title, memberId, "", "", null, new Date(), 0, summaryId, new HashMap<String, Object>());
            }else{
                _superviseId = Long.valueOf(superviseId);
            }

            //进行催办,发送催办消息
            List<Long> notHas = new ArrayList<Long>();
            try{
            	notHas = hasten(hastenParam);
            }catch(Throwable e){
            	log.error("",e);
            	ret = false;
            }
        	
        	//督办人进行督办时，才增加催办次数，并使列表中对应的督办次数在督办完成后同步更新
        	if(_superviseId != null) {
				//增加的内容 = 新集合 - (新集合与旧集合的交集)
				Collection<Long> common = CollectionUtils.intersection(personIds, notHas);
				List<Long> result = new ArrayList<Long>(CollectionUtils.subtract(personIds, common));
        		saveLog(_superviseId, AppContext.getCurrentUser().getId(), result, content);
        	}
        }
        return ret;
	}
	/**
	 * 催办，发送催办消息给相关人
	 */
	@SuppressWarnings("unchecked")
	public List<Long>  hasten(SuperviseHastenParam hastenParam)  throws BusinessException{
	    int additional_remarkFlag = Strings.isBlank(hastenParam.getContent()) ? 0 : 1;
        User user =AppContext.getCurrentUser();
        List<Long> memberIds = hastenParam.getPersonIds();//所催办的人员
        
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        //如果催办所有的节点则查询所有的待办事项
        if (hastenParam.getIsAllHasten()) {
        	//待办事项列表（根据协同id和事项状态获取事项列表）
        	affairs = affairManager.getAffairs(hastenParam.getSummaryId(),StateEnum.col_pending);
        } else {
        	affairs = affairManager.getAffairsByObjectIdAndNodeId(hastenParam.getSummaryId(), Long.valueOf(hastenParam.getActivityId()));
        }
        Set<Long> canHasMember = new HashSet<Long>();//该协同目前待办事项状态的人员集合
        List<Long> affairids = new ArrayList<Long>();
        if(Strings.isNotEmpty(affairs)){
        	CtpAffair hastenAffair = affairs.get(0);
            String subject = hastenAffair.getSubject();
            Set<Long> existMemberIds = new HashSet<Long>();//已经发送消息的人员
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();//所有消息接收人
			Set<MessageReceiver> agentReceivers = new HashSet<MessageReceiver>();//所有消息接收人
			
			for (CtpAffair affair : affairs) {
				Long memberId = affair.getMemberId();
				canHasMember.add(memberId);
				affairids.add(affair.getId());
				
				if (!memberIds.contains(memberId)|| existMemberIds.contains(memberId) || affair.isDelete()) {
					continue;
				}
				existMemberIds.add(memberId);

				//判断当前的代理人是否有效
				Long agentId = null;
				if(affair.getApp() == ApplicationCategoryEnum.edocSend.getKey()
						|| affair.getApp() == ApplicationCategoryEnum.edocRec.getKey()
						|| affair.getApp() == ApplicationCategoryEnum.edocSign.getKey()){
					List<AgentModel> agentToModels = MemberAgentBean.getInstance().getAgentModelToList(memberId);
			        if(agentToModels != null && !agentToModels.isEmpty()){
			        	for(AgentModel agentModel : agentToModels){
			        		if(affair.getReceiveTime().compareTo(agentModel.getStartDate()) >= 0 && affair.getReceiveTime().before(agentModel.getEndDate())){
			        			if(agentModel.isHasEdoc()){
			        				agentId = Long.valueOf(agentModel.getAgentId());
			        				break;
			        			}
			        		}
			        	}
			        }
				}else{
					agentId = ColUtil.getAgentMemberId(hastenAffair.getTempleteId(), memberId, affair.getReceiveTime());
				}
				if (agentId != null) {
					add2Receives(agentReceivers, affair, agentId);
				}
				Integer hastenTimes = affair.getHastenTimes();
				if (hastenTimes == null){
				    hastenTimes = 0;
				}
				hastenTimes += 1;
				affair.setHastenTimes(hastenTimes);
				affairManager.updateAffair(affair);//CtpAffair保存催办次数

				add2Receives(receivers, affair, memberId);
			}
			if (hastenAffair.getApp() == ApplicationCategoryEnum.collaboration.getKey()) {
				String forwardMemberId = affairs.get(0).getForwardMember();//转发人
				int forwardMemberFlag = 0;
				String forwardMember = null;
				if (Strings.isNotBlank(forwardMemberId)) {
					try {
						forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
						forwardMemberFlag = 1;
					} catch (Exception e) {
					    log.error(e);
					}
				}
				Integer importantLevel = ColUtil.getImportantLevel(hastenAffair);
				if(Strings.isNotEmpty(agentReceivers)){
				    userMessageManager.sendSystemMessage(new MessageContent("col.hasten", user.getName(), subject,
                            forwardMemberFlag, forwardMember, additional_remarkFlag, hastenParam.getContent()).setImportantLevel(hastenAffair.getImportantLevel()).add("col.agent"),
                            ApplicationCategoryEnum.valueOf(hastenAffair.getApp()),user.getId(), agentReceivers, importantLevel);
				}
				if(Strings.isNotEmpty(receivers)){
				    userMessageManager.sendSystemMessage(new MessageContent("col.hasten", user.getName(), subject,
                            forwardMemberFlag, forwardMember, additional_remarkFlag, hastenParam.getContent()).setImportantLevel(hastenAffair.getImportantLevel()),
                            ApplicationCategoryEnum.valueOf(hastenAffair.getApp()),user.getId(), receivers, importantLevel);
				}

			} else if (hastenAffair.getApp() == ApplicationCategoryEnum.info.getKey()) {// 信息报送
				MessageContent messageContent = new MessageContent("info.hasten", subject, user.getName(),hastenParam.getContent(), hastenAffair.getApp()).
				        setImportantLevel(hastenAffair.getImportantLevel());
				messageContent.setResource("com.seeyon.v3x.info.resources.i18n.InfoResources");
				userMessageManager.sendSystemMessage(messageContent,ApplicationCategoryEnum.valueOf(hastenAffair.getApp()),user.getId(), receivers);
				if(Strings.isNotEmpty(agentReceivers)){
				    MessageContent messageContent1 = new MessageContent("info.hasten", subject, user.getName(),hastenParam.getContent(), hastenAffair.getApp()).
                    setImportantLevel(hastenAffair.getImportantLevel());
                    messageContent.setResource("com.seeyon.v3x.info.resources.i18n.InfoResources");
                    userMessageManager.sendSystemMessage(messageContent1.add("col.agent"),ApplicationCategoryEnum.valueOf(hastenAffair.getApp()),user.getId(), agentReceivers);
				}
			} else {// 公文
				String forwardMemberId = affairs.get(0).getForwardMember();//转发人
				int forwardMemberFlag = 0;
				String forwardMember = null;
				if (Strings.isNotBlank(forwardMemberId)) {
					try {
						forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
						forwardMemberFlag = 1;
					} catch (Exception e) {
					    log.error(e);
					}
				}
				userMessageManager.sendSystemMessage(new MessageContent("edoc.hasten", user.getName(),hastenAffair.getApp(),subject,
						forwardMemberFlag, forwardMember,additional_remarkFlag, hastenParam.getContent()).setImportantLevel(hastenAffair.getImportantLevel()),
						ApplicationCategoryEnum.valueOf(hastenAffair.getApp()),user.getId(), receivers, hastenAffair.getApp());

			    if(Strings.isNotEmpty(agentReceivers)){
			        userMessageManager.sendSystemMessage(new MessageContent("edoc.hasten", user.getName(),hastenAffair.getApp(),subject,
	                        forwardMemberFlag, forwardMember,additional_remarkFlag, hastenParam.getContent()).setImportantLevel(hastenAffair.getImportantLevel()).add("col.agent"),
	                        ApplicationCategoryEnum.valueOf(hastenAffair.getApp()),user.getId(), agentReceivers, hastenAffair.getApp());
			    }
			}
			SuperviseEvent  event = new SuperviseEvent(this);
			event.setAffairs(affairs);
			event.setBjectID(hastenParam.getSummaryId()+"");
			event.setUser(user);
			EventDispatcher.fireEvent(event);
			
        }
		Collection<Long> inter = CollectionUtils.intersection(memberIds,canHasMember);
		Collection<Long> result = CollectionUtils.subtract(memberIds,inter);
		
		List<Long> notHastens = new ArrayList<Long>(result);
		
		if(Strings.isNotEmpty(notHastens)){
			StringBuilder logInfo = new StringBuilder();
			logInfo.append("superviselog_______________________：").append("\r\n");
			logInfo.append("affairids:"+Strings.join(affairids,",")).append("\r\n");
			logInfo.append("notHastens:"+Strings.join(notHastens,","));
			log.error(logInfo.toString());
		}
        return notHastens;
    }

	private static Integer getApp(int app){
	    if(ApplicationCategoryEnum.edoc.key() == app ||
            ApplicationCategoryEnum.edocSend.key() == app ||
            ApplicationCategoryEnum.edocRec.key() == app ||
            ApplicationCategoryEnum.edocSign.key() == app ||
            ApplicationCategoryEnum.edocRegister.key() == app ||
            ApplicationCategoryEnum.exSend.key() == app ||
            ApplicationCategoryEnum.exSign.key() == app ||
            ApplicationCategoryEnum.exchange.key() == app ||
            ApplicationCategoryEnum.edocRecDistribute.key() == app){
	        return ApplicationCategoryEnum.edoc.key();
	    }
	    else{
	        return app;
	    }
	}

    /**
     * @param receivers
     * @param urlKey
     * @param affair
     * @param memberId
     */
    private void add2Receives(Set<MessageReceiver> receivers, CtpAffair affair, Long memberId) {
    	if(affair != null){
    		if (affair.getApp() != ApplicationCategoryEnum.collaboration.getKey()) {
            	if (affair.getApp() == ApplicationCategoryEnum.info.getKey()) {// 信息报送
            	    String urlKey = "message.link.info.pending";
            		receivers.add(new MessageReceiver(affair.getId(),memberId, urlKey, affair.getId().toString()));
            	} else {// 公文及其它
            	    String urlKey = "message.link.edoc.pending";
            		receivers.add(new MessageReceiver(affair.getId(),memberId, urlKey, affair.getId().toString()));
            	}
            } else {// 协同
                String urlKey = "message.link.col.pending";
                //2那个位置上给致信那边一个标记知道是督办过去的消息
                MessageReceiver messageReceiver = new MessageReceiver(affair.getId(), memberId,urlKey, affair.getId().toString(),"","isHasten");
                receivers.add(messageReceiver);
            }
    	}
    }
	/**
	 * 1、在协同督办主表中将督办次数加一
	 * 2、保存催办人员
	 * 3、保存催办日志信息
	 */
	public void saveLog(long superviseId,long userId,List<Long> receivers,String content) throws BusinessException{
		List<Long> receiversTemp=new ArrayList<Long>();
		if(receivers != null && receivers.isEmpty()){
    		return ;
    	}else{
    		receiversTemp=receivers;
    	}
    	// DB Log
    	superviseDao.saveDbLog(superviseId);//count+1
    	CtpSuperviseLog superviseLog = new CtpSuperviseLog();
    	superviseLog.setIdIfNew();
    	superviseLog.setSender(userId);
    	superviseLog.setSendTime(new Date());
    	superviseLog.setSuperviseId(superviseId);
    	superviseLog.setType(SuperviseEnum.suerviseLogType.hasten.ordinal());
    	superviseLog.setContent(content);
    	List<CtpSuperviseReceiver> lists = new ArrayList<CtpSuperviseReceiver>();
    	for(long receiverId:receiversTemp) {
    		CtpSuperviseReceiver receiver = new CtpSuperviseReceiver();
    		receiver.setIdIfNew();
    		receiver.setLogId(superviseLog.getId());
    		receiver.setReceiver(receiverId);
    		lists.add(receiver);
    	}
    	superviseDao.saveCtpSuperviseReceiverAll(lists);
    	superviseDao.save(superviseLog);
    }

	public void updateStatusBySummaryIdAndType(SuperviseEnum.superviseState state,Long summaryId,SuperviseEnum.EntityType superviseType) throws BusinessException{
		superviseDao.updateDetailStatusByModuleIdAndType(summaryId,state,superviseType);
	}
//    @Override
//    public List<SuperviseModelVO> getSuperviseModelList(FlipInfo filpInfo, Map<String, String> map)
//            throws BusinessException {
//        SuperviseHandler handler = getSuperviseHandler(ModuleType.collaboration);
//        return handler.getSuperviseModelList(filpInfo, map);
//    }

    @Override
    public boolean isSupervisor(Long userId, Long summaryId) throws BusinessException{
        return this.superviseDao.isSupervisor(userId, summaryId);
    }

    @Override
    public void updateDescDate(Long superviseId, String desc, Date awakeDate) throws BusinessException{
        CtpSuperviseDetail detail = superviseDao.getSuperviseDetail(superviseId);
        if(detail != null) {
            if(awakeDate!=null){
                detail.setAwakeDate(awakeDate);
            }
            detail.setDescription(desc);
            superviseDao.update(detail);
        }
    }
    public int getSuperviseListCount(Map<String, List<Object>> param) throws BusinessException {
        getSuperviseListParam(param);
        return superviseDao.getSuperviseListCount(param);
    }

    private void getSuperviseListParam(Map<String, List<Object>> param) throws BusinessException {
        //督办中
        int status = SuperviseEnum.superviseState.supervising.ordinal();
        if(param.get("userId") == null){
	        List<Object> list = new ArrayList<Object>(1);
	        list.add(AppContext.getCurrentUser().getId().toString());
	        param.put("userId", list);
        }
        List<Object> listStatus = new ArrayList<Object>(1);
        listStatus.add(status);
        param.put("status", listStatus);
        //督办类型
        List<Object> entityType = new ArrayList<Object>();
        Object type = param.get("entityType");
        if(null != type){
            List types = (List) type;
            for(int i=0;i<types.size();i++){
                entityType.add(Integer.parseInt(types.get(i).toString()));
            }
        }else{
            entityType.add(SuperviseEnum.EntityType.summary.ordinal()); //协同
            entityType.add(SuperviseEnum.EntityType.edoc.ordinal());//公文
        }
        param.put("entityType", entityType);
        
        try {
        	List<Object> templateList = param.get("templete");
        	if(null != templateList && templateList.size()>0){
        		String soureceTemplatePendingValue = param.get("templete").get(0).toString();
        		List<Object> list = new ArrayList<Object>();
        		soureceTemplatePendingValue = templateManager.getAllTemplateCategoryIdsAndTemplateIds(soureceTemplatePendingValue);
        		list.add(soureceTemplatePendingValue);
        		param.put("templete", list);
        	}
		} catch (BusinessException e) {
			 log.error("",e);
		}
    }

    @Override
    public FlipInfo getSuperviseList4Portal(FlipInfo filpInfo, Map<String, List<Object>> param) throws BusinessException {
    	getSuperviseListParam(param);
    	
        List<Object> result = superviseDao.getSuperviseList(filpInfo, param);
        if(result != null && result.size()>0){
        	  /**
             * 用于显示当前待办人
             * 将affairs中公文和协同的取出来放在不同的List中，然后通过in sql语句将各自的summary的currentNodesInfo取出保存在PendingRow中
             */
            List<Long> collIdList = new ArrayList<Long>();
            List<Long> edocIdList = new ArrayList<Long>();
            for (Object obj:result) {
            	 Object[] res = (Object[])obj;
            	 Integer app = res[7] == null ? null : (Integer)res[7];
                 if(app != null){
                 	Long summaryId = res[3] == null ? null : (Long)res[3];
                 	if(app == 1){
                 		collIdList.add(summaryId);
                 	}else{
                 		edocIdList.add(summaryId);
                 	}
                 }
            }
            Map<Long, SuperviseAppInfoBO> appInfoMaps = new HashMap<Long,SuperviseAppInfoBO>();
            if (AppContext.hasPlugin("collaboration") && collIdList.size()>0) {
            	appInfoMaps.putAll(getSuperviseHandler(ModuleType.collaboration).getAppInfo(collIdList));
            }
    		if (AppContext.hasPlugin("edoc") && edocIdList.size()>0) {
    			appInfoMaps.putAll(getSuperviseHandler(ModuleType.edoc).getAppInfo(edocIdList));
    		}
    		List<SuperviseModelVO> modelList = null;
    		String rowStr = null;
    		boolean edocMarkFlag = true;
    		boolean sendUnitFlag = true;
    		if(param.get("rowList") != null ){
    			rowStr = (String)((List<Object>)param.get("rowList")).get(0);
	    		if(rowStr.indexOf("edocMark") == -1){
	    			edocMarkFlag = false;
	    		} 
	    		if(rowStr.indexOf("sendUnit") == -1){
	    			sendUnitFlag= false;
	    		}
    		}
    		
            if(result != null && !result.isEmpty()){
                modelList = new ArrayList<SuperviseModelVO>();
                Date now = new Date();
                SuperviseModelVO model =null;
                for (int i = 0; i < result.size(); i++) {
                    Object[] res = (Object[]) result.get(i);
                    if(res[7] == null){ //app == null表示没升级的数据，先屏蔽。
                    	continue;
                    }
                    model = new SuperviseModelVO();
                    int j = 0;
                    model.setId((Long)res[j++]);
                    Date awakeDate = (Date)res[j++];
                    if(awakeDate != null && now.after(awakeDate)){
                        model.setIsRed(true);
                    }
                    //督办期限
                    model.setAwakeDate(awakeDate);
                    model.setCount((Long)res[j++]);
                    Long sumId = (Long)res[j++];
                    model.setSummaryId(sumId);
                    model.setContent(Strings.toHTML((String)res[j++]));
                    //状态（已结办、未结办）
                    model.setStatus((Integer)res[j++]);
                    model.setHasWorkflow(false);
                    //类型(协同1、公文2)对应枚举SuperviseEnum
                    int theEntityType = (Integer)res[j++];
                    model.setEntityType(theEntityType);
                    //类型(协同1、公文4) 对应枚举ModuleType
                    Integer app = (Integer)res[j++];
                    model.setAppType(app);
                    String title = (String)res[j++];
                    
                    SuperviseAppInfoBO superviseAppInfoBO  = appInfoMaps.get(model.getSummaryId());
                    if(superviseAppInfoBO != null){
                    	model.setCurrentNodesInfo(superviseAppInfoBO.getCurrentNodesInfo());
                        model.setCaseId(superviseAppInfoBO.getCaseId());
                        model.setProcessId(superviseAppInfoBO.getProcessId());
                        model.setTemplateId(superviseAppInfoBO.getTemplateId() == null ? "":String.valueOf(superviseAppInfoBO.getTemplateId()));
                        model.setDeadlineDatetime(superviseAppInfoBO.getDeadlineDatetime());
                        model.setWorkflowTimeout(superviseAppInfoBO.isWorkflowTimeout());
                        
                        if(superviseAppInfoBO.getAutoRun() != null && superviseAppInfoBO.getAutoRun()){
                    		title = ResourceUtil.getString("collaboration.newflow.fire.subject",title);
                    	}
                    }
                   
                    model.setAppName(ResourceUtil.getString("application."+model.getAppType()+".label"));
                   
                	
                    model.setTitle(title);
                    model.setSender((Long)res[j++]);
                    model.setSenderName(Functions.showMemberName(model.getSender()));
                    Date startDate = (Date)res[j++];
                    model.setSendDate(startDate);
                    model.setImportantLevel((Integer)res[j++]);
                    model.setResendTime((Integer)res[j++]);
                    model.setForwardMember((String)res[j++]);
                    model.setBodyType(res[j++].toString());
                    Object identifier = res[j++];
                    if(identifier != null){
                        Boolean hasAtt = IdentifierUtil.lookupInner(identifier.toString(),0, '1');
                        model.setHasAttachment(hasAtt);
                    } else {
                        model.setHasAttachment(false);
                    }
                    model.setAffairId((Long)res[j++]);
                    String extProps = (String)res[j++];
                    model.setExtProps(extProps);
                    //公文--扩展字段 公文文号和发文单位
            	    if(app != null &&(edocMarkFlag || sendUnitFlag) && 
                            (app.equals(ApplicationCategoryEnum.edocSend.getKey())||
                             app.equals(ApplicationCategoryEnum.edocRec.getKey())||
                             app.equals(ApplicationCategoryEnum.edocSign.getKey())||
                             app.equals(ApplicationCategoryEnum.exSend.getKey())||
                             app.equals(ApplicationCategoryEnum.exSign.getKey()))){
                        Map<String,Object> extMap = getExtProperty(extProps);
                        String edocMark = Strings.escapeNULL((String)extMap.get(AffairExtPropEnums.edoc_edocMark.name()), "");
                        String sendUnit = Strings.escapeNULL((String)extMap.get(AffairExtPropEnums.edoc_sendUnit.name()), "");

                        model.setEdocMark(edocMark);
                        model.setSendUnit(sendUnit);
                    }
                    model.setCoverTime((Boolean)res[j++]);
                    modelList.add(model);
                }
            }
            filpInfo.setData(modelList);
        }
      
        return filpInfo;
    }
    


    private static Map<String,Object> getExtProperty(String extProps){
        Map<String,Object> map = null;
        if(Strings.isNotBlank(extProps)){
        	map = (Map<String, Object>) XMLCoder.decoder(extProps);
        }
        else{
            map = new HashMap<String, Object>();
        }

        return map;
    }


	public void saveSuperviseTemplateRole(Long templateId, String roles) {
		String[] strs = roles.split(",");
		if(null != templateId){
			try {
				superviseDao.deleteAllTemplateRole(templateId);
			} catch (Exception e) {
				log.error("删除模板角色出错："+e.getLocalizedMessage(),e);
			}
		}
		for(String str : strs){
			if(!Strings.isBlank(str)){
				CtpSuperviseTemplateRole role = new CtpSuperviseTemplateRole();
				role.setIdIfNew();
				role.setSuperviseTemplateId(templateId);
				role.setRole(str);
//			colSuperviseTemplateRoleDao.save(role);
				DBAgent.saveOrUpdate(role);
			}
		}

	}
	public Long saveForTemplate(String title,
			Long senderId,
			String supervisorNames,
			List<Long> supervisorIds,
			Long templateDateTerminal,
			Date awakeDate,
			int entityType,
			Long entityId) throws BusinessException{

		CtpSuperviseDetail detail = new CtpSuperviseDetail();
		detail.setIdIfNew();
		detail.setCount(0L);

		saveSuperviseDetailAndSupervisors(title, senderId, supervisorNames,supervisorIds, templateDateTerminal,awakeDate, entityType, entityId, detail);
		return detail.getId();
	}
	
	public void updateForTemplate(String title,
			Long senderId,
			String supervisorNames,
			List<Long> supervisorIds,
			Long templateDateTerminal,
			Date awakeDate,
			int entityType,
			Long entityId) throws BusinessException {
		CtpSuperviseDetail detail = this.getSupervise(entityType, entityId);
		if(detail == null) {
			this.saveForTemplate(title, senderId, supervisorNames, supervisorIds, templateDateTerminal,awakeDate, entityType, entityId);
			return;
		}
		superviseDao.deleteSupervisorsByDetailId(detail.getId());//根据督办明细的ID删除该督办下的所有督办人记录

		saveSuperviseDetailAndSupervisors(title, senderId, supervisorNames,supervisorIds, templateDateTerminal, awakeDate,entityType, entityId, detail);
	}
	
	private void saveSuperviseDetailAndSupervisors(String title,
			Long senderId,
			String supervisorNames,
			List<Long> supervisorIds,
			Long templateDateTerminal,
			Date awakeDate,
			int entityType,
			Long entityId,
			CtpSuperviseDetail detail)throws BusinessException {
		detail.setTitle(title);
		detail.setSenderId(senderId);
		detail.setSupervisors(supervisorNames);
		detail.setTemplateDateTerminal(templateDateTerminal);
		detail.setAwakeDate(awakeDate);
		detail.setStatus(SuperviseEnum.superviseState.supervising.ordinal());
		detail.setEntityId(entityId);
		detail.setEntityType(entityType);

		Map<Long,Long> hash = new HashMap<Long,Long>();
		if(supervisorIds != null) {
			for(Long supervisorId:supervisorIds) {
				hash.put(supervisorId, supervisorId);
			}
		}
		if(supervisorIds != null) {
			List<CtpSupervisor> supervisors = new ArrayList<CtpSupervisor>();
			for(Long supervisorId:supervisorIds) {
				CtpSupervisor colSupervisor = new CtpSupervisor();
				colSupervisor.setIdIfNew();
				colSupervisor.setSuperviseId(detail.getId());
				colSupervisor.setSupervisorId(supervisorId);
				supervisors.add(colSupervisor);
			}
			DBAgent.saveAll(supervisors);
		}

		superviseDao.saveOrUpdateDetail(detail);
	}

	public void updateSuperviseTemplateRole(long templateId, String supervisors) throws BusinessException{
		superviseDao.deleteAllTemplateRole(templateId);
		if(supervisors != null && !"".equals(supervisors))
			saveSuperviseTemplateRole(templateId, supervisors);
	}
    @Override
    public boolean saveSuperviseByCopyTemplete(long senderId, ColSummary newSummary, Long templeteId)throws BusinessException {
        V3xOrgMember sender = orgManager.getMemberById(senderId);
    	CtpSuperviseDetail detail = this.getSupervise(SuperviseEnum.EntityType.template.ordinal(), templeteId);
        if(detail != null) {
            Date superviseDate = null;
            Long terminalDate = detail.getTemplateDateTerminal();
            if(null!=terminalDate){
                superviseDate = Datetimes.addDate(new Date(), terminalDate.intValue());
            }else if(detail.getAwakeDate() != null) {
                superviseDate = detail.getAwakeDate();
            }
            List<CtpSupervisor> supervisors = this.getSupervisors(detail.getId());
            Set<Long> sIdSet = new HashSet<Long>();
            for(CtpSupervisor supervisor:supervisors){
                sIdSet.add(supervisor.getSupervisorId());
            }
            List<CtpSuperviseTemplateRole> roleList = this.findRoleByTemplateId(templeteId);

            V3xOrgRole orgRole = null;
            for(CtpSuperviseTemplateRole role : roleList){
                if(null==role.getRole() || "".equals(role.getRole())){
                    continue;
                }
                if(role.getRole().toLowerCase().equals(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.toLowerCase())){
                    sIdSet.add(sender.getId());
                }
                if(role.getRole().toLowerCase().equals(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.toLowerCase() + OrgConstants.Role_NAME.DepManager.name().toLowerCase())){
                    orgRole = orgManager.getRoleByName(OrgConstants.Role_NAME.DepManager.name(), sender.getOrgAccountId());
                    if(null!=orgRole){
                        List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(sender.getId());
                        for(V3xOrgDepartment dep : depList){
                            List<V3xOrgMember> managerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
                            for(V3xOrgMember mem : managerList){
                                sIdSet.add(mem.getId());
                            }
                        }
                    }
                }
                if(role.getRole().toLowerCase().equals(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.toLowerCase() + WorkFlowMatchUserManager.ORGENT_META_KEY_SUPERDEPMANAGER.toLowerCase())){
                    orgRole = orgManager.getRoleByName(WorkFlowMatchUserManager.ORGENT_META_KEY_SUPERDEPMANAGER, sender.getOrgAccountId());
                    if(null!=orgRole){
                        List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(sender.getId());
                        for(V3xOrgDepartment dep : depList){
                        List<V3xOrgMember> superManagerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
                            for(V3xOrgMember mem : superManagerList){
                                sIdSet.add(mem.getId());
                            }
                        }
                    }
                }
            }
            if(!sIdSet.isEmpty()){
              //  long[] ids = Long.valueOf[sIdSet.size()];
                List<Long> ids =  new ArrayList<Long>();
                StringBuffer nameBf = new StringBuffer();
                int i = 0;
                for (Long id : sIdSet) {
                    V3xOrgMember mem = orgManager.getMemberById(id);
                    if(mem!=null){
                        ids.add(id);
                        if(nameBf.length() > 0){
                            nameBf.append(",");
                        }
                        nameBf.append(mem.getName());
                    }
                }
                SuperviseMessageParam smp = new SuperviseMessageParam();
                smp.setImportantLevel(newSummary.getImportantLevel());
                smp.setForwardMember(newSummary.getForwardMember());
                smp.setSendMessage(true);
                smp.setSubject(newSummary.getSubject());
                smp.setMemberId(newSummary.getStartMemberId());
                this.save(smp,detail.getTitle(), sender.getId(), sender.getName(), nameBf.toString(),
                        ids, superviseDate, SuperviseEnum.EntityType.summary.ordinal(),
                        newSummary.getId(), new HashMap<String, Object>());
                return true;
            }
        }
        return false;
    }
    @Override
    public void deleteLogs(long entityId) throws BusinessException {
        this.superviseDao.deleteRemindersLog(entityId);
    }
    public void deleteRemindersLog(long moduleId) throws BusinessException {
        this.superviseDao.deleteRemindersLog(moduleId);
    }
    
    @Override
    public void updateStatus2Cancel(Long entityId) throws BusinessException{
        this.superviseDao.updateStatus2Cancel(entityId);
    }
	@Override
	public void deleteAllInfoByTemplateId(long templateId) throws BusinessException {
		//detail
		CtpSuperviseDetail detail = this.getSupervise(SuperviseEnum.EntityType.template.ordinal(),templateId);
		//人员
		if(null != detail){
			this.deleteSupervisorsByDetailId(detail.getId());
			DBAgent.delete(detail);
		}
		//role
		this.deleteAllTemplateRole(templateId);
	}

	@Override
	public SuperviseSetVO parseTemplateSupervise(Long templateId)throws BusinessException {
		SuperviseSetVO ssvo = new SuperviseSetVO();
		if(templateId != null){
			CtpSuperviseDetail detail = getSupervise(SuperviseEnum.EntityType.template.ordinal(),templateId);
			if(null != detail){
				List<CtpSupervisor> supervisors = getSupervisors(detail.getId());
				Set<Long> idSets = new HashSet<Long>();
				Set<String> nameSets = new HashSet<String>();
				for(CtpSupervisor ctps: supervisors){
					idSets.add(ctps.getSupervisorId());
					V3xOrgMember orgMember = orgManager.getMemberById(ctps.getSupervisorId());
					nameSets.add(orgMember.getName());
				}
				//督办角色
				List<CtpSuperviseTemplateRole> roleList = findRoleByTemplateId(templateId);
				Set<String> roleSet = new HashSet<String>();
				for(CtpSuperviseTemplateRole srole: roleList){
					roleSet.add(srole.getRole());
				}

				ssvo.setDetailId( detail.getId());
		        ssvo.setSupervisorIds(Strings.join(idSets, ","));
		        ssvo.setSupervisorNames(Strings.join(nameSets, "、"));
		        ssvo.setRole(Strings.join(roleSet, ","));
		        ssvo.setTitle(detail.getTitle());


		        ssvo.setTemplateDateTerminal(detail.getTemplateDateTerminal());
			}
		}
	    AppContext.putRequestContext("_SSVO",ssvo);
	    return ssvo;
	}

	@Override
	public SuperviseSetVO parseProcessSupervise(Long moduleId,Long templateId,Long startMemberId,SuperviseEnum.EntityType senum)throws BusinessException {
		 SuperviseSetVO ssvo = new SuperviseSetVO();

		 Set<Long> idSets = new HashSet<Long>();
		 Set<String> nameSets = new HashSet<String>();
		 //模板的督办人员ID串
		 Set<Long> templateIdSets = new HashSet<Long>();
		 if(moduleId != null){

			 CtpSuperviseDetail detail = getSupervise(senum.ordinal(),moduleId);

			 if(detail != null) {
				 List<CtpSupervisor> supervisorsList = getSupervisors(detail.getId());
				 for(CtpSupervisor supervisor:supervisorsList){
					 idSets.add(supervisor.getSupervisorId());
					 nameSets.add(Functions.showMemberName(supervisor.getSupervisorId()));
				 }

				 if(templateId != null){
					 templateIdSets = parseTemplateSupervisorIds(templateId, startMemberId);
					 for(Long l : templateIdSets){
					     if(!idSets.contains(l)){
					         idSets.add(l);
		                     nameSets.add(Functions.showMemberName(l));
					     }
					 }
				 }

				 ssvo.setDetailId( detail.getId());
				 ssvo.setSupervisorIds(Strings.join(idSets, ","));
				 ssvo.setSupervisorNames(Strings.join(nameSets, ","));
				 ssvo.setAwakeDate(Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));
				 ssvo.setTitle(detail.getTitle());

				 ssvo.setTemplateDateTerminal(detail.getTemplateDateTerminal());
				 ssvo.setUnCancelledVisor(Strings.join(templateIdSets, ","));
			 }
		 }
	     AppContext.putRequestContext("_SSVO",ssvo);



	     return ssvo;
	 }
	public Set<Long> parseTemplateSupervisorIds(Long templateId,Long startMemberId) throws BusinessException {
		CtpSuperviseDetail templateDetail = getSupervise(SuperviseEnum.EntityType.template.ordinal(),templateId);
		return parseTemplateSupervisorIds(templateDetail, templateId, startMemberId);
	}
	/**
	 * 解析模板所设置的督办人
	 * @param templateId
	 * @param startMemberId
	 * @return
	 * @throws BusinessException
	 */
	public Set<Long> parseTemplateSupervisorIds(CtpSuperviseDetail templateDetail,Long templateId,Long startMemberId) throws BusinessException {
		Set<Long> templateIdSets = new HashSet<Long>();
		if(null != templateDetail){
		    List<CtpSupervisor> tempVisors = getSupervisors(templateDetail.getId());
		    for(CtpSupervisor ts : tempVisors){
		        templateIdSets.add(ts.getSupervisorId());
		    }
		    List<CtpSuperviseTemplateRole> roleList = findRoleByTemplateId(templateId);
		    V3xOrgRole orgRole = null;
		    V3xOrgMember starter = orgManager.getMemberById(startMemberId);
		    if(null!=starter){
		        for (CtpSuperviseTemplateRole role : roleList) {
		            if (null == role.getRole() || "".equals(role.getRole())) {
		                continue;
		            }
		            if (role.getRole().toLowerCase().equals(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.toLowerCase())) {
		                templateIdSets.add(starter.getId());
		            }
		            if (role.getRole().toLowerCase().equals(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.toLowerCase() + OrgConstants.Role_NAME.DepManager.toString().toLowerCase())) {
		               
		            	Long loginAccount = starter.getOrgAccountId();
		            	User user = AppContext.getCurrentUser() ;
		            	
		            	if(user != null ){
		            		loginAccount = AppContext.getCurrentUser().getLoginAccount();
		            	}
		            	orgRole = orgManager.getRoleByName(OrgConstants.Role_NAME.DepManager.name(),loginAccount);
		               
		            	if (null != orgRole) {
		                    List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(starter.getId());
		                    boolean titleFlag = true;//存在兼职人员  这里取出来的 depList  是个list
		                    for (V3xOrgDepartment dep : depList) {
		                        List<V3xOrgMember> managerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
		                        if(Strings.isNotEmpty(managerList)){
		                        	titleFlag = false;
		                        }
		                        for (V3xOrgMember mem : managerList) {
		                            templateIdSets.add(mem.getId());
		                        }
		                    }
		                    if(titleFlag){
		                    	AppContext.putRequestContext("noDepManager", true);
		                    }
		                }
		            }
		        }
		    }
		}
		return templateIdSets;
	}
	 public void saveOrUpdateSupervise4Process(SuperviseMessageParam smp,Long moduleId,SuperviseEnum.EntityType superviseType) {
		  @SuppressWarnings("unchecked")
		  Map<String,Object> superviseMap = (Map<String,Object>)ParamUtil.getJsonDomain("superviseDiv");
		  SuperviseSetVO ssvo = (SuperviseSetVO)ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
		  if(smp == null){
			  smp = new SuperviseMessageParam();
		  }
		  saveOrUpdateSupervise4Process(ssvo, smp, moduleId, superviseType);
	 }
	 /**
     * 根据页面的参数 进行督办设置
     * @param colSummary
     */
	 @Override
	 public void saveOrUpdateSupervise4Process(SuperviseSetVO ssvo,SuperviseMessageParam smp,Long moduleId,SuperviseEnum.EntityType superviseType) {
		 this.saveOrUpdateSupervise4Process(ssvo, smp, moduleId, superviseType, new HashMap<String, Object>());
     }

	 /**
     * 根据页面的参数 进行督办设置
     * @param colSummary
     */
	 @Override
	 public void saveOrUpdateSupervise4Process(SuperviseSetVO ssvo,SuperviseMessageParam smp,Long moduleId,SuperviseEnum.EntityType superviseType, Map<String, Object> param) {

		if(smp == null){
			  smp = new SuperviseMessageParam();
		  }
     	User user = AppContext.getCurrentUser();

        try{

	        if (Strings.isBlank(ssvo.getSupervisorIds()) && ssvo.getDetailId()!= null) {//删除督办信息
        		/*CtpSuperviseDetail detail = getSupervise(superviseType.ordinal(), moduleId);
        		if(detail!=null){
        			deleteSuperviseAllAndSendMsgById(detail.getId(),moduleId);
        		}*/
        		
        		deleteSuperviseAllAndSendMsgById(ssvo.getDetailId(),moduleId,smp);
        		
	        }else if(Strings.isNotBlank(ssvo.getAwakeDate())){ //真正的进行了督办设置，则进行督办相关信息的保存与更新
	            Date date = Datetimes.parse(ssvo.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle);
	            String[] idsStr = ssvo.getSupervisorIds().split(",");
	            List<Long> ids = new ArrayList<Long>();
	            for (String id : idsStr) {
	                ids.add(Long.valueOf(id));
	            }
	            if(ssvo.getDetailId() == null){
	            	save(smp, ssvo.getTitle(), user.getId(),
	            			user.getName(), ssvo.getSupervisorNames(), ids, date, superviseType.ordinal(),moduleId, param);
	            }else{
	            	update(smp, ssvo.getTitle(),
	            			user.getId(), user.getName(), ssvo.getSupervisorNames(), ids, date,superviseType.ordinal(), moduleId);        }
	        }
		 }catch(Exception e){
			 log.error("",e);
		 }
    }
	@Override
	public void saveOrUpdateSupervise4Template(Long templateId) throws BusinessException{
		@SuppressWarnings("unchecked")
		Map map = ParamUtil.getJsonDomain("superviseDiv");
		SuperviseSetVO ssvo = (SuperviseSetVO)ParamUtil.mapToBean(map, new SuperviseSetVO(), false);
		saveOrUpdateSupervise4Template(templateId,ssvo);
	}
	@Override
	 public void saveOrUpdateSupervise4Template(Long templateId,SuperviseSetVO ssvo) throws BusinessException {
			User user = AppContext.getCurrentUser();
			String detailId = ssvo.getDetailId() == null ? "" : String.valueOf(ssvo.getDetailId());
			String supervisorIds = ssvo.getSupervisorIds();
			String supervisorNames = ssvo.getSupervisorNames();
			Long templateDateTerminal = ssvo.getTemplateDateTerminal() == null ? null : ssvo.getTemplateDateTerminal();
			String superviseTitle = ssvo.getTitle();

			String role = ssvo.getRole();
	        Date awakeDate = Strings.isBlank(ssvo.getAwakeDate())?null:Datetimes.parse(ssvo.getAwakeDate());
	        if(Strings.isBlank(supervisorIds) && Strings.isBlank(role)){//不存在督办角色和没选择督办人员的时候 清楚督办信息
		    	deleteAllInfoByTemplateId(templateId);
		    }else{
	    		List<Long> ids = new ArrayList<Long>();
	    		if(Strings.isNotBlank(supervisorIds)) {
	    			String[] idsStr = supervisorIds.split(",");
	    			for(String id:idsStr) {
	    				ids.add(Long.valueOf(id));
	    			}
	    		}
	    		//重要程度
	    		if(Strings.isBlank(detailId)){
	    			if(null != templateId){
	    				deleteAllInfoByTemplateId(templateId);
	    			}
	    			saveForTemplate(superviseTitle,user.getId(),
	    					supervisorNames, ids, templateDateTerminal,
	    					awakeDate,
	    					SuperviseEnum.EntityType.template.ordinal(),templateId);
	    			if(Strings.isNotBlank(role)){
	    				saveSuperviseTemplateRole(templateId, role);
	    			}
	    		}else{
	    			updateForTemplate(superviseTitle,user.getId(),
	    					supervisorNames, ids, templateDateTerminal,
	    					awakeDate, SuperviseEnum.EntityType.template.ordinal(),templateId);
	    			updateSuperviseTemplateRole(templateId, role);
	    		}
	    	}
	    }
	
	 public SuperviseSetVO parseTemplateSupervise4New(Long templateId,Long startMemberId,Long templateOrgAccountId) throws BusinessException {

		 SuperviseSetVO ssvo = new SuperviseSetVO();
		 CtpSuperviseDetail templateDetail = getSupervise(SuperviseEnum.EntityType.template.ordinal(),templateId);
		 if(templateDetail != null){
			 Set<Long> templateIdSets =  parseTemplateSupervisorIds(templateId, startMemberId);

			 Set<String> nameSets = new HashSet<String>();
			 if(Strings.isNotEmpty(templateIdSets)){
				 for(Long mid:templateIdSets){
					 nameSets.add(Functions.showMemberName(mid));
				 }
			 }
			 ssvo.setDetailId(null);
			 ssvo.setSupervisorIds(Strings.join(templateIdSets, ","));
			 ssvo.setSupervisorNames(Strings.join(nameSets, ","));
			 ssvo.setTitle(templateDetail.getTitle());
			 ssvo.setUnCancelledVisor(Strings.join(templateIdSets, ","));

			 Long terminalDate = templateDetail.getTemplateDateTerminal();
			 if(null!=terminalDate && Strings.isNotBlank(ssvo.getSupervisorNames())){
				 Date superviseDate = workTimeManager.getCompleteDate4Nature(new Date(),terminalDate.intValue()*60*24 ,templateOrgAccountId);
				 String date = Datetimes.format(superviseDate, Datetimes.datetimeWithoutSecondStyle);
				 ssvo.setAwakeDate(date);
			 }else if(templateDetail.getAwakeDate() != null && Strings.isNotBlank(ssvo.getSupervisorNames())){
				 //个人模板terminalDate为Null,直接存的是AwakeDate
				 String date = Datetimes.format(templateDetail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle);
				 ssvo.setAwakeDate(date);
			 }
		 }
    	AppContext.putRequestContext("_SSVO",ssvo);
    	return ssvo;
	 }

	 public void saveOrUpdateSupervise4ProcessImmediate()throws BusinessException {
		  Map<String,Object> superviseMap = (Map<String,Object>)ParamUtil.getJsonParams();
		  SuperviseSetVO ssvo = (SuperviseSetVO)ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);

		  String superviseType = (String) superviseMap.get("superviseType");
		  String moduleId = (String) superviseMap.get("moduleId");
		  if(Strings.isBlank(moduleId) || Strings.isBlank(superviseType)){
			  log.info( "无法获取所需参数，不能立即保存督办信息,moduleId："+moduleId+",superviseType:"+superviseType);
			  return;
		  }
		  int moduleType = ModuleType.collaboration.getKey();
		  if(String.valueOf(SuperviseEnum.EntityType.edoc.ordinal()).equals(superviseType)){
			  moduleType = ModuleType.edoc.getKey();
		  }
		  SuperviseHandler superviseHandler = getSuperviseHandler(ModuleType.getEnumByKey(moduleType));
	      SuperviseMessageParam smp = superviseHandler.getSuperviseMessageParam4SaveImmediate(Long.valueOf(moduleId));

	      if(smp == null){
			  smp = new SuperviseMessageParam();
		  }
	      saveOrUpdateSupervise4Process(ssvo,smp, Long.valueOf(moduleId), SuperviseEnum.EntityType.valueOf(Integer.valueOf(superviseType)));
	 }

	@Override
	public Map getSenderAffair(String objectId) throws BusinessException{
		  Map map = new HashMap();
		  if(Strings.isBlank(objectId)){
		    return null;
		  }else{
		    Long _objctId = Long.valueOf(objectId);
		    CtpAffair senderAffair = affairManager.getSenderAffair(_objctId);
		    if(null == senderAffair && AppContext.hasPlugin("fk")){
		        senderAffair = affairManager.getSenderAffairByHis(_objctId);
		    }
		    if(null != senderAffair){
		      map.put("affairId", senderAffair.getId());
		      return map;
		    }else{
		      return null;
		    }
		  }
	}
	
	@Override
	public String checkSuperviseIsCancel(Map<String,String> map)throws BusinessException{
    	User user = AppContext.getCurrentUser();
    	if(ColOpenFrom.supervise.name().equals(map.get("openFrom"))){
			boolean isSupervisor = isSupervisor(user.getId(),Long.valueOf(map.get("objId")));
			if(!isSupervisor){
				String errMsg = "";
				String appName = map.get("appName");
				if("edocSend".equals(appName)||"signReport".equals(appName)||"recEdoc".equals(appName)){
					errMsg = ResourceUtil.getString("edoc.supercise.cancel.acl");
				}else{
					errMsg = ResourceUtil.getString("collaboration.supercise.cancel.acl");
				}
                return errMsg;
			}
		}
    	return "";
    }

	@Override
	public void updateSubjectByEntityId(String newSubject, Long summaryId) throws BusinessException {
		superviseDao.updateSubjectByEntityId(newSubject, summaryId);
	}

	@Override
	public void saveSuperviseLog(Long summaryId,List<Long> supervisorIds,String title,Long memberId,String content) throws BusinessException {
		if(!supervisorIds.isEmpty()) {
			Long superviseId = null;
			CtpSuperviseDetail  detail = getSupervise(summaryId);
			if(detail == null) {
				superviseId = save(new SuperviseMessageParam(), title, memberId, "", "", null, new Date(), 0, summaryId, new HashMap<String, Object>());
			}else {
				superviseId = detail.getId();
			}
			saveLog(superviseId, memberId, supervisorIds, content);
		}
	}

}