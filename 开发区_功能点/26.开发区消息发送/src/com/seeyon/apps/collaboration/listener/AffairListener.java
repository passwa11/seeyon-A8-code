package com.seeyon.apps.collaboration.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.agent.event.AgentAddEvent;
import com.seeyon.apps.agent.event.AgentDeleteEvent;
import com.seeyon.apps.agent.event.AgentUpdateEvent;
import com.seeyon.apps.collaboration.event.CollaborationAffairPrintEvent;
import com.seeyon.apps.doc.event.DocCancelFavoriteEvent;
import com.seeyon.apps.doc.event.DocFavoriteEvent;
import com.seeyon.ctp.common.affair.manager.AffairAgentMangager;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;

public class AffairListener {
    private AffairManager affairManager;
    private AffairAgentMangager affairAgentMangager;

    public void setAffairAgentMangager(AffairAgentMangager affairAgentMangager) {
        this.affairAgentMangager = affairAgentMangager;
    }
    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    @ListenEvent(event = DocCancelFavoriteEvent.class)
    public void onCancelFavoriteEvent(DocCancelFavoriteEvent event) throws BusinessException {
    	Long affairId = event.getSourceId() ;
        if (affairId != null) {
        	
        	Map<String,Object> columnValue = new HashMap<String,Object>();
        	columnValue.put("hasFavorite", Boolean.FALSE);
        	affairManager.update(affairId, columnValue);
        }
    }
    
    @ListenEvent(event = DocFavoriteEvent.class)
    public void onFavoriteEvent(DocFavoriteEvent event) throws BusinessException {
    	Long affairId = event.getSourceId() ;
        if (affairId != null) {

        	Map<String,Object> columnValue = new HashMap<String,Object>();
        	columnValue.put("hasFavorite", Boolean.TRUE);
        	affairManager.update(affairId, columnValue);
        }
    }

	// 添加代理事件
	@ListenEvent(event = AgentAddEvent.class)
	public void addAgent(AgentAddEvent agentAddEvent) throws BusinessException {
		affairAgentMangager.addAffairByAgent(agentAddEvent.getAgent());
	}

	// 修改代理：ModifyAgentEvent,
	@ListenEvent(event = AgentUpdateEvent.class)
	public void modifyAgent(AgentUpdateEvent agentUpdateEvent) throws BusinessException {
		affairAgentMangager.modifyAffairByAgent(agentUpdateEvent.getAgent(), agentUpdateEvent.getOldAgent());
	}

	// 代理到期：AgentExpireEvent
	@ListenEvent(event = AgentDeleteEvent.class)
	public void deleteAgent(AgentDeleteEvent agentDeleteEvent) throws BusinessException {
		affairAgentMangager.deleteAffairByMemeberIdAndProxyId(agentDeleteEvent.getAgent().getAgentId(),
				agentDeleteEvent.getAgent().getAgentToId());
	}
	@ListenEvent(event = CollaborationAffairPrintEvent.class)
	public void collAffairPrint(CollaborationAffairPrintEvent collaborationAffairPrintEvent) throws BusinessException {
		List<CtpAffair> affairList =   collaborationAffairPrintEvent.getAffairs();
		if(affairList != null  && affairList.size() >0){
			Map<String,Object> columnValue = new HashMap<String,Object>();
			columnValue.put("print", 1);
			for(CtpAffair ctpAffair:affairList){
				affairManager.update(ctpAffair.getId(), columnValue);
			}
		}

	}
}
