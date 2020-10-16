package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.util.Constants;

/**
 * Data access object (DAO) for domain model class EdocMarkCategory.
 * @see .EdocMarkCategory
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkCategoryDAO extends BaseHibernateDao<EdocMarkCategory> {

    private static final Log log = LogFactory.getLog(EdocMarkCategoryDAO.class);

	/**
     * 方法描述：修改公文文号类别的当前编号
     */
    public void updateEdocMarkCategory(EdocMarkCategory edocMarkCategory) {
        log.debug("updating EdocMarkCategory instance");
        try {
            update(edocMarkCategory);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        }
    }
	/**
	 * 设置当前值为最小值
	 *
	 */
	public void setCurrentNoMinBatch(){
		log.debug("set edoc_mark_category current no min!");
		try{
			String hsql = "update EdocMarkCategory as category set category.currentNo = category.minNo where yearEnabled=true ";
			super.bulkUpdate(hsql,null);
			log.debug("set edoc_mark_category current no min successful");
		}catch(RuntimeException re){
			log.debug("set edoc_mark_category current no min failed", re);
			throw re;
		}
	}
	
    public void saveEdocMarkCategory(EdocMarkCategory edocMarkCategory){
    	log.debug("saving EdocMarkCategory instance");
    	save(edocMarkCategory);
    	log.debug("save EdocMarkCategory sucessful");
    }
    
    public List<EdocMarkCategory> findEdocMarkCategoryByTypeAndDomainId(Short type,Long domainId){
    	Object [] values={Short.valueOf(type),Long.valueOf(domainId)};
    	String hsql = "from EdocMarkCategory as category where category.codeMode = ? and category.domainId = ? ";    	
    	List<EdocMarkCategory> list = super.findVarargs(hsql,values);
    	return list;
    }
    public Map<Long,Integer> findEdocMarkCategoryByIds(List<Long> ids){
    	Map<Long,Integer> map=new HashMap<Long,Integer>();
    	String hsql = "from EdocMarkCategory as category where id in(:ids)"; 
    	Map<String, Object> parameterMap = new HashMap<String, Object>();
    	parameterMap.put("ids", ids);
    	List<EdocMarkCategory> list = super.find(hsql,parameterMap);
    	if(list!=null&&list.size()!=0){
    		for(EdocMarkCategory category:list){
    			if(category.getCodeMode()!=null){
    				map.put(category.getId(),category.getCodeMode().intValue());
    			}else{
    				map.put(category.getId(),1);
    			}
    		}
    	}
    	return map;
    }
        
    public List<EdocMarkCategory> findByPage(Short type,Long domainId) {
    	DetachedCriteria criteria = DetachedCriteria.forClass(EdocMarkCategory.class);
    	criteria.add(Restrictions.eq("codeMode", type));
		criteria.add(Restrictions.eq("domainId", domainId));
		return super.executeCriteria(criteria);
    }
    
    public Boolean containEdocMarkCategory(String name, long domainId) {
    	String hsql = "from EdocMarkCategory as category where category.codeMode=? and category.categoryName=? and category.domainId=?";
    	Object[] values = {Constants.EDOC_MARK_CATEGORY_BIGSTREAM, name, domainId};
    	List<EdocMarkCategory> categories = super.findVarargs(hsql, values);
    	if (categories != null && categories.size() > 0) {
    		return true;
    	}
    	return false;
    }
    
    public Boolean containEdocMarkCategory(long categoryId, String name, long domainId) {
    	String hsql = "from EdocMarkCategory as category where category.codeMode=? and category.categoryName=? and category.domainId=? and category.id!=?";
    	Object[] values = {Constants.EDOC_MARK_CATEGORY_BIGSTREAM, name, domainId, categoryId};
    	List<EdocMarkCategory> categories = super.findVarargs(hsql, values);
    	if (categories != null && categories.size() > 0) {
    		return true;
    	}
    	return false;
    }
    
}