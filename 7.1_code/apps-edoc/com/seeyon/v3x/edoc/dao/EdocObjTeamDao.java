package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.organization.po.OrgUnit;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.edoc.domain.EdocObjTeamMember;

public class EdocObjTeamDao extends BaseHibernateDao<EdocObjTeam> {
	
	public List<EdocObjTeam> findAll()
	{
		List<EdocObjTeam> edocObjTeams=null;
		DetachedCriteria criteria = DetachedCriteria.forClass(EdocObjTeam.class)
		.add(Expression.eq("state",EdocObjTeam.STATE_USE))
		;
		edocObjTeams=super.executeCriteria(criteria);
		return edocObjTeams;
	}
	
	public List<EdocObjTeam> findAllByAccount(Long accountId, boolean isPager)
	{
		List<EdocObjTeam> edocObjTeams=null;
		DetachedCriteria criteria = DetachedCriteria.forClass(EdocObjTeam.class)
		.add(Expression.eq("orgAccountId",accountId))
		.add(Expression.eq("state",EdocObjTeam.STATE_USE))
		.addOrder(Order.asc("sortId"))
		.addOrder(Order.desc("createTime"));
		;
		if(isPager){
			edocObjTeams=super.executeCriteria(criteria);
		}
		else{
			edocObjTeams=super.executeCriteria(criteria, -1, -1);
		}
		return edocObjTeams;
	}
	public List<EdocObjTeam> findAllSimpleByAccount(Long accountId, boolean isPager)
	{
	    List<Object[]> temp=null;
	    DetachedCriteria criteria = DetachedCriteria.forClass(EdocObjTeam.class)
	            .setProjection(Projections.projectionList().add(Projections.property("id")).add(Projections.property("name")).add(Projections.property("sortId")))
	            .add(Expression.eq("orgAccountId",accountId))
	            .add(Expression.eq("state",EdocObjTeam.STATE_USE))
	            .addOrder(Order.desc("createTime"));
	    ;
	    if(isPager){
	        temp=super.executeCriteria(criteria);
	    }
	    else{
	        temp=super.executeCriteria(criteria, -1, -1);
	    }
	    
	    List<EdocObjTeam> edocObjTeams = new ArrayList<EdocObjTeam>(temp.size());
	    for (Object[] o : temp) {
	        EdocObjTeam t = new EdocObjTeam();
	        t.setId((Long)o[0]);
	        t.setName((String)o[1]);
	        t.setSortId((Integer)o[2]);
	        
	        edocObjTeams.add(t);
        }
	    
	    return edocObjTeams;
	}
	
	
	public void updateState(String ids,Byte state)
	{
		String hql="update EdocObjTeam set state=? where id in (:ids)";
		Map<String,Object> namedParameter = new HashMap<String,Object>();
		List<Long> idList = new ArrayList<Long>();
		String[] tmp = ids.split(",");
		for(String id:tmp)
			idList.add(Long.valueOf(id));
		namedParameter.put("ids", idList);
		super.bulkUpdate(hql,namedParameter,state);
	}
	public EdocObjTeam findByAccountAndName(Long accountId,String orgName)
	{
		List<EdocObjTeam> edocObjTeams=null;
		DetachedCriteria criteria = DetachedCriteria.forClass(EdocObjTeam.class)
		.add(Expression.eq("orgAccountId",accountId))
		.add(Expression.eq("state",EdocObjTeam.STATE_USE))
		.add(Expression.eq("name",orgName))
		;
		edocObjTeams=super.executeCriteria(criteria);
		if(edocObjTeams.size()>0){return edocObjTeams.get(0);}
		else{return null;}
	}
	
	/**
	* @Title: getOrgTeamForDepartment
	* @Description: TODO 获取机构组下的部门信息
	* @param @param ids
	* @param @return    设定文件
	* @return Map<Long,String>    返回类型
	* @throws
	*/
	public Map<Long,String> getOrgTeamForDepartment(String ids,Long loginAccountId){
		// TODO Auto-generated method stub
		Map<Long,String> map = new HashMap<Long,String>();
		if(StringUtils.isBlank(ids)){
			return map;
		}
		Set<Long> setValue = new HashSet<Long>();
		StringBuilder hql = new StringBuilder();
		StringBuilder hqls = new StringBuilder();
		StringBuilder returnValue = new StringBuilder();
		Map<String,Object> params = new HashMap<String,Object>();
		hql.append(" FROM OrgUnit as t , EdocObjTeamMember as u ")
		   .append(" WHERE t.id = u.memberId ")
		   .append(" AND  u.teamId in (:ids)");
		hql.append("  ORDER BY u.teamId asc ");
		List<Long> idList = new ArrayList<Long>();
		String[] tmp = ids.split(",");
		for(String id:tmp){
			idList.add(Long.valueOf(id));
		}
		params.put("ids", idList);
		List<Object[]> ll = DBAgent.find(hql.toString(), params);
		List<OrgUnit> list = new ArrayList<OrgUnit>();
		List<EdocObjTeamMember>  edocObjTeamMemberlist = new ArrayList<EdocObjTeamMember>();
		Map<Long,Long> objTeamMemberMap = getEdocObjTeamMemberInfo(ids);
		for (Object[] o : ll) {
			OrgUnit orgunit = (OrgUnit)o[0];
			EdocObjTeamMember edocobjteammember = (EdocObjTeamMember)o[1];
			list.add(orgunit);
			edocObjTeamMemberlist.add(edocobjteammember);
			
		}
		int i = 0;
		int isAppend=0;
		int j=0;
		boolean isTrue=true;
		for(OrgUnit ou:list){
			if (!setValue.contains(objTeamMemberMap.get(edocObjTeamMemberlist.get(j).getId()))) {
				setValue.add(objTeamMemberMap.get(edocObjTeamMemberlist.get(j).getId()));
				if (isAppend >= 1) {
					returnValue.append("]");
					isAppend = 0;
					map.put(objTeamMemberMap.get(edocObjTeamMemberlist.get(j-1).getId()), returnValue.toString());
					returnValue.setLength(0);
					isTrue=false;
				}
				isAppend++;
				returnValue.append("[");
			}
			if(i++!=0&&isTrue){
				returnValue.append(",");
			}
			returnValue.append("'"+ou.getType()+"")
					   .append("|")
					   .append(ou.getOrgAccountId())
					   .append("_")
					   .append(ou.getId())
					   .append("|")
					   .append(ou.getName())
					   .append("'");
			isTrue=true;
			if (i == list.size()) {
				returnValue.append("]");
				isAppend = 0;
				map.put(objTeamMemberMap.get(edocObjTeamMemberlist.get(j).getId()), returnValue.toString());
				returnValue.setLength(0);
			}
			j++;
		}
		return map;
	
	}
	/**
	* @Title: getEdocObjTeamMemberInfo
	* @Description: TODO获取机构组关联表信息
	* @param @param ids
	* @param @return    设定文件
	* @return Map<Long,Long>    返回类型
	* @throws
	*/
	private Map<Long,Long> getEdocObjTeamMemberInfo(String ids){
		Map<Long,Long> map = new HashMap<Long,Long>();
		Map<String,Object> params = new HashMap<String,Object>();
		StringBuilder hql=new StringBuilder();
		hql.append(" FROM EdocObjTeamMember ")
		   .append(" WHERE teamId IN(:ids)")
		   .append(" ORDER BY teamId asc");
		List<Long> idList = new ArrayList<Long>();
		String[] tmp = ids.split(",");
		for(String id:tmp){
			idList.add(Long.valueOf(id));
		}
		params.put("ids", idList);
		List<Object> ll = DBAgent.find(hql.toString(), params);
		EdocObjTeamMember edocObjTeamMember = new EdocObjTeamMember();
		List<EdocObjTeamMember> edocTeamMemberList = new ArrayList<EdocObjTeamMember>();
		for (Object o : ll) {
			EdocObjTeamMember edocobjteammember = (EdocObjTeamMember)o;
			edocTeamMemberList.add(edocobjteammember);
			
		}
		for(EdocObjTeamMember objTeamMember : edocTeamMemberList){
			map.put(objTeamMember.getId(),objTeamMember.getTeamId());
		}
		return map;
	}
	
}
