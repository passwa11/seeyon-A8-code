package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;

public class EdocDocTemplateDao extends BaseHibernateDao<EdocDocTemplate> {
	
//	public boolean checkHasName(String name){
//		String hsql="from EdocDocTemplate as edoc where edoc.name=?";
//		List list=super.find(hsql,name);
//		if(list.isEmpty()==false){
//			return true;
//		}
//		return false;
//	}
	
	@SuppressWarnings("unchecked")
	public List<EdocDocTemplate> findByUserIds(List<Long> theIds, int type){
		String sql = "from EdocDocTemplate as edoc where edoc.type=? and edoc.status=? and edoc.domainId in (:ids)";
		
		Map<String, Object> namedParameterMap = new HashMap<String, Object>();
		namedParameterMap.put("ids", theIds);
		
		List<EdocDocTemplate> list = super.find(sql, -1, -1, namedParameterMap, type, Constants.EDOC_USEED);
		return list;
	}
	
	public List<EdocDocTemplate> findByDomainId(Long domainId){
    	return super.findVarargs("from EdocDocTemplate as template where template.domainId=? order by template.domainId", domainId);
		
	}
	
	public List<EdocDocTemplate> findByDomainId(Long domainId,String condition,String textfield){
		StringBuilder sb = new StringBuilder();
		sb.append("select template from EdocDocTemplate as template where template.domainId=:domainId");
		Map<String,Object> parameter = new HashMap<String,Object>();
		parameter.put("domainId",domainId);
		if(Strings.isNotBlank(textfield)){
			if("name".equals(condition) ){
				sb.append(" and template.name like :name ");
				parameter.put("name", "%"+SQLWildcardUtil.escape(textfield)+"%");
			}
			else if("sort".equals(condition) ){
				sb.append(" and template.type = :type ");
				parameter.put("type", Integer.parseInt(textfield));
			}
			else if("status".equals(condition) ){
				sb.append(" and template.status = :status ");
				parameter.put("status", Integer.parseInt(textfield));
			}
		}
		sb.append(" order by template.domainId");
		return super.find(sb.toString(),parameter);
		
	}
	
	public List<EdocDocTemplate> findByDomainIdAndType(Long domainId,int type){
		String sql ="from EdocDocTemplate as template where template.domainId=? and template.type=? order by template.domainId";

    	return super.findVarargs(sql, domainId, type);
		
	}
	
	/**
	 * 根据传入的id集合传（单位/部门），类型（正文/文单），来查找授权的模板
	 * @param ids  id集合传（单位/部门）
	 * @param type  类型（正文/文单）
	 * @param textType 正文类型
	 * @return
	 */
	public List<EdocDocTemplate> findGrantedTemplateForTaohong(String ids, int type, String textType){
		
		StringBuffer sql = new StringBuffer("select template from EdocDocTemplate as template,EdocDocTemplateAcl as tempAcl where template.id = tempAcl.templateId and template.type =?");
		Map<String,Object> namedParameter = new HashMap<String,Object>();
		List<Long> idList = null;
		if(ids != null){
			idList = new ArrayList<Long>();
			String[] tmps = ids.split(",");
			for(String id:tmps)
				idList.add(Long.valueOf(id));
		}
		if(textType!=null && !"".equals(textType)){
			sql.append(" and template.textType =:textType");
			namedParameter.put("textType", textType);
		}
		sql.append(" and tempAcl.depId in (:ids) order by template.createTime");
		namedParameter.put("ids", idList);
		//不分页
		return super.find(sql.toString(),-1,-1,namedParameter,type);
	}
	
	public List searchByCriteria(DetachedCriteria detachedCriteria){
		return super.getHibernateTemplate().findByCriteria(detachedCriteria);
	}
	
}
