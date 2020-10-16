package com.seeyon.apps.synorg.po.hr;

import java.io.Serializable;
import java.util.Date;

/**
 * 人员中间表实体对象
 * 
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:30:08
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class HrMember implements Serializable {

	/** 序列化ID */
	private static final long serialVersionUID = 6956399632849967317L;

	/** 编号 */
	private String code;

	/** 姓名 */
	private String name;

	/** 登录名 */
	private String loginName;

	/** 是否启用：0-停用 1-启用 */
	private Boolean enable = true;

	/** 部门编号 */
	private String departmentCode;

	/** 岗位编号 */
	private String postName;

	/** 职务级别编号 */
	private String levelCode;

	/** 邮件地址(对应ORG_MEMBER表：EXT_ATTR_2) */
	private String email;

	/** 手机号码(对应ORG_MEMBER表：EXT_ATTR_1) */
	private String telNumber;

	/** 性别 1 - 男 2 - 女(对应ORG_MEMBER表：EXT_ATTR_11) */
	private Integer gender;

	/** 出生日期(对应ORG_MEMBER表：EXT_ATTR_21) */
	private Date birthday;

	/** 身份证号码(对应ORG_MEMBER表：EXT_ATTR_33) */
	private String idNum;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the loginName
	 */
	public String getLoginName() {
		return loginName;
	}

	/**
	 * @param loginName
	 *            the loginName to set
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	/**
	 * @return the enable
	 */
	public Boolean getEnable() {
		return enable;
	}

	/**
	 * @param enable
	 *            the enable to set
	 */
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	/**
	 * @return the departmentCode
	 */
	public String getDepartmentCode() {
		return departmentCode;
	}

	/**
	 * @param departmentCode
	 *            the departmentCode to set
	 */
	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}

	/**
	 * @return the postCode
	 */
	public String getPostName() {
		return postName;
	}

	/**
	 * @param postCode
	 *            the postCode to set
	 */
	public void setPostName(String postName) {
		this.postName = postName;
	}

	/**
	 * @return the levelCode
	 */
	public String getLevelCode() {
		return levelCode;
	}

	/**
	 * @param levelCode
	 *            the levelCode to set
	 */
	public void setLevelCode(String levelCode) {
		this.levelCode = levelCode;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the telNumber
	 */
	public String getTelNumber() {
		return telNumber;
	}

	/**
	 * @param telNumber
	 *            the telNumber to set
	 */
	public void setTelNumber(String telNumber) {
		this.telNumber = telNumber;
	}

	/**
	 * @return the gender
	 */
	public Integer getGender() {
		return gender;
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public void setGender(Integer gender) {
		this.gender = gender;
	}

	/**
	 * @return the birthday
	 */
	public Date getBirthday() {
		return birthday;
	}

	/**
	 * @param birthday
	 *            the birthday to set
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return the idNum
	 */
	public String getIdNum() {
		return idNum;
	}

	/**
	 * @param idNum
	 *            the idNum to set
	 */
	public void setIdNum(String idNum) {
		this.idNum = idNum;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
