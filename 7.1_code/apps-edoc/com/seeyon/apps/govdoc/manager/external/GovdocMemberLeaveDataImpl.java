/**
 * 
 */
package com.seeyon.apps.govdoc.manager.external;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.agent.AgentConstants;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.manager.AgentIntercalateManager;
import com.seeyon.apps.agent.po.Agent;
import com.seeyon.apps.agent.po.AgentDetail;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.bo.AffairCondition;
import com.seeyon.ctp.common.affair.bo.AffairCondition.SearchCondition;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.memberleave.bo.MemberLeavePendingData;
import com.seeyon.ctp.organization.memberleave.manager.AbstractMemberLeaveData;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

/**
 * 离职办理
 * @author tanggl
 *
 */
public class GovdocMemberLeaveDataImpl extends AbstractMemberLeaveData {
    
    private AgentIntercalateManager agentIntercalateManager;
    private UserMessageManager userMessageManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
    
    @Override
    public String getLabel() {
        return "member.leave.documenttodo.title";
    }
    
    @Override
    public boolean isEnabled() {
    	return true;
    }

    @Override
    public Integer getCount(long memberId) throws BusinessException {
    	if(!AppContext.hasPlugin("edoc")) {
    		return 0;
    	}
        AffairCondition condition = new AffairCondition(memberId, StateEnum.col_pending,
            ApplicationCategoryEnum.edoc
        );
        condition.addSearch(SearchCondition.catagory, "catagory_edoc", null,false);
        return condition.getPendingCount(affairManager);
    }
    
    @Override
    public boolean isMustSetAgentMember(long leaveMemberId) throws BusinessException{
    	if(!AppContext.hasPlugin("edoc")) {
    		return false;
    	}
        if(getCount(leaveMemberId) <= 0){
            return false;
        }
        List<MemberRole> roles =  this.orgManager.getMemberRoles(leaveMemberId, null);
        for (MemberRole r : roles) {
            if(r.getRole().getName().matches(OrgConstants.Role_NAME.Accountexchange.name() + "|" + OrgConstants.Role_NAME.Departmentexchange.name())){
                return true;
            }
        }
        
        return false;
    }

    @Override
    public List<MemberLeavePendingData> list(FlipInfo fi, Map<String, Object> params) throws BusinessException {
    	if(!AppContext.hasPlugin("edoc")) {
    		return null;
    	}
        Pagination.setNeedCount(true);
        Pagination.setFirstResult(fi.getStartAt());
        Pagination.setMaxResults(fi.getSize());
        
        SearchCondition condition = null;
        String textfield = null;
        String textfield1 = null;
        
        if(Strings.isNotBlank((String)params.get("senderName"))){
            condition = SearchCondition.sender;
            textfield = (String)params.get("senderName");
        }
        else if(Strings.isNotBlank((String)params.get("sendDate"))){
            condition = SearchCondition.createDate;
            textfield = (String)params.get("textfield");
            textfield1 = (String)params.get("textfield1");//OA-48428
            
            textfield  = Strings.isNotBlank(textfield)  ? textfield  + " 00:00:00" : textfield; 
            textfield1 = Strings.isNotBlank(textfield1) ? textfield1 + " 23:59:59" : textfield1;
        }
        else if(Strings.isNotBlank((String)params.get("subject"))){
            condition = SearchCondition.subject;
            textfield = (String)params.get("subject");
        }
        
        AffairCondition affairCondition = new AffairCondition((Long)params.get("memberId"), StateEnum.col_pending,
                ApplicationCategoryEnum.edoc
        );
        affairCondition.addSearch(SearchCondition.catagory, "catagory_edoc", null,false);
        if(condition != null){
            affairCondition.addSearch(condition, textfield, textfield1);
        }
        
        List<CtpAffair> queryList = affairCondition.getPendingAffair(affairManager, fi);
        
        List<MemberLeavePendingData> result = new ArrayList<MemberLeavePendingData>();
        
        for (CtpAffair c : queryList) {
            MemberLeavePendingData d = new MemberLeavePendingData();
            
            d.setSenderId(c.getSenderId());
            d.setSendDate(c.getCreateDate());
            d.setSubject(c.getSubject());
            
            result.add(d);
        }
        
        return result;
    }

    @Override
    public int getSortId() {
        return 3;
    }

    @Override
    public boolean doHandle(long leaveMemberId, long agentMemberId) throws BusinessException {
    	if(!AppContext.hasPlugin("edoc")) {
    		return false;
    	}
        try{
            Date startTime = affairManager.getMinStartTimePending(leaveMemberId);
            startTime = Strings.escapeNULL(startTime, new Date());
            Date endTime = Datetimes.addYear(new Date(), 3);
            Agent agent = new Agent();
            {
                agent.setIdIfNew();
                agent.setAgentId(agentMemberId);
                agent.setAgentToId(leaveMemberId);
                agent.setCreateDate(new Timestamp(System.currentTimeMillis()));
                agent.setStartDate(new Timestamp(startTime.getTime()));
                agent.setEndDate(new Timestamp(endTime.getTime()));
                agent.setCancelFlag(false);
                agent.setAgentRemind(true);
                agent.setAgentToRemind(true);
                agent.setAgentType(AgentConstants.MEMBER_LEAVE);
                AgentUtil.setAgentOption(agent, AgentConstants.Agent_Option.EDOC.name(), true);
                //保存代理关系
                agentIntercalateManager.save(agent,null);
            }
            
            List<Agent> oldList= agentIntercalateManager.queryAvailabilityList(leaveMemberId);
            for (Agent v3xAgent : oldList) {
                if(agent.getId().longValue() == v3xAgent.getId().longValue()
                        || !AgentUtil.hasXXX(v3xAgent, AgentConstants.Agent_Option.EDOC.name())){
                    continue;
                }
                
                AgentUtil.setAgentOption(v3xAgent, AgentConstants.Agent_Option.EDOC.name(), false);
                
                if(!AgentUtil.isHasAgentOption(v3xAgent)) {
                    v3xAgent.setCancelDate(new Timestamp(System.currentTimeMillis()));
                    v3xAgent.setCancelFlag(true);
                    agentIntercalateManager.cancel(v3xAgent);
                }
                else{
                    List<AgentDetail> details = null;
                    if(AgentUtil.isHasTemplate(v3xAgent)){
                        //如果存在模板，则要把模版也进行保存
                        details = new ArrayList<AgentDetail>();
                        details.addAll(v3xAgent.getAgentDetails());
                    }
                    agentIntercalateManager.update(v3xAgent, details);
                }
                
            }
            
            //消息要带上后缀：(来自离职办理)
            MessageContent c = MessageContent.get("agent.setting.msg.remind.from.memberleave", Functions.showMemberNameOnly(leaveMemberId), 2);
            MessageReceiver receiver = MessageReceiver.get(-1L, agentMemberId);
            this.userMessageManager.sendSystemMessage(c, ApplicationCategoryEnum.organization, AppContext.currentUserId(), receiver);
        }
        catch(Exception e){
            throw new BusinessException(e);
        }
        
        return false;
    }
    
    @Override
    public Long getAgentMemberId(long leaveMemberId) { 
    	if(!AppContext.hasPlugin("edoc")) {
    		return null;
    	}
        AgentModel agentModel = AgentUtil.getAgent4MemberLeave(leaveMemberId, AgentConstants.Agent_Option.EDOC.name());
        if(agentModel != null){
            return agentModel.getAgentId();
        }
        
        return null;
    }
    
    @Override
    public boolean removeAgent(long leaveMemberId) {
    	if(!AppContext.hasPlugin("edoc")) {
    		return true;
    	}
    	
        AgentModel agentModel = AgentUtil.getAgent4MemberLeave(leaveMemberId, AgentConstants.Agent_Option.EDOC.name());
        if(agentModel != null){
            agentModel.setCancelDate(new Timestamp(System.currentTimeMillis()));
            agentModel.setCancelFlag(true);
            Agent agent = agentModel.toAgent();
            agentIntercalateManager.cancel(agent);
        }
        
        return true;
    }
    
    public void setAgentIntercalateManager(AgentIntercalateManager agentIntercalateManager) {
        this.agentIntercalateManager = agentIntercalateManager;
    }    
    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }    
    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }    
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

}
