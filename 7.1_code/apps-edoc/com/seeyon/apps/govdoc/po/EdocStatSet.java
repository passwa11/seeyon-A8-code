package com.seeyon.apps.govdoc.po;

import java.sql.Timestamp;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 公文统计授权
 */
public class EdocStatSet extends BasePO {

	private static final long serialVersionUID = 5112042293339006526L;
	
	private String name;//统计名称
	private Long parentId;//父亲节点ID
	private Long accountId;//单位id
	private String statType;//权限类别
	private String recNode;//收文节点
	private String sendNode;//发文节点
	private Timestamp modifyTime;//创建时间或修改时间
	
	private String deptIds;//公文处理统计部门列表id
	private String deptNames;//公文处理统计部门列表名称
	/*客开 项目名称：贵州省政府，作者：myc，修改时间：20160421，修改功能：添加自定义统计授权项 ,start*/
	private String statNodePolicy;//发文节点名称
	private int state;//是否启用
	private String comments;//备注
	private int orderNo;
	private String timeType;
	private String govType;
	private String recNodePolicy;//收文节点名称
    private int initState;//是否预制数据
    
    public EdocStatSet() {
	}
    
	public int getInitState() {
		return initState;
	}
	public void setInitState(int initState) {
		this.initState = initState;
	}
	public String getRecNode() {
		return recNode;
	}
	public void setRecNode(String recNode) {
		this.recNode = recNode;
	}
	public String getSendNode() {
		return sendNode;
	}
	public void setSendNode(String sendNode) {
		this.sendNode = sendNode;
	}
	public String getRecNodePolicy() {
		return recNodePolicy;
	}
	public void setRecNodePolicy(String recNodePolicy) {
		this.recNodePolicy = recNodePolicy;
	}
	public int getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}
	public String getTimeType() {
		return timeType;
	}
	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}
	public String getGovType() {
		return govType;
	}
	public void setGovType(String govType) {
		this.govType = govType;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public String getDeptNames() {
		return deptNames;
	}
	public void setDeptNames(String deptNames) {
		this.deptNames = deptNames;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getStatNodePolicy() {
		return statNodePolicy;
	}
	public void setStatNodePolicy(String statNodePolicy) {
		this.statNodePolicy = statNodePolicy;
	}
	public String getDeptIds() {
		return deptIds;
	}
	public void setDeptIds(String deptIds) {
		this.deptIds = deptIds;
	}
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public String getStatType() {
		return statType;
	}
	public void setStatType(String statType) {
		this.statType = statType;
	}
	public Timestamp getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}
}
