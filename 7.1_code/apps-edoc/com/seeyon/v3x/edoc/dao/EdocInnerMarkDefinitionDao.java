package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

import com.seeyon.v3x.edoc.domain.EdocInnerMarkDefinition;
import com.seeyon.v3x.edoc.util.Constants;

import com.seeyon.ctp.common.dao.BaseHibernateDao;

public class EdocInnerMarkDefinitionDao extends BaseHibernateDao<EdocInnerMarkDefinition> {
	
	
	public Map findInnerMarkList(Long domainId){
		String hsql="from EdocInnerMarkDefinition as def where def.domainId = ? ";
		Object[] values = {domainId};
		List<EdocInnerMarkDefinition> list = super.findVarargs(hsql, values);
		Map map = new HashMap();
		for(EdocInnerMarkDefinition def:list){
			map.put(def.getType(), def);
		}
		return map;
	}
    
	public void saveOrUpdate(Object o) {
	      getHibernateTemplate().saveOrUpdate(o);
//	      getSession().save(o);
	  }
		
	public void removeObject(Object entity) {
	       getHibernateTemplate().delete(entity);
	  } 
	
	public void deleteAll(Long domainId){
		Object[] values = {domainId};
		String hql = "delete from EdocInnerMarkDefinition as def where def.domainId = ? ";
		this.getHibernateTemplate().bulkUpdate(hql, values);
		//super.bulkUpdate("delete from EdocInnerMarkDefinition as def where def.domainId = "+domainId,null);		
	}
	
	public List<EdocInnerMarkDefinition> getAllList(Long domainId){		
		Object[] values = {domainId};
		List<EdocInnerMarkDefinition> list = super.findVarargs("from EdocInnerMarkDefinition as def where def.domainId = ? ", values);
		if(null!=list && list.size()>0){
			return list;
		}else{
			return null;
		}
	}
	
	public EdocInnerMarkDefinition getEdocInnerMarkDefinitionByType(int type,Long domainId){
		String hsql="from EdocInnerMarkDefinition as def where def.type = ? and def.domainId = ? ";		
		List<EdocInnerMarkDefinition> list = super.findVarargs(hsql,type,domainId);
		if(null!=list && list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}
	
	public EdocInnerMarkDefinition getUnificationEdocInnerMarkDefinition(Long domainId){		
		Object[] values = {domainId};
		List<EdocInnerMarkDefinition> list = super.findVarargs("from EdocInnerMarkDefinition as def where def.domainId = ? ", values);
		if(null!=list && list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}
	
	public int uniqueOrSeparate(Long domainId){
		Object[] values = {domainId};
		int result = 0;		
		List<EdocInnerMarkDefinition> list = super.findVarargs("from EdocInnerMarkDefinition as def where domainId = ? ", values);		
		
		if(null==list || list.isEmpty()){
			result = 0;
		}else if(list.size()==1){
			result = 1;
		}else if(list.size()==3){
			result = 2;
		}
		return result;
	}
	
	public List<EdocInnerMarkDefinition> getEdocInnerMarkDefs(int type, long domainId) {
		String hsql = "from EdocInnerMarkDefinition as innerMark where innerMark.type=? and innerMark.domainId=?";
		Object[] values = {type, domainId};
		return super.findVarargs(hsql, values);
	}
	
}
