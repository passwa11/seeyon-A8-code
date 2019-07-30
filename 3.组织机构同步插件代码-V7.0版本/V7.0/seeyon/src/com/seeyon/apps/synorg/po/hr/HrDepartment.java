package com.seeyon.apps.synorg.po.hr;

import java.io.Serializable;

/**
 * 部门中间表实体对象
 * 
 * @author Yang.Yinghai
 * @date 2015-8-18下午3:44:47
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class HrDepartment implements Serializable {

	/** 序列化ID */
	private static final long serialVersionUID = -7763167719405115848L;

	/** 编号 */
	private String code;

	/** 名称 */
	private String name;

	/** 上级部门编号 */
	private String parentCode;

	/** 排序 */
	private Long sortId = 1L;

	/**
	 * 获取name
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置name
	 * 
	 * @param name
	 *            name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取code
	 * 
	 * @return code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 设置code
	 * 
	 * @param code
	 *            code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 获取parentCode
	 * 
	 * @return parentCode
	 */
	public String getParentCode() {
		return parentCode;
	}

	/**
	 * 设置parentCode
	 * 
	 * @param parentCode
	 *            parentCode
	 */
	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	/**
	 * 获取sortId
	 * 
	 * @return sortId
	 */
	public Long getSortId() {
		return sortId;
	}

	/**
	 * 设置sortId
	 * 
	 * @param sortId
	 *            sortId
	 */
	public void setSortId(Long sortId) {
		this.sortId = sortId;
	}
}
