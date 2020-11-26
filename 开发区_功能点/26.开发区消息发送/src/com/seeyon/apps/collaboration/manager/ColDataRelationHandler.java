package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.datarelation.manager.DataRelationHandler;
import com.seeyon.ctp.common.datarelation.manager.DataRelationManager;
import com.seeyon.ctp.common.datarelation.po.DataRelationPO;
import com.seeyon.ctp.common.datarelation.po.json.TemplateDealConfigs;
import com.seeyon.ctp.common.datarelation.po.json.TemplateSendConfigs;
import com.seeyon.ctp.common.datarelation.vo.BaseConfigVO;
import com.seeyon.ctp.common.datarelation.vo.ProjectConfigVO;
import com.seeyon.ctp.common.datarelation.vo.SelfCollConfig;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;

public class ColDataRelationHandler implements DataRelationHandler {
	private static Log LOG = CtpLogFactory.getLog(ColDataRelationHandler.class);
    private ColManager          colManager;
    private TemplateManager     templateManager;
    private AffairManager       affairManager;
    private OrgManager          orgManager;
    private DataRelationManager dataRelationManager;
    
	@Override
	@AjaxAccess
	public FlipInfo getDataBySendTemplate(FlipInfo flipInfo, Map<String, Object> param) throws BusinessException {
		User user = AppContext.getCurrentUser();
		Map<String,String> summaryConditon = new HashMap<String,String>();
		Long id = ParamUtil.getLong(param, "id");
		Long currentTemplateId = ParamUtil.getLong(param, "templateId");
		Long affairId = ParamUtil.getLong(param, "affairId",-1L);
		Long summaryId = ParamUtil.getLong(param, "summaryId");
		String openFrom = (String) param.get("openFrom");
		LOG.info("数据关联更多页面入口参数:id="+id+",currentTemplateId="+currentTemplateId+",affairId="+affairId
				+",summaryId="+summaryId+",openFrom="+openFrom);
		DataRelationPO  po = dataRelationManager.getDataRelationPOByCache(id);
		if(po == null){
			po = dataRelationManager.get(id);
			LOG.info("数据关联更多页面入口参数="+ (null != po?po.getId():"po为空"));
		}
				
        if (null == po) {
            return flipInfo;
        }
		TemplateSendConfigs configVO =JSONUtil.parseJSONString(po.getConfigs(), TemplateSendConfigs.class);
		List<Integer> affairStates = new ArrayList<Integer>();
		
		if(configVO.getSendChecked() && user.hasResourceCode("F01_listSent")){
			affairStates.add(StateEnum.col_sent.key());
		}
		if(configVO.getWaitSendChecked() && user.hasResourceCode("F01_listWaitSend")){
			affairStates.add(StateEnum.col_waitSend.key());
		}
		
        if (!affairStates.isEmpty()) {
            summaryConditon.put(ColQueryCondition.state.name(), Strings.join(affairStates, ","));
        }else{
        	return flipInfo;
        }
        
        List<Long> tempIds = new ArrayList<Long>();
        Long one = Long.valueOf(-1L);
        tempIds.add(one);
        if (configVO.getCurrentTemplate() && !one.equals(currentTemplateId)) {
            tempIds.add(currentTemplateId);
        }
        
       // CtpAffair affair = affairManager.get(affairId);
        
		String selectTemplates = configVO.getSelectTemplate();
        if (Strings.isNotBlank(selectTemplates) || configVO.getCurrentTemplate()) {
			List<String> templateIdString = Strings.newArrayList(selectTemplates.split(","));
			List<Long> categoryIds = new ArrayList<Long>();
            for (String temp : templateIdString) {
                if (temp.contains("C_")) {
                    String categoryIdStr = temp.substring(2, temp.length());
                    Long categoryId = Long.valueOf(categoryIdStr);
                    categoryIds.add(categoryId);
                } else {
                    if (Strings.isNotBlank(temp)) {
                        tempIds.add(Long.valueOf(temp));
                    }
                }
            }
			List<Long> tempId = templateManager.getTemplateIdsByCategoryIds(categoryIds);
			tempIds.addAll(tempId);
		}
        
        summaryConditon.put(ColQueryCondition.templeteIds.name(), Strings.join(tempIds, ","));
		
		summaryConditon.put(ColQueryCondition.affairMemberId.name(), String.valueOf(AppContext.currentUserId()));
		summaryConditon.put(ColQueryCondition.currentUser.name(), String.valueOf(AppContext.currentUserId()));
		if(param.get("subject")!=null){
			summaryConditon.put(ColQueryCondition.subject.name(), param.get("subject").toString());
		}
		if(param.get("startMemberName")!=null){
			summaryConditon.put(ColQueryCondition.startMemberName.name(), param.get("startMemberName").toString());
		}
		if(param.get("receiveDate")!=null){
			summaryConditon.put(ColQueryCondition.receiveDate.name(), param.get("receiveDate").toString());
		}
		if(param.get("createDate")!=null){
			summaryConditon.put(ColQueryCondition.createDate.name(), param.get("createDate").toString());
		}
		if(!"more".equals(openFrom)){
			flipInfo.setSize(configVO.getPageSize());
		}
		//已办列表统计，待办、已办列表未去重
		summaryConditon.put(ColQueryCondition.deduplication.name(), String.valueOf(false));
        
		if (summaryId != null) {
		    summaryConditon.put(ColQueryCondition.removeSummary.name(), String.valueOf(summaryId));
		}
		
		summaryConditon.put(ColQueryCondition.delete.name(),"0");
		
		List<ColSummaryVO> colSummaryVOs = colManager.queryByCondition4DataRelation(flipInfo,summaryConditon);
		
	      //移除当前协同的。
		if (summaryId != null) { //发起节点为空
			__removeCurrent(summaryId, colSummaryVOs);
		}
        
		flipInfo.setData(colSummaryVOs);
		return flipInfo;
	}

	@Override
	@AjaxAccess
    public FlipInfo getDataByDealTemplate(FlipInfo flipInfo, Map<String, Object> param) throws BusinessException {
        Long id = ParamUtil.getLong(param, "id", -1l);
        Long currentTemplateId = ParamUtil.getLong(param, "templateId");
        Long memberId = ParamUtil.getLong(param, "memberId");
        Long objectId = ParamUtil.getLong(param, "summaryId");
        Long senderId = ParamUtil.getLong(param, "senderId");
        String nodePolicy = ParamUtil.getString(param, "nodePolicy");
        DataRelationPO  po =  (DataRelationPO) param.get("po");
        if (po == null) {
            po = dataRelationManager.get(id);
        }
        if (po != null) {
            User user = AppContext.getCurrentUser();
            Map<String, String> summaryConditon = new HashMap<String, String>();
            TemplateDealConfigs configVO = JSONUtil.parseJSONString(po.getConfigs(), TemplateDealConfigs.class);
            Set<Integer> affairStates = new HashSet<Integer>();
            List<Long> memberIds = new ArrayList<Long>();
            List<Long> deptIds = new ArrayList<Long>();
            
            List<Long> tempIds = new ArrayList<Long>();
            Long one = Long.valueOf(-1L);
            tempIds.add(one);
            if (configVO.getCurrentTemplate() && !one.equals(currentTemplateId)) {
                tempIds.add(currentTemplateId);
            }
            String selectTemplates = configVO.getSelectTemplate();
            if (Strings.isNotBlank(selectTemplates)) {
                List<Long> categoryIds = new ArrayList<Long>();
                List<String> selectTemplateIds = Strings.newArrayList(selectTemplates.split(","));
                for (String temp : selectTemplateIds) {
                    if (temp.contains("C_")) {
                        String categoryIdStr = temp.substring(2, temp.length());
                        Long categoryId = Long.valueOf(categoryIdStr);
                        categoryIds.add(categoryId);
                    } else {
                        Long tempId = Long.valueOf(temp);
                        tempIds.add(tempId);
                    }
                }

                List<Long> tempId = templateManager.getTemplateIdsByCategoryIds(categoryIds);
                tempIds.addAll(tempId);
            }
            
            
            summaryConditon.put(ColQueryCondition.templeteIds.name(), Strings.join(tempIds, ","));
            
            List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(user.getId());
        	Long currentUserId = user.getId();
        	if(!Strings.isEmpty(agentModelList) && !currentUserId.equals(memberId)){
        		currentUserId = memberId;
        		summaryConditon.put("hasNeedAgent", "true");
        		summaryConditon.put(ColQueryCondition.transactorId.name(), String.valueOf(user.getId()));
        	}
            summaryConditon.put(ColQueryCondition.currentUser.name(), String.valueOf(user.getId()));
            summaryConditon.put(ColQueryCondition.affairMemberId.name(), String.valueOf(currentUserId));
           
            //当前发起人
            if (configVO.getSendChecked2()) {
                memberIds.add(senderId);
            }
            
            //我”的部门成员发给“我”的事项 
            if (configVO.getDeptMemberChecked()) {
                findMemberDeptMembers(memberId, memberIds);
            }
            //当前发起者部门成员发给“我”的事项 
            if (configVO.getSenderDeptMemberChecked()) {
                V3xOrgMember senderMember = orgManager.getMemberById(senderId);
                findMemberDeptMembers(senderMember.getId(), memberIds);
            }
            //所有
            if (configVO.getAllSend()) {
                deptIds.clear();
                memberIds.clear();
            }
            if (!memberIds.isEmpty()) {
                summaryConditon.put(ColQueryCondition.startMemberId.name(), Strings.join(memberIds, ","));
            }
            
            if (!deptIds.isEmpty()) {
                summaryConditon.put(ColQueryCondition.startMemberDept.name(), Strings.join(deptIds, ","));
            }

            //已办，待办
            if(configVO.getMyRadio()){
                if (configVO.getMyDone() && user.hasResourceCode("F01_listDone")) {
                    affairStates.add(StateEnum.col_done.key());
                }
                if (configVO.getMyPending() && user.hasResourceCode("F01_listPending")) {
                    affairStates.add(StateEnum.col_pending.key());
                }
            }
            
            if(configVO.getNodeRadio()){
                if (configVO.getNodeDone() && user.hasResourceCode("F01_listDone")) {
                    affairStates.add(StateEnum.col_done.key());
                }
                if (configVO.getNodePending() && user.hasResourceCode("F01_listPending")) {
                    affairStates.add(StateEnum.col_pending.key());
                }
            }
           

            if (configVO.getNodeRadio()) {
                summaryConditon.put(ColQueryCondition.nodePolicy.name(), nodePolicy);
            }

            if (!affairStates.isEmpty()) {
                summaryConditon.put(ColQueryCondition.state.name(), Strings.join(affairStates, ","));
            }else{
            	return flipInfo;
            }
            if (param.get("subject") != null) {
                summaryConditon.put(ColQueryCondition.subject.name(), param.get("subject").toString());
            }
            if (param.get("startMemberName") != null) {
                summaryConditon.put(ColQueryCondition.startMemberName.name(), param.get("startMemberName").toString());
            }
            if (param.get("receiveDate") != null) {
                summaryConditon.put(ColQueryCondition.receiveDate.name(), param.get("receiveDate").toString());
            }
            if (param.get("createDate") != null) {
                summaryConditon.put(ColQueryCondition.createDate.name(), param.get("createDate").toString());
            }
            /*if (!"more".equals(openFrom)) {
                flipInfo.setSize(configVO.getPageSize());
            }*/
            //已办列表统计，待办、已办列表未去重
            summaryConditon.put(ColQueryCondition.deduplication.name(), String.valueOf(false));
            
            //去除当前协同
            if (objectId != null) {
                summaryConditon.put(ColQueryCondition.removeSummary.name(), String.valueOf(objectId));
            }
            
            summaryConditon.put(ColQueryCondition.delete.name(),"0");
            
            List<ColSummaryVO> summaryVOs = colManager.queryByCondition4DataRelation(flipInfo, summaryConditon);
            
            //移除当前协同的。
    		if (objectId != null) { //发起节点为空
    			__removeCurrent(objectId, summaryVOs);
    		}

            flipInfo.setData(summaryVOs);
        }
        return flipInfo;
    }
	
	/**
	 * 获取用户主副兼部门下的所有用户
	 * @Author      : xuqw
	 * @Date        : 2016年2月25日下午8:04:27
	 * @param memberId
	 * @return
	 * @throws BusinessException 
	 */
	private void findMemberDeptMembers(Long memberId, List<Long> memberIds) throws BusinessException{
	    
	    List<V3xOrgDepartment> depts = orgManager.getDepartmentsByUser(memberId);
	    if(Strings.isNotEmpty(depts)){
	        for(V3xOrgDepartment d : depts){
	            List<V3xOrgMember> members = orgManager.getMembersByDepartment(d.getId(), false);
	            if(Strings.isNotEmpty(members)){
	                for(V3xOrgMember member : members){
	                    if(!memberIds.contains(member.getId())){
	                        memberIds.add(member.getId());
	                    }
	                }
	            }
	        }
	    }
	}
	
	@Override
	@AjaxAccess
	public FlipInfo getSelfCollData(FlipInfo flipInfo, Map<String,Object> param) throws BusinessException {
		Long id = ParamUtil.getLong(param, "id", -1l);
        DataRelationPO po = (DataRelationPO) param.get("po");
        Long affairId = ParamUtil.getLong(param, "affairId");
        Long projectId = ParamUtil.getLong(param, "projectId");
        Long senderId = ParamUtil.getLong(param, "senderId");
        Long memberId = ParamUtil.getLong(param, "memberId");
        Long objectId = ParamUtil.getLong(param, "summaryId");
        if (po == null) {
            List<BaseConfigVO> selfCollConfigs = dataRelationManager.findSelfCollConfig(affairId,projectId);
            for (BaseConfigVO selfCollConfig : selfCollConfigs) {
                if (selfCollConfig.getId().equals(id)) {
                    if (selfCollConfig instanceof SelfCollConfig) {
                        SelfCollConfig selfConfig = (SelfCollConfig) selfCollConfig;
                        po = selfConfig.getDataRelationPO();
                    }

                    if (selfCollConfig instanceof ProjectConfigVO) {
                        ProjectConfigVO projectConfig = (ProjectConfigVO) selfCollConfig;
                        po = projectConfig.getDataRelationPO();
                    }
                }
            }
        }
        
        if (null != po && null != affairId) {
        	User user = AppContext.getCurrentUser();
    		Map<String, String> summaryConditon = new HashMap<String, String>();
           // CtpAffair affair = affairManager.get(affairId);
            List<Integer> affairStates = new ArrayList<Integer>();
            SelfCollConfig selfCollConfig = JSONUtil.parseJSONString(po.getConfigs(), SelfCollConfig.class);
            Boolean isProxy = false;
            if(null!=objectId){
            	List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(user.getId());
            	Long currentUserId = user.getId();
            	if(!Strings.isEmpty(agentModelList) && !currentUserId.equals(memberId)){
            		currentUserId = memberId;
            		isProxy = true;
            	}
                //我收到发起人（转给我）的协同
                if(selfCollConfig.getSendToMe()){
                	if(!user.hasResourceCode("F01_listDone") && !user.hasResourceCode("F01_listPending")){
                		return flipInfo;
                	}
                	//当前待办人
                	 summaryConditon.put(ColQueryCondition.currentUser.name(), String.valueOf(user.getId()));
                     summaryConditon.put(ColQueryCondition.affairMemberId.name(), String.valueOf(currentUserId));
                    //当前事项发起人
                    summaryConditon.put(ColQueryCondition.startMemberId.name(), String.valueOf(senderId));
                    //当前事项发起人转发给我的
                    summaryConditon.put(ColQueryCondition.fromId.name(), String.valueOf(senderId));
                    if(isProxy){
                    	summaryConditon.put("hasNeedAgent", "true");
                    	summaryConditon.put(ColQueryCondition.transactorId.name(), String.valueOf(user.getId()));
                    	
                    }
                    //已办
                    if(user.hasResourceCode("F01_listDone")){
                    	affairStates.add(StateEnum.col_done.key());
                    }
                    //待办
                    if(user.hasResourceCode("F01_listPending")){
                    	affairStates.add(StateEnum.col_pending.key());
                    }
                    
                    summaryConditon.put(ColQueryCondition.delete.name(),"0");
                }
                
                //我发起/转给发起人的协同
                if(selfCollConfig.getSendToOther()){
                	if(!user.hasResourceCode("F01_listSent")){
                		return flipInfo;
                	}
                	//当前待办人
                    summaryConditon.put(ColQueryCondition.currentUser.name(), String.valueOf(user.getId()));
                    summaryConditon.put(ColQueryCondition.affairMemberId.name(), String.valueOf(senderId));
                    //当前事项发起人
                    summaryConditon.put(ColQueryCondition.startMemberId.name(), String.valueOf(user.getId()));
                    
                    List<Long> fromIds = new ArrayList<Long>();
                    fromIds.add(user.getId());
                    //我转发给当前事项发起人
                    summaryConditon.put(ColQueryCondition.fromId.name(), Strings.join(fromIds,","));
                    
                    List<Integer> fromType = new ArrayList<Integer>();
                    fromType.add(ChangeType.Transfer.getKey());
                    summaryConditon.put(ColQueryCondition.fromType.name(), Strings.join(fromType, ","));
                    
                    //已办，待办,已发
                    affairStates.add(StateEnum.col_done.key());
                    affairStates.add(StateEnum.col_pending.key());
                    if(!senderId.equals(user.getId())){
                        affairStates.add(StateEnum.col_sent.key());
                    }
                    
                }
                summaryConditon.put(ColQueryCondition.state.name(), Strings.join(affairStates, ","));
                summaryConditon.put(ColQueryCondition.deduplication.name(), String.valueOf(false));
                //summaryConditon.put(ColQueryCondition.CollType.name(),"Self");
              
                if (param.get("subject") != null) {
                    summaryConditon.put(ColQueryCondition.subject.name(), param.get("subject").toString());
                }
                if (param.get("startMemberName") != null) {
                    summaryConditon.put(ColQueryCondition.startMemberName.name(), param.get("startMemberName").toString());
                }
                if (param.get("receiveDate") != null) {
                    summaryConditon.put(ColQueryCondition.receiveDate.name(), param.get("receiveDate").toString());
                }
                if (param.get("createDate") != null) {
                    summaryConditon.put(ColQueryCondition.createDate.name(), param.get("createDate").toString());
                }
                
                /*if (!"more".equals(openFrom)) {
                    flipInfo.setSize(10);
                }*/
                summaryConditon.put(ColQueryCondition.removeSummary.name(), String.valueOf(objectId));
                
                List<ColSummaryVO> summaryVOs = colManager.queryByCondition4DataRelation(flipInfo, summaryConditon);
                
                //移除当前协同的。
                __removeCurrent(objectId, summaryVOs);
                
                flipInfo.setData(summaryVOs);
            }
            
        }
		
		return flipInfo;
	}
	
	//移除当前协同的。
	private void __removeCurrent(Long summaryId, List<ColSummaryVO> summaryVOs) {
		if(summaryId != null && Strings.isNotEmpty(summaryVOs)){
			for(Iterator<ColSummaryVO> it = summaryVOs.iterator() ; it.hasNext() ; ){
				ColSummaryVO svo = it.next();
				if(summaryId.equals(svo.getAffair().getObjectId())){
					it.remove();
				}
			}
		}
	}
	
	@Override
    public Map<String, Object> getFormMapByAffairId(CtpAffair affair, Long userId) throws BusinessException {
        Map<String, Object> map = new HashMap<String, Object>();
        if(userId == null){
            userId = AppContext.getCurrentUser().getId();
        }
        V3xOrgMember user = orgManager.getMemberById(userId);
        if (affair == null) {
            map.put("startDate", null);
            map.put("startMemberId", user.getId());
            
            if(user.getIsAssigned()){
                map.put("startDeptId", user.getOrgDepartmentId());
                map.put("startPostId", user.getOrgPostId());
                map.put("startLeveId", user.getOrgLevelId());
                map.put("startAccountId", user.getOrgAccountId());
            }
            
        } else if (affair != null) {
            Long summaryId = affair.getObjectId();
            ColSummary summary = colManager.getSummaryById(summaryId);
            if (summary != null) {
                if (summary.getFormAppid() != null && summary.getFormRecordid() != null) {
                    map.put("formAppId", summary.getFormAppid());
                    map.put("recordid", summary.getFormRecordid());
                } else {//自由协同
                    Date startDate = summary.getStartDate();
                    Long startMemberId = summary.getStartMemberId();
                    // affair
                    Long memberId = affair.getMemberId();
                    map.put("startDate", startDate);
                    map.put("startMemberId", startMemberId);
                    map.put("dealMemberId", memberId);
                    //关联项目
                    map.put("projectId", summary.getProjectId());
                }

                map.put("startDate", summary.getStartDate());
                map.put("startMemberId", summary.getStartMemberId());
                V3xOrgMember member = orgManager.getMemberById(summary.getStartMemberId());
                if (member != null && member.getIsInternal()) {
                    map.put("startDeptId", member.getOrgDepartmentId());
                    map.put("startPostId", member.getOrgPostId());
                    map.put("startLeveId", member.getOrgLevelId());
                    map.put("startAccountId", member.getOrgAccountId());
                }

                if (StateEnum.col_pending.getKey() == affair.getState()) {//当前人，当前时间
                    map.put("dealDate", new Date());
                    map.put("dealMemberId", user.getId());
                    if(user.getIsInternal()){
                        map.put("dealDeptId", user.getOrgDepartmentId());
                        map.put("dealPostId", user.getOrgPostId());
                        map.put("dealLeveId", user.getOrgLevelId());
                        map.put("dealAccountId", user.getOrgAccountId());
                    }
                } else {
                    map.put("dealDate", affair.getCompleteTime());
                    map.put("dealMemberId", affair.getMemberId());
                    V3xOrgMember member2 = orgManager.getMemberById(affair.getMemberId());
                    if (member2 != null && member2.getIsInternal()) {
                        map.put("dealDeptId", member2.getOrgDepartmentId());
                        map.put("dealPostId", member2.getOrgPostId());
                        map.put("dealLeveId", member2.getOrgLevelId());
                        map.put("dealAccountId", member2.getOrgAccountId());
                    }
                }
            }
        }
        return map;
    }
	
	public Long getProjectIdByAffairId(Long affairId) throws BusinessException {
        Long projectId = null;
        CtpAffair affair = affairManager.get(affairId);
        if (affair != null) {
            ColSummary summary = colManager.getSummaryById(affair.getObjectId());
            if (summary != null) {
                projectId = summary.getProjectId();
            }
        }
        if (Long.valueOf(-1L).equals(projectId)) {
            projectId = null;
        }
        return projectId;
    }
	
    public Long getProjectIdBySummaryId(Long summaryId) throws BusinessException {
        Long projectId = null;
        if (summaryId != null) {
            ColSummary summary = colManager.getSummaryById(summaryId);
            if (summary != null) {
                projectId = summary.getProjectId();
            }
        }
        if (Long.valueOf(-1L).equals(projectId)) {
            projectId = null;
        }
        return projectId;
    }
    

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	
	public void setDataRelationManager(DataRelationManager dataRelationManager) {
		this.dataRelationManager = dataRelationManager;
	}
	
}
