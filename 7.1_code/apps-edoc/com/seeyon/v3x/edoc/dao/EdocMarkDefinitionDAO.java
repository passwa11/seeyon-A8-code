package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.apps.govdoc.po.GovdocMarkRecord;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.util.Constants;

/**
 * Data access object (DAO) for domain model class EdocMarkDefinition.
 * @see .EdocMarkDefinition
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkDefinitionDAO extends BaseHibernateDao<EdocMarkDefinition> {

    private static final Log log = LogFactory.getLog(EdocMarkDefinitionDAO.class);

	/**
     * 方法描述：保存公文文号定义
     */
    public void saveEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        log.debug("saving EdocMarkDefinition instance");
        try {
            super.save(edocMarkDefinition);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }
    
    /**
     * 方法描述：修改公文文号定义
     */
    public void updateEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        log.debug("updating EdocMarkDefinition instance");
        try {
            super.update(edocMarkDefinition);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        }
    }

	/**
	 * 
	 * @param definitionId
	 * @param status
	 */
	public void updateMarkDefinitionStatus(long definitionId, short status){
        log.debug("logical deleting EdocMarkDefinitions instance");
		try{
		String hsql = "update EdocMarkDefinition as markDef set markDef.status = ? where markDef.id = ?";
		super.bulkUpdate(hsql,null,status,definitionId);
			log.debug("logical delete successful");
		}catch(RuntimeException re){
			log.debug("logical delete failed", re);
		}
	}
	
    /**
     * 方法描述：删除公文文号定义
     */
	public void deleteEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        log.debug("deleting EdocMarkDefinitions instance");
        try {
            super.delete(edocMarkDefinition);
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
	
	/**
     * 方法描述：根据公文文号定义ID查询公文文号定义
     */
    @SuppressWarnings("unchecked")
	public EdocMarkDefinition findEdocMarkDefAndCategoryById(Long id) {
    	EdocMarkDefinition markDef = null;
        log.debug("getting EdocMarkDefinitions instance with id: " + id);
        try {
        	Map<String, Object> paramMap = new HashMap<String, Object>();
        	paramMap.put("markDefId", id);
        	List<Object[]> list = super.find("select markDef, category from EdocMarkDefinition markDef, EdocMarkCategory category where markDef.categoryId=category.id and markDef.id=:markDefId", paramMap);
        	if(Strings.isNotEmpty(list)) {
        		markDef = (EdocMarkDefinition)list.get(0)[0];
        		markDef.setEdocMarkCategory((EdocMarkCategory)list.get(0)[1]);
        	}
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
        return markDef;
    }
    
	 /**
     * 方法描述：根据公文文号定义ID查询公文文号定义
     */
    public EdocMarkDefinition findEdocMarkDefinitionById(Long id) {
        log.debug("getting EdocMarkDefinitions instance with id: " + id);
        try {
            EdocMarkDefinition instance = (EdocMarkDefinition) super.get(id);                    
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
    
    /**
     * 
     * @param categoryId
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<EdocMarkDefinition> getEdocMarkDefsByCategoryId(Long categoryId) {
    	log.debug("getEdocMarkDefsByCategoryId()");
    	String hsql = "select distinct markDef,edocMarkCategory from EdocMarkDefinition as markDef, EdocMarkCategory edocMarkCategory where markDef.categoryId=edocMarkCategory.id and markDef.categoryId = :categoryId";
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("categoryId", categoryId);
    	List<Object[]> result = super.find(hsql, -1, -1, paramMap);
    	if(Strings.isNotEmpty(result)) {
    		List<EdocMarkDefinition> markDefList = new ArrayList<EdocMarkDefinition>();
    		for(Object[] objects : result) {
    			EdocMarkDefinition markDef = (EdocMarkDefinition)objects[0];
    			EdocMarkCategory markCategory = (EdocMarkCategory)objects[1];
    			markDef.setEdocMarkCategory(markCategory);
    			markDefList.add(markDef);
    		}
    		return markDefList;
    	}
    	return null;
    }
    
    /**
     * 
     * @param ids
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<EdocMarkDefinition> findEdocMarkDefinitionListById(List<Long> ids) {
    	if(Strings.isEmpty(ids)) {
    		return null;
    	}
    	String hsql = " select markDef,c from EdocMarkDefinition markDef, EdocMarkCategory c where markDef.categoryId=c.id and markDef.id in (:ids)";
    	Map map = new HashMap();
    	map.put("ids", ids);
    	
    	List<Object[]> result = super.find(hsql,-1,-1, map);
    	if(Strings.isNotEmpty(result)) {
    		List<EdocMarkDefinition> markDefList = new ArrayList<EdocMarkDefinition>();
    		for(Object[] objects : result) {
    			EdocMarkDefinition markDef = (EdocMarkDefinition)objects[0];
    			EdocMarkCategory markCategory = (EdocMarkCategory)objects[1];
    			markDef.setEdocMarkCategory(markCategory);
    			markDefList.add(markDef);
    		}
    		return markDefList;
    	}
    	
    	return null;
    }
   
    /**
     * 方法描述：根据属性查询公文文号定义
     * @param propertyName
     * @param value
     * @param loadCategory
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<EdocMarkDefinition> findEdocMarkDefinitionsByProperty(String propertyName, Object value) {
    	log.debug("finding EdocMarkDefinition instances with property: " + propertyName
    			+ ", value: " + value);
    	try {
    		Map<String, Object> paramMap = new HashMap<String, Object>();
        	paramMap.put("propertyName", propertyName);
    		String queryString = "from EdocMarkDefinition as model where model." + propertyName + "= :propertyName";
    		return super.find(queryString, paramMap); 
    	} catch (RuntimeException re) {
    		log.error("find by property name failed", re);
    		throw re;
    	}
    }
    
    /**
     * @param loadCategory  是否加载edocMarkCategory
     */
    @SuppressWarnings("unchecked")
	public List<EdocMarkDefinition> getMyEdocMarkDefs(String deptIds,boolean loadCategory,int markType,Long markDefId) {
    	Map<String,Object> namedParameter = new HashMap<String,Object>();
    	
    	String hsql = "select distinct markDef,edocMarkCategory";
    	hsql += " from EdocMarkDefinition as markDef, EdocMarkCategory edocMarkCategory, EdocMarkAcl markAcl";
    	hsql += " where markDef.categoryId=edocMarkCategory.id and markDef.id=markAcl.markDefId";
    	hsql += " and markDef.status != " + Constants.EDOC_MARK_DEFINITION_DELETED;
    	hsql += " and markDef.markType = :markType";
    	hsql += " and (";
    	hsql += " 	markAcl.deptId in (:deptId) ";
    	if(markDefId != null && markDefId.longValue()!=-1 && markDefId.longValue()!=0) {
    		hsql += " or markDef.id = :markDefId";
    		namedParameter.put("markDefId", markDefId);
		}
    	hsql += " )";
    	hsql += " order by markDef.markType, markDef.sortNo, markDef.wordNo";
    	
    	List<Long> depIds = new ArrayList<Long>();
    	String[] tmp = deptIds.split(",");
    	for(String depId:tmp){
    		if(Strings.isNotBlank(depId)){
    			depIds.add(Long.valueOf(depId));
    		}
    	}
		namedParameter.put("deptId", depIds);
    	namedParameter.put("markType", markType);

    	//注释上面两行代码的原因：上面查询的时候进行了分页查询，而此处不需要分页。
    	List<Object[]> result = super.find(hsql,-1,-1,namedParameter);
    	if(Strings.isNotEmpty(result)) {
    		List<EdocMarkDefinition> markDefList = new ArrayList<EdocMarkDefinition>();
    		for(Object[] objects : result) {
    			EdocMarkDefinition markDef = (EdocMarkDefinition)objects[0];
    			EdocMarkCategory markCategory = (EdocMarkCategory)objects[1];
    			markDef.setEdocMarkCategory(markCategory);
    			markDefList.add(markDef);
    		}
    		return markDefList;
    	}
    	return null;
    }

    /**
     * 
     * @param domainId
     * @param loadCategory  是否加载edocMarkCategory
     * @return
     */
	@SuppressWarnings("unchecked")
	public List<EdocMarkDefinition> findEdocMarkAndSerinalDefList(Long domainId, List<Long> deptIdList) {
		String hsql = "select distinct markDef,edocMarkCategory from"
				+ " EdocMarkDefinition markDef, EdocMarkCategory edocMarkCategory, EdocMarkAcl acl"
				+ " where markDef.categoryId=edocMarkCategory.id and markDef.id=acl.markDefId"
				+ " and markDef.markType in (0,1)"
				+ " and (markDef.domainId=:domainId or acl.deptId in (:deptId))"
				+ " and markDef.status in (:status) order by markDef.sortNo, markDef.wordNo";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<Short> statusList = new ArrayList<Short>();
		statusList.add(Constants.EDOC_MARK_DEFINITION_PUBLISHED);
		statusList.add(Constants.EDOC_MARK_DEFINITION_DRAFT);
		paramMap.put("status", statusList);
		paramMap.put("domainId", domainId);
		paramMap.put("deptId", deptIdList);
		
		List<Object[]> result = super.find(hsql, paramMap);		
		if(Strings.isNotEmpty(result)) {
			List<EdocMarkDefinition> markDefList = new ArrayList<EdocMarkDefinition>();
			for(Object[] objects : result) {
				EdocMarkDefinition markDef = (EdocMarkDefinition)objects[0];
				EdocMarkCategory markCategory = (EdocMarkCategory)objects[1];
				markDef.setEdocMarkCategory(markCategory);
				markDefList.add(markDef);
			}
			return markDefList;
		}		
		return null;		
	}
	
	/**
	 * 公文文号管理列表
	 * @param domainId
	 * @param orgIds
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getEdocMarkDefsIncludeAccountAndAcl(Long domainId,List<Long> orgIds) {
		orgIds.add(domainId);
		
		//BUG_普通_V5_V5.6SP1_武汉烽火通信科技股份有限公司_点击公文文号管理显示：出现异常，_20160406018799_2016-04-06
        //对in里面的数据进行拆分，以300个为单位生成一个list循环传递参数
		String deptCondition = "";
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		List<List<Long>> result = createList(orgIds, 300); 
		for(int i=0; i<result.size(); i++) {
			deptCondition += " or markDef.id in (";
			deptCondition += "    select acl.markDefId from EdocMarkAcl acl where ";
			deptCondition += " 		acl.markDefId = markDef.id";
			deptCondition += " 		and acl.deptId IN (:deptids" + i + ") ";
			deptCondition += ")";
			
			parameterMap.put("deptids"+i, result.get(i));
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("select distinct markDef.id,markDef.wordNo,markDef.markType,markDef.expression,markDef.length,markDef.sortNo");
		buffer.append(",edocMarkCategory.id,edocMarkCategory.categoryName,edocMarkCategory.codeMode,edocMarkCategory.currentNo,edocMarkCategory.minNo,edocMarkCategory.maxNo,edocMarkCategory.readonly,edocMarkCategory.domainId,edocMarkCategory.yearEnabled");
		buffer.append(" from EdocMarkDefinition markDef, EdocMarkCategory edocMarkCategory");
		buffer.append(" where markDef.categoryId=edocMarkCategory.id"); 
		buffer.append(" and markDef.status != :stat");
		buffer.append(" and (");
		buffer.append("	  markDef.domainId=:domainId");
		if(Strings.isNotEmpty(orgIds)) {
			buffer.append(deptCondition);
		}
		buffer.append(" )");
		buffer.append(" order by markDef.markType, markDef.sortNo, markDef.wordNo");
		
	    parameterMap.put("domainId", domainId);
	    parameterMap.put("stat", Constants.EDOC_MARK_DEFINITION_DELETED);

	    return super.find(buffer.toString(), parameterMap);
	}
	
	/*客开  作者:mtech 项目名称:贵州省政府  时间：2015-09-13 修改功能:公文统计授权start*/ 
	public List<EdocMarkDefinition> getEdocDocMarkDefinitions(Integer markType) {
		String hsql = (new StringBuilder(  
				"select distinct markDef from EdocMarkDefinition markDef"))
				.append(" where markDef.status != ") 
				.append(com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_DEFINITION_DELETED)
				.append(" and markDef.domainId = :domainId")//本单位的
				.append(" and markDef.markType = :markType order by markDef.sortNo,markDef.wordNo").toString();
		Map<String,Object> namedParameter = new HashMap<String,Object>();
		namedParameter.put("markType", Integer.valueOf(markType));
		namedParameter.put("domainId", AppContext.currentAccountId()); 
		return super.find(hsql, -1, -1, namedParameter, new Object[0]);
	} 
	/*客开  作者:mtech 项目名称:贵州省政府  时间：2015-09-13 修改功能:公文统计授权end*/ 

	/**
	 * 解除EdocMarkDefinition映射后，手动取数据
	 * @param markDefIdList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<EdocMarkAcl> findEdocMarkAcl(List<Long> markDefIdList) {
		Map<String, Object> markDefIdMap = new HashMap<String, Object>();
		markDefIdMap.put("markDefIdList", markDefIdList);
		return (List<EdocMarkAcl>)super.find("from EdocMarkAcl where markDefId in (:markDefIdList)" , -1, -1, markDefIdMap);
	}
	
	
	/**
	 * 验证文号机构代字是否重复
	 * @param wordNo
	 * @param domainId
	 * @param markType
	 * @return
	 */
	public Boolean containEdocMarkDef(String wordNo, long domainId,int markType) {
		String hsql = "from EdocMarkDefinition as markDef where markDef.domainId=? and markDef.status!=? and markDef.markType=? ";
		if (Strings.isNotBlank(wordNo)) {
			hsql = hsql+" and markDef.wordNo=? ";
			Object[] values = {domainId, Constants.EDOC_MARK_DEFINITION_DELETED,markType,wordNo};
			Type[] types = {Hibernate.LONG,Hibernate.SHORT,Hibernate.INTEGER,Hibernate.STRING};
			//直接按count查询
			return super.getQueryCount(hsql, values, types)>0;
		}else {
			hsql = hsql+" and (markDef.wordNo IS NULL or  markDef.wordNo = '') ";
			Object[] values = {domainId, Constants.EDOC_MARK_DEFINITION_DELETED,markType};
			Type[] types = {Hibernate.LONG,Hibernate.SHORT,Hibernate.INTEGER};
			//直接按count查询
			return super.getQueryCount(hsql, values, types)>0;
		}
	}
	
	/**
	 * 
	 * @param markDefId
	 * @param wordNo
	 * @param domainId
	 * @param markType
	 * @return
	 */
	public Boolean containEdocMarkDef(long markDefId, String wordNo, long domainId,int markType) {
		String hsql = "from EdocMarkDefinition as markDef where markDef.domainId=? and markDef.status!=? and markDef.id!=? and markDef.markType=? ";
		if (wordNo != null) {
			hsql = hsql+" and markDef.wordNo=? ";
			Object[] values = {domainId, Constants.EDOC_MARK_DEFINITION_DELETED,markDefId,markType, wordNo};
			
			//直接按count查询
			Type[] types = {Hibernate.LONG,Hibernate.SHORT,Hibernate.LONG,Hibernate.INTEGER,Hibernate.STRING};
			return super.getQueryCount(hsql, values, types)>0;
		}else {
			hsql = hsql+" and markDef.wordNo IS NULL ";
			Object[] values = {domainId, Constants.EDOC_MARK_DEFINITION_DELETED,markDefId,markType};
			//直接按count查询
			Type[] types = {Hibernate.LONG,Hibernate.SHORT,Hibernate.LONG,Hibernate.INTEGER};
			return super.getQueryCount(hsql, values, types)>0;
		}
		
	}
	
	
	/**
     * 判断公文文号定义是否已经被删除
     * @param	文号定义表ID
     * @return 0:已经删除  1：存在
     */
	public int judgeEdocDefinitionExsit(Long definitionId){
		String hql="from EdocMarkDefinition as markDef where markDef.status!=2 and id=?";
		int count=super.getQueryCount(hql, new Object[]{definitionId}, new Type[]{Hibernate.LONG});
		if(count>0){
			return 1;
		}else {
			return 0;
		}
	}
	
	/**
	 * 
	 * @param targe
	 * @param size
	 * @return
	 */
	private List<List<Long>>  createList(List<Long> targe,int size) {  
		List<List<Long>> listArr = new ArrayList<List<Long>>();  
		//获取被拆分的数组个数  
		int arrSize = targe.size()%size==0?targe.size()/size:targe.size()/size+1;  
		for(int i=0;i<arrSize;i++) {  
			List<Long>  sub = new ArrayList<Long>();  
			//把指定索引数据放入到list中  
			for(int j=i*size;j<=size*(i+1)-1;j++) {  
				if(j<=targe.size()-1) {  
					sub.add(targe.get(j));  
				}  
			}  
			listArr.add(sub);  
		}  
		return listArr;  
	}
	
	public Integer getAccountMarkCount(Long accountId){
		int count = 0;
		if(accountId != null){
			String hql = "from EdocMarkDefinition as m where m.status in (0,1) and m.domainId = :domainId";
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("domainId", accountId);
			count = DBAgent.count(hql,param);
		}
		
		return count;
	}
	
}