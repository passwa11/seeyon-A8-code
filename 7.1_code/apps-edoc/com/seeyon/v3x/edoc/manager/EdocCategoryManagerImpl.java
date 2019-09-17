package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.v3x.edoc.constants.EdocCategoryStoreTypeEnum;
import com.seeyon.v3x.edoc.dao.EdocCategoryDao;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.util.Constants;

public class EdocCategoryManagerImpl implements EdocCategoryManager {
	private static final Log LOG = CtpLogFactory.getLog(EdocCategoryManagerImpl.class);
	private static String edocresource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
	private EdocCategoryDao edocCategoryDao;

	public void setEdocCategoryDao(EdocCategoryDao edocCategoryDao) {
		this.edocCategoryDao = edocCategoryDao;
	}
	
	public List<EdocCategory> getCategoryByRoot(Long rootId, Long accountId,boolean isPagination){
		return getCategoryByRoot(rootId, accountId,null,null, isPagination);
	}

	@Override
	public List<EdocCategory> getCategoryByRoot(Long rootId, Long accountId,String condition,String textfield,boolean isPagination) {
		return this.edocCategoryDao.findByRoot(rootId, accountId,Constants.SubEdocCategory.normal.ordinal(),condition,textfield,isPagination);
	}

	@Override
	public List<EdocCategory> getCategoryByAccount(Long accountId) {
		return this.edocCategoryDao.findByAccountId(accountId,Constants.SubEdocCategory.normal.ordinal());
	}
	
	public EdocCategory getFirstCategoryByAccount(Long accountId) {
		List<EdocCategory> categoryList = getCategoryByAccount(accountId);
		if(categoryList !=null && categoryList.size()>0) {
			return categoryList.get(0);
		}
		return null;
	}
	
	public EdocCategory getCategoryById(Long categoryId) {
		return this.edocCategoryDao.get(categoryId);
	}

	@Override
	public void saveCategory(String name,Long rootCategory,Long accountId,Integer state,Integer storeType,Long modifyUserId,Date modifyTime){
		EdocCategory ec = new EdocCategory();
		ec.setIdIfNew();
		ec.setName(name);
		ec.setAccountId(accountId);
		ec.setRootCategory(rootCategory);
		//ec.setState(Constants.SubEdocCategory.normal.ordinal());
		ec.setState(state);
		ec.setStoreType(storeType);
		ec.setModifyUserId(modifyUserId);
		ec.setModifyTime(modifyTime);
		this.edocCategoryDao.save(ec);
	}
	
	@Override
	public void handleCategory(String[] newCategory,List<Long> modifyList,List<String> modifyNameList,
			List<Long> removeList,Long accountId,Long rootCategory) {
		List<EdocCategory> newList = null;
		if(newCategory != null && newCategory.length>0) {
			EdocCategory ec = null;
			newList = new ArrayList<EdocCategory>(newCategory.length);
			for(String c:newCategory) {
				ec = new EdocCategory();
				ec.setIdIfNew();
				ec.setName(c);
				ec.setAccountId(accountId);
				ec.setRootCategory(rootCategory);
				ec.setState(Constants.SubEdocCategory.normal.ordinal());
				ec.setStoreType(EdocCategoryStoreTypeEnum.USER_DEFINED.ordinal());
				newList.add(ec);
			}
		}
		saveCategory(newList,modifyList,modifyNameList,removeList);
	}
	
	@Override
	public void saveCategory(List<EdocCategory> newCategory,List<Long> modifyList,List<String> modifyNameList,List<Long> removeList) {
		if(newCategory != null)
			this.edocCategoryDao.save(newCategory);
		if(modifyList != null && modifyNameList != null)
			this.edocCategoryDao.update(modifyList, modifyNameList);
		if(removeList != null){
			List<Long[]> usedList = this.edocCategoryDao.findCountOfCategory(removeList.toArray(new Long[]{}));
			Long num;
			if(null != usedList && !usedList.isEmpty()){
				for(int index=0;index<usedList.size();index++){
					num = usedList.get(index)[1];
					if(null != num && num > 0){
						LOG.info("发文种类已被使用，不能删除：id："+usedList.get(index)[0]);
						return;
					}
				}
			}
			this.edocCategoryDao.remove(removeList,Constants.SubEdocCategory.cancel.ordinal());
		}
	}
	
	@Override
	/**
	 * 填充edocform中的分类名称
	 */
	public void fillFormCategoryName(List<EdocForm> forms) {
		if(forms == null || forms.isEmpty())
			return;
		List<Long> list = new ArrayList<Long>(forms.size());
		for(EdocForm form:forms)
			list.add(form.getSubType());
		Map<Long,String> map = this.edocCategoryDao.getCategoryName(list);
		if(map != null) {
			EdocForm form = null;
			for(int i=0;i<forms.size();i++) {
				form = forms.get(i);
				form.setSubTypeName(map.get(form.getSubType()));
			}
		}
	}
	
	/**
	 * 产生单位缺省公文分类：行政公文，用于创建单位时调用
	 */
	@Override
    public void generateAccountDefaultCategory(Long accountId) {

	    //检查一次， 作为修复数据复用
        List<EdocCategory> catgs = this.edocCategoryDao.findCategory(accountId,
                EdocCategoryStoreTypeEnum.SYSTEM.ordinal(), Long.valueOf(ApplicationCategoryEnum.edocSend.getKey()));
        
        if(Strings.isEmpty(catgs)){
            EdocCategory edocCategory = new EdocCategory();
            edocCategory.setIdIfNew();
            edocCategory.setAccountId(accountId);
            edocCategory.setRootCategory(Long.valueOf(ApplicationCategoryEnum.edocSend.getKey()));
            edocCategory.setState(Constants.SubEdocCategory.normal.ordinal());
            edocCategory.setName(ResourceBundleUtil.getString(edocresource, "edoccategory.default.1"));
            edocCategory.setStoreType(EdocCategoryStoreTypeEnum.SYSTEM.ordinal());// 设置为系统类型
            this.edocCategoryDao.save(edocCategory);
        }
    }
	
	@Override
	public List<Long[]> findCountOfCategory(Long[] ids) {
		return this.edocCategoryDao.findCountOfCategory(ids);
	}
	
	public void deleteCategoryById(Long categoryId){
		edocCategoryDao.delete(categoryId);
	}
	
	/**
	 * 修改公文分类
	 * @param categoryId
	 */
	public void updateCategory(EdocCategory category){
		edocCategoryDao.update(category);
	}
	
	/**
	 * 判断种类名称是否已经存在，不包括自身
	 * @param categoryName  新的种类名称
	 * @param id            当前种类id，如果为空说明是新建，不需要传递
	 * @param domainId      单位id
	 * @return              true:存在同名种类;不存在同名种类
	 */
	@Override
	public boolean hasExistName(String categoryName,Long id,Long domainId){
		boolean exist = false;
		if(Strings.isNotBlank(categoryName)){
			exist = this.edocCategoryDao.getCountByName(categoryName, id,domainId) > 0;
		}
		return exist;
	}
	
	public void updateCategory(Long id,String categoryName,Long modifyUserId,Date modifyTime){
		this.edocCategoryDao.updateCategory(id, categoryName, modifyUserId, modifyTime);
	}
	
	public void deleteCategoryById(List<Long> ids){
		this.edocCategoryDao.deleteCategory(ids);
	}
	
	/**
	 * 通过id列表查找发文种类
	 * @param ids
	 * @return
	 */
	public List<EdocCategory> findCategory(List<Long> ids){
		if(ids == null || ids.isEmpty())
			return null;
		return this.edocCategoryDao.findCategoryByIds(ids);
	}
}
