/*
 * Created on 2005-12-3
 *
 * 
 */
package net.joinwork.bpm.engine.wapi;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.seeyon.ctp.workflow.util.MessageUtil;
import com.seeyon.ctp.workflow.xml.StringXMLElement;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 流程实例运行日志</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-10 上午11:14:56
 */
public class CaseLog implements Serializable {
    
    public static final long serialVersionUID           = 1;
    /**
     * 流程实例Id
     * <pre>
     * 这个caseId在实际应用中是没有任何作用的。属于冗余字段。
     * 如果将其改为Long型，不兼容老数据(老数据反序列化不成功).
     * 不在保存这个数据。
     * </pre>
     */
    protected int            caseId;
    
    /**
     * 当前动作发生的节点Id
     */
    protected String         nodeId;
    
    /**
     * 当前动作发生的节点名称
     */
    protected String         nodeName;
    
    /**
     * 动作发生时间
     */
    protected Date           datetime;
    
    /**
     * 运行动作
     */
    protected int            action;
    
    /**
     * 备注
     */
    protected String         note;
    
    /**
     * 动作详细信息列表. 包含CaseDetailLog对象的List
     * @see CaseDetailLog
     */
    protected List<CaseDetailLog>           detailLogList;

    /**
     * 运行动作: 流程启动
     */
    public static final int  ACTION_CASE_START          = 1;

    /**
     * 运行动作: 活动完成
     */
    public static final int  ACTION_ACTIVITY_FINISHED   = 2;

    /**
     * 运行动作: 活动取消
     */
    public static final int  ACTION_ACTIVITY_CANCEL     = 3;

    /**
     * 运行动作: 活动跳转 
     */
    public static final int  ACTION_SET_READY_NODE      = 4;

    /**
     * 运行动作: 子流程完成
     */
    public static final int  ACTION_SUBPROCESS_FINISHED = 5;

    /**
     * 运行动作: 定时触发
     */
    public static final int  ACTION_TIME_FIRED          = 6;

    /**
     * 运行动作: 消息触发
     */
    public static final int  ACTION_MESSAGE_FIRED       = 7;

    /**
     * 运行动作: 任务完成
     */
    public static final int  ACTION_TASK_FINISHED       = 8;

    /**
     * 运行动作: 流程结束
     */
    public static final int  ACTION_CASE_FINISHED       = 9;

    /**2007-04-28 jincm add
     * 运行动作: 活动停止
     */
    public static final int  ACTION_ACTIVITY_STOP       = 10;

    /**2007-04-28 jincm add
     * 运行动作: 活动暂存待办
     */
    public static final int  ACTION_ACTIVITY_ZCDB       = 11;

    /**2007-04-28 jincm add
     * 运行动作: 活动取回待办
     */
    public static final int  ACTION_ACTIVITY_TAKEBACK   = 12;

    public String toString() {
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = "<流程实例操作日志:时间="+sdf.format(getDatetime()) + " - 动作=" + getActionName() + " - 节点名称=" + getNodeName() + " -节点标识= "+getNodeId()+" - 备注=" + getNote()+">";
        return str;
    }

    /**
     * 转成flash能有识别的xml字符串
     * @param Root
     */
    public void toXML(StringXMLElement Root) {
        StringXMLElement retRoot = Root.addElement("S");
        retRoot.addAttribute("A", String.valueOf(action));
        retRoot.addAttribute("N", this.getNodeId());
        if (detailLogList != null) {
            for (int i = 0; i < detailLogList.size(); i++) {
                CaseDetailLog detailLog = (CaseDetailLog) detailLogList.get(i);
                detailLog.toXML(retRoot);
            }
        }
    }

    /**
     * @return 运行动作.
     */
    public int getAction() {
        return action;
    }

    /**
     * 获得动作国际化名称
     * @return
     */
    public String getActionName() {
        if (action == ACTION_CASE_START) {//流程启动
            return MessageUtil.getString("CaseLog.ACTION_CASE_START");
        } else if (action == ACTION_ACTIVITY_FINISHED) {//活动完成
            return MessageUtil.getString("CaseLog.ACTION_ACTIVITY_FINISHED");
        } else if (action == ACTION_ACTIVITY_CANCEL) {//活动取消
            return MessageUtil.getString("CaseLog.ACTION_ACTIVITY_CANCEL");
        } else if (action == ACTION_SET_READY_NODE) {//活动跳转
            return MessageUtil.getString("CaseLog.ACTION_SET_READY_NODE");
        } else if (action == ACTION_SUBPROCESS_FINISHED) {//子流程结束
            return MessageUtil.getString("CaseLog.ACTION_SUBPROCESS_FINISHED");
        } else if (action == ACTION_TIME_FIRED) {//定时触发
            return MessageUtil.getString("CaseLog.ACTION_TIME_FIRED");
        } else if (action == ACTION_MESSAGE_FIRED) {//消息触发
            return MessageUtil.getString("CaseLog.ACTION_MESSAGE_FIRED");
        } else if (action == ACTION_TASK_FINISHED) {//任务完成
            return MessageUtil.getString("CaseLog.ACTION_TASK_FINISHED");
        } else if (action == ACTION_CASE_FINISHED) {//流程结束
            return MessageUtil.getString("CaseLog.ACTION_CASE_FINISHED");
        } else if (action == ACTION_ACTIVITY_STOP) {//活动终止
            return MessageUtil.getString("CaseLog.ACTION_ACTIVITY_STOP");
        } else if (action == ACTION_ACTIVITY_ZCDB) {//活动暂存待办
            return MessageUtil.getString("CaseLog.ACTION_ACTIVITY_ZCDB");
        } else if (action == ACTION_ACTIVITY_TAKEBACK) {//活动取回
            return MessageUtil.getString("CaseLog.ACTION_ACTIVITY_TAKEBACK");
        } else {
            return "";
        }
    }

    /**
     * @return 流程实例Id
     */
    public int getCaseId() {
        return caseId;
    }

    /**
     * @return 发生时间
     */
    public Date getDatetime() {
        return datetime;
    }

    /**
     * @return 备注信息
     */
    public String getNote() {
        return note;
    }

    /**
     * @return 当前节点Id
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * @return
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @return
     */
    public List<CaseDetailLog> getDetailLogList() {
        return detailLogList;
    }

}
