package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

/**
 * 当前用户是否设置了当前列表的公文标题的多行显示记录对象  
 * @author xiangfan
 *
 */
public class EdocSubjectWrapRecord extends BaseModel  implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6157796752672339405L;

	/* 发文管理-待办 */
	public final static int Edoc_Send_Pending = 1;
	
	/* 发文管理-分发 */
	public final static int Edoc_Send_Fenfa = 2;
	
	/* 收文管理-签收 */
	public final static int Edoc_Receive_Signin = 3;
	
	/* 收文管理-登记 */
	public final static int Edoc_Receive_Register = 4;
	
	/* 收文管理-分发 */
	public final static int Edoc_Receive_Fenfa = 5;
	
	/* 收文管理-待办 */
	public final static int Edoc_Receive_Pending = 6;
	
	/* 收文管理-待阅 */
	public final static int Edoc_Receive_WaitReading = 7;
	
	/* 用户ID */
	private Long userId;
	
	/* 单位ID */
	private Long AccountId; 
	
	/* 公文类型 */
	private Integer edocType;
	
	/* 列表类型 收文管理-签收，收文管理-登记，收文管理-分发 等等*/
	private Integer listType;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getAccountId() {
		return AccountId;
	}

	public void setAccountId(Long accountId) {
		AccountId = accountId;
	}

	public Integer getListType() {
		return listType;
	}

	public void setListType(Integer listType) {
		this.listType = listType;
	}

	public Integer getEdocType() {
		return edocType;
	}

	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
	}
	
}
