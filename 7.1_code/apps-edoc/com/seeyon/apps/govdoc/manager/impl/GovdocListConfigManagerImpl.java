package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.dao.GovdocListConfigDao;
import com.seeyon.apps.govdoc.manager.GovdocListConfigManager;
import com.seeyon.apps.govdoc.po.GovdocListConfig;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;

/**
 * 公文列表分类配置实现
 * @author 唐桂林
 *
 */
public class GovdocListConfigManagerImpl implements GovdocListConfigManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocListConfigManagerImpl.class);
	
	private PermissionManager permissionManager;
	private GovdocListConfigDao govdocListConfigDao;
	
	/**
	 * 获取某单位下的所有节点权限（非重复）
	 * @param domainId
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public List<PermissionVO> getListPermissions(Long domainId) throws BusinessException {
		List<PermissionVO> configList = new ArrayList<PermissionVO>();
		
		List<PermissionVO> permissionList = new ArrayList<PermissionVO>();
		permissionList.addAll(permissionManager.getPermission("edoc_new_send_permission_policy", 1, domainId));
		permissionList.addAll(permissionManager.getPermission("edoc_new_rec_permission_policy", 1, domainId));
		permissionList.addAll(permissionManager.getPermission("edoc_new_change_permission_policy", 1, domainId));
		//permissionList.addAll(permissionManager.getPermission("edoc_send_permission_policy", 1, domainId));
		//permissionList.addAll(permissionManager.getPermission("edoc_rec_permission_policy", 1, domainId));
		if(!(Boolean)SysFlag.sys_isG6S.getFlag()) {
			permissionList.addAll(permissionManager.getPermission("edoc_new_sign_permission_policy", 1, domainId));
			//permissionList.addAll(permissionManager.getPermission("edoc_sign_permission_policy", 1, domainId));
		}
		
		List<String> policyList = new ArrayList<String>();
		
		for(PermissionVO bean : permissionList) {
			if(policyList.contains(bean.getName())) {
				continue;
			}
			policyList.add(bean.getName());
			configList.add(bean);
		}
		return configList;
	}
	
	/**
	 * 获取某人设置的列表分类配置结果
	 * @param type
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public List<String> findListConfigResult(int type, Long userId) throws BusinessException {
		List<GovdocListConfig> configList = govdocListConfigDao.findPermission(type, userId);
		List<String> sysConfigList = new ArrayList<String>();
		String sysConfigName = "govdoc.done.all";
		if(type == 1) {//待办
			sysConfigName = "govdoc.pending.all";
		}
		for (int i = 0; i < configList.size(); i++) {
			GovdocListConfig glc = configList.get(i);
			if(glc.getListType()==type && sysConfigName.equals(glc.getName())) {
				String govdocPermissions=glc.getPermissions();
				String[] str=govdocPermissions.split(",");
				for (int j = 0; j < str.length; j++) {
					sysConfigList.add(str[j]);
				}
			}
		}
		if(Strings.isEmpty(sysConfigList)) {
			sysConfigList.add("1");
			sysConfigList.add("1");
			sysConfigList.add("1");
		}
		return sysConfigList;
	}
	

    /**
	 * 获取公文列表节点权限
	 * @param configId
	 * @return
	 * @throws BusinessException
	 */
    @Override
	public String getNodePolicyByConfigId(Long configId) throws BusinessException {
		String nodePolicy = "";
		GovdocListConfig bean = govdocListConfigDao.getGovdocListConfig(configId);
		String permissions = bean.getPermissions();
		if(Strings.isNotBlank(permissions)) {
			String[] ids = permissions.split(",");
			for(String id : ids) {
				if(Strings.isNotBlank(id)) {
					PermissionVO permission = permissionManager.getPermission(Long.parseLong(id));
					if(permission != null) {
						if(Strings.isNotEmpty(nodePolicy)) {
							nodePolicy += ",";
						}
						nodePolicy += permission.getName();
					}
				}
			}
			
		}
		return nodePolicy;
	}
	
	/**
     * 公文列表分类配置(Ajax方法)
     * @param listType
     * @return
     * @throws BusinessException
     */
    @Override
    public String getListConfigs(String listType) throws BusinessException {
		int type = 2;
		String sysConfigName = "govdoc.done.all";
		String sysConfigName1 = "在办";
		String sysConfigName2 = "已办结";
		String linkType1 = "listDone";
		String linkType2 = "listFinished";
		if(Strings.isNotBlank(listType) && listType.startsWith("listPending")) {
			type = 1;
			sysConfigName = "govdoc.pending.all";
			sysConfigName1 = "待办";
			sysConfigName2 = "待阅";
			linkType1 = "listPending";
			linkType2 = "listReading";
		}
		
		GovdocListConfig sysConfig =  null;
		List<GovdocListConfig> newconfigList = new ArrayList<GovdocListConfig>();
		List<GovdocListConfig> otherconfigList = new ArrayList<GovdocListConfig>();
    	List<GovdocListConfig> configList = govdocListConfigDao.findPermission(type, AppContext.currentUserId());
    	for(GovdocListConfig bean : configList) {
    		if(sysConfigName.equals(bean.getName())) {
    			sysConfig = bean;
    		} else {
    			otherconfigList.add(bean);
    		}
    	}
    	if(sysConfig == null) {//第1位为全部，不处理
    		GovdocListConfig listConfig1 = new GovdocListConfig();//第2位待办/在办
    		listConfig1.setName(sysConfigName1);
    		listConfig1.setLinkType(linkType1);
    		listConfig1.setConfigName(sysConfigName);
    		listConfig1.setListType(type);
    		
    		GovdocListConfig listConfig2 = new GovdocListConfig();//第3位待阅/已办结
    		listConfig2.setName(sysConfigName2);
    		listConfig2.setLinkType(linkType2);
    		listConfig2.setConfigName(sysConfigName);
    		listConfig2.setListType(type);
    		
    		newconfigList.add(listConfig1);
    		newconfigList.add(listConfig2);
    		newconfigList.addAll(configList);
    	} else {
    		String permissions = sysConfig.getPermissions();
    		String[] strs = permissions.split(",");//第1位-全部 第2位-待办/在办 第3位-待阅/已办结 
    		for(int i=1; i<strs.length; i++) {
    			if("1".equals(strs[i])) {
    				GovdocListConfig listConfig = new GovdocListConfig();
    				listConfig.setListType(type);
    				listConfig.setConfigName(sysConfigName);
    				if(i == 1) {
    					listConfig.setName(sysConfigName1);
    					listConfig.setLinkType(linkType1);
    				} else {
    					listConfig.setName(sysConfigName2);
    					listConfig.setLinkType(linkType2);
    				}
    				newconfigList.add(listConfig);
    			}
    		}
    		newconfigList.addAll(otherconfigList);
    	}
    	return JSONUtil.toJSONString(newconfigList);
    }
	
	/**
	 * 保存列表分类配置(Ajax方法)
	 * @param params
	 * @throws BusinessException
	 */
	@Override
	public String saveListConfig(Map<String, String> params) {
		try {
			String listType = params.get("listType");
			int type = 1;
			if(Strings.isNotBlank(listType)) {
				if(listType.startsWith("listPending")) {
					type = 1;
				} else if(listType.startsWith("listDone")) {
					type = 2;
				}
			}
			GovdocListConfig po = new GovdocListConfig();
			po.setNewId();
			po.setName(String.valueOf(params.get("configName")));
			po.setOwnerId(AppContext.currentUserId());
			po.setListType(Integer.parseInt(String.valueOf(type)));
			po.setPermissions(String.valueOf(params.get("permissions")));
			po.setCreateDate(DateUtil.currentDate());
			if("govdoc.done.all".equals(po.getName()) || "govdoc.pending.all".equals(po.getName())) {
				govdocListConfigDao.deleteListConfig(AppContext.currentUserId(), type, po.getName());
			}
			govdocListConfigDao.saveOrUpdateListConfig(po);
		} catch(Exception e) {
			LOGGER.error("保存列表分类配置出错", e);
			return "false";
		}
		return "true";
	}
	
	/**
	 * 删除列表分类配置(Ajax方法)
	 * @param id
	 * @throws BusinessException
	 */
	@Override
	public String deleteListConfig(String id) {
		try {
			govdocListConfigDao.deleteListConfig(Long.parseLong(id));
		} catch(Exception e) {
			LOGGER.error("删除列表分类配置出错", e);
			return "false";
		}
		return "true";
	}
	
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public void setGovdocListConfigDao(GovdocListConfigDao govdocListConfigDao) {
		this.govdocListConfigDao = govdocListConfigDao;
	}
	
}
