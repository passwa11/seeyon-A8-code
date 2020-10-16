package com.seeyon.v3x.edoc.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocForm;
public interface EdocCategoryManager {

	/**
	 * 查找指定单位下公文子分类
	 * @param rootId      大公文分类
	 * @param accountId   单位id
	 * @return            分类列表
	 */
	public List<EdocCategory> getCategoryByRoot(Long rootId, Long accountId,boolean isPagination);
	
	public List<EdocCategory> getCategoryByRoot(Long rootId,Long accountId,String condition,String textfield,boolean isPagination);
	
	/**
	 * 通过单位id查找单位下所有公文分类
	 * @param accountId    单位id
	 * @return             单位下公文分类列表
	 */
	public List<EdocCategory> getCategoryByAccount(Long accountId);
	
	/**
	 * 通过单位id查找单位下第一个公文分类
	 * @param accountId    单位id
	 * @return             单位下公文分类列表
	 */
	public EdocCategory getFirstCategoryByAccount(Long accountId);
	
	/**
	 * 通过公文种类id，得到公文种类对象
	 * @param categoryId
	 * @return
	 */
	public EdocCategory getCategoryById(Long categoryId);
	
	/**
	 * 根据id删除公文分类
	 * @param categoryId
	 */
	public void deleteCategoryById(Long categoryId);
	
	/**
	 * 修改公文分类
	 * @param categoryId
	 */
	public void updateCategory(EdocCategory category);
	
	
	/**
	 * 保存发文种类
	 * @param name 名称
	 * @param rootCategory
	 * @param accountId 单位id
	 * @param state 删除状态
	 * @param storeType 存储状态
	 */
	public void saveCategory(String name,Long rootCategory,Long accountId,Integer state,Integer storeType,Long modifyUserId,Date modifyTime);
	
	public void saveCategory(List<EdocCategory> newCategory,List<Long> modifyList,List<String> modifyNameList,List<Long> reomveList);
	
	public void handleCategory(String[] newCategory,List<Long> modifyList,List<String> modifyNameList,
			List<Long> removeList,Long accountId,Long rootCategory);
	/**
	 * 填充edocform中的分类名称
	 */
	public void fillFormCategoryName(List<EdocForm> forms);
	
	/**
	 * 产生单位缺省公文分类：行政公文，用于创建单位时调用
	 */
	public void generateAccountDefaultCategory(Long accountId);
	
	/**
	 * 查询发文种类对应文单的数量
	 * @param ids 发文种类id
	 * @return List<Long[]{发文种类id,文单数量}>
	 */
	public List<Long[]> findCountOfCategory(Long[] ids);
	
	/**
	 * 判断单位下种类名称是否已经存在，不包括自身
	 * @param categoryName  新的种类名称
	 * @param id            当前种类id，如果为空说明是新建，不需要传递
	 * @param domainId      单位id
	 * @return              true:存在同名种类;不存在同名种类
	 */
	public boolean hasExistName(String categoryName,Long id,Long domainId);
	
	public void updateCategory(Long id,String categoryName,Long modifyUserId,Date modifyTime);
	
	public void deleteCategoryById(List<Long> ids);
	
	/**
	 * 通过id列表查找发文种类
	 * @param ids
	 * @return
	 */
	public List<EdocCategory> findCategory(List<Long> ids);
}
 