/*
 * Created on 2004-6-29
 *
 */
package net.joinwork.bpm.engine.wapi;

import java.util.Date;
import java.util.List;

/**
 *  流程实例信息
 * @author dinghong
 * @version 1.00
 */
public class CaseInfo {
	protected boolean isSubCase = false;
	/**
	 * 用户是否可以查询流程实例的状态.
	 */
	protected boolean canQueryState = false;
	/**
	 * 用户是否可以查询流程实例的运行日志.
	 */
	protected boolean canQueryRunlog = false;
	/**
	* 用户是否可以取消流程实例.
	*/
	protected boolean canCancel = false;
	/**
	* 用户是否可以挂起或恢复流程实例.
	*/
	protected boolean canSuspend = false;
	/**
	* 用户是否可以替换流程实例的模板.
	*/
	protected boolean canReplaceProcess = false;
	/**
	 * 流程实例Id,唯一标识.
	 */
	public long caseId;
	/**
	 * 流程实例描述，一般由启动此流程实例的用户输入.
	 */
	public String caseName;
	/**
	 * 流程实例对应的流程模板名称.
	 */
	public String processName;
	/**
	 * 流程实例对应的流程模板Id.
	 */
	public String processId;
	/**
	 * 流程实例对应的流程模板编号.
	 */
	protected String processIndex;
	/**
	* 流程实例当前就绪的状态节点列表.
	* 包含Status对象。
	* @see Status
	*/
	protected List statusList = null;
	/**
	 * 流程实例所处状态Id
	 */
	protected int state;
	/**
	 * 流程实例处于运行状态
	 */
	public static final int STATE_RUNNING = 2;
	/**
	 * 流程实例处于完成状态
	 */
	public static final int STATE_FINISHED = 3;
	/**
	 * 流程实例处于取消状态
	 */
	public static final int STATE_CANCEL = 4;
	/**
	 * 流程实例处于挂起状态
	 */
	public static final int STATE_SUSPEND = 5;
	/**
	 * 流程实例处于终止状态
	 */
	public static final int STATE_STOP = 6;
	/**
	 * 流程实例所处状态名称.
	 */
	protected String stateName;
	/**
	 * 流程实例的启动时间.
	 */
	public Date startDate;
	/**
	 * 流程实例的最后一次的修改时间。如果流程实例处于完成或取消状态，就是结束时间.
	 */
	protected Date finishDate;
	/**
	 * 启动此流程实例的用户
	 */
	public String startUser;
	/**
		 * 此流程实例中最近一次执行任务的用户
		 */
		public String lastPerformer;


	public long getCaseId() {
		return caseId;
	}

	public String getCaseName() {
		return caseName;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public String getProcessName() {
		return processName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getStartUser() {
		return startUser;
	}

	public int getState() {
		return state;
	}

	public String getStateName() {
		return stateName;
	}


	public boolean getCanQueryRunlog() {
		return canQueryRunlog;
	}

	public boolean getCanQueryState() {
		return canQueryState;
	}

	public boolean getCanCancel() {
		return canCancel;
	}

	public boolean getCanSuspend() {
		return canSuspend;
	}

	

	public boolean getCanReplaceProcess() {
		return canReplaceProcess;
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
	public List getStatusList() {
		return statusList;
	}

	/**
	 * @return
	 */
	public String getProcessIndex() {
		return processIndex;
	}

	
	/**
	 * @return
	 */
	public String getLastPerformer() {
		return lastPerformer;
	}

	public boolean getIsSubCase(){
		return isSubCase;
	}

}
