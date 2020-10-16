/*
 * Created on 2004-5-18
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.joinwork.bpm.engine.wapi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.workflow.util.MessageUtil;

/**
 * 工作任务信息.
 * @author dinghong
 * @version 1.00
 */
public class WorkItem implements Serializable {
	public static final long serialVersionUID = 1;

	/**
	 * 
	 */
	public WorkItem() {

	}
	/**
	 * 工作任务所属的流程实例Id
	 */
	protected long caseId;
	/**
	 * 相关的流程定义模板外部Id
	 */
	protected String processId;
	/**
	 * 相关的活动Id
	 */
	protected String activityId;
	/**
	 * 工作任务Id，唯一
	 */
	protected Long id;
	/**
	 * 优先级
	 */
	protected int level;
	/**
	 * 工作任务负责人的用户Id
	*/
	protected String owner;
	/**
	 * 执行人或被负责人委托执行任务的用户Id
	 */
	protected String performer;
	/**
	 * 完成任务的用户Id
	 */
	protected String finisher;
	/**
	 * 委托人
	 */
	protected String entruster;
	/**
	 * 代理人
	 */
	protected String delegater;
	/**
	 * 工作任务生成时间
	 */
	protected java.util.Date createDate;
	/**
	 * 工作任务最后执行时间
	 */
	protected java.util.Date updateDate;
	/**
	 * 工作任务状态
	 */
	protected int state;
	
	/**
	 * 工作任务状态过程，如：1,17,26
	 */
	protected String actionState;
	/**
	 * 备注信息
	 */
	protected String note;
	
	protected String processIndex="-1";

	public String getProcessIndex() {
		return processIndex;
	}

	public void setProcessIndex(String processIndex) {
		this.processIndex = processIndex;
	}
	protected Map itemDataMap = null;
	
	/**
	 * 任务处于取消状态
	 */
	public static final int STATE_CANCEL = 1;
	/**
	 * 任务处于已完成状态
	 */
	public static final int STATE_FINISHED = 2;
	/**
	 * 任务处于生成待分配状态
	 */
	public static final int STATE_ASSIGN = 3;
	/**
     * 任务处于需要重做状态
     */
    public static final int STATE_NEEDREDO = 4;
    
    /**
     * 任务处于需要重做状态:提交给我
     */
    public static final int STATE_NEEDREDO_TOME= 41;
    
    /**
     * 任务处于待审核状态
     */
    public static final int STATE_DONE = 5;
    /**
     * 任务处于挂起状态
     */
    public static final int STATE_SUSPENDED = 6;
    /**
     * 任务处于待执行状态
     */
    public static final int STATE_READY = 7;
	/**
	 * 任务处于生成待认领状态
	 */
	public static final int STATE_CLAIM = 8;
	/**
     * 任务处于终止状态
     */
    public static final int STATE_STOP = 9;
	/**
	 * 任务创建
	 */
	public static final int STATE_CREATED = STATE_CLAIM;
	/**
	 * 当前用户是否可执行此任务
	 */
	protected int canDo = 0;
	/**
	* 当前用户是否可退回此任务
	*/
	protected int canReturn = 0;
	/**
	* 当前用户是否可委托他人执行此任务
	*/
	protected int canEntrust = 0;
	/**
	 * 当前用户是否可重分配此任务
	 */
	protected int canReAssign = 0;
	/**
	* 当前用户是否可代理执行此任务
	*/
	protected int canDelegate = 0;
	/**
	 * 当前用户是否可认领此任务
	 */
	protected int canClaim = 0;
	/**
	 * 当前用户是否可分配此任务
	 */
	protected int canAssign = 0;
	/**
	 * 当前用户是否可审核此任务
	 */
	protected int canReview = 0;
	/**
	 * 最后期限
	 */
	protected java.util.Date deadline;
	/**
	 * 产生此任务的流程引擎ID
	 */
	protected String EngineDomain;
	/**
	 * 此任务所属的任务管理模块ID
	 */
	protected String TaskDomain;
	
	protected int sort = 0;
	
	protected List logList;
	
	private int superState;

	private long batch;

	private int itemNum;
	
	private String nextStatus;

	public WorkItem(WorkItem item) {
		caseId = item.caseId;
		activityId = item.activityId;
		id = item.id;
		state = item.state;
		actionState = item.actionState;
		sort = item.sort;
	}

	/**
	 * @return
	 */
	public boolean isCanAssign() {
		if (canAssign == 1)
			return true;
		else
			return false;
	}

	/**
	 * @return
	 */
	public boolean isCanClaim() {
		if (canClaim == 1)
			return true;
		else
			return false;
	}

	/**
	 * @return
	 */
	public boolean isCanDo() {
		if (canDo == 1)
			return true;
		else
			return false;
	}

	/**
		 * @return
		 */
	public int getCanAssign() {
		return canAssign;
	}

	/**
	 * @return
	 */
	public boolean getCanClaim() {
		if (canClaim == 1)
			return true;
		else
			return false;
	}

	/**
	 * @return
	 */
	public boolean getCanDo() {
		if (canDo == 1)
			return true;
		else
			return false;
	}

	/**
	 * @return
	 */
	public int getCanReview() {
		return canReview;

	}

	/**
	 * @return
	 */
	public long getCaseId() {
		return caseId;
	}

	/**
	 * @return
	 */
	public java.util.Date getCreateDate() {
		return createDate;
	}

	/**
	 * @return
	 */
	public Long getId() {
		return id;
	}

	public void setNote(String note){
		this.note = note;
//		if(null!= this.note){
//			this.itemDataMap.put("note", this.note);
//		}
	}

	/**
	 * @return
	 */
	public int getState() {
		return state;
	}
	
	public String getActionState(){
		return this.actionState;
	}

	/**
	 * @return
	 */
	public java.util.Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @return
	 */
	public String getPerformer() {
		return performer;
	}

	/**
	* @return
	*/
	public String getProcessId() {
		return processId;
	}
	/**
	 * @return
	 */
	public String getActivityId() {
		return activityId;
	}

	/**
	 * @return
	 */
	public String getEngineDomain() {
		return EngineDomain;
	}

	/**
	 * @return
	 */
	public String getTaskDomain() {
		return TaskDomain;
	}

	/**
	 * @return
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return
	 */
	public int getCanDelegate() {
		return canDelegate;
	}

	/**
	 * @return
	 */
	public int getCanEntrust() {
		return canEntrust;
	}

	/**
	 * @return
	 */
	public int getCanReAssign() {
		return canReAssign;
	}

	/**
	 * @return
	 */
	public int getCanReturn() {
		return canReturn;
	}

	/**
	 * @return
	 */
	public String getDelegater() {
		return delegater;
	}

	/**
	 * @return
	 */
	public String getEntruster() {
		return entruster;
	}

	/**
	 * @return
	 */
	public java.util.Date getDeadline() {
		return deadline;
	}
	public void setDeadline(java.util.Date date) {
		deadline = date;
	}

	/**
	 * @return
	 */
	public String getFinisher() {
		return finisher;
	}

	public String getNote() {
		return note;
	}


    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }
    
	public String getStateName() {
		return null;
	}
	public String getLevelName() {
		if (level == 1)
			return MessageUtil.getString("WorkItemLevel.Higher");
		if (level == 2)
			return MessageUtil.getString("WorkItemLevel.High");
		if (level == 3)
			return MessageUtil.getString("WorkItemLevel.Normal");
		if (level == 4)
			return MessageUtil.getString("WorkItemLevel.Low");
		if (level == 5)
			return MessageUtil.getString("WorkItemLevel.Lower");
		return MessageUtil.getString("WorkItemLevel.Unknow");
	}
	
	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String toString() {
		return "";
	}
	
	/**
	 * @return
	 */
	public int getSuperState() {
		return superState;
	}

	/**
	 * @param i
	 */
	public void setSuperState(int i) {
		superState = i;
	}
	
	/**
	 * @param string
	 */
	public void setFinisher(String string) {
		finisher = string;
	}
	
	public void addLog(WorkItemLog log) {
		if (logList == null){
			logList = new ArrayList();
		}
		logList.add(log);
		//this.itemDataMap.put("logList", logList);
		
		if(actionState == null || "".equals(actionState)){
			actionState = "";
			if(!logList.isEmpty()){
				for (int i = 0; i < logList.size(); i++) {
					if(i != 0){
						actionState += ",";
					}
					
					WorkItemLog l = (WorkItemLog)logList.get(i);
					actionState += l.getAction();
				}
			}
		}
		else{
			actionState += "," + String.valueOf(log.getAction());
		}
		actionState = subStringActionState(actionState);
		// System.out.println("Item "+this.Name+" addLog: "+logList);
	}

	/**
	 * @return
	 */
	public List getLogList() {
		return logList;
	}
	
	private String subStringActionState(String actionState){
	    if(actionState != null && actionState.length() > 100){
            int len = actionState.length() - 100;
            actionState = actionState.substring(len);
            if(actionState.startsWith(",")){
                actionState = actionState.substring(1);
            }
        }
	    return actionState;
	}
	
	protected void doActionState(){
	    if(actionState == null || "".equals(actionState)){
            actionState = "";
            if(!this.logList.isEmpty()){
                for (int i = 0; i < logList.size(); i++) {
                    if(i != 0){
                        actionState += ",";
                    }
                    
                    WorkItemLog l = (WorkItemLog)logList.get(i);
                    actionState += l.getAction();
                }
            }
        }
	}
	
	/**
	 * @param string
	 */
	public void setNextStatus(String string) {
		nextStatus = string;
	}
	
	/**
	 * @return
	 */
	public String getNextStatus() {
		return nextStatus;
	}
	
	public void setActionState(String actionState) {
		this.actionState = subStringActionState(actionState); 
	}
	
	/**
	 * @return
	 */
	public long getBatch() {
		return batch;
	}

	/**
	 * @return
	 */
	public int getItemNum() {
		return itemNum;
	}

	/**
	 * @param i
	 */
	public void setBatch(long i) {
		batch = i;
	}
	
	/**
	 * @param i
	 */
	public void setItemNum(int i) {
		itemNum = i;
	}
}
