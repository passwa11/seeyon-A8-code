/*
 * Created on 2004-5-17
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.joinwork.bpm.engine.wapi;

import java.util.Date;
import java.util.List;

/**
 * 流程定义模板信息
 * @author dinghong
 * @version 1.00
 */
public class ProcessInfo {
	private int sortIndex;
	/**
	 * 模板内部Id.
	 */
	public String id;
	/**
	 * 模板外部Id，即在Studio中开发人员指定的Id.
	 */
	public String processId;
	/**
	 * 模板名称
	 */
	public String name;
	/**
	 * 模板描述
	 */
	public String desc;
	/**
	 * 模板类型
	 */
	public String type;
	/**
	 * 模板流程类型.workflow:协同流程,sessionflow:会话流程
	 */
	public String flowType;
	/**
	 * 模板流程类型显示名称
	 */
	public String flowTypeName;
	/**
	 * 模板的启动节点.包含Status对象的List
	 * @see Status
	 */
	public List startStatusList;
	/**
	 * 模板加入库中的日期
	 */
	public Date createDate;
	/**
	 * 模板最后一次更改的日期
	 */
	public Date updateDate;
	/**
	 * 用户是否可查询此模板
	 */
	public boolean isCanQuery = false;
	/**
		 * 用户是否可删除此模板
		 */
	public boolean isCanDelete = false;
	/**
		 * 用户是否可更新此模板
		 */
	public boolean isCanUpdate = false;
	/**
		 * 用户是否可将此模板从开发状态设置为可执行状态
		 */
	public boolean isCanSetReady = false;
	/**
		 * 用户是否可启动基于此模板的流程实例
		 */
	public boolean isCanStart = false;
	/**
	 * 模板是否有错误
	 */
	public boolean isOk = true;

	public ProcessInfo() {
		isCanQuery = false;
		isCanDelete = false;
		isCanUpdate = false;
	}

	public String toString() {
		return "ProcessInfo: id=" + processId + " name=" + name;
	}
	/**
	 * @return
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @return
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
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
	public String getType() {
		return type;
	}

	/**
	 * @return
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param date
	 */
	public void setCreateDate(Date date) {
		createDate = date;
	}

	/**
	 * @param string
	 */
	public void setDesc(String string) {
		desc = string;
	}

	/**
	 * @param i
	 */
	public void setId(String i) {
		id = i;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setProcessId(String string) {
		processId = string;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

	/**
	 * @param date
	 */
	public void setUpdateDate(Date date) {
		updateDate = date;
	}

	/**
	 * @return
	 */
	public boolean isCanDelete() {
		return isCanDelete;
	}

	/**
	 * @return
	 */
	public boolean isCanQuery() {
		return isCanQuery;
	}

	/**
	 * @return
	 */
	public boolean isCanUpdate() {
		return isCanUpdate;
	}

	/**
		 * @return
		 */
	public boolean getIsCanDelete() {
		return isCanDelete;
	}

	/**
	 * @return
	 */
	public boolean getIsCanQuery() {
		return isCanQuery;
	}

	/**
	 * @return
	 */
	public boolean getIsCanUpdate() {
		return isCanUpdate;
	}

	/**
	 * @param b
	 */
	public void setCanDelete(boolean b) {
		isCanDelete = b;
	}

	/**
	 * @param b
	 */
	public void setCanQuery(boolean b) {
		isCanQuery = b;
	}

	/**
	 * @param b
	 */
	public void setCanUpdate(boolean b) {
		isCanUpdate = b;
	}

	/**
	 * @return
	 */
	public boolean isCanSetReady() {
		return isCanSetReady;
	}

	/**
		 * @return
		 */
	public boolean getIsCanSetReady() {
		return isCanSetReady;
	}

	/**
	 * @param b
	 */
	public void setCanSetReady(boolean b) {
		isCanSetReady = b;
	}

	/**
	 * @return
	 */
	public boolean isCanStart() {
		return isCanStart;
	}

	/**
		 * @return
		 */
	public boolean getIsCanStart() {
		return isCanStart;
	}

	/**
	 * @param b
	 */
	public void setCanStart(boolean b) {
		isCanStart = b;
	}

	/**
	 * @return
	 */
	public String getFlowType() {
		return flowType;
	}

	/**
	 * @return
	 */
	public String getFlowTypeName() {
		return flowTypeName;
	}

	/**
	 * @return
	 */
	public List getStartStatusList() {
		return startStatusList;
	}

	/**
	 * @param string
	 */
	public void setFlowType(String string) {
		flowType = string;
	}

	/**
	 * @param string
	 */
	public void setFlowTypeName(String string) {
		flowTypeName = string;
	}

	/**
	 * @param list
	 */
	public void setStartStatusList(List list) {
		startStatusList = list;
	}

	/**
	 * @return
	 */
	public int getSortIndex() {
		return sortIndex;
	}

	/**
	 * @param i
	 */
	public void setSortIndex(int i) {
		sortIndex = i;
	}

	/**
	 * @return
	 */
	public boolean isOk() {
		return isOk;
	}

	public boolean getIsOk() {
		return isOk;
	}

	/**
	 * @param b
	 */
	public void setOk(boolean b) {
		isOk = b;
	}

}
