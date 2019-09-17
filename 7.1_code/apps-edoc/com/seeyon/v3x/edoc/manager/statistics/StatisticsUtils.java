package com.seeyon.v3x.edoc.manager.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;

public class StatisticsUtils {
	private static final Log LOGGER = LogFactory.getLog(StatisticsUtils.class);
	/**
	 * 将查询分组后获得的List数据存入Map中
	 * @param statMap
	 * @param tempList
	 * @param orgIds
	 */
	public static void statHandle(Map<Object, List<Object>> statMap,
		 List tempList,List orgIds) {
		
		Map tempMap = new HashMap();
		for(int j=0;j<tempList.size();j++){
			Object[] obj = (Object[])tempList.get(j);
			int num = Integer.parseInt(String.valueOf(obj[0]));
			tempMap.put(obj[1], num);
		}
		
		
		int count;
		String orgId;   
		//当开始时间大于接收时间时，直接返回       
		if(orgIds.size() == 0){
			return;
		}
		if(tempList.size() < orgIds.size()){ 		
			for(int i=0;i<orgIds.size();i++){
				Object key = orgIds.get(i);
				if(tempMap.get(key) == null){
					if(statMap.get(key) == null){
						List<Object> numList = new ArrayList<Object>();
						numList.add(0);
						statMap.put(key, numList);
					}else{
						List<Object> numList = statMap.get(key);
						numList.add(0);
						statMap.put(key, numList);
					}
				}else{
					if(statMap.get(key) == null){
						List<Object> numList = new ArrayList<Object>();
						
						Object ob = tempMap.get(key);
						numList.add(Integer.parseInt(String.valueOf(ob)));
						
						statMap.put(key, numList);
					}else{
						List<Object> numList = statMap.get(key);
						
						Object ob = tempMap.get(key);
						numList.add(Integer.parseInt(String.valueOf(ob)));
						statMap.put(key, numList);
					}
				}
			}
		}
		else{ 
			for(int j=0;j<tempList.size();j++){
//				Object key = orgIds.get(j);
				Object[] obj = (Object[])tempList.get(j);
				Object key = obj[1];
				if(obj[0] == null){
					count = 0;
				}else{
					count = Integer.parseInt(String.valueOf(obj[0]));
				}
				//j+1 表示第几行，如果统计多个组织的，就有多行
				if(statMap.get(key) == null){
					List<Object> numList = new ArrayList<Object>();
					numList.add(count);
					statMap.put(key, numList);
				}else{
					List<Object> numList = statMap.get(key);
					numList.add(count);
					statMap.put(key, numList);
				}
			}
		}
	}
	
	
	
	public static List getRealTempList(int dimensionType,int orgType,List tempList,Map hasChildOrgMap){
		List temp2List = new ArrayList();
		if(dimensionType == 2 && orgType == StatConstants.ORG){
			Map<Long,Integer> realOrgMap = new HashMap<Long,Integer>(); 
			//把子部门的统计数量叠加到 上级部门中
			for(int k=0;k<tempList.size();k++){
				Object[] obj = (Object[])tempList.get(k);
				int num = Integer.parseInt(String.valueOf(obj[0]));
				long orgId = ((Long)obj[1]).longValue();
				if(hasChildOrgMap.get(StatConstants.CHILD_ORG_SIGN+orgId)!=null){
					String pcIdStr = ((String)hasChildOrgMap.get(StatConstants.CHILD_ORG_SIGN+orgId));
					String[] pcs = pcIdStr.split("_");
					long pId = Long.parseLong(pcs[0]);	//父部分ID
					long cId = Long.parseLong(pcs[1]);	//子部门ID
					
					if(cId == orgId){
						if(realOrgMap.get(pId) == null){
							realOrgMap.put(pId, num);
						}else{
							int n = realOrgMap.get(pId);
							realOrgMap.put(pId, n+num);
						}
					}
				}else{
					temp2List.add(obj);
				}
			}
			
			//当父部门下的人员没有统计值，而其子部门的人员有统计值，这时候realOrgMap有值，而temp2List没有该父部门的值
			//需要将这种情况的父部门统计值加入到 temp2List中
			Map temp2Map = new HashMap();
			for(int k=0;k<temp2List.size();k++){
				Object[] obj = (Object[])temp2List.get(k);
				long orgId = ((Long)obj[1]).longValue();
				temp2Map.put(orgId, "");
			}
			
			//具体的加入过程
			Iterator<Long> it = realOrgMap.keySet().iterator();
			while(it.hasNext()){
				long pId = it.next();
				if(temp2Map.get(pId) == null){
					Object[] ob = new Object[2];
					ob[0] = 0;
					ob[1] = pId;
					temp2List.add(ob);
				}
			}
			
			//将子部门的统计值叠加到父部门统计值中
			for(int k=0;k<temp2List.size();k++){
				Object[] obj = (Object[])temp2List.get(k);
				int num = Integer.parseInt(String.valueOf(obj[0]));
				long orgId = ((Long)obj[1]).longValue();
				if(realOrgMap.get(orgId)!=null){
					int n = realOrgMap.get(orgId);
					obj[0] = n+num;
				}
			}
		}else{
			temp2List = tempList;
		}
		return temp2List;
	}
	
	
	/**
	 * 根据组织类型，获得在Hql中用到的组织部分的Hql
	 * @param orgType
	 * @return
	 */
	public static String getOrgHqlByOrgType(int orgType){
		String orgHql = "";
		switch(orgType){
			case StatConstants.ACCOUNT :
				orgHql = " where b.orgAccountId in (:orgDepartmentId )";
				break;
			case StatConstants.ORG :
				orgHql = " where b.orgDepartmentId in (:orgDepartmentId )";
				break;
			//岗位 (政务版的 职务级别)
			case StatConstants.JOB :	
				orgHql = " ,OrgMember c where c.id = b.startUserId and c.orgPostId in (:orgDepartmentId )";
				break;
			//职务级别 (政务版的 职级)
			case StatConstants.LEVEL :	
				orgHql = " ,OrgMember c where c.id = b.startUserId and c.orgLevelId in (:orgDepartmentId )";
				break;	
			//部门下人员
			case StatConstants.PERSON :	
				//这里 还是写成:orgDepartmentId 只是为了程序统一，但这里表示人员的ID
				orgHql = " where b.startUserId in (:orgDepartmentId )";
				break;		
					
		}
		return orgHql;
	}
	
	public static int getOrgTypeByOrgId(String orgId){
		int orgType = 0;
		if(orgId.startsWith("Account|")){
			orgType = 1;
		}else if(orgId.startsWith("Department|")){
			orgType = 2;
		}else if(orgId.startsWith("Department_Post|")|| orgId.startsWith("Post|") ){
			orgType = 3;
		}else if(orgId.startsWith("Level|")){
			orgType = 4;
		}else if(orgId.startsWith("Member|")){
			orgType = 5;
		}
		return orgType;
		
	}
	
	public static long getOrgIdByOrgInfo(String orgId){
		long oid = 0L;
		if(!orgId.startsWith("Department_Post") || orgId.startsWith("Post|")){
			oid = Long.parseLong(orgId.substring(orgId.indexOf("|")+1));
		}else{
			oid = Long.parseLong(orgId.substring(orgId.lastIndexOf("_")+1));
		}
		return oid;
	}
	
	public static Map<Integer,List<Long>> getOrgMapByOrgId(String orgId){
		Map<Integer,List<Long>> map = new LinkedHashMap<Integer,List<Long>>();
		if(orgId!=null && !"".equals(orgId)){
			String[] orgs = orgId.split(",");
			for(String org : orgs){
				int orgType = getOrgTypeByOrgId(org);
				long oid = getOrgIdByOrgInfo(org);
				
				if(map.get(orgType) == null){
					List<Long> list = new ArrayList<Long>();
					list.add(oid);
					map.put(orgType, list);
				}else{
					List<Long> list = map.get(orgType);
					list.add(oid);
					map.put(orgType, list);
				}
			}
		}
		return map;
	}
	
	
	public static Map getHasChirdOrgMapByOrgId(String orgId){
		Map map = new HashMap();
		if(orgId!=null && !"".equals(orgId)){
			String[] orgs = orgId.split(",");
			for(String org : orgs){
				int orgType = getOrgTypeByOrgId(org);
				long oid = getOrgIdByOrgInfo(org);
				
				if(map.get(orgType) == null){
					List<Long> list = new ArrayList<Long>();
					list.add(oid);
					map.put(orgType, list);
				}else{
					List<Long> list = (List<Long>)map.get(orgType);
					list.add(oid);
					map.put(orgType, list);
				}
				
				//如果是部门，则还要找到其下属部门
				if(orgType == StatConstants.ORG){
					List<Long> allDepIds = findAllChirdrenDep(oid);
					for(Long childId : allDepIds){
						map.put(StatConstants.CHILD_ORG_SIGN +childId, oid+"_"+childId);
					}
					
					List<Long> list = (List<Long>)map.get(orgType);
					if(allDepIds !=null && allDepIds.size()>0){
						list.addAll(allDepIds);
					}
				}	
			}
		}
		return map;
	}
	
	public static List<Long> findAllChirdrenDep(long orgId){
		OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager"); 
		List<V3xOrgDepartment> depList = null;
		try {
			depList = orgManager.getChildDepartments(orgId, false, true);			
		} catch (BusinessException e) {
			LOGGER.error("",e);
		}
		List<Long> allDepIds = new ArrayList<Long>();
		if(depList !=null && depList.size()>0){
			for(V3xOrgDepartment dep: depList){
				allDepIds.add(dep.getId());
			}
		}
		return allDepIds;
	}
	
	
	//获得默认顺序的List
	public static List<Long> getOrderOrgId(String orgId){
		List<Long> list = new ArrayList<Long>();
		String[] orgs = orgId.split(",");
		for(String org : orgs){
			long oid = getOrgIdByOrgInfo(org);
			list.add(oid);
		}
		return list;
	}
	
	
	/**
	 * 根据时间类型，获得在Hql中用到的时间分组部分的Hql
	 * @param orgType
	 * @return
	 */
	public static String getTimeGroupHqlByTimeType(int timeType,int docType){
		String timeHql = "";
		switch(timeType){
			case StatConstants.YEAR :
				timeHql = " year(b.createTime)";
				break;
			case StatConstants.SEASON :
			case StatConstants.MONTH:
			    timeHql =  " year(b.createTime), month(b.createTime) ";
			    break;
            case StatConstants.DAY:
                timeHql = " year(b.createTime), month(b.createTime),day(b.createTime) ";
                break;
		}
		return timeHql;
	}
	
	public static String getOrgGroupHqlByTimeType(int orgType){
		String orgHql = "";
		switch(orgType){
			case StatConstants.ACCOUNT : 
				orgHql =  " b.orgAccountId ";
				break;
			case StatConstants.ORG :
				orgHql =  " b.orgDepartmentId ";
				break;
			case StatConstants.JOB :
				orgHql =  " c.orgPostId ";
				break;	
			case StatConstants.LEVEL:
				orgHql =  " c.orgLevelId ";
				break;
				//部门下人员
			case StatConstants.PERSON :
				orgHql =  " b.startUserId ";
				break;
		}
		return orgHql;
	}
	
	
	public static List timeHandler(int timeType,int dimensionType,List tempList){
		List tList = null;
		//表示季度
		if(timeType == 2){
			tList = new ArrayList();
			int oldyear = 0;
			int oldseason = 0;
			int total = 0;
			String seasonName = "";
			for(int k=0;k<tempList.size();k++){
				Object[] objs = (Object[])tempList.get(k);
				int num = Integer.parseInt(String.valueOf(objs[0]));
				int year = Integer.parseInt(String.valueOf(objs[1]));
				int month = Integer.parseInt(String.valueOf(objs[2]));
				int season = (month-1)/3+1;
				if(k==0){
					oldyear = year;
					oldseason = season;
				}
				if(oldyear == year && oldseason == season){
					total += num;
					seasonName = year+" "+season; 
				}else{
					Object[] seaobj = new Object[2];
					seaobj[0] = total;
					seaobj[1] = seasonName;
					tList.add(seaobj);
					
					total = num;
					seasonName = year+" "+season; 
				}
				oldyear = year;
				oldseason = season;
			}
			//补上最后一个季度的
			Object[] seaobj = new Object[2];
			seaobj[0] = total;
			seaobj[1] = seasonName;
			tList.add(seaobj);
			
			tempList = tList;
		}else{
			if(dimensionType == 1){
				for(int k=0;k<tempList.size();k++){
					Object[] objs = (Object[])tempList.get(k);
					if(objs.length == 2){
						objs[1] = String.valueOf(objs[1]);
					}else if(objs.length == 3){
						//objs[1]为年 objs[2]为月
						Object[] ob = new Object[2];
						ob[0] = objs[0];
						int month = Integer.parseInt(String.valueOf(objs[2]));
						if(month >= 10){
							ob[1] = objs[1]+"-"+objs[2];
						}else{
							ob[1] = objs[1]+"-0"+objs[2];
						}
						tempList.remove(k);
						tempList.add(k, ob);
					}else if(objs.length == 4){
						//objs[1]为年 objs[2]为月 objs[3]为日
						Object[] ob = new Object[2];
						ob[0] = objs[0];
						int month = Integer.parseInt(String.valueOf(objs[2]));
						String s = "";
						if(month >= 10){
							s = objs[1]+"-"+objs[2];
						}else{
							s = objs[1]+"-0"+objs[2];
						}
						int day = Integer.parseInt(String.valueOf(objs[3]));
						if(day >= 10){
							ob[1] = s+"-"+objs[3];
						}else{
							ob[1] = s+"-0"+objs[3];
						}
						tempList.remove(k);
						tempList.add(k, ob);
					}
				}
			}
			tList = tempList;
		}
		return tList;
	}
}
