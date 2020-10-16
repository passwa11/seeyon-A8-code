package com.seeyon.v3x.edoc.manager.statistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.dao.BaseHibernateDao;

public class DealSituationImpl extends BaseHibernateDao implements ContentHandler {

	@Override
	public List contentDisplay(long loginAccount) {
		List<StatContent> list = new ArrayList<StatContent>();
		
		//OA-22287 公文统计中---统计内容---办理情况，选项还有办文、阅文、A8是没有这些的，G6版本才有
		String isCoverFlag=SystemProperties.getInstance().getProperty("edoc.isG6");
        if("true".equals(isCoverFlag)){
            list.add(new StatContent(StatConstants.BAN_WEN,"edoc.banwen"));//办文
            list.add(new StatContent(StatConstants.YUE_WEN,"edoc.read"));//阅文
            list.add(new StatContent(StatConstants.YI_YUE,"edoc.element.receive.readed"));//已阅
            list.add(new StatContent(StatConstants.WEI_YUE,"edoc.element.receive.reading"));//待阅
            list.add(new StatContent(StatConstants.YUEDU_LV,"edoc.readed.rate"));//阅读率
        }
        list.add(new StatContent(StatConstants.YI_BANJIE,"edoc.receive.incomplete"));//已办结
        list.add(new StatContent(StatConstants.WEI_BANJIE,"edoc.receive.notcomplete"));//未办结
        list.add(new StatContent(StatConstants.BANJIE_LV,"edoc.banjie.rate"));//办结率
		return list;
	}

	@Override
	public Map<Object, List<Object>> statisticsTimeAfterFind(
			StatParamVO statParam, List contents) {
		DecimalFormat decimalFormat = new DecimalFormat("######.00");
		Map<Object,List<Object>> statMap = new LinkedHashMap<Object,List<Object>>();
		  
		Map<Integer,List<Long>> orgMap = statParam.getOrgs();     
		//如果是组织维度
		Map hasChildOrgMap = statParam.getHasChirdOrgs();
		
		int timeType = statParam.getTimeType();
		String contentName = null;   
		for(int i=0;i<contents.size();i++){ 
			List finalList = new ArrayList();
			List dimensionFinalList = new ArrayList();
			List tempList = null;
			List dimensionList = null;
			
			List readyList = null;
			List allList = null;
			List readyFinalList = new ArrayList();
			List allFinalList = new ArrayList();
			Iterator<Integer> it2 = orgMap.keySet().iterator();
			while(it2.hasNext()){
				int orgType = it2.next();
				List<Long> orgIds = orgMap.get(orgType);
				List<Long> hasChildOrgIds = (List<Long>)hasChildOrgMap.get(orgType);
				long banWen = 1L;
				long yueWen = 2L;
				String orgHql = StatisticsUtils.getOrgHqlByOrgType(orgType);
				
				String dimensionGroup = "";
				if(statParam.getDimensionType() == 1){
					dimensionGroup = StatisticsUtils.getTimeGroupHqlByTimeType(timeType,1);
					dimensionList = statParam.getTimes();
				}else if(statParam.getDimensionType() == 2){
					dimensionGroup = StatisticsUtils.getOrgGroupHqlByTimeType(orgType);
					dimensionList = orgIds;
				}
				dimensionFinalList.addAll(dimensionList);
				
				OrderContent oc = (OrderContent)contents.get(i);
				//列序号
				int order = oc.getOrder();
				contentName = oc.getContentName(); 
				
				if(contentName.equals(StatConstants.YUE_WEN)){
					//阅文
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					" and b.processType = :processType and b.createTime >= :starttime and b.createTime <= :endtime "+
					" and b.orgAccountId = :loginAccountId "+        
					" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",yueWen);                 
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
					tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				}	
				else if(contentName.equals(StatConstants.BAN_WEN)){
					//办文 
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and  (b.processType is null or b.processType = :processType)  "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime "+
					" and b.orgAccountId = :loginAccountId "+   
					" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",banWen);                  
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
					tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				}	
				//收文办文中的  已办结(未测试)
				else if(contentName.equals(StatConstants.YI_BANJIE)){
//					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,EdocRegister a "+ orgHql +" and b.processType = :processType "+
//					" and a.distributeEdocId = b.id and a.registerDate >= :starttime and a.registerDate <= :endtime and b.completeTime is not null "+
//					" group by "+dimensionGroup ;
					//GOV-4800 公文统计-收发统计，已办结统计不到！
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and  (b.processType is null or b.processType = :processType) "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime and b.completeTime is not null "+
					" and b.orgAccountId = :loginAccountId "+  
					" group by "+dimensionGroup ;
					
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",banWen);                 
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
					tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				}
				//未办结(未测试)
				else if(contentName.equals(StatConstants.WEI_BANJIE)){
//					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,EdocRegister a "+ orgHql +" and b.processType = :processType "+
//					" and a.distributeEdocId = b.id and a.registerDate >= :starttime and a.registerDate <= :endtime and b.completeTime is null "+
//					" group by "+dimensionGroup ;
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and  (b.processType is null or b.processType = :processType)  "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime and b.completeTime is null "+
					" and b.orgAccountId = :loginAccountId "+  
					" group by "+dimensionGroup ;
					
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",banWen);                 
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
					tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				}
				//收文中的阅文   已阅(未测试)
				else if(contentName.equals(StatConstants.YI_YUE)){
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and b.processType = :processType "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime and b.completeTime is not null "+
					" and b.orgAccountId = :loginAccountId "+         
					" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",yueWen);                 
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
					tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				}
				//未阅(未测试)
				else if(contentName.equals(StatConstants.WEI_YUE)){
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and b.processType = :processType "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime and b.completeTime is null "+
					" and b.orgAccountId = :loginAccountId "+  
					" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",yueWen);                 
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
					tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				}
				
				//办结率(未测试)	  已办结 / ( 已办结 + 未办结 )
				else if(contentName.equals(StatConstants.BANJIE_LV)){
					//已办结
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and  (b.processType is null or b.processType = :processType) "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime and b.completeTime is not null "+
					" and b.orgAccountId = :loginAccountId "+  
					" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",banWen);                 
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime());
					parameter.put("loginAccountId",statParam.getAccountId());  
					if(orgIds.size() == 0){
					    readyList = new ArrayList();
                    }else{
                        readyList = super.find(hql,-1,-1,parameter); 
                    }
					readyList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, readyList, hasChildOrgMap);
					
					//全部办文
					hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and  (b.processType is null or b.processType = :processType)  "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime "+
					" and b.orgAccountId = :loginAccountId "+  
					" group by "+dimensionGroup ;
					parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",banWen);                 
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime());
					parameter.put("loginAccountId",statParam.getAccountId());
					if(orgIds.size() == 0){
					    allList = new ArrayList();
                    }else{
                        allList = super.find(hql,-1,-1,parameter); 
                    }
					
					allList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, allList, hasChildOrgMap);
				}
				else if(contentName.equals(StatConstants.YUEDU_LV)){
					//已阅
					String hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and b.processType = :processType "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime and b.completeTime is not null "+
					" and b.orgAccountId = :loginAccountId "+   
					" group by "+dimensionGroup ;
					Map<String,Object> parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",yueWen);                 
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime());
					parameter.put("loginAccountId",statParam.getAccountId());
					if(orgIds.size() == 0){
                        readyList = new ArrayList();
                    }else{
                        readyList = super.find(hql,-1,-1,parameter); 
                    }
					readyList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, readyList, hasChildOrgMap);
					
					//全部阅文
					hql = "select count(*),"+dimensionGroup+" from  EdocSummary b,OrgMember m "+ orgHql +" and b.processType = :processType "+
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					        " and b.createTime >= :starttime and b.createTime <= :endtime "+
					" and b.orgAccountId = :loginAccountId "+   
					" group by "+dimensionGroup ;
					parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("processType",yueWen);                 
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime());
					parameter.put("loginAccountId",statParam.getAccountId());
					if(orgIds.size() == 0){
					    allList = new ArrayList();
                    }else{
                        allList = super.find(hql,-1,-1,parameter); 
                    }
					
					allList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, allList, hasChildOrgMap);	
				}
				if(StatConstants.BANJIE_LV.equals(contentName)||StatConstants.YUEDU_LV.equals(contentName)){
					
					//只有为时间维度时才调用，因为要用时间分组，需要特殊处理
					if(statParam.getDimensionType() == 1){
						readyList = StatisticsUtils.timeHandler(timeType, statParam.getDimensionType(), readyList);
						allList = StatisticsUtils.timeHandler(timeType, statParam.getDimensionType(), allList);
					}
					
					readyFinalList.addAll(readyList);
					allFinalList.addAll(allList);
				}else{
					//只有为时间维度时才调用，因为要用时间分组，需要特殊处理
					if(statParam.getDimensionType() == 1){
						tempList = StatisticsUtils.timeHandler(timeType, statParam.getDimensionType(), tempList);
					}
					finalList.addAll(tempList);
				}
				
			}
			//处理阅读率和办结率
			if(StatConstants.BANJIE_LV.equals(contentName)||StatConstants.YUEDU_LV.equals(contentName)){
				StatisticsUtils.statHandle(statMap, readyFinalList,dimensionFinalList);
				StatisticsUtils.statHandle(statMap, allFinalList,dimensionFinalList);
				
				//map中每一行中List的倒数第二个为已办结  最后的数据就是办文总数量
				int length = statMap.size();
				Iterator it = statMap.keySet().iterator();
				List flist = new ArrayList();
				String percent = "";
				while(it.hasNext()){
					Object key = it.next();
					List<Object> list = (List<Object>)statMap.get(key);
					int size = list.size();
					float yiban = (Integer)list.get(size-2);
					float total = (Integer)list.get(size-1);
					if(Math.abs(yiban-0)< 0.000001 || Math.abs(total-0)< 0.000001){
						percent = "0%";
					}else{
						percent = decimalFormat.format(yiban/total*100)+"%"; 
					}
					list.remove(size-1);
					list.remove(size-2);
					//GOV-4803 公文统计-收发统计，办结率合计显示错误，详见附件！
					list.add(percent+"_"+yiban+"_"+total);
				}
				
			}else{
				StatisticsUtils.statHandle(statMap, finalList,dimensionFinalList);
			}
			
		}
		return statMap;
	}
	
}
