package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocUtil;

public class EdocFormDao extends BaseHibernateDao<EdocForm>
{
	
	public List<EdocForm> getAllEdocForms(Long domainId)
	{
		String hsql = "select a from EdocForm as a left join a.edocFormExtendInfo as info  where info.accountId = ? and info.status<>? ";
		////TODO(5.0sprint3)--还是有疑问，如果没有公文插件，就查询签报的，这个sql还是有问题。-杨帆，2.2
		//TODO(5.0sprint3)if(SystemEnvironment.hasPlugin("edoc")==false){
		//TODO(5.0sprint3)	hsql+=" and a.type=2";
		//TODO(5.0sprint3)}
		hsql+=" order by info.status desc,a.type asc , a.lastUpdate asc";
		return super.findVarargs(hsql, domainId, EdocForm.C_iStatus_Deleted);
	}
	
	/**
	 * 获取某个单位下需要修复的系统文单
	 * @Author      : xuqw
	 * @Date        : 2015年4月1日下午4:44:11
	 * @param domainId
	 * @return
	 */
	public List<EdocForm> getToFixSysEdocForms(Long domainId){
	    
	    String hsql = "select a from EdocForm as a left join a.edocFormExtendInfo as info  where info.accountId = ? and info.status<>? and a.isSystem = ? and (a.content is null or a.content like '')";
        hsql+=" order by info.status desc,a.type asc , a.lastUpdate asc";
        return super.findVarargs(hsql, domainId, EdocForm.C_iStatus_Deleted, Boolean.TRUE);
	}
	
	/**
	 * 查询数据库中是否存在同名的公文单
	 * @param id 		公文单ID
	 * @param domainId  单位ID
	 * @param formName  公文单名字
	 * @param type		公文单类型
	 * @return
	 */
	public int getEdocFormByName(String id,Long domainId,String formName,int type)
	{
		String hsql = " from EdocForm as a left join a.edocFormExtendInfo as info  where info.accountId = ? and info.status = ? and a.name =? and a.type=? ";
		if(Strings.isNotBlank(id)){
			hsql+=" and a.id <> ? ";
		}
		if(Strings.isNotBlank(id)){
			return super.getQueryCount(hsql, new Object[]{domainId, Constants.EDOC_USEED,formName,type,Long.valueOf(id)},
					new org.hibernate.type.Type[]{Hibernate.LONG,Hibernate.INTEGER,Hibernate.CHARACTER,Hibernate.INTEGER,Hibernate.LONG});
		}else{
			return super.getQueryCount(hsql, new Object[]{domainId, Constants.EDOC_USEED,formName,type},
					new org.hibernate.type.Type[]{Hibernate.LONG,Hibernate.INTEGER,Hibernate.CHARACTER,Hibernate.INTEGER});
	
		}
		
	}
	/**
	 * 查询授权给这些单位的公文单
	 * @param dimaind :  当前单位
	 * @param domainIds  ：被授权的单位列表
	 * @param isNeedCreateDomainName  ：是否需要查询公文单的制作单位.
	 * @return
	 */
	public List<EdocForm> getAllEdocFormsForWeb(Long domainId,String domainIds,String condition,String textfield)
	{
		
		//由于Hibernate解析左内连接的时候出错，所以专门拆出来一个方法查询公文单ID
		List<Long> formids  = getEdocFormIdsByAclOrCreate(domainId,domainIds,null,-1);
		if(formids == null || formids.isEmpty()) 
			return new ArrayList<EdocForm>();
		StringBuilder sb = new StringBuilder();
		sb.append(" select ef from EdocForm ef inner join ef.edocFormExtendInfo info " );
		sb.append(" where ef.id in (:ids)");	
		sb.append(" and  info.accountId = :accountId ");
		
		
		Map<String,Object> parameter = new HashMap<String,Object>();
		
		parameter.put("ids",formids);
		parameter.put("accountId",domainId); 
		
		if(Strings.isNotBlank(textfield)){
			if("name".equals(condition) ){
				sb.append(" and ef.name like :name ");
				parameter.put("name", "%"+SQLWildcardUtil.escape(textfield)+"%");
			}
			else if("sort".equals(condition) ){
				sb.append(" and ef.type = :type ");
				parameter.put("type", Integer.parseInt(textfield));
			}
			else if("status".equals(condition) ){
				sb.append(" and info.status = :status ");
				parameter.put("status", Integer.parseInt(textfield));
			}
		}else{
			//默认打开已启用的文单
			sb.append(" and info.status = 1 ");
		}
		
		sb.append(" order by info.status desc,ef.type asc , ef.lastUpdate desc");
		
		
		
//		List<EdocForm> list  =  super.find(sb.toString(),-1,-1,parameter);
		List<EdocForm> list  =  super.find(sb.toString(),parameter);
		return EdocUtil.convertExtendInfo2EdocForm(list,domainId); 
		
		
//		return getEdocForms(domainId,domainIds,null);
	}
	
	//根据文单授权查询文单列表
	//加了一个是否分页的参数
	private List<Long> getEdocFormIdsByAcl(Long domainId,String domainIds,Integer type, long subType,boolean isPage){
		//G6版本，并且开启了区分文单种类
		boolean hasSubType=false;
		if(EdocHelper.isG6Version()&&EdocHelper.hasEdocCategory()){
			hasSubType=true;
		}
		Map parameter = new HashMap();
		StringBuilder sb = new StringBuilder();
		sb.append(" select distinct a.id  ");
		sb.append(" from EdocForm a  left join a.edocFormAcls acl");
		/*
		 * 企业版没有文单授权，所以要验证当前版本  author：wangwei
		 */
		//sb.append(" where  (acl.domainId in (:domainId))"); //显示文单的时候只显示授权的文单，创建的没权限不包含
		List<Long> idList = null;
		if(Strings.isNotBlank(domainIds)){
			idList = new ArrayList<Long>();
			String[] tmps = domainIds.split(",");
			for(String id:tmps)
				idList.add(Long.valueOf(id));
		}
        
		if(domainId!=null){
			if(idList==null){
				idList = new ArrayList<Long>();
			}
		    idList.add(domainId);
		}
		boolean isGroupVer = (Boolean) (SysFlag.sys_isGroupVer.getFlag());// 判断是否为集团版
		if (isGroupVer) {
			sb.append(" where acl.domainId in (:domainId)");
			parameter.put("domainId",idList);
		}else{
			sb.append(" where (a.domainId = :accountId)");
			parameter.put("accountId",domainId);
		}
		//TODO changyi
//		if(SystemEnvironment.hasPlugin("edoc")==false){
//		    sb.append(" and a.type=2");
//		}else{
			if(type!= null){
				sb.append(" and a.type = :type");
				parameter.put("type", type);
				if(hasSubType&&subType != -1) {
					sb.append(" and a.subType = :subType");
					parameter.put("subType", Long.valueOf(subType));
				}
			}
//		}
		//parameter.put("domainId",idList);
		//parameter.put("accountId",domainId);
		//OA-33741 客户bug：建立公文模板时，部分公文单无法选择到
		List<Long> list = null;
		if(isPage){
		    list = (List<Long>)super.find(sb.toString(),parameter);
		}else{
		    list = (List<Long>)super.find(sb.toString(),-1,-1,parameter);
		}
		return list;
	}
	
	//根据文单授权或创建单位查询文单列表
	private List<Long> getEdocFormIdsByAclOrCreate(Long domainId,String domainIds,Integer type, long subType){
		Map parameter = new HashMap();
		StringBuilder sb = new StringBuilder();
		sb.append(" select distinct a.id  ");
		sb.append(" from EdocForm a  left join a.edocFormAcls acl");
		sb.append(" where  (acl.domainId in (:domainId)  or a.domainId = :accountId )"); //显示文单的时候包括授权的、创建的文单
		if(SystemEnvironment.hasPlugin("edoc")==false){
		    //TODO changyi  目前判断插件的还不太清楚，所以这里先注释掉
//			sb.append(" and a.type=2");
		}else{
			if(type!= null){
				sb.append(" and a.type = :type");
				parameter.put("type", type);
				if(subType != -1) {
					sb.append(" and a.subType = :subType");
					parameter.put("subType", Long.valueOf(subType));
				}
			}
		}
		List<Long> idList = null;
		if(domainIds != null){
			idList = new ArrayList<Long>();
			String[] tmps = domainIds.split(",");
			for(String id:tmps)
				idList.add(Long.valueOf(id));
		}
		parameter.put("domainId",idList);
		parameter.put("accountId",domainId);
		return (List<Long>)super.find(sb.toString(),-1,-1,parameter);
	}
	
	public List<EdocForm> getEdocForms(Long domainId,String domainIds,Integer type) {
		return getEdocForms(domainId, domainIds, type, -1); 
	}
	
	public List<EdocForm> getEdocForms(Long domainId,String domainIds,Integer type, long subType) {
		
		//由于Hibernate解析左内连接的时候出错，所以专门拆出来一个方法查询公文单ID
		List<Long> formids  = getEdocFormIdsByAcl(domainId,domainIds,type,subType,false);
		if(formids == null || formids.isEmpty()) 
			return new ArrayList<EdocForm>();
		StringBuilder sb = new StringBuilder();
		sb.append(" select ef from EdocForm ef inner join ef.edocFormExtendInfo info " );
		sb.append(" where ef.id in (:ids)");	
		sb.append(" and  info.accountId = :accountId ");
		sb.append(" order by info.status desc,ef.type asc , ef.lastUpdate desc");
		
		
		Map<String,Object> parameter = new HashMap<String,Object>();
		
		parameter.put("ids",formids);
		parameter.put("accountId",domainId); 
		
		List<EdocForm> list  =  super.find(sb.toString(),-1,-1,parameter);
//		List<EdocForm> list  =  super.find(sb.toString(),parameter);
		return EdocUtil.convertExtendInfo2EdocForm(list,domainId); 
	}
	
	public List<EdocForm> getEdocForms(Long domainId,String domainIds,Integer type, long subType,boolean isPage) {
        
        //由于Hibernate解析左内连接的时候出错，所以专门拆出来一个方法查询公文单ID
        List<Long> formids  = getEdocFormIdsByAcl(domainId,domainIds,type,subType,isPage);
        if(formids == null || formids.isEmpty()) 
            return new ArrayList<EdocForm>();
        StringBuilder sb = new StringBuilder();
        sb.append(" select ef from EdocForm ef inner join ef.edocFormExtendInfo info " );
        sb.append(" where ef.id in (:ids)");    
        sb.append(" and  info.accountId = :accountId ");
        sb.append(" order by info.status desc,ef.type asc , ef.lastUpdate desc");
        
        
        Map<String,Object> parameter = new HashMap<String,Object>();
        
        parameter.put("ids",formids);
        parameter.put("accountId",domainId); 
        
        List<EdocForm> list  =  super.find(sb.toString(),-1,-1,parameter);
//        List<EdocForm> list  =  super.find(sb.toString(),parameter);
        return EdocUtil.convertExtendInfo2EdocForm(list,domainId); 
    }
	
	
	public List<EdocForm> getAllEdocFormsByType(Long domainId,int type)
	{
		String hsql = "select a from EdocForm as a left join a.edocFormExtendInfo info where info.accountId = ? and info.status<>? and a.type=?";
		if(SystemEnvironment.hasPlugin("edoc")==false){
			hsql+=" and a.type=2";
		}
		hsql+=" order by a.lastUpdate asc";
		Object[] values = new Object[]{domainId, EdocForm.C_iStatus_Deleted, type};
		return super.findVarargs(hsql, values);		
	}
	
	public List<EdocForm> getAllEdocFormsByStatus(Long domainId,int status)
	{
		String hsql = "select a from EdocForm as a left join a.edocFormExtendInfo info where info.accountId = ? and info.status=? order by a.type asc , a.lastUpdate asc";
		return super.findVarargs(hsql, domainId,status);
	}
	
	public List<EdocForm> getAllEdocFormsByTypeAndStatus(Long domainId,int type, int status)
	{
		String hsql = "select a from EdocForm as a left join a.edocFormExtendInfo info where info.accountId = ? and info.status=? and a.type=? order by a.lastUpdate asc";
		Object[] values = new Object[]{domainId, status, type};
		return super.findVarargs(hsql, values);
	}
	
	
	public List<EdocForm> getEdocFormByAcl(String domainIds){
		StringBuilder sb = new StringBuilder();
		sb.append(" select ef ");
		sb.append(" from EdocForm ef inner join ef.edocFormAcls acl  ");
		sb.append(" where ");
		sb.append(" acl.domainId in (:domainIds)");
		
		Map parameter = new HashMap();
		List<Long> idList = null;
		if(domainIds != null){
			idList = new ArrayList<Long>();
			String[] tmps = domainIds.split(",");
			for(String id:tmps)
				idList.add(Long.valueOf(id));
		}
		parameter.put("domainIds",idList);
		return  super.find(sb.toString(),-1,-1,parameter);
	}
	

	
	public List<EdocForm> getEdocForms(String formIds)
	{		
		Map<String,Object> namedParameter = new HashMap<String,Object>();
		List<Long> ids = new ArrayList<Long>();
		String[] tmp = formIds.split(",");
		for(String id:tmp)
			ids.add(Long.valueOf(id));
		namedParameter.put("ids", ids);
		String hsql = "from EdocForm as a where a.id in (:ids)";		
		return super.find(hsql,namedParameter);
	}
	
	public EdocForm findDefaultFormByDomainIdAndType(Long domainId,int type){
		String hsql="from EdocForm as edocForm where edocForm.domainId = ? and edocForm.type = ? and edocForm.isDefault = ? order by edocForm.lastUpdate asc";		
		Object [] values={domainId,type,true};
		List<EdocForm> list = super.findVarargs(hsql, values);
		if(null!=list && list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}
	
	public void updateDefaultEdocForm(Long domainId,int type){
		String hsql="update EdocForm as edocForm set edocForm.isDefault = ? where edocForm.domainId = ? and edocForm.type = ? and edocForm.isDefault = ? ";
		super.bulkUpdate(hsql,null,false,domainId,type,true);
	}

	public boolean isReferenced(Long formId){
		String hql = "from EdocSummary as es where es.formId = ?";
		Long[] values = {formId};
		Type[] types = {Hibernate.LONG};
		int count = super.getQueryCount(hql, values, types);
		
		if(count>0){
			return true;
		}	
		return false;
	}
	public boolean isExsit(Long formId){
		String hql = "from EdocForm as ef where ef.id = ?";
		Long[] values = {formId};
		Type[] types = {Hibernate.LONG};
		int count = super.getQueryCount(hql, values, types);
		
		if(count>0){
			return true;
		}	
		return false;
	}
	/**
	  * 方法描述：判断本单位是否存在该文单
	  *
	 */
	public boolean isExsitInUnit(String formId,Long domainId){
		String hsql = " from EdocForm as a left join a.edocFormExtendInfo as info  where info.accountId = ? and a.id= ?";
		if(Strings.isNotBlank(formId)){
			return super.getQueryCount(hsql, new Object[]{domainId,Long.valueOf(formId)},
					new org.hibernate.type.Type[]{Hibernate.LONG,Hibernate.LONG})>0;
		}
		return false;
	}
	/**
	 * 获得本单位创建的文单
	 * @param domainId
	 * @param type  
	 * @return
	 */
	public List<EdocForm> getMyAccountEdocForms(Long domainId,int type){
		String hsql = "select a from EdocForm a where a.domainId = ? and a.type = ? ";
		return super.findVarargs(hsql, domainId, type);
	}
	
	/**
	 *验证当前文单是否属于本单位
	 * @param domainId
	 * @param type  
	 * @return
	 */
	public List<EdocForm> getFormAccountEdoc(Long domainId,long edocFormId){
		String hsql = "select a from EdocForm a where a.domainId = ? and a.id = ? ";
		return super.findVarargs(hsql, domainId, edocFormId);
	}
	
	/**
	 * 查询 公文文单
	 * @Author      : xuqw
	 * @Date        : 2015年8月31日下午10:30:27
	 * @param conditions
	 * @param flipInfo
	 * @return
	 */
	public List<EdocForm> findForms(Map<String, Object> conditions, FlipInfo flipInfo){
	    
	    StringBuilder hql = new StringBuilder("from ");
        hql.append(EdocForm.class.getName());
        hql.append("  where 1=1 ");
        if(conditions.get("domainId") != null){
            hql.append(" and domainId=:domainId ");
        }
        if(conditions.get("type") != null){
            hql.append(" and type=:type ");
        }
        if(conditions.get("isSystem") != null){
            hql.append(" and isSystem=:isSystem");
        }
        
        if(flipInfo != null){
            return DBAgent.find(hql.toString(), conditions, flipInfo);
        }else {
            return DBAgent.find(hql.toString(), conditions);
        }
	}
}