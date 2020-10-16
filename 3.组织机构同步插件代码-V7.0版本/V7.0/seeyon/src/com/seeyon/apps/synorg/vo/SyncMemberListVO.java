package com.seeyon.apps.synorg.vo;


/**
 * Description
 * 
 * <pre>中间表的人员数据实体</pre>
 * 
 * @author FanGaowei<br>
 *         Date 2018年2月24日 下午3:44:27<br>
 *         Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncMemberListVO {
	/** 编码 */
	private String code;
	/** 编码 */
	private String name;
	/** 登录名 */
	private String loginName;
	/** 部门编码 */
	private String deptCode;
	/** 岗位编码  */
	private String postCode;
	/** 职务编码 */
	private String levelCode;
	/** 邮件地址 */
	private String email;
	/** 电话号码 */
	private String telNum;
	/** 性别 */
	private String gender;
	/** 同步状态 */
	private String syncState;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getLevelCode() {
		return levelCode;
	}

	public void setLevelCode(String levelCode) {
		this.levelCode = levelCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelNum() {
		return telNum;
	}

	public void setTelNum(String telNum) {
		this.telNum = telNum;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSyncState() {
		return syncState;
	}

	public void setSyncState(String syncState) {
		this.syncState = syncState;
	}

}
