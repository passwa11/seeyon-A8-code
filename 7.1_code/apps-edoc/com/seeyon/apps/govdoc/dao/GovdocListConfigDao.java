package com.seeyon.apps.govdoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.util.Strings;

import com.seeyon.apps.govdoc.po.GovdocListConfig;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;

public class GovdocListConfigDao {

	/**
	 * 
	 * @param type
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListConfig> findPermission(int type, Long userId) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("type", type);
		paramMap.put("userId", userId);
    	String hql = "from GovdocListConfig as g where g.ownerId=:userId and g.listType=:type order by g.createDate";
        return (List<GovdocListConfig>)DBAgent.find(hql, paramMap);  	
	}
	
	/**
	 * 
	 * @param configId
	 * @return
	 * @throws BusinessException
	 */
	public GovdocListConfig getGovdocListConfig(Long configId) throws BusinessException {
		return DBAgent.get(GovdocListConfig.class, configId);
	}
	
	/**
	 * 
	 * @param govdocListConfig
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public void saveOrUpdateListConfig(GovdocListConfig govdocListConfig) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("name", govdocListConfig.getName());
		paramMap.put("userId", govdocListConfig.getOwnerId());
		String hql = "from GovdocListConfig as g where g.name=:name and g.ownerId=:userId";
        List<GovdocListConfig> listGovdocListConfig = DBAgent.find(hql, paramMap);
        if(listGovdocListConfig.size() > 0) {
        	GovdocListConfig gdlc = listGovdocListConfig.get(0);
        	govdocListConfig.setId(gdlc.getId());
        	DBAgent.update(govdocListConfig);
        } else {
        	DBAgent.save(govdocListConfig);
        }
	}
	
	/**
	 * 删除列表分类配置
	 * @param id
	 * @throws BusinessException
	 */
	public void deleteListConfig(Long id) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("id", id);
		DBAgent.bulkUpdate("delete from GovdocListConfig where id = :id", paramMap);
	}
	
	/**
	 * 删除列表分类配置
	 * @param id
	 * @throws BusinessException
	 */
	public void deleteListConfig(Long ownerId, Integer type, String name) throws BusinessException {
		String hsql = "delete from GovdocListConfig where ownerId = :ownerId";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("ownerId", ownerId);
		if(type.intValue() != -1) {
			hsql += " and listType = :listType";
			paramMap.put("listType", type);
		}
		if(Strings.isNotBlank(name)) {
			hsql += " and name = :name";
			paramMap.put("name", name);
		}
		DBAgent.bulkUpdate(hsql, paramMap);
	}
	
}
