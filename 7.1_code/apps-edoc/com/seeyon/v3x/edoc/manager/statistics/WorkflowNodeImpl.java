package com.seeyon.v3x.edoc.manager.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.v3x.edoc.domain.EdocStat;



public class WorkflowNodeImpl extends BaseHibernateDao<EdocStat> implements ContentHandler {
	
	private PermissionManager permissionManager;
	
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}


	@Override
	public List contentDisplay(long loginAccount) {
		
		List l = new ArrayList();
		try {
			//发文时 节点列表

 			List<Permission> sendList = permissionManager.getPermissionsByStatus("edoc_send_permission_policy",1, loginAccount);
	    	List<Permission> recieveList = permissionManager.getPermissionsByStatus("edoc_rec_permission_policy",1, loginAccount);
	    	l.add(sendList);
	    	l.add(recieveList);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	
		return l;
	}


	@Override
	public Map<Object, List<Object>> statisticsTimeAfterFind(
			StatParamVO statParam, List contents) {
		Map<Object,List<Object>> statMap = new LinkedHashMap<Object,List<Object>>();
		
		//orgMap存储的形式为
		//岗位    key 3  List(5235530654617519331,527668748992990125)
		//部门    key 2  List(2676763115351880013)  
		Map<Integer,List<Long>> orgMap = statParam.getOrgs();   
		//如果是组织维度
		Map hasChildOrgMap = statParam.getHasChirdOrgs();
		int timeType = statParam.getTimeType();
		int docType = 0;   
		for(int i=0;i<contents.size();i++){ 
			List finalList = new ArrayList();
			List dimensionFinalList = new ArrayList();
			List tempList = null;
			List dimensionList = null;
			Iterator<Integer> it = orgMap.keySet().iterator();
			while(it.hasNext()){
				OrderContent oc = (OrderContent)contents.get(i);
				//列序号
				int order = oc.getOrder();
				String contentName = oc.getContentName();
				String realContentName = contentName.substring(0,contentName.lastIndexOf("-"));
				
				int orgType = it.next();
				List<Long> orgIds = orgMap.get(orgType);
				List<Long> hasChildOrgIds = (List<Long>)hasChildOrgMap.get(orgType);
				
				String orgHql = StatisticsUtils.getOrgHqlByOrgType(orgType);
				String dimensionGroup = "";
				if(contentName.endsWith(StatConstants.REC_NODE_SUFFIX)){
					docType = 1;
				}
				
				if(statParam.getDimensionType() == 1){
					dimensionGroup = StatisticsUtils.getTimeGroupHqlByTimeType(timeType,docType);
					dimensionList = statParam.getTimes();
				}else if(statParam.getDimensionType() == 2){
					dimensionGroup = StatisticsUtils.getOrgGroupHqlByTimeType(orgType);
					dimensionList = orgIds; 
				} 
				dimensionFinalList.addAll(dimensionList);
				
				//属于发文流程节点
				if(contentName.endsWith(StatConstants.SEND_NODE_SUFFIX)){
					String hql = "select count(*),"+dimensionGroup+" from CtpAffair a, EdocSummary b,OrgMember m "+ orgHql +" and a.objectId = b.id "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime "+
							" and a.nodePolicy = :nodePolicy and a.state = :state and b.edocType = :edocType "+
							" and b.orgAccountId = :loginAccountId "+   
							" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);
					parameter.put("edocType",0);
					parameter.put("state",StateEnum.col_pending.getKey());//待办
					parameter.put("nodePolicy",realContentName);
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime());
					parameter.put("loginAccountId",statParam.getAccountId());
					if(orgIds.size() == 0){
                        tempList = new ArrayList();
                    }else{
                        tempList = super.find(hql,-1,-1,parameter); 
                    }
				}
				//收文流程节点
				else if(contentName.endsWith(StatConstants.REC_NODE_SUFFIX)){
					String hql = "select count(*),"+dimensionGroup+" from CtpAffair z, EdocSummary b,EdocRegister a,OrgMember m "+ orgHql +" and z.objectId = b.id "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and a.distributeEdocId = b.id and a.registerDate >= :starttime and a.registerDate <= :endtime "+
					" and z.nodePolicy = :nodePolicy and z.state = :state and b.edocType = :edocType "+
					" and b.orgAccountId = :loginAccountId "+   
					" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("edocType",1);
					parameter.put("state",StateEnum.col_pending.getKey());//待办
					parameter.put("nodePolicy",realContentName);
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime());
					parameter.put("loginAccountId",statParam.getAccountId());
					if(orgIds.size() == 0){
                        tempList = new ArrayList();
                    }else{
                        tempList = super.find(hql,-1,-1,parameter); 
                    }
				}
				tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				//只有为时间维度时才调用，因为要用时间分组，需要特殊处理
				if(statParam.getDimensionType() == 1){
					tempList = StatisticsUtils.timeHandler(timeType, statParam.getDimensionType(), tempList);
				}
				finalList.addAll(tempList);
			}
			StatisticsUtils.statHandle(statMap, finalList,dimensionFinalList);
		}
		return statMap;
	}
	
	
}
