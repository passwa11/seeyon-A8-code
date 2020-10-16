package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocCustomerType;

public class EdocCustomerTypeDao extends BaseHibernateDao<EdocCustomerType>
{
	/**
	 * 获取用户已经勾选的自定义类别列表
	 * @param memberId 	用户ID 		
	 * @param edocType 公文类型
	 * @return                   
	 * 
	 */
	public List<EdocCustomerType> getUserCustomerTypeList(long memberId,long edocType){
		String hql = " from EdocCustomerType t where t.memberId = :memberId and t.edocType = :edocType order by t.typeId";
		Map parameter = new HashMap();
		parameter.put("memberId",memberId);
		parameter.put("edocType",edocType);
		return  super.find(hql,-1,-1,parameter);
	}
	
	/**
	 * 获取单位管理员自定义类别列表
	 * @param memberId 	用户ID 		
	 * @param edocType 公文类型
	 * @return
	 */
	public List<EdocCustomerType> getAccountCustomerTypeList(long accountId,long edocType){
		String hql = " from EdocCustomerType t where t.accountId = :accountId and t.edocType = :edocType order by t.typeId";
		Map parameter = new HashMap();
		parameter.put("accountId",accountId);
		parameter.put("edocType",edocType);
		return  super.find(hql,-1,-1,parameter);
	}
	
	
	
	/**
	 * 用户增加一组公文自定义类别
	 * @param types 	自定义类别List
	 * @return
	 */
	public void saveEdocCustomerType(List<EdocCustomerType> types){
		super.savePatchAll(types);
	}
	
	/**
	 * 用户删除自己的 公文自定义类别
	 * @param memberId 	用户ID 		
	 * @param edocType 公文类型
	 * @return
	 */
	public void deleteEdocCustomerType(long memberId,long edocType){
		Object[] obj = new Object[]{memberId,edocType};
		String hsql = " delete from EdocCustomerType t where t.memberId = ? and edocType = ? ";
		super.bulkUpdate(hsql, null, obj);
	}
	
	/**
	 * 单位管理员设置公文自定义类别 修改v3x_config表中 
	 * @param accountId  单位ID
	 * @param edocType  公文类型 		
	 * @return
	 */
	public boolean isUseAccountByEdocSwitchConfig(long accountId,long edocType){
		 
		String configItem = edocType == 0 ? "sendCustomType" : "recCustomType";
		Object[] obj = new Object[]{accountId,configItem};
		String hsql = " from ConfigItem t where t.orgAccountId = ? and t.configItem = ? ";
		
		
		ConfigItem item = (ConfigItem)findUnique(hsql,null,obj);
		if(item == null)return false;
		return "yes".equals(item.getConfigValue()) ? true : false;
	}
	
	
	
	
	
	
	
	/**
	 * 判断当前用户是否 勾选过 公文自定义类别
	 * @param memberId 	用户ID
	 * @return
	 */
	public boolean isHaveType(long memberId){
		String hql = " from EdocCustomerType t where t.memberId = ? ";
		Long[] values = {memberId};
		Type[] types = {Hibernate.LONG};
		int count = super.getQueryCount(hql, values, types);
		
		if(count>0){
			return true;
		}	
		return false;
	}
	
	
	
}