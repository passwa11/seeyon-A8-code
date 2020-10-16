package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.v3x.edoc.constants.EdocCustomConstant;
import com.seeyon.v3x.edoc.constants.EdocCustomerTypeTimeEnum;
import com.seeyon.v3x.edoc.constants.TimeType;
import com.seeyon.v3x.edoc.dao.EdocCustomerTypeDao;
import com.seeyon.v3x.edoc.domain.EdocCustomerType;
import com.seeyon.v3x.edoc.manager.statistics.SendContentImpl;
import com.seeyon.v3x.edoc.manager.statistics.StatContent;


public class EdocCustomerTypeManagerImpl implements EdocCustomerTypeManager {
	private EdocCustomerTypeDao edocCustomerTypeDao;
	private SendContentImpl sendContentImpl;

	public void setSendContentImpl(SendContentImpl sendContentImpl) {
		this.sendContentImpl = sendContentImpl;
	}

	public void setEdocCustomerTypeDao(EdocCustomerTypeDao edocCustomerTypeDao) {
		this.edocCustomerTypeDao = edocCustomerTypeDao;
	}

	public EdocCustomerTypeDao getEdocCustomerTypeDao() {
		return edocCustomerTypeDao;
	}

	@Override
	public Map getUserCustomerTypeList(User user,long edocType,String isAdminSet,int type) {
		boolean isAdminSet_flag = false; 
		if(isAdminSet!=null && "true".equals(isAdminSet)){ 
			isAdminSet_flag = true;
		}
		
		Map map = new HashMap();
		List<EdocCustomerType> customerTypeList = null;  
		
		//单位管理员统一设置时的类型编号
		int ktype= 0;
		if(type == 0){
			ktype = EdocCustomConstant.SEND_ADMIN;   //统一设置发文自定义类型
		}
		else if(type == 1){
			ktype = EdocCustomConstant.REC_ADMIN;   //统一设置收文自定义类型
		}
		
		//不是统一设置页面打开的
		if(!isAdminSet_flag){  
			// 获得用户已经选择的
			customerTypeList = edocCustomerTypeDao.getUserCustomerTypeList(user.getId(),edocType); 
			
			//如果用户还没有 自定义，先取单位管理员定义的
			if(customerTypeList.size() == 0 && edocCustomerTypeDao.isUseAccountByEdocSwitchConfig(user.getAccountId(),type)){
				//customerTypeList = edocCustomerTypeDao.getAccountCustomerTypeList(user.getLoginAccount(),edocType);
				customerTypeList = edocCustomerTypeDao.getUserCustomerTypeList(user.getAccountId(),ktype);
			}
		}
		//在统一设置页面打开的，应该显示管理员设置的
		else{
			customerTypeList = edocCustomerTypeDao.getUserCustomerTypeList(user.getAccountId(),ktype);
		}
		map.put("customerTypeList", customerTypeList);
		
		//获得时间 类别列表 
		List<TimeType> timeList = new ArrayList<TimeType>();
		
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.DAY.getKey(),EdocCustomerTypeTimeEnum.DAY.getName()));
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.YESTERDAY.getKey(),EdocCustomerTypeTimeEnum.YESTERDAY.getName()));
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.WEEK.getKey(),EdocCustomerTypeTimeEnum.WEEK.getName()));
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.LAST_WEEK.getKey(),EdocCustomerTypeTimeEnum.LAST_WEEK.getName()));
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.MONTH.getKey(),EdocCustomerTypeTimeEnum.MONTH.getName()));
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.LAST_MONTH.getKey(),EdocCustomerTypeTimeEnum.LAST_MONTH.getName()));
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.YEAR.getKey(),EdocCustomerTypeTimeEnum.YEAR.getName()));
		timeList.add(createTimeType(EdocCustomerTypeTimeEnum.LAST_YEAR.getKey(),EdocCustomerTypeTimeEnum.LAST_YEAR.getName()));
		map.put("timeList", timeList);
		
		//发文自定义类别
		List<StatContent> sendList = sendContentImpl.contentDisplay(user.getAccountId());
		map.put("sendList", sendList);
		
		return map;
	}
	
	private TimeType createTimeType(int id,String name){
		TimeType t = new TimeType();
		t.setId(id);
		t.setName(name);
		return t;
	}
	

	@Override
	public boolean saveUserCustomerType(User user, long[] typeIds,String[] typeName,String[] typeCode,long bigTypeId,long iEdocType,String isAdminSet,int etype) {
		boolean isAdminSet_flag = false;  
		if(isAdminSet!=null && "true".equals(isAdminSet)){ 
			isAdminSet_flag = true;
		}
		if(!isAdminSet_flag){
			edocCustomerTypeDao.deleteEdocCustomerType(user.getId(),iEdocType);
		}else{
			int k = 0;
			if(etype == 0){
				k = EdocCustomConstant.SEND_ADMIN;   //统一设置发文自定义类型
    		}
    		else if(etype == 1){
    			k = EdocCustomConstant.REC_ADMIN;   //统一设置收文自定义类型
    		}			
			edocCustomerTypeDao.deleteEdocCustomerType(user.getLoginAccount(),k);
		}
		
		if(typeIds != null && typeIds.length > 0){
			//但前用户是 单位管理员时，保存单位ID, 普通用户 保存 单位ID 为 0
			List<EdocCustomerType> types = new ArrayList<EdocCustomerType>();
			
//			long accountId = user.isAdministrator() || isEdocAdmin ? user.getLoginAccount() : 0;
			for(int i = 0;i<typeIds.length;i++){  
				EdocCustomerType type = new EdocCustomerType(); 
				//设置主键 ID, 采用assigned策略
				type.setIdIfNew();
				if(!isAdminSet_flag){
					type.setMemberId(user.getId());
					type.setEdocType(iEdocType);
				}else{
					//如果是管理员(包括单位管理员，公文管理员)在后台统一设置的，则用户ID都设成创建单位时的账号ID
					type.setMemberId(user.getLoginAccount());  
					if(etype == 0){
						type.setEdocType(EdocCustomConstant.SEND_ADMIN);   //统一设置发文自定义类型
		    		}
		    		else if(etype == 1){
		    			type.setEdocType(EdocCustomConstant.REC_ADMIN);   //统一设置收文自定义类型
		    		}
				}
				
				type.setTypeId(typeIds[i]);
				if(typeName[i]!=null)
					type.setTypeName(typeName[i]);
				if(typeCode[i]!=null)
					type.setTypeCode(typeCode[i]);
				type.setBigTypeId(bigTypeId);
//				type.setAccountId(accountId);
				
				types.add(type);
			}
			edocCustomerTypeDao.saveEdocCustomerType(types);
		}
		return true;
	}
}
