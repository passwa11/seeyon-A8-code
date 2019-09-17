package com.seeyon.v3x.edoc.webmodel;

import java.util.Date;

import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;

public class EdocStatParamVO {
	
	private Long summaryId;
	private Long templeteId;
	private Integer edocType;
	private String subject;
	private String sendDepartmentId;
	private Integer state;
	private Long orgDepartmentId;
	private Long orgAccountId;
	private Long startUserId;
	private Date startTime;
	
	private Long affairId;
	private Long objectId;
	private Long memberDeptId;
	private Long affairMemberId;
	private Integer affairState;
	private Integer affairSubState;
	private Integer affairApp;
	private Integer affairSubApp;
	private String affairNodePolicy;
	private Long countAffairState;
	private Long countAffairSubState;
	
	public void setStatDoneParam(Object[] object) {
		int i = 0;
		this.setEdocType((Integer)object[i++]);
		this.setState((Integer)object[i++]);
		this.setStartTime((Date)object[i++]);
		this.setSummaryId((Long)object[i++]);
		this.setSubject((String)object[i++]);
		this.setAffairMemberId((Long)object[i++]);
		this.setAffairApp((Integer)object[i++]);
		this.setAffairSubApp((Integer)object[i++]);
		this.setAffairNodePolicy((String)object[i++]);
		this.setAffairState((Integer)object[i++]);
		this.setAffairSubState((Integer)object[i++]);
		this.setCountAffairState((Long)object[i++]);
		this.setCountAffairSubState((Long)object[i++]);
		this.setMemberDeptId((Long)object[i++]);
		if(this.getAffairState() == null) {
			this.setAffairState(StateEnum.col_sent.key());
		}
		if(this.getAffairSubApp() == null) {
			this.setAffairSubApp(ApplicationSubCategoryEnum.edocRecHandle.key());
		}
	}
	
	public void setStatSentParam(Object[] object) {
		this.setSummaryId((Long)object[0]);
		this.setStartTime((Date)object[6]);
		this.setOrgDepartmentId((Long)object[7]);
		this.setStartUserId((Long)object[8]);
		this.setOrgAccountId((Long)object[9]);
	}
	
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Long getTempleteId() {
		return templeteId;
	}
	public void setTempleteId(Long templeteId) {
		this.templeteId = templeteId;
	}
	public String getSendDepartmentId() {
		return sendDepartmentId;
	}
	public void setSendDepartmentId(String sendDepartmentId) {
		this.sendDepartmentId = sendDepartmentId;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Long getAffairId() {
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	public Integer getAffairState() {
		return affairState;
	}
	public void setAffairState(Integer affairState) {
		this.affairState = affairState;
	}
	public Integer getAffairSubState() {
		return affairSubState;
	}
	public void setAffairSubState(Integer affairSubState) {
		this.affairSubState = affairSubState;
	}
	public Long getAffairMemberId() {
		return affairMemberId;
	}
	public void setAffairMemberId(Long affairMemberId) {
		this.affairMemberId = affairMemberId;
	}
	
	public Integer getEdocType() {
		return edocType;
	}
	public Long getMemberDeptId() {
		return memberDeptId;
	}

	public void setMemberDeptId(Long memberDeptId) {
		this.memberDeptId = memberDeptId;
	}

	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
	}

	public Integer getAffairApp() {
		return affairApp;
	}

	public void setAffairApp(Integer affairApp) {
		this.affairApp = affairApp;
	}

	public Integer getAffairSubApp() {
		return affairSubApp;
	}

	public void setAffairSubApp(Integer affairSubApp) {
		this.affairSubApp = affairSubApp;
	}

	public String getAffairNodePolicy() {
		return affairNodePolicy;
	}

	public void setAffairNodePolicy(String affairNodePolicy) {
		this.affairNodePolicy = affairNodePolicy;
	}
	
	public Long getOrgDepartmentId() {
		return orgDepartmentId;
	}

	public void setOrgDepartmentId(Long orgDepartmentId) {
		this.orgDepartmentId = orgDepartmentId;
	}

	public Long getOrgAccountId() {
		return orgAccountId;
	}

	public void setOrgAccountId(Long orgAccountId) {
		this.orgAccountId = orgAccountId;
	}

	public Long getCountAffairState() {
		return countAffairState;
	}

	public void setCountAffairState(Long countAffairState) {
		this.countAffairState = countAffairState;
	}

	public Long getCountAffairSubState() {
		return countAffairSubState;
	}

	public void setCountAffairSubState(Long countAffairSubState) {
		this.countAffairSubState = countAffairSubState;
	}

	public Long getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(Long startUserId) {
		this.startUserId = startUserId;
	}
	
}
