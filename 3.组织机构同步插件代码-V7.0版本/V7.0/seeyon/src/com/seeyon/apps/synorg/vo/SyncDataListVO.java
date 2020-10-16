package com.seeyon.apps.synorg.vo;

import java.util.Date;

/**
 * Description
 * 
 * <pre>
 * 中间表中的数据实体
 * </pre>
 * 
 * @author FanGaowei<br>
 *         Date 2018年2月24日 下午2:28:16<br>
 *         Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncDataListVO {

	/**
	 * <pre>
	 * 编码
	 * </pre>
	 */
	private String code;

	/**
	 * <pre>
	 * 名称
	 * </pre>
	 */
	private String name;

	/**
	 * <pre>
	 * 上级编码
	 * </pre>
	 */
	private String parentCode;

	/**
	 * <pre>
	 * 创建时间
	 * </pre>
	 */
	private Date createDate;

	/**
	 * <pre>
	 * 同步状态
	 * </pre>
	 */
	private String syncState;
	
	/**
	 * <pre>类型/区分部门、职务、岗位</pre>
	 */
	private String type;

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

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getSyncState() {
		return syncState;
	}

	public void setSyncState(String syncState) {
		this.syncState = syncState;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
