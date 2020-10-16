package com.seeyon.v3x.edoc.manager.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocFormDao;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.manager.EdocHelper;

public class SendContentImpl extends BaseHibernateDao<EdocCategory> implements ContentHandler {
 
	private EnumManager enumManagerNew;
	private EdocFormDao edocFormDao;
	
	public void setEdocFormDao(EdocFormDao edocFormDao) {
		this.edocFormDao = edocFormDao;
	}

	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}
	
	public String getSendContentName(String[] scArray){
		StringBuilder content = new StringBuilder();
		
        boolean hasEdocRegister = EdocHelper.hasEdocRegister();
        
        if(!hasEdocRegister){ //屏蔽时，读取公文元素的发文类型 doc_type
        	
            if(scArray!=null && scArray.length > 0){
    			for(String s : scArray){
    				if(Strings.isBlank(s))continue;
    				CtpEnumItem metaItem =  enumManagerNew.getEnumItem(EnumNameEnum.edoc_doc_type,s);
                    if(metaItem!=null){
                    	String enumLabel=ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", metaItem.getLabel());
                    	content.append(enumLabel);
                    	content.append("、");
                    }else{
                    	continue;
                    }
    			}
    		}
        }else{
        	//GOV-4735 公文统计，发文统计，统计内容只选择流程节点，并且全选的时候报红三角
    		if(scArray!=null && scArray.length > 0){
    			
    			List<Long> sc = new ArrayList<Long>();
    			int i=0;
    			for(String s : scArray){
    				//GOV-4735 公文统计，发文统计，统计内容只选择流程节点，并且全选的时候报红三角
    				if(Strings.isBlank(s))return "";
    				sc.add(Long.parseLong(s));
    			}
    			String hql = " from EdocCategory e where e.id in (:id) ";
    			Map<String,Object> parameter = new HashMap<String,Object>();
    			parameter.put("id",sc);
    			Map map = new HashMap();
    			List<EdocCategory> tempList = super.find(hql,-1,-1,parameter);
    			for(int j=0;j<tempList.size();j++){
    				EdocCategory ec = tempList.get(j);
    				map.put(ec.getId(), ec.getName());
    				
    			}
    			String c = "";
    			for(String s : scArray){
    				if(map.get(Long.parseLong(s)) != null){
    					c = (String)map.get(Long.parseLong(s));
    				}else{
    					c = "其他";
    				}
    				content.append(c);
    				content.append("、");
    			}
    			content=content.deleteCharAt(content.length() - 1);
    		}
        }
		
		
		
		return content.toString();
	}
	
	
	@Override
	public List contentDisplay(long loginAccount) {
		List<StatContent> list = new ArrayList<StatContent>();
		
        boolean hasEdocRegister = EdocHelper.hasEdocRegister();
        if(!hasEdocRegister){ //屏蔽时，读取公文元素的发文类型 doc_type
        	List<CtpEnumItem> metaItem =  enumManagerNew.getEnumItems(EnumNameEnum.edoc_doc_type);
        	for(int x=0;x<metaItem.size();x++){
        		CtpEnumItem c=metaItem.get(x);
        		String enumLabel=ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", c.getLabel());
    			StatContent sc = new StatContent(String.valueOf(c.getValue()),enumLabel);
    			list.add(sc);
        	}
        	
        }else{
    		String hql = " from EdocCategory e where e.rootCategory = :rootCategory and e.state = :state and accountId= :accountId ";
    		Map<String,Object> parameter = new HashMap<String,Object>();
    		parameter.put("rootCategory",(long)ApplicationCategoryEnum.edocSend.getKey());                 
    		parameter.put("state",0);
    		parameter.put("accountId",loginAccount);
    		List tempList = super.find(hql,-1,-1,parameter);

    		for(int i=0;i<tempList.size();i++){
    			EdocCategory e = (EdocCategory)tempList.get(i);
    			StatContent sc = new StatContent(String.valueOf(e.getId()),e.getName());
    			list.add(sc);
    		}
    		//Beta版公文统计时 暂时加入其他(外单位授权的文单类型放进其他),SP1会去掉的
    		//但自定义分类不加入
    		StatContent sc = new StatContent("-1","其他");
    		list.add(sc);
        }
		
		return list;
	}

	@Override
	public Map<Object, List<Object>> statisticsTimeAfterFind(
			StatParamVO statParam, List contents) {
		
		Map<Object,List<Object>> statMap = new LinkedHashMap<Object,List<Object>>();
		Map<Integer,List<Long>> orgMap = statParam.getOrgs();  
		//如果是组织维度
		Map hasChildOrgMap = statParam.getHasChirdOrgs();
		
		int timeType = statParam.getTimeType();
		int sendType = 0;
		for(int i=0;i<contents.size();i++){ 
			List finalList = new ArrayList();
			List dimensionFinalList = new ArrayList();
			List tempList = null;
			List dimensionList = null;
			Iterator<Integer> it = orgMap.keySet().iterator();
			while(it.hasNext()){
				int orgType = it.next(); 
				List<Long> orgIds = orgMap.get(orgType);;
				List<Long> hasChildOrgIds = (List<Long>)hasChildOrgMap.get(orgType);
				String orgHql = StatisticsUtils.getOrgHqlByOrgType(orgType);
				String dimensionGroup = "";
				
				if(statParam.getDimensionType() == 1){
					dimensionGroup = StatisticsUtils.getTimeGroupHqlByTimeType(timeType,sendType);
					dimensionList = statParam.getTimes();
				}else if(statParam.getDimensionType() == 2){
					dimensionGroup = StatisticsUtils.getOrgGroupHqlByTimeType(orgType);
					dimensionList = orgIds;
				}
				dimensionFinalList.addAll(dimensionList);
				OrderContent oc = (OrderContent)contents.get(i);
				//列序号
				int order = oc.getOrder();
				String contentName = oc.getContentName();
				long subEdocType = Long.parseLong(contentName);
				
				
				Map<String,Object> parameter = new HashMap<String,Object>();
				
				boolean hasEdocRegister = EdocHelper.hasEdocRegister();
		        
		        
		        if(!hasEdocRegister){ //屏蔽时，读取公文元素的发文类型 doc_type
					StringBuilder hql = new StringBuilder("select count(*),")
					        .append(dimensionGroup)
					        .append(" from EdocSummary b,OrgMember m ")
					        .append(orgHql)
					        .append(" and b.startUserId = m.id and m.deleted = :isDelete ")
					        .append(" and b.createTime >= :starttime and b.createTime <= :endtime ")
                            .append(" and b.docType = :docType and b.edocType = :edocType ")
					        .append(" and b.orgDepartmentId is not null ")   //这里很重要，可以过滤掉草稿箱中的
					        .append(" and b.orgAccountId = :loginAccountId ")
					        .append(" group by ")
					        .append(dimensionGroup) ;
					
					parameter.put("isDelete",false);  
					parameter.put("edocType",0);  
					parameter.put("docType",String.valueOf(subEdocType));
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
					    tempList = super.find(hql.toString(),-1,-1,parameter); 
					}
		        }else if(subEdocType == -1){
					
					StringBuilder hql = new StringBuilder("select count(*),")
					        .append(dimensionGroup)
					        .append(" from EdocSummary b,OrgMember m ")
					        .append(orgHql)
					        .append(" and b.startUserId = m.id and m.deleted = :isDelete ")
                            .append(" and b.createTime >= :starttime and b.createTime <= :endtime ")
                            .append(" and b.edocType = :edocType ")
                            .append(" and b.orgDepartmentId is not null ")
                            .append(" group by ")
                            .append(dimensionGroup) ;
					parameter = new HashMap<String,Object>();  
					parameter.put("isDelete",false);  
					parameter.put("edocType",0);
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime()); 
					List list1 = super.find(hql.toString(),-1,-1,parameter);    
					
					//本单位的所有文单分类
					List<EdocForm> formList = edocFormDao.getMyAccountEdocForms(statParam.getAccountId(),0);
					List<Long> subEdocTypeList = new ArrayList<Long>();
					for(EdocForm form : formList){
						subEdocTypeList.add(form.getSubType());
					}
					
					hql = new StringBuilder("select count(*),")
                                   .append(dimensionGroup)
                                   .append(" from EdocSummary b,OrgMember m ")
                                   .append(orgHql)
                                   .append(" and b.startUserId = m.id and m.deleted = :isDelete ")
                                   .append(" and b.createTime >= :starttime and b.createTime <= :endtime ")
                                   .append(" and b.subEdocType in (:subEdocType) and b.edocType = :edocType ")
                                   .append(" and b.orgDepartmentId is not null ")
                                   .append(" group by ")
                                   .append(dimensionGroup) ;
					
					parameter = new HashMap<String,Object>();
					parameter.put("isDelete",false);  
					parameter.put("edocType",0);
					parameter.put("subEdocType",subEdocTypeList);
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime()); 
					List list2 = super.find(hql.toString(),-1,-1,parameter);    
					Map<String,Integer> map = new HashMap<String,Integer>();
					for(int k=0;k<list2.size();k++){
						Object[] obj = (Object[])list2.get(k);
						map.put(getObjectStr(obj), Integer.parseInt(String.valueOf(obj[0])));
					}
					
					if(list1 != null && list1.size() >0){
						for(int k=0;k<list1.size();k++){
							Object[] obj = (Object[])list1.get(k);
							int count = Integer.parseInt(String.valueOf(obj[0]));
							String key = getObjectStr(obj);
							
							if(map.get(key)!=null){
								int num = map.get(key);
								obj[0] = count - num;
							}
						}
					}
					tempList = list1;
				}else{
					String hql = "select count(*),"+dimensionGroup+" from EdocSummary b,OrgMember m "+ orgHql + 
					        " and b.startUserId = m.id and m.deleted = :isDelete " +
					" and b.createTime >= :starttime and b.createTime <= :endtime "+
					" and b.subEdocType = :subEdocType and b.edocType = :edocType "+
					" and b.orgDepartmentId is not null " +   //这里很重要，可以过滤掉草稿箱中的
					" group by "+dimensionGroup ;
					
					parameter.put("isDelete",false);  
					parameter.put("edocType",0);  
					parameter.put("subEdocType",subEdocType);
					if(orgType == StatConstants.ORG){
						parameter.put("orgDepartmentId",hasChildOrgIds);
					}else{
						parameter.put("orgDepartmentId",orgIds);
					}
					parameter.put("starttime",statParam.getStarttime());
					parameter.put("endtime",statParam.getEndtime()); 
					if(orgIds.size() == 0){
                        tempList = new ArrayList();
                    }else{
                        tempList = super.find(hql,-1,-1,parameter); 
                    }
				}
				
				tempList = StatisticsUtils.getRealTempList(statParam.getDimensionType(), orgType, tempList, hasChildOrgMap);
				
				//当分组用的时间时，比如按年度分组，用hibernate的year函数，查询出来的第二列年度就为int类型了
				/***
				 * select year(b.signingDate) from EdocSummary b

					当是oracle数据库时，会自动翻译成sql语句
					select  extract(year
					           from 
					             b.signing_date)
					from edoc_summary b
				 */ 
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
	
	public static String getObjectStr(Object[] obj){
		StringBuilder str = new StringBuilder();
		for(int i=1;i<obj.length;i++){
			str.append(obj[i]);
		}
		return str.toString();
	}
	
}
