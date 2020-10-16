/*
 * Created on 2005-12-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.joinwork.bpm.engine.wapi;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.workflow.xml.StringXMLElement;

/**
 * 工作任务执行日志详细信息.
 * @author shenjian
 * @version 1.0
 * 
 */
public class WorkItemLog implements Serializable {
	
	static Log log = CtpLogFactory.getLog(WorkItemLog.class);
	
	/**
	 * 任务动作：任务处理时间到期，系统自动跳过该任务
	 */
	public static final int ACTION_AUTO_SKIP_OVER= 27;
	/**
	 * 任务动作：已读；指记录第一次打开
	 */
	public static final int ACTION_READ = 26;
	/**
	 * 任务动作：任务取回--当前人
	 */
	public static final int ACTION_TAKEBACK = 25;
	/**
	 * 任务动作：任务终止--当前人
	 */
	public static final int ACTION_STOP_EXECUTOR = 24;
	/**
	 * 任务动作：任务终止
	 */
	public static final int ACTION_STOP = 22;
	
	/**
	 * 任务动作：任务暂存待办
	 */
	public static final int ACTION_ZCDB = 23;
	/**
	 * 任务动作：任务因对应的活动取消而取消
	 */
	public static final int ACTION_CANCEL_BYACTIVITY = 21;
	/**
	 * 任务动作：任务因对应的流程取消而取消
	 */
	public static final int ACTION_CANCEL_BYCASE = 20;
	/**
	 * 任务动作：任务因对应的流程恢复而恢复
	 */
	public static final int ACTION_RESUME_BYCASE = 19;
	/**
	 * 任务动作：任务因对应的流程挂起而挂起
	 */
	public static final int ACTION_SUSPEND_BYCASE = 18;
	/**
	 * 任务动作：任务执行人取消了委托他人执行任务
	 */
	public static final int ACTION_CANCEL_ENTRUST = 17;
	/**
	 * 任务动作：任务评审未通过
	 */
	public static final int ACTION_REVIEW_REDO = 16;
	/**
	 * 任务动作：代理人完成任务
	 */
	public static final int ACTION_FINISHED_DELEGATE = 15;
	/**
	 * 任务动作：委托人完成任务
	 */
	public static final int ACTION_FINISHED_ENTRUST = 14;
	/**
	 * 任务动作：任务重分配
	 */
	public static final int ACTION_REASSIGN = 13;
	/**
	 * 任务动作：委托的任务被退回
	 */
	public static final int ACTION_RETURN_ENTRUST = 12;
	/**
	 * 任务动作：任务恢复
	 */
	public static final int ACTION_RESUME = 11;
	/**
	 * 任务动作：任务挂起
	 */
	public static final int ACTION_SUSPEND = 10;
	/**
	 * 任务动作：保存任务数据
	 */
	public static final int ACTION_SAVE_DATE = 9;
	/**
	 * 任务动作：任务退回
	 */
	public static final int ACTION_RETURN = 8;
	/**
	 * 任务动作：任务审批通过
	 */
	public static final int ACTION_REVIEW_PASS = 7;
	/**
	 * 任务动作：任务相关人员改变
	 */
	public static final int ACTION_REPLACE_USER = 6;
	/**
	 * 任务动作：任务完成
	 */
	public static final int ACTION_FINISHED = 5;
	/**
	 * 任务动作：任务委托他人执行
	 */
	public static final int ACTION_ENTRUST = 4;
	/**
	 * 任务动作：任务被认领
	 */
	public static final int ACTION_CLAIM = 3;
	/**
	 * 任务动作：任务被取消
	 */
	public static final int ACTION_CANCEL = 2;
	/**
	 * 任务动作：任务被分配
	 */
	public static final int ACTION_ASSIGN = 1;
	/**
	 * 任务动作：任务生成
	 */
	public static final int ACTION_CREATE = 0;
	//
	public static final long serialVersionUID = 1;
	/**
	 * 发生时间
	 */
	protected Date datetime;
	/**
	 * 发生动作
	 */
	protected int action;

	/**
	* 工作任务备注信息
	*/
	protected String note;

	protected WorkItemLog() {
	}

	/**
	 * @return
	 */
	public int getAction() {
		return action;
	}

	/**
	 * @return
	 */
	public Date getDatetime() {
		return datetime;
	}

	/**
	 * @param retRoot
	 */
	public void toXML(StringXMLElement Root) {
		StringXMLElement retRoot = Root.addElement("L");
		retRoot.addAttribute("A", action);
		if(datetime!=null){
			retRoot.addAttribute("D", datetime.getTime());
		}

		if(note != null){
			retRoot.addAttribute("T", note);
		}
	}

}
