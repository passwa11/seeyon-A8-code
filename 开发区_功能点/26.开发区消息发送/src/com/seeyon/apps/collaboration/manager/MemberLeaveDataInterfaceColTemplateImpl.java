/**
 * 
 */
package com.seeyon.apps.collaboration.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.agent.AgentConstants;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.manager.AgentIntercalateManager;
import com.seeyon.apps.agent.po.Agent;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.memberleave.bo.MemberLeavePendingData;
import com.seeyon.ctp.organization.memberleave.manager.AbstractMemberLeaveData;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

/**
 * 离职办理：显示自由协同数字和列表
 * @author tanmf
 */
public class MemberLeaveDataInterfaceColTemplateImpl extends AbstractMemberLeaveData {
    
    private static Log LOG = CtpLogFactory.getLog(MemberLeaveDataInterfaceColTemplateImpl.class);
    private AgentIntercalateManager agentIntercalateManager;
    private UserMessageManager userMessageManager;
    
    private AffairManager affairManager;
    
    private ColManager colManager;
    
    public void setAgentIntercalateManager(AgentIntercalateManager agentIntercalateManager) {
        this.agentIntercalateManager = agentIntercalateManager;
    }
    
    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }
    
    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }
    
    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    @Override
    public String getLabel(){
        return "member.leave.colformtemplate.title";
    }
    
    public Integer getCount(long memberId) {
        Map<String, String> condition = new HashMap<String, String>();
        condition.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_pending.key()));
        condition.put(ColQueryCondition.currentUser.name(), String.valueOf(memberId));
        condition.put(ColQueryCondition.CollType.name(), "Templete"); //Templete Self
        
        try {
            return this.colManager.countByCondition(condition);
        }
        catch (Exception e) {
            LOG.error("", e);
        }
        
        return null;
    }

    @Override
    public List<MemberLeavePendingData> list(FlipInfo fi, Map<String, Object> params) throws BusinessException{
        Map<String, String> condition = new HashMap<String, String>();
        condition.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_pending.key()));
        condition.put(ColQueryCondition.currentUser.name(), String.valueOf(params.get("memberId")));
        condition.put(ColQueryCondition.CollType.name(), "Templete"); //Templete Self
        if(params.containsKey("subject")){
            condition.put(ColQueryCondition.subject.name(), (String)params.get("subject"));
        }
        if(params.containsKey("senderName")){
            condition.put(ColQueryCondition.startMemberName.name(), (String)params.get("senderName"));
        }
        if(params.containsKey("sendDate") && Strings.isNotBlank((String)params.get("sendDate"))){
            //condition.put(ColQueryCondition.createDate.name(), Datetimes.formatDate((Date)params.get("sendDate")));
            //OA-48428
            String textfield = (String)params.get("textfield");
            String textfield1 = (String)params.get("textfield1");
            condition.put(ColQueryCondition.createDate.name(), textfield+"#"+textfield1);
        }
        
        List<ColSummaryVO> colSummaryVOs = this.colManager.queryByCondition(fi, condition);
        
        List<MemberLeavePendingData> result = new ArrayList<MemberLeavePendingData>();
        
        for (ColSummaryVO c : colSummaryVOs) {
            MemberLeavePendingData d = new MemberLeavePendingData();
            
            d.setSenderId(c.getStartMemberId());
            d.setSendDate(c.getCreateDate());
            d.setSubject(c.getSubject());
            
            result.add(d);
        }
        
        return result;
    }
    
    @Override
    public int getSortId(){
        return 2;
    }

    @Override
    public boolean doHandle(long leaveMemberId, long agentMemberId) throws BusinessException {
        try{
            Date startTime = affairManager.getMinStartTimePending(leaveMemberId);
            startTime = Strings.escapeNULL(startTime, new Date());
            Date endTime = Datetimes.addMonth(new Date(), 3);
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
                AgentUtil.setAgentOption(agent, AgentConstants.Agent_Option.TEMPLATE.name(), true);
                
                //保存代理关系
                agentIntercalateManager.save(agent,null,null);
            }
            
            List<Agent> oldList= agentIntercalateManager.queryAvailabilityList(leaveMemberId);
            for (Agent v3xAgent : oldList) {
                if(agent.getId().longValue() == v3xAgent.getId().longValue()
                        || !AgentUtil.hasXXX(v3xAgent, AgentConstants.Agent_Option.TEMPLATE.name())){
                    continue;
                }
                
                AgentUtil.setAgentOption(v3xAgent, AgentConstants.Agent_Option.TEMPLATE.name(), false);
                
                if(!AgentUtil.isHasAgentOption(v3xAgent)) {
                    v3xAgent.setCancelDate(new Timestamp(System.currentTimeMillis()));
                    v3xAgent.setCancelFlag(true);
                    agentIntercalateManager.cancel(v3xAgent);
                }
                else{
                    agentIntercalateManager.update(v3xAgent, null,null);
                }
            }
            
            //消息要带上后缀：(来自离职办理)
            MessageContent c = MessageContent.get("agent.setting.msg.remind.from.memberleave", Functions.showMemberNameOnly(leaveMemberId), 1);
            MessageReceiver receiver = MessageReceiver.get(-1L, agentMemberId);
            this.userMessageManager.sendSystemMessage(c, ApplicationCategoryEnum.organization, AppContext.currentUserId(), receiver);
        }
        catch(Exception e){
            throw new BusinessException(e);
        }
        
        return false;
    }
    
    public Long getAgentMemberId(long leaveMemberId){
        AgentModel agentModel = AgentUtil.getAgent4MemberLeave(leaveMemberId, AgentConstants.Agent_Option.TEMPLATE.name());
        if(agentModel != null){
            return agentModel.getAgentId();
        }
        
        return null;
    }
    
    public boolean removeAgent(long leaveMemberId){
        AgentModel agentModel = AgentUtil.getAgent4MemberLeave(leaveMemberId, AgentConstants.Agent_Option.TEMPLATE.name());
        
        if(agentModel != null){
            agentModel.setCancelDate(new Timestamp(System.currentTimeMillis()));
            agentModel.setCancelFlag(true);
            Agent agent = agentModel.toAgent();
            agentIntercalateManager.cancel(agent);
        }
        
        return true;
    }

}
