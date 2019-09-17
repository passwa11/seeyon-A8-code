package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocFormAcl;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;

public class EdocFormExtendInfoDao extends BaseHibernateDao<EdocFormExtendInfo>{
	public void cancelDefaultEdocForm(Long domainId,int type,Long subType,boolean hasSubType){
		Map<String,Object> params = new HashMap<String,Object>();
		String hsql="update EdocFormExtendInfo s set s.isDefault = :isDefault where s.accountId=:accountId and s.edocForm in (from EdocForm as f where f.type=:type";
		params.put("isDefault", false);
		params.put("accountId", domainId);
		params.put("type", type);
		if(hasSubType){
			if(subType != null){
				hsql += " and f.subType=:subType)";
				params.put("subType", subType);
			}else{
				hsql += " and f.subType is null)";
			}
		}else{
			hsql+=" )";
		}
		super.bulkUpdate(hsql,params);
	}
	/*public EdocFormExtendInfo getDefaultEdocFormExtendInfo(Long domainId,int type){
		return getDefaultEdocFormExtendInfo(domainId, type, -1);
	}*/
	
	public List<EdocFormExtendInfo> getDefaultEdocFormExtendInfo(Long domainId,int type, Long subType){
		String hsql = "select s from EdocForm ef inner join ef.edocFormExtendInfo s  where   s.accountId = ? and ef.type = ? and s.isDefault = ? ";
		List<EdocFormExtendInfo> list = null;
		if(subType!=null && Long.valueOf(subType)!=-1 && Long.valueOf(subType)!=0) {
			hsql += " and ef.subType=?";
			list = super.findVarargs(hsql, new Object[]{domainId,type,true, subType});
		} else {
			list = super.findVarargs(hsql, new Object[]{domainId,type,true});
		}
		
		if(list!=null && list.size()!=0){
			return list;
		}else{
			return null;
		}
	}
	
	
	/**
	 * 获取表单在单位中的授权信息
	 * 
	 * @param formId
	 * @param accountId
	 * @return
	 *
	 * @Since A8-V5 6.1
	 * @Author      : xuqw
	 * @Date        : 2017年4月21日下午4:11:40
	 *
	 */
	public int countExtendInfo(Long formId,Long accountId){
		String hql = "from EdocFormExtendInfo where accountId = :accountId and edocForm.id = :formId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("accountId", accountId);
		params.put("formId", formId);
		return super.count(hql, params);
	}
}
