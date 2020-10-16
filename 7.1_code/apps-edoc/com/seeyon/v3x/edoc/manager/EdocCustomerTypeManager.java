package com.seeyon.v3x.edoc.manager;

import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;

public interface EdocCustomerTypeManager {

	/**
	 * 获取用户已经勾选的自定义类别列表
	 * @param user 	用户 		
	 * @param iEdocType 公文类型	
	 * @return
	 */
	public Map getUserCustomerTypeList(User user,long iEdocType,String isAdminSet,int edocType);
	
	
	/**
	 * 用户保存 自定义类别列表的 选择
	 * @param user 	用户对象
	 * @param typeIds	类别ID数组
	 * @param bigTypeId	大类别ID
	 * @param iEdocType	是收文还是发文
	 * @param accountId	单位ID
	 * @param isAdminSet 是否管理员后台设置的
	 * @return
	 */
	public boolean saveUserCustomerType(User user,long[] typeIds,String[] typeName,String[] typeCode,long bigTypeId,long iEdocType,String isAdminSet,int type); 
}
