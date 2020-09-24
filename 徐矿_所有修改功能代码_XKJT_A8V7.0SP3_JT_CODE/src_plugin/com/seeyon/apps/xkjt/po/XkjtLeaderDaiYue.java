package com.seeyon.apps.xkjt.po;

import java.sql.Timestamp;
import java.util.Date;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 
 * @ClassName: XkjtLeaderDaiYue  
 * @Description: TODO(这里用一句话描述这个类的作用)  
 * @author wxt.admin
 * @date 2019年4月12日 下午3:13:09
 */
public class XkjtLeaderDaiYue extends BasePO {
	private Long id;
	private String leaderName;//领导姓名
	/*
	 * 11 未阅
	 * 12 已阅
	 * 13 已撤销
	 */
	private Integer status;
	private String title;//公文名称
	private Timestamp sendDate;//送往时间
	private String senderName;//发起者名字
	private Integer edocType;//公文类型
	private Long sendRecordId;//公文发送记录ID
	private Long edocId;//公文ID
	private Long leaderId;
	/** 项目：徐州矿物集团【添加了一个新字段SIGN_FOR_DATE，且阅读后添加进当前时间】 作者：wxt.xiangrui 时间：2019-6-3 start */
	private Timestamp signForDate;//送往时间
	/** 项目：徐州矿物集团【添加了一个新字段SIGN_FOR_DATE，且阅读后添加进当前时间】 作者：wxt.xiangrui 时间：2019-6-3 start */

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLeaderName() {
		return leaderName;
	}
	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Timestamp getSendDate() {
		return sendDate;
	}
	public void setSendDate(Timestamp sendDate) {
		this.sendDate = sendDate;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public Integer getEdocType() {
		return edocType;
	}
	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
	}
	public Long getSendRecordId() {
		return sendRecordId;
	}
	public void setSendRecordId(Long sendRecordId) {
		this.sendRecordId = sendRecordId;
	}
	public Long getEdocId() {
		return edocId;
	}
	public void setEdocId(Long edocId) {
		this.edocId = edocId;
	}
	public Long getLeaderId() {
		return leaderId;
	}
	public void setLeaderId(Long leaderId) {
		this.leaderId = leaderId;
	}
	public Timestamp getSignForDate() {
		return signForDate;
	}
	public void setSignForDate(Timestamp signForDate) {
		this.signForDate = signForDate;
	}
	
	
	
	
	
}
