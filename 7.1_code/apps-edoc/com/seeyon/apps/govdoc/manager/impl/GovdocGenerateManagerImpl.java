package com.seeyon.apps.govdoc.manager.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.apps.govdoc.manager.GovdocGenerateManager;
import com.seeyon.apps.govdoc.manager.GovdocObjTeamManager;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;

public class GovdocGenerateManagerImpl implements GovdocGenerateManager {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocGenerateManagerImpl.class);
	
	private ConfigManager configManager;
	private TemplateManager templateManager;
	private GovdocElementManager govdocElementManager;
	private GovdocObjTeamManager govdocObjTeamManager;
	
	@Override
	public void generate(V3xOrgAccount account) throws BusinessException {
		//复制新公文元素权限
		generateElement(account);
		
		//复制公文节点权限-需屏蔽掉，因为组件做了新公文节点权限的预置
		//generatePermission(account);
		
		//复制新公文模板分类
		generateTemplateCategory(account);
	}
	
	private void generateTemplateCategory(V3xOrgAccount account) throws BusinessException {
		LOGGER.info("创建单位"+ account.getName() +"，自动新建新发文下级行政公文子分类开始...");
		CtpTemplateCategory ctpTemplateCategory = new CtpTemplateCategory();
		ctpTemplateCategory.setParentId(Long.parseLong(String.valueOf(ModuleType.govdocSend.getKey())));
		ctpTemplateCategory.setType(ModuleType.govdocSend.getKey());
		ctpTemplateCategory.setName("行政公文");
		ctpTemplateCategory.setDelete(false);
		ctpTemplateCategory.setOrgAccountId(account.getId());
		ctpTemplateCategory.setSort(0);
		ctpTemplateCategory.setNewId();
		templateManager.saveCtpTemplateCategory(ctpTemplateCategory);
		LOGGER.info("创建单位"+ account +"，自动新建新发文下级行政公文子分类结束...");
		
		LOGGER.info("创建单位"+ account.getName() +"，自动新建新收文下级办件、阅件子分类开始...");
		CtpTemplateCategory ctpTemplateCategoryRec1 = new CtpTemplateCategory();
		ctpTemplateCategoryRec1.setParentId(Long.parseLong(String.valueOf(ModuleType.govdocRec.getKey())));
		ctpTemplateCategoryRec1.setType(ModuleType.govdocRec.getKey());
		ctpTemplateCategoryRec1.setName(ResourceUtil.getString("govdocrec.category.default.1"));
		ctpTemplateCategoryRec1.setDelete(false);
		ctpTemplateCategoryRec1.setOrgAccountId(account.getId());
		ctpTemplateCategoryRec1.setSort(1);
		ctpTemplateCategoryRec1.setNewId();
		templateManager.saveCtpTemplateCategory(ctpTemplateCategoryRec1);
		CtpTemplateCategory ctpTemplateCategoryRec2 = new CtpTemplateCategory();
		ctpTemplateCategoryRec2.setParentId(Long.parseLong(String.valueOf(ModuleType.govdocRec.getKey())));
		ctpTemplateCategoryRec2.setType(ModuleType.govdocRec.getKey());
		ctpTemplateCategoryRec2.setName("govdocrec.category.default.2");
		ctpTemplateCategoryRec2.setDelete(false);
		ctpTemplateCategoryRec2.setOrgAccountId(account.getId());
		ctpTemplateCategoryRec2.setSort(2);
		ctpTemplateCategoryRec2.setNewId();
		templateManager.saveCtpTemplateCategory(ctpTemplateCategoryRec2);
		LOGGER.info("创建单位"+ account +"，自动新建新收文下级办件、阅件子分类结束...");
	}
	
	/**
	 * 新建单位预置公文相关枚举
	 * @param account
	 * @throws BusinessException
	 */
	/*private void generateEnum(V3xOrgAccount account) throws BusinessException {
		
	}*/
	
	/**
	 * 新建单位预置公文元素
	 * @throws BusinessException
	 */
	private void generateElement(V3xOrgAccount account) throws BusinessException {
		LOGGER.info("开始为新建单位复制系统公文元素transCopyGroupElement2NewAccout...");
		try {
			govdocElementManager.transGenerateElement(account.getId());
		} catch(Exception e) {
			LOGGER.error("新建单位的时候复制系统公文元素异常",e);
		}
		LOGGER.info("复制系统公文元素结束。");
	}
	
	/**
	 * 新建单位预置公文节点权限
	 * @throws BusinessException
	 */
	private void generatePermission(V3xOrgAccount account) throws BusinessException {
		LOGGER.info("开始为新建单位复制公文节点权限...");
		try{
			configManager.saveInitCmpConfigData(EnumNameEnum.edoc_new_send_permission_policy.name(),account.getId());
			configManager.saveInitCmpConfigData(EnumNameEnum.edoc_new_rec_permission_policy.name(),account.getId());
			configManager.saveInitCmpConfigData(EnumNameEnum.edoc_new_qianbao_permission_policy.name(),account.getId());
			configManager.saveInitCmpConfigData(EnumNameEnum.edoc_new_change_permission_policy.name(),account.getId());
		}catch(Exception e){
			LOGGER.error("新建单位的时候复制公文节点权限异常",e);
		}

		LOGGER.info("复制系统公文节点权限结束。");
	}
	
	/**
	 * 新建单位预置公文元素
	 * @param account
	 * @throws BusinessException
	 */
	/*private void generateElement(V3xOrgAccount admin) throws BusinessException {
		
	}*/
	
	/**
	 * 新建单位预置公文单
	 * @param ACCOUNT
	 * @throws BusinessException
	 */
	public void generateGovform(V3xOrgMember admin) throws BusinessException {
		
	}
	@Override
	public void deleteEdocObjTeam(V3xOrgDepartment department) throws BusinessException {
		if(department != null){
			try {
				govdocObjTeamManager.deleteByMemberId(department.getId());
			}catch(Exception e) {
				LOGGER.error("监听删除部门事件，从机构组中删除chucuo ", e);
				throw new BusinessException(e);
			}
		}
	}
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	public void setGovdocElementManager(GovdocElementManager govdocElementManager) {
		this.govdocElementManager = govdocElementManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setGovdocObjTeamManager(GovdocObjTeamManager govdocObjTeamManager) {
		this.govdocObjTeamManager = govdocObjTeamManager;
	}

}
