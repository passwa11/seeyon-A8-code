/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.v3x.edoc.dao.EdocMarkCategoryDAO;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public interface EdocMarkCategoryManager {	
	
	public EdocMarkCategoryDAO getEdocMarkCategoryDAO();
	
	/**
     * 方法描述：修改公文文号类别
     */
    public void updateCategory(EdocMarkCategory edocMarkCategory);
    
    /**
     * 方法描述：保存公文文号类别
     * @param edocMarkCategory
     */
    public void saveCategory(EdocMarkCategory edocMarkCategory);
    
    /**
     * 方法描述：修改公文文号类别的当前编号
     */
    public void increaseCurrentNo(Long catId,int currentNo);
    
    /**
     * 修改文号序号为所传参数
     * @param catId
     * @param currentNo
     */
    public void updateCategoryCurrentNo(Long categoryId, int currentNo);
    
    /**
     * 方法描述：通过id取得公文类别对象
     * @param id
     * @return
     */
    public EdocMarkCategory findById(Long id);
    /**
     * 方法描述：通过id列表取得公文类别对象
     * @param id
     * @return
     */
    public Map<Long,Integer> findByIds(List<Long> id);
    
    /**
     * 方法描述：根据类别(大流水,小流水)查找类别
     * 
     * @param type
     * @param domainId 单位id
     * @return
     */
    public List<EdocMarkCategory> findByTypeAndDomainId(Short type,Long domainId);
    
    /**
     * 根据类别(大流水,小流水)查找类别（分页显示）
     * @param type
     * @param domainId 单位id
     * @return List
     */
    public List<EdocMarkCategory> findByPage(Short type,Long domainId);
    
    public EdocMarkCategory findByCategoryName(String categoryName);
    
    public void deleteCategory(long categoryId);
    
    public List<EdocMarkCategory> findAll();
    public void turnoverCurrentNoAnnual();
    
    /**
     * 返回指定单位的所有大流水定义。
     * @param domainId 单位id
     * @return List
     */
    public List<EdocMarkCategory> getEdocMarkCategories(Long domainId);
    
    public Boolean containEdocMarkCategory(String name, long domainId);
    
    public Boolean containEdocMarkCategory(long categoryId, String name, long domainId);
    
}
