package com.seeyon.apps.govdoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;

public class GovdocStatSetDao extends BaseHibernateDao<EdocStatSet> {

	/*公文统计授权列表查询*/
	@SuppressWarnings("unchecked")
	public List<EdocStatSet> findEdocStatSetByAccount(long accountId,String statType) throws BusinessException  {
		String hql = "from EdocStatSet where accountId = :accountId";
		if("v3x_edoc_sign_count".equals(statType)){
			hql+="  and (statType='v3x_edoc_sign_count' or statType = 'v3x_edoc_sign_self_count')";
		} else {
			hql+="  and statType='work_count'";
		}
		hql+=" and state = 0 order by orderNo";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("accountId", accountId);
		return DBAgent.find(hql, paramMap);
	}
	
	/*公文统计授权列表树*/
	@SuppressWarnings("unchecked")
	public List<EdocStatSet> findEdocStatTreeList(String statType) throws BusinessException {
		String hql = "from EdocStatSet where statType=:statType and state = 0";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("statType", statType);
		
		return DBAgent.find(hql, paramMap);
	}

	@SuppressWarnings("unchecked")
	public List<EdocStatSet> findEdocStatSetByAccount(Map<String, String> para, FlipInfo fi) throws BusinessException {
		String hql = "from EdocStatSet where statType <> 'state_type' and accountId = :accountId";
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("accountId", AppContext.currentAccountId());
		String condition = String.valueOf(para.get("condition"));
		if (Strings.isNotBlank(para.get("statType"))) {
			hql += " and statType =:statType";
			parameterMap.put("statType", para.get("statType"));
		}
		if ("deptNames".equals(condition)) {
			String str = String.valueOf(para.get("value")).replaceAll("[\\[\\]]", "").split(",")[0];
			String[] strs = str.trim().split("、");
			if (strs.length > 0) {
				hql += " and (";
				for (int i = 0; i < strs.length; i++) {
					if (i == 0) {
						hql += " deptNames like :name" + i;
					} else {
						hql += " or deptNames like :name" + i;
					}
					parameterMap.put("name" + i, "%" + SQLWildcardUtil.escape(strs[i].replaceAll("\"", "")) + "%");

				}
				hql += " )";
			}
		}
		if (Strings.isNotBlank(para.get("deptNames"))) {
			hql += " and deptNames like :deptNames";
			parameterMap.put("deptNames", "%" + SQLWildcardUtil.escape(para.get("deptNames")) + "%");
		}
		if (Strings.isNotBlank(para.get("parentId"))) {
			hql += " and parentId =" + para.get("parentId");
		}
		if ("name".equals(condition)) {
			hql += " and name like :name";
			parameterMap.put("name", "%" + SQLWildcardUtil.escape(para.get("value")) + "%");
		}
		if (Strings.isNotBlank(para.get("name"))) {
			hql += " and name like :name";
			parameterMap.put("name", "%" + SQLWildcardUtil.escape(para.get("name")) + "%");
		}
		if (Strings.isNotBlank(para.get("state"))) {
			hql += " and state =" + para.get("state");
		}
		hql += " order by orderNo";
		return DBAgent.find(hql.toString(), parameterMap, fi);
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocStatSet> checkNameExist(String name,long statId) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		String hql = "from EdocStatSet where name = :name and accountId = :accountId";
		parameterMap.put("name", name);
		parameterMap.put("accountId", AppContext.currentAccountId());
		if(statId !=0L){
			hql +=" and id <> :statId";
			parameterMap.put("statId", statId);
		}
		return DBAgent.find(hql,parameterMap);
	}
	
}
