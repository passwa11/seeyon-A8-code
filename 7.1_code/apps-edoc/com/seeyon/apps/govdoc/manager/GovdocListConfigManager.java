package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.permission.vo.PermissionVO;

/**
 * 公文列表分类配置接口
 * @author 唐桂林
 *
 */
public interface GovdocListConfigManager {

	/**
	 * 获取某单位下的所有节点权限（非重复）
	 * @param domainId
	 * @return
	 * @throws BusinessException
	 */
	public List<PermissionVO> getListPermissions(Long domainId) throws BusinessException;
	
	/**
	 * 获取某人设置的列表分类配置结果
	 * @param type
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	public List<String> findListConfigResult(int type, Long userId) throws BusinessException;
	
	/**
	 * 获取公文列表节点权限
	 * @param configId
	 * @return
	 * @throws BusinessException
	 */
	public String getNodePolicyByConfigId(Long configId) throws BusinessException;
	
	/**
     * 公文列表分类配置(Ajax方法)
     * @param listType
     * @return
     * @throws BusinessException
     */
    public String getListConfigs(String listType) throws BusinessException;
	
	/**
	 * 保存列表分类配置(Ajax方法)
	 * @param params
	 * @throws BusinessException
	 */
	public String saveListConfig(Map<String, String> params) throws BusinessException;
	
	/**
	 * 删除列表分类配置(Ajax方法)
	 * @param id
	 * @throws BusinessException
	 */
	public String deleteListConfig(String id) throws BusinessException;
	
}
