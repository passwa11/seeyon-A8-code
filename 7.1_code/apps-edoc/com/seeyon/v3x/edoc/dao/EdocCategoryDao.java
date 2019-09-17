package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseDao;
import com.seeyon.v3x.edoc.domain.EdocCategory;

public class EdocCategoryDao extends BaseDao<EdocCategory> {
	public void save(EdocCategory edocCategory){
		super.save(edocCategory);
	}
	
	public void update(EdocCategory edocCategory){
		super.update(edocCategory);
	}
	
	public void remove(Long id,Integer state){
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(id);
		remove(ids,state);
	}
	
	public void remove(List<Long> ids,Integer state){
		if(ids == null)
			return;
		String hql = "update EdocCategory as e set e.state=? where e.id=?";
		for(int i=0;i<ids.size();i++) {
			super.bulkUpdate(hql, null, state,ids.get(i));
		}
	}
	
	/**
	 * 通过单位id查找，不分页
	 * @param accountId   单位id
	 * @return
	 */
	public List<EdocCategory> findByAccountId(Long accountId,Integer state){
		return super.find("from EdocCategory as ec where ec.accountId=? and ec.state=?", -1, -1, null, accountId,state);
	}
	
	public List<EdocCategory> findByRoot(Long rootId,Long accountId,Integer state,String condition,String textfield,boolean isPagination){
		StringBuilder from = new StringBuilder("from EdocCategory as ec");
		StringBuilder where = new StringBuilder(" where ec.accountId=:accountId and ec.rootCategory=:rootCategory and ec.state=:state");
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("accountId", accountId);
		params.put("rootCategory", rootId);
		params.put("state", state);
		if("categoryName".equals(condition)){
			where.append(" and ec.name like :categoryName");
			params.put("categoryName", "%" + textfield + "%");
		}
		from.append(where);
		from.append(" order by ec.modifyTime desc ");
		if(isPagination){
			return super.find(from.toString(), params);
		}else{
			return super.find(from.toString(), -1, -1, params);
		}
	}
	
	public void save(List<EdocCategory> categories) {
		super.savePatchAll(categories);
	}
	
	public void update(List<Long> ids,List<String> names) {
		if(ids == null || names == null || ids.size()!=names.size())
			return;
		String hql = "update EdocCategory as e set e.name=? where e.id=?";
		for(int i=0;i<ids.size();i++) {
			super.bulkUpdate(hql, null, names.get(i),ids.get(i));
		}
	}
	
	/**
	 * 通过id列表查询名称
	 */
	public Map<Long,String> getCategoryName(List<Long> ids){
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("ids", ids); 
		List<Object> result = super.find("select e.id,e.name from EdocCategory as e where e.id in(:ids)", -1, -1, param);
		Map<Long,String> map = null;
		if(result != null && !result.isEmpty()) {
			map = new HashMap<Long,String>();
			for(Object obj :result) {
				Object[] o = (Object[])obj;
				map.put((Long)o[0], (String)o[1]);
			}
		}
		return map;
	}
	
	/**
	 * 查询发文种类对应文单的数量
	 * @param ids 发文种类id
	 * @return List<Long[]{发文种类id,文单数量}>
	 */
	public List<Long[]> findCountOfCategory(Long[] ids) {
		String sql = "select subType,count(id) from EdocForm where subType in (:subTypeIds) group by subType";
		Map param = new HashMap();
		param.put("subTypeIds", ids);
		List list = this.find(sql, -1,-1,param);
		if(null == list || list.isEmpty()){
			return null;
		}
		List<Long[]> result = new ArrayList<Long[]>();
		Object[] item;
		for(int index=0;index<list.size();index++){
			item = (Object[])list.get(index);
			result.add(new Long[]{Long.valueOf(item[0].toString()),Long.valueOf(item[1].toString())});
		}
		return result;
	}
	
	public int getCountByName(String categoryName,Long id,Long domainId){
		String sql = "from EdocCategory as ec where ec.name=:categoryName";
		Map<String,Object> namedParameterMap = new HashMap<String,Object>();
		namedParameterMap.put("categoryName", categoryName);
		if(id != null){
			sql += " and ec.id!=:categoryId";
			namedParameterMap.put("categoryId", id);
		}
		if(domainId != null){
			sql += " and ec.accountId=:accountId";
			namedParameterMap.put("accountId", domainId);
		}
		int num = 0;
		List<EdocCategory> categorys = super.find(sql,-1,-1,namedParameterMap);
		if(categorys!=null){
			num = categorys.size();
		}
		return num;
	}
	
	public void updateCategory(Long id,String categoryName,Long modifyUserId,Date modifyTime){
		String sql = "update EdocCategory set name=?,modifyUserId=?,modifyTime=? where id=?";
		super.bulkUpdate(sql, null, categoryName,modifyUserId,modifyTime,id);
	}
	
	public void deleteCategory(List<Long> ids){
		String hql = "delete from EdocCategory where id in(:ids)";
		Map<String,Object> namedParameterMap = new HashMap<String,Object>();
		namedParameterMap.put("ids", ids);
		super.bulkUpdate(hql, namedParameterMap);
	}
	
	public List<EdocCategory> findCategoryByIds(List<Long> ids) {
		String hql = "from EdocCategory as ec where ec.id in(:ids)";
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("ids", ids);
		return super.find(hql, -1, -1, params);
	}
	
	/**
	 * 通过类型查询公文分类
	 * 
	 * @param accountId
	 * @param storeType
	 * @param rootId
	 * @return
	 *
	 * @Since A8-V5 6.1
	 * @Author      : xuqw
	 * @Date        : 2017年4月20日下午5:31:30
	 *
	 */
	public List<EdocCategory> findCategory(Long accountId, Integer storeType, Long rootId){
	    
	    StringBuilder hql = new StringBuilder("from EdocCategory as ec where ");
	    Map<String, Object> params = new HashMap<String, Object>();
	    
	    hql.append(" ec.accountId=:accountId ");
	    params.put("accountId", accountId);
	    
	    hql.append(" and ec.rootCategory=:rootCategory ");
	    params.put("rootCategory", rootId);
	    
	    hql.append(" and ec.storeType=:storeType ");
	    params.put("storeType", storeType);
	    
	    return super.find(hql.toString(), -1, -1, params);
	}
}
