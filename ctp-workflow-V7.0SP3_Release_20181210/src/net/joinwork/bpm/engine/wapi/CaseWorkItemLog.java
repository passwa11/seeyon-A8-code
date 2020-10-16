/*
 * Created on 2005-12-6
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.joinwork.bpm.engine.wapi;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.xml.StringXMLElement;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作任务执行日志.</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-8-3 上午09:44:15
 */
public class CaseWorkItemLog implements Serializable {
	public static final long serialVersionUID = 1;
	
	private static final Log log =CtpLogFactory.getLog(CaseWorkItemLog.class);
	
	static ProcessOrgManager processOrgManager;
    static ProcessOrgManager getProcessOrgManager(){
        if(processOrgManager == null){
            processOrgManager = (ProcessOrgManager) AppContext.getBean("processOrgManager");
        }
        return processOrgManager;
    }
	
	/**
	 * 人员状态：删除
	 */
	public static final String delete = "0";
	/**
	 * 人员状态：离职
	 */
	public static final String dimission = "1";
	/**
	 * 人员状态：未分配
	 */
	public static final String UnAssign = "2";

	/**
	 * 人员状态：停用
	 */
	public static final String truce = "3";
	
	/**
	 * 工作任务对应流程实例Id
	 */
	protected long caseId;
	/**
	 * 任务批次
	 */
	protected long batch;
	/**
	 * 工作任务Id
	 */
	protected long workitemId;
	protected int workitemSort;
	/**
	* 工作任务名称
	*/
	protected String workitemName;
	/**
	 * 对应的流程节点Id
	 */
	protected String nodeId;
	/**
	 * 日志详细信息列表.包含WorkItemLog对象的List。
	 * @see WorkItemLog
	 */
	
	protected List logList;
	
	/**
	 * workitem对应的人员id
	 */
	protected String performer;
	
	/**
	 * workitem的状态
	 */
	protected String actionState;
	
	protected Long itemCreateDate;
	
	protected Long itemUpdateDate;
	
	protected CaseWorkItemLog(){
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
	public List getLogList() {
		return logList;
	}

	/**
	 * @return
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @return
	 */
	public long getWorkitemId() {
		return workitemId;
	}

	/**
	 * @return
	 */
	public String getWorkitemName() {
		return workitemName;
	}
	
	public String getPerformer() {
		return performer;
	}
	
	public void setPerformer(String performer) {
		this.performer = performer;
	}
	
	public String getActionState() {
		return actionState;
	}
	
	public int getWorkitemSort() {
		return workitemSort;
	}
	
	public void setWorkitemSort(int workitemSort) {
		this.workitemSort = workitemSort;
	}
	/**
	 * @param logNode
	 */
	public void toXML(StringXMLElement Root) {
	    String[] info = getProcessOrgManager().getNodePerformerInfo(getPerformer(), getWorkitemName());
	    if(info==null){
	        return;
	    }
		StringXMLElement retRoot = Root.addElement("WL");
//		retRoot.addAttribute("caseId", getCaseId());
		retRoot.addAttribute("B", batch);
		retRoot.addAttribute("I", getWorkitemId());
		retRoot.addAttribute("N", getNodeId());
		
		retRoot.addAttribute("P", getPerformer());
		
		retRoot.addAttribute("PN", info[0]);
		retRoot.addAttribute("PS", info[1]);
		retRoot.addAttribute("PA", info[2]);
		
		if(this.itemCreateDate != null){
			retRoot.addAttribute("ICD", this.itemCreateDate);
		}
		if(this.itemUpdateDate != null){
			retRoot.addAttribute("IUD", this.itemUpdateDate);
		}
		
		if(isBlank(getActionState())){
			if (logList != null) {
				for (int i = 0; i < logList.size(); i++) {
					WorkItemLog detailLog = (WorkItemLog)logList.get(i);
					detailLog.toXML(retRoot);
				}
			}
		}
		else{
			retRoot.addAttribute("AS", getActionState());
		}
	}
	private static boolean isBlank(String a){
		return a == null || "".equals(a);
	}
}
