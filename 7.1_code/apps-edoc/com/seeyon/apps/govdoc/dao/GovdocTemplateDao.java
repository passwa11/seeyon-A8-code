package com.seeyon.apps.govdoc.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
/**
 *	公文模板专用Dao 
 */
public class GovdocTemplateDao {
	
	@SuppressWarnings("rawtypes")
	public int getCategoryMaxSortId(Long parentId) {
		Map<String, Object> param = new HashMap<String, Object>();
		String sql = "SELECT max(sort) from CtpTemplateCategory where parentId=:parentId";
		param.put("parentId", parentId);
		List lst = DBAgent.find(sql, param);
		if (!lst.isEmpty() && lst.get(0) != null) {
			return Integer.valueOf(lst.get(0).toString());
		} else {
			return 0;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	public FlipInfo selectAllSystemTempletes(FlipInfo flipInfo,
			Map<String, String> params) {
		long orgAccountId = AppContext.getCurrentUser().getLoginAccount();

		StringBuilder sql = new StringBuilder();
		Map map = new HashMap<String, Object>();

		sql.append("from CtpTemplate c where 1=1");
		sql.append(" and system=:isSystem ");
		map.put("isSystem", true);
		

		if (params.get("categoryId") != null) {
			String categoryId = params.get("categoryId");
			if(!StringUtil.checkNull(categoryId)){
			    String[] categoryIds = categoryId.split(",");
		        Long[] categoryIdsInt = new Long[categoryIds.length];
		        for (int i = 0; i < categoryIdsInt.length; i++) {
		            categoryIdsInt[i] = Long.parseLong(categoryIds[i]);

		        }
			    sql.append(" and categoryId in (:categoryId) ");
			    map.put("categoryId", categoryIdsInt);
			}
		}else {
			sql.append(" and categoryId is not null ");
		}
		// 根据模版名称查找
		if (params.get("subject") != null) {
            sql.append(" and subject like :subject ");
            map.put("subject", "%" + SQLWildcardUtil.escape(params.get("subject").toString()) + "%");
        }
		// 根据修改时间查找
		if (params.get("startdate") != null) {
            sql.append(" and modifyDate between :startdate and :enddate ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            try {
                String startdate = params.get("startdate").toString();
                if(StringUtil.checkNull(startdate)){
                    startdate = "1900-01-01";
                }
                String enddate = params.get("enddate").toString();
                if(StringUtil.checkNull(enddate)){
                    enddate = "2050-01-01";
                }
                map.put("startdate", sdf.parse(startdate + " 00:00"));
                map.put("enddate", sdf.parse(enddate + " 23:59"));
            } catch (ParseException e) {
                
            }
        }
		// 是否删除
		if (params.get("delete") != null) {
            sql.append(" and c.delete=:delete");
            map.put("delete", Boolean.valueOf(params.get("delete")));
        }
		if (params.get("type") != null) {
            sql.append(" and c.type=:type");
            map.put("type", params.get("type"));
        }
		// 流程复制时只显示流程模板和协同模板
		if (params.get("onlyFlowTemplate") != null) {
            sql.append(" and c.type in ('template','templete','workflow')");
        }
		if (Strings.isNotBlank(params.get("templateId"))) {
            sql.append(" and c.id != :templateId");
            map.put("templateId", Long.parseLong(params.get("templateId")));
        }
        if (params.get("memberId") != null) {
            sql.append(" and c.memberId in (:memberIds)");
            List<Long> ms = new ArrayList<Long>();
            for(String m : params.get("memberId").split(",")){
                ms.add(new Long(m));
            }
            
            map.put("memberIds", ms);
        }
        //是否停用，0启用、1停用
        if (params.get("state") != null) {
            sql.append(" and c.state=:state");
            map.put("state", Integer.valueOf(params.get("state")));
        }
        if (params.get("bodyType") != null) {
            sql.append(" and c.bodyType in (:bodyTypes)");
            
            List<String> bt = Strings.newArrayList(params.get("bodyType").split(","));
            if ("4".equals(params.get("app")) || "401".equals(params.get("app")) || "402".equals(params.get("app"))||"404".equals(params.get("app"))) {
				bt.add("20");
			}
            map.put("bodyTypes", bt);
        }
        sql.append(" and orgAccountId in(:orgAccountIds) and moduleType in (:categoryType) order by sort asc,modifyDate desc,createDate desc");
		List<Long> orgAccountIds = new ArrayList<Long>();
		if(Strings.isNotBlank(params.get("orgAccountId"))){
			List<String> orgAccountIdStr = Strings.newArrayList(params.get("orgAccountId").split(","));
			for(String id : orgAccountIdStr){
				if(Strings.isNotBlank(id)){
					orgAccountIds.add(Long.valueOf(id));
				}
			}
		}else{
			orgAccountIds.add(orgAccountId);
		}
		map.put("orgAccountIds", orgAccountIds);
		//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-9 start
        String[] categoryTypes = String.valueOf(params.get("categoryType")).split(",");
        //客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-9 end
        Integer[] categoryTypesInt = new Integer[categoryTypes.length];
        for (int i = 0; i < categoryTypesInt.length; i++) {
            categoryTypesInt[i] = Integer.parseInt(categoryTypes[i]);

        }
        map.put("categoryType", categoryTypesInt);
		DBAgent.find(sql.toString(), map, flipInfo);
//		if (!page.isEmpty())
//			Log.error("===="+((CtpTemplate)page.get(0)).toJSON());
		return flipInfo;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked" })
	public List getPersonTemplateIds(Long memberId,String subject,String type,boolean isEdoc) {
		String inOrNotIn = isEdoc ? "in" : "not in";

		String hql = "select id from CtpTemplate c where c.memberId=:memberId and c.system =:system and" +
		" c.subject=:subject and c.type=:type and c.delete=:delete and c.moduleType "+ inOrNotIn+" (:moduleTypes)";
		Map map = new HashMap();
		//map.put("orgAccountId",user.getLoginAccount());
		map.put("memberId", memberId);
		map.put("system", Boolean.FALSE);
		map.put("subject",subject);
		map.put("type", type);
		map.put("delete", Boolean.FALSE);
		Set<Integer> moduleTypes = new HashSet<Integer>();
		moduleTypes.add(19);
		moduleTypes.add(20);
		moduleTypes.add(21);
		moduleTypes.add(401);
		moduleTypes.add(402);
		moduleTypes.add(404);
		map.put("moduleTypes", moduleTypes);

		List list = DBAgent.find(hql,map);
		return list;
	}
	/**
	 * 查找联合发文模板
	 */
	@SuppressWarnings("unchecked")
	public List<CtpTemplate> findTemplatesByAuthAccount(Map<String, Object> map) {
		StringBuilder hql = new StringBuilder(
				"select c.id,c.subject from CtpTemplate as c,CtpTemplateAuth as a where c.id=a.moduleId and a.accountId=:orgAccountId and c.delete=0 and c.moduleType=:moduleType");
		List<CtpTemplate> list = new ArrayList<CtpTemplate>();
		List<Long> idList = new ArrayList<Long>();
		List<Object[]> result =  DBAgent.find(hql.toString(), map);
		for (Object[] objects : result) {
			int i =0;
			Long id = (Long) objects[i++];
			String subject = (String) objects[i++];
			if(idList.contains(id)){
				continue;
			}else{
				idList.add(id);
			}
			CtpTemplate ct = new CtpTemplate();
			ct.setId(id);
			ct.setSubject(subject);
			list.add(ct);
		}
		return list;
	}
	
	public Integer getAccountGovdocSysTemplateCount(Long accountId){
		int count = 0;
		if(accountId != null){
			String hql = 
					"from CtpTemplate as c where c.delete=0 and c.system=1 and c.moduleType in (401,402,403,404) and c.orgAccountId=:orgAccountId";
			Map<String, Object> map = new HashMap<String,Object>();
			map.put("orgAccountId", accountId);
			count = DBAgent.count(hql,map);
		}
		
		return count;
	}
}
