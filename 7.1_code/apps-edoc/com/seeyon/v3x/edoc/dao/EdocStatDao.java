package com.seeyon.v3x.edoc.dao;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocStat;
import com.seeyon.v3x.edoc.domain.EdocStatCondition;
import com.seeyon.v3x.edoc.manager.statistics.StatisticsUtils;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocStatHelper;

public class EdocStatDao extends BaseHibernateDao<EdocStat> {
	
    private static Log LOG = CtpLogFactory.getLog(EdocStatDao.class);
    
	public Hashtable getEdocStatResult(int edocType, String queryCondition, int groupType) {
		Hashtable<String,Integer> hashtable = new Hashtable<String,Integer>();
		String groupName = "deptId";
		if (groupType == Constants.EDOC_STAT_GROUPBY_DOCTYPE) {
			groupName = "docType";
		}
		StringBuilder sb = new StringBuilder("select count(*),es." + groupName + " from EdocStat es");
		sb.append(" where es.edocType=" + edocType);
		//sb.append(" and es.docType <> -1 ");
		if (queryCondition != null && !"".equals(queryCondition) && !"null".equals(queryCondition)) {
			sb.append(queryCondition);
		}
		sb.append(" group by es." + groupName);			
				
			List tempList = execute(sb.toString());
			
			if (tempList != null && tempList.size() > 0) {
				for (int i = 0; i < tempList.size(); i++) {
					Object[] objs = (Object[])tempList.get(i);
					if (objs != null) {
						String key = null;
						if (groupType == Constants.EDOC_STAT_GROUPBY_DEPT) {
							key = String.valueOf((Long)objs[1]);
						}
						else {
							key = (String)objs[1];
						}
						
						
						Integer value = (Integer)objs[0];
						if(null==key || "".equals(key)){
							key="nulldata";
						}
						hashtable.put(key, value);
					}
				}
			}		
		return hashtable;
	}
	
	public List<EdocStat> queryEdocStat(String hsql,Map objects) {		
		return super.find(hsql, objects);
	}
	
	public List<EdocStat> queryEdocStatAll(String hsql,Map objects) {		
		return super.find(hsql, -1, -1, objects);
	}
	
	public List execute(final String hsql){
		return (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.createQuery(hsql).list();
			}
		});
	}
	
	public List getMembersByOrgId(long orgId,int orgType){
		
		String hql = "";
		switch(orgType){
			//单位
			case 1 : 
				hql = "from OrgMember a where orgAccountId = :orgDepartmentId ";
				break;
			//岗位 (政务版的 职务级别)	
			case 3 : 
				hql = "from OrgMember a where orgPostId = :orgDepartmentId ";
				break;
			//职务级别 (政务版的 职级)	
			case 4 : 
				hql = "from OrgMember a where orgLevelId = :orgDepartmentId ";
				break;	
		}
		Map parameter = new HashMap();
		parameter.put("orgDepartmentId",orgId);
		//部门
		if(orgType == 2){
			List<Long> allOrgIds = new ArrayList<Long>();
			allOrgIds.add(orgId);
			List<Long> orgIds = StatisticsUtils.findAllChirdrenDep(orgId);
			if(orgIds!=null&&orgIds.size()>1){
				allOrgIds.addAll(orgIds);
			}
			hql = "from OrgMember a where orgDepartmentId in (:orgDepartmentId) ";
			parameter.put("orgDepartmentId",allOrgIds);
		}
		return  super.find(hql,-1,-1,parameter);
	}
	
	/**
     * 插入推送到首页的 公文统计的查询条件
     */
	public void saveEdocStatCondition(EdocStatCondition statCondition){
	    super.save(statCondition);
	}
	
	public List<EdocStatCondition> getEdocStatCondition(long accountId,Map<String,Object> paramMap){
	    Map<String,Object> parameter = new HashMap<String,Object>();
	    String hql = "from EdocStatCondition where accountId=:accountId ";
	    
	    String subject = "";
	    if(paramMap.get("subject") != null){
	        subject = String.valueOf(paramMap.get("subject"));
	    }       
	    if(Strings.isNotBlank(subject)){
	        hql += " and title like :subject ";
	        parameter.put("subject", "%" + SQLWildcardUtil.escape(subject) + "%");
	    }
	    try {
	    	Long userId = paramMap.get("userId")==null ? -1 : (Long)paramMap.get("userId");
	    	List<Long> currentUserAllDeptIdList = new ArrayList<Long>();
	    	List<V3xOrgDepartment> currentUserAllDeptList = EdocStatHelper.getCurrentUserAllDeptList();
	    	if(Strings.isNotEmpty(currentUserAllDeptList)) {
	    		for(V3xOrgDepartment dept : currentUserAllDeptList) {
	    			if(GovdocRoleHelper.isDepartmentExchange(userId, dept.getId(), accountId)) {
	    				currentUserAllDeptIdList.add(dept.getId());
	    			}
	    		}
	    	}
		    boolean isAccountExchange = GovdocRoleHelper.isAccountExchange();
		    hql += " and (";
		    hql += " (isOld is null or isOld=:old1) ";
		    parameter.put("old1",true);
		    hql += " or (";
		    hql += " isOld=:old2";
		    parameter.put("old2",false);
		    if(!isAccountExchange) {//单位公文收发员
		        
		        //统计标题只有单位管理员和自己能看到
		        hql += " and userId = :userId";
                parameter.put("userId", userId);
		        
		    	/*hql += " and (";
			    if(Strings.isNotEmpty(currentUserAllDeptIdList)) {
				    hql += " (pushRole=2 and pushFrom in (:pushFromDept))";
				    parameter.put("pushFromDept", currentUserAllDeptIdList);
			    }
			    hql += " or userId = :userId";
			    parameter.put("userId", userId);
			    hql += ")";*/
		    }
		    hql += ")";//
		    hql += ")";
		    hql += ")";
	    } catch(Exception e) {
	        LOG.error("", e);
	    }
	    hql += " order by createTime desc ";
	    
	    int count = -1;
	    if(paramMap.get("count") != null){
            count = Integer.parseInt(String.valueOf(paramMap.get("count")));
        }
	    parameter.put("accountId", accountId);
	    List<EdocStatCondition> list = null;
	    if(count == -1) {
	        list = super.find(hql,parameter);
	    }else{
	        list = super.find(hql,0,count,parameter);
	    }
	    return list;
	}

    public void delEdocStatCondition(long id) {
        super.delete(EdocStatCondition.class, id);
    }

    public EdocStatCondition getEdocStatConditionById(long id) {
        String hql = "from EdocStatCondition where id = :id";
        Map parameter = new HashMap();
        parameter.put("id", id);
        List<EdocStatCondition> list = super.find(hql,-1,-1, parameter);
        if(list != null && list.size()>0 ){
            return list.get(0);
        }
        return null;
    }

    public int getEdocStatConditionTotal(long accountId,String subject) {
        Map<String,Object> parameter = new HashMap<String,Object>();
        String hql = " select title from EdocStatCondition where accountId=:accountId ";       
        parameter.put("accountId", accountId);
        if(Strings.isNotBlank(subject)){
            hql += " and title like :subject ";
            parameter.put("subject", "%"+subject+"%");
        }
        
        List list = super.find(hql, -1,-1, parameter);
        int total = list.size();
        return total;
    }
    
    public void delEdocStatConditionByAccountId(Long accountId){
		String hql = "delete from EdocStatCondition as statcondition  where statcondition.accountId=:accountId";
		HashMap map = new HashMap();
		map.put("accountId",accountId);
		super.bulkUpdate(hql,map);
    }
}
