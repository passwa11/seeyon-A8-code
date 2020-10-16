package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;

/**
 * Data access object (DAO) for domain model class EdocMarkAcl.
 * @see .EdocMarkAcl
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkAclDAO extends BaseHibernateDao<EdocMarkAcl>{

    private static final Log log = LogFactory.getLog(EdocMarkAclDAO.class);

	//property constants
	public static final String DEPT_ID = "deptId";

    /**
     * 方法描述：保存公文文号使用授权
     */
    public void saveEdocMarkAcl(EdocMarkAcl edocMarkAcl) {
        log.debug("saving V3xEdocMarkAcl instance");
        try {
            super.save(edocMarkAcl);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }
    
    /**
     * 方法描述：根据属性查询公文文号使用授权
     */
    @SuppressWarnings("unchecked")
	public List<EdocMarkAcl> findEdocMarkAclByProperty(String propertyName, Object value) {
        log.debug("finding EdocMarkAcl instance with property: " + propertyName
              + ", value: " + value);
        try {
        	Map<String, Object> paramMap = new HashMap<String, Object>();
        	paramMap.put("propertyName", value);
        	String queryString = "from EdocMarkAcl as model where model." 
           						+ propertyName + "= :propertyName order by model.deptId desc";
  		   	return super.find(queryString, paramMap);
        } catch (RuntimeException re) {
           log.error("find by property name failed", re);	
           throw re;
        }
  	}

    public void deleteEdocMarkAclByDefinitionId(Long definitionId){
    	try{
    		String hsql="delete from EdocMarkAcl as acl where acl.markDefId = ?";    		
    		super.bulkUpdate(hsql,null,definitionId);
    	}catch(RuntimeException re){
    		throw re;
    	}
    }
    
    public List<EdocMarkAcl> findMarkAclByMarkAndEdocId(String edocMark, Long edocId){
    	StringBuilder hql = new StringBuilder("select a from EdocMarkHistory h,EdocMarkAcl a");
    	hql.append(" where h.markDefId.id=a.markDefId");
    	Map parameterMap = new HashMap();
    	hql.append(" and h.docMark=:docMark");
    	parameterMap.put("docMark", edocMark);
    	if(edocId != null) {
    		hql.append(" and h.edocId<>:edocId");
    		parameterMap.put("edocId", edocId);
    	}
    	return find(hql.toString(), -1, -1, parameterMap);
    }
    
    public List<EdocMarkAcl> findMarkAclByMarkAndEdocId(String edocMark, Long markDefId, Long edocId){
    	StringBuilder hql = new StringBuilder("select a from EdocMarkHistory h,EdocMarkAcl a");
    	hql.append(" where h.markDefId=a.markDefId and h.markDefId=:markDefId");
    	Map parameterMap = new HashMap();
    	parameterMap.put("markDefId", markDefId);
    	hql.append(" and h.docMark=:docMark");
    	parameterMap.put("docMark", edocMark);
    	if(edocId != null) {
    		hql.append(" and h.edocId<>:edocId");
    		parameterMap.put("edocId", edocId);
    	}
    	return find(hql.toString(), -1, -1, parameterMap);
    }
    
    public List<EdocMarkAcl> findMarkAclByMarkAndEdocId(String edocMark, Integer docMarkNo, Long edocId) {
    	StringBuilder hql = new StringBuilder("select a from EdocMarkHistory h,EdocMarkAcl a");
    	hql.append(" where h.markDefId=a.markDefId");
    	Map parameterMap = new HashMap();
    	hql.append(" and (h.docMark=:docMark or h.docMarkNo=:docMarkNo)");
    	parameterMap.put("docMark", edocMark);
    	parameterMap.put("docMarkNo", docMarkNo);
    	if(edocId != null) {
    		hql.append(" and h.edocId<>:edocId");
    		parameterMap.put("edocId", edocId);
    	}
    	return find(hql.toString(), -1, -1, parameterMap);
    }
    
    
}