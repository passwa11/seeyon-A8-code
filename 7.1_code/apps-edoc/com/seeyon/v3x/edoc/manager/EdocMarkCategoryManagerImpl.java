/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.util.DBAgent;
import com.seeyon.v3x.edoc.dao.EdocMarkCategoryDAO;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.util.Constants;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocMarkCategoryManagerImpl implements EdocMarkCategoryManager {
	
	private EdocMarkCategoryDAO edocMarkCategoryDAO;
	
	public void setEdocMarkCategoryDAO(EdocMarkCategoryDAO edocMarkCategoryDAO) {
		this.edocMarkCategoryDAO = edocMarkCategoryDAO;
	}
	
	public EdocMarkCategoryDAO getEdocMarkCategoryDAO() {
		return edocMarkCategoryDAO;
	}
	
	/**
     * 方法描述：修改公文文号类别
     */
    public void updateCategory(EdocMarkCategory edocMarkCategory){
    	this.edocMarkCategoryDAO.updateEdocMarkCategory(edocMarkCategory);
    }
    
    public void saveCategory(EdocMarkCategory edocMarkCategory){
    	this.edocMarkCategoryDAO.saveEdocMarkCategory(edocMarkCategory);
    }
    
    public void increaseCurrentNo(Long catId,int currentNo){
    	EdocMarkCategory cat = this.findById(catId);
    	if(cat != null && cat.getCurrentNo()==currentNo){
    		cat.setCurrentNo(cat.getCurrentNo()+1);
    		this.updateCategory(cat);
    	}	
    }
    
    /**
     * 修改文号序号为所传参数
     * @param categoryId
     * @param currentNo
     */
    @Override
    public void updateCategoryCurrentNo(Long categoryId, int currentNo) {
    	if(categoryId == null) {
    		return;
    	}
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("currentNo", currentNo);
    	paramMap.put("categoryId", categoryId);
    	DBAgent.bulkUpdate("update EdocMarkCategory set currentNo=:currentNo where id=:categoryId", paramMap);
    }
    
    public EdocMarkCategory findByCategoryName(String categoryName){
    	List<EdocMarkCategory> list = edocMarkCategoryDAO.findBy("categoryName", categoryName);
    	return null!=list && list.size()>0 ? list.get(0) : null;
    }
    
    public EdocMarkCategory findById(Long id){
    	return edocMarkCategoryDAO.get(id);
    }
    public Map<Long,Integer> findByIds(List<Long> ids){
    	return edocMarkCategoryDAO.findEdocMarkCategoryByIds(ids);
    }
    public List<EdocMarkCategory> findByTypeAndDomainId(Short type,Long domainId){
   
    	return edocMarkCategoryDAO.findEdocMarkCategoryByTypeAndDomainId(type,domainId);
    }
    
    public List<EdocMarkCategory> findByPage(Short type,Long domainId) {
    	return edocMarkCategoryDAO.findByPage(type,domainId);
    }
    
    public void deleteCategory(long categoryId){
    	edocMarkCategoryDAO.delete(categoryId);
    }
    public List<EdocMarkCategory> findAll(){
    	return edocMarkCategoryDAO.getAll();
    }
    public void turnoverCurrentNoAnnual(){
    	edocMarkCategoryDAO.setCurrentNoMinBatch();
    }
    public List<EdocMarkCategory> getEdocMarkCategories(Long domainId) {
    	return this.findByTypeAndDomainId(Constants.EDOC_MARK_CATEGORY_BIGSTREAM, domainId);    	
    }
    
    public Boolean containEdocMarkCategory(String name, long domainId) {
    	return containEdocMarkCategory(0, name, domainId);
    }
    
    public Boolean containEdocMarkCategory(long categoryId, String name, long domainId) {
    	if (categoryId == 0) {
    		return edocMarkCategoryDAO.containEdocMarkCategory(name, domainId);
    	}
    	else {
    		return edocMarkCategoryDAO.containEdocMarkCategory(categoryId, name, domainId);
    	}
    }
    
}
