/*
 * Created on 2004-11-8
 *
 */
package net.joinwork.bpm.engine.wapi;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.po.HistoryWorkitemDAO;
import com.seeyon.ctp.workflow.po.WorkitemDAO;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.execute.BPMCase;

/**
 * 任务管理模块
 * 
 * @author dinghong
 * @version 1.0
 */
public interface WorkItemManager {
    
	/**
	 * 
	 * @param item
	 * @param process
	 * @param theCase
	 * @param activity
	 * @return
	 * @throws BPMException
	 */
	public boolean isExcuteFinished(WorkItem item, BPMProcess process,BPMCase theCase,BPMAbstractNode activity,WorkflowBpmContext   context) throws BPMException;
    
    /**
     * 
     * @param processId
     * @param caseId
     * @param performer
     * @param state
     * @return
     * @throws BPMException
     */
    public List getWorkItemList(String processId, Long caseId,String performer, int state) throws BPMException;
    
    /**
     * 
     * @param processId
     * @param caseId
     * @param activityId
     * @param performer
     * @param state
     * @return
     * @throws BPMException
     */
    public List getWorkItemList(String processId, Long caseId,String activityId,String performer,int state) throws BPMException;
    
    /**
     * 
     * @param performer
     * @param finisher
     * @param processId
     * @param caseId
     * @param activityId
     * @param state
     * @return
     * @throws BPMException
     */
    public List getHistoryWorkitemList(String performer,String processId, Long caseId, String activityId,int state)throws BPMException;

    /**
     * 
     * @param caseId
     * @param activityId
     * @return
     * @throws BPMException
     */
    public String getLastPerformerOfActivity(Long caseId, String activityId) throws BPMException;

    
    /**
     * 
     * @param itemId
     * @return
     * @throws BPMException
     */
    public WorkItem getWorkItemInfo(Long itemId) throws BPMException;

    /**
     * 
     * @param workItemId
     * @return
     * @throws BPMException
     */
    public WorkItem getWorkItemOrHistory(Long workItemId) throws BPMException;

    /**
     * 
     * @param item
     * @param process
     * @return
     * @throws BPMException
     */
    public boolean isExcuteFinished(WorkItem item, BPMProcess process) throws BPMException;

    /**
     * 更新任务项信息
     * @param workitem
     * @throws BPMException
     */
    public void updateWorkItem(WorkitemDAO workitem) throws BPMException;

    /**
     * 
     * @param appName
     * @param subObjectId
     * @return
     */
    public WorkItem getWorkItemById(String appName, Long subObjectId) throws BPMException;
    
    /**
     * 
     * @param caseId
     * @return
     * @throws BPMException
     */
    public String getCaseWorkItemLogXml(String caseId) throws BPMException;
    
    /**
     * 
     * @param caseId
     * @param isHistoryFlag
     * @return
     * @throws BPMException
     */
    public String getCaseWorkItemLogXml(String caseId, boolean isHistoryFlag) throws BPMException;
    
    /**
     * 获得所有节点的所有任务事项的状态
     * @param caseId
     * @return
     * @throws BPMException
     */
    public Map<String, List<String[]>> getNodesItemsStatus(long caseId,Map<String, String[]> nodesStatus) throws BPMException;

    /**
     * 获得任务事项列表
     * @param caseId
     * @param childs
     * @throws BPMException
     */
    public List<WorkItem> getWorItemList(long caseId, List<BPMHumenActivity> childs) throws BPMException;

    /**
     * 获得指定节点指定状态的任务事项列表
     * @param caseId
     * @param childs
     * @param states
     * @return
     * @throws BPMException
     */
    public List<WorkItem> getWorItemListByStates(long caseId, List<BPMHumenActivity> childs, Integer[] states) throws BPMException;
    
    /**
     * 
     * @param actorsList
     * @param humenActivity
     * @param processId
     * @param caseId
     * @param engineDomain
     * @param itemNum
     * @param batch
     * @return
     * @throws BPMException
     */
    public Object[] generateWorkItems(List<V3xOrgMember> actorsList, BPMHumenActivity humenActivity,String processId,long caseId, 
    		String engineDomain,int itemNum,long batch)throws BPMException;

    /**
     * 
     * @param hwdao
     * @throws BPMException
     */
    public void updateHistoryWorkItem(HistoryWorkitemDAO hwdao) throws BPMException;

    /**
     * 
     * @param workitems
     * @throws BPMException
     */
    public void saveWorkitems(List<WorkitemDAO> workitems) throws BPMException;

    /**
     * 
     * @param workitemId
     * @throws BPMException
     */
    public void deleteWorkitem(long workitemId,boolean isHistory) throws BPMException;
    
    public void deleteWorkitemsAndHistoryByProcessId(long processId) throws BPMException;

    /**
     * 
     * @param caseId
     * @param nodeId
     * @param itemNum
     * @throws BPMException
     */
    public void updateWorkItem(Long caseId, String nodeId, int itemNum) throws BPMException;

    /**
     * 
     * @param workitemId
     * @param state
     * @param actionstate
     * @throws BPMException
     */
    public void updateWorkItemToRunState(Long workitemId, int state, String actionstate) throws BPMException;

    /**
     * bpm接口使用
     * @param processId
     * @param state
     * @return
     * @throws BPMException
     */
	public List<Map<String, String>> getWorkItemList(String processId, int state) throws BPMException;

	/**
	 * 
	 * @param workitem
	 * @param process
	 * @param theCase
	 * @param currentActivity
	 * @param context
	 * @return
	 * @throws BPMException
	 */
	public boolean[] isAllExcuteFinished(WorkItem workitem, BPMProcess process, BPMCase theCase,
			BPMAbstractNode currentActivity, WorkflowBpmContext context) throws BPMException;

}
