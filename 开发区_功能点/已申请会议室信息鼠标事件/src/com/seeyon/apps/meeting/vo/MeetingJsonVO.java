package com.seeyon.apps.meeting.vo;

import java.io.Serializable;

public class MeetingJsonVO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1520478593343440925L;

	private String id;
	private String mtappid;
	private String start_date;
	private String end_date;
	private String text;
	private String text_hid;
	private String color;
	private String textColor;
	private String section_id;
	private int status;
	private int timeout;
	private String meetingid;
	private int state;
	private String upmtid;
	private String createUserName;
//zhou
	private String sqrdh;
	private String sqDeptname;
	private String hcyq;

	public String getHcyq() {
		return hcyq;
	}

	public void setHcyq(String hcyq) {
		this.hcyq = hcyq;
	}

	public String getSqrdh() {
		return sqrdh;
	}

	public void setSqrdh(String sqrdh) {
		this.sqrdh = sqrdh;
	}

	public String getSqDeptname() {
		return sqDeptname;
	}

	public void setSqDeptname(String sqDeptname) {
		this.sqDeptname = sqDeptname;
	}

	/**
	 * 用途,说明
	 */
	private String description;

	/** 申请人 */
    private Long perId;

	public String getCreateUserName() {
		return createUserName;
	}
	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}
	public String getUpmtid() {
		return upmtid;
	}
	public void setUpmtid(String upmtid) {
		this.upmtid = upmtid;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getMeetingid() {
		return meetingid;
	}
	public void setMeetingid(String meetingid) {
		this.meetingid = meetingid;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getText_hid() {
		return text_hid;
	}
	public void setText_hid(String text_hid) {
		this.text_hid = text_hid;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getTextColor() {
		return textColor;
	}
	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}
	public String getSection_id() {
		return section_id;
	}
	public void setSection_id(String section_id) {
		this.section_id = section_id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMtappid() {
		return mtappid;
	}
	public void setMtappid(String mtappid) {
		this.mtappid = mtappid;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getPerId() {
		return perId;
	}
	public void setPerId(Long perId) {
		this.perId = perId;
	}
}
