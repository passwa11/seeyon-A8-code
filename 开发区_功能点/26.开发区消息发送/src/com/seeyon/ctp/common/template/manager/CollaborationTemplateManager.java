/**
 * $Author$	wuwl
 * $Rev$
 * $Date::  2012-08-29               $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.template.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.po.template.CtpTemplateConfig;
import com.seeyon.ctp.common.template.event.FormTemplateSaveAllParam;
import com.seeyon.ctp.common.template.vo.TemplateCategory;
import com.seeyon.ctp.util.FlipInfo;
/**
 * <p>Title: Collaboration Template Interface.</p>
 * <p>Description: Collaboration Template Modular CRUD Operation.</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 */
public interface CollaborationTemplateManager {

    /**
      * 修改协同模版时数据回显
      *
      * @param 	modelAndView	ModelAndView
      * @param 	user 			当前用户
      * @param 	from
      * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
      */
    public void newCollaborationTemplate(ModelAndView modelAndView, User user, String from) throws BusinessException;

    /**
      * 修改协同模版时数据回显
      *
      * @param 	user 			当前用户
      * @param 	modelAndView	ModelAndView
      * @param 	from 			数与模版区别标识
      * @param 	templeteId 		模版Id
      * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
      * @return Template
      */
    public String updateCollaborationTemplate(User user, ModelAndView modelAndView, String from, String templeteId)
            throws BusinessException;

    /**
      * 保存协同模版
      *
      * @param 	user 			当前用户
      * @param 	colBody 		模版正文
      * @param 	colSummary 		模版属性
      * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
      * @return Template
     * @throws Exception 
      */
    public CtpTemplate saveCollaborationTemplate() throws BusinessException, Exception;

    /**
      * 根据单位ID查询模版分类
      *
      * @param accountId 单位Id.
      * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
      * @return List<TemplateCategory>
      */
    public List<CtpTemplateCategory> getCategory(Long accountId) throws BusinessException;

    /**
      * 查询可管理模版分类
      *
      * @param 	userId			用户Id
      * @param 	accountId 		单位Id
      * @param 	templateCategories	模版分类
      * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
      * @return List<Long> 模版分类id
      */
    public List<Long> getCanManager(Long userId, Long accountId, List<CtpTemplateCategory> templateCategories)
            throws BusinessException;

    /**
     * 保存模版类型
     * @param category 要保存模版类型对象
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
    public TemplateCategory saveCategory(TemplateCategory category) throws BusinessException;
    
    /**
     * 更新模版类型
     * @param category 要保存模版类型对象
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
    public TemplateCategory updateCategory(TemplateCategory category) throws BusinessException;
    
    /**
     * 删除模版类型
     * @param category 要保存模版类型对象
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
   //  */
    //public String deleteCategory(Long id) throws BusinessException;
    
    /**
     * @param accountId 单位Id
     * @param parentCategoryId 父类型ID
     * @return 得到模版类型选择框select的html
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
    public String getCategory2HTML(Long accountId, Long parentCategoryId) throws BusinessException;
    
    /**
     * 根据单位ID和模版类型查询模版分类
     *
     * @param accountId 单位Id.
     * @param type 模版类型
     * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     * @return List<TemplateCategory>
     */
   public List<CtpTemplateCategory> getCategory(Long accountId, int type) throws BusinessException;
   
    /**
     * 根据ID查询模版分类
     *
     * @param id Id.
     * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     * @return TemplateCategory
     */
    public CtpTemplateCategory getCategoryById(Long id) throws BusinessException;
  
    /**
    * @param c 模版对象
    * @return 模版授权信息
    * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
    */
    public String[] getCategoryAuths(CtpTemplateCategory c) throws BusinessException;
    
    /**
     * @param flipInfo 分页信息
     * @param params 查询参数
     * @return 模版列表
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
  //  public FlipInfo selectTempletes(FlipInfo flipInfo, Map<String, String> params) throws BusinessException;
    
    /**
     * @param flipInfo 分页信息
     * @param params 查询参数
     * @return 模版列表
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
   // public FlipInfo selectTempletesForCopy(FlipInfo flipInfo, Map<String, String> params) throws BusinessException;
    
    /**
     * 
     * @param accountId
     * @param types
     * @param memberId
     * @return
     * @throws BusinessException
     */
   // public List<CtpTemplateCategory> getCategorysByAuthForCopy(List<Long> allAccountIds, List<ModuleType> types, Long memberId) throws BusinessException;
    
    /**
     * @param ids 要删除模版的ID数组
     * @param categoryType 模版类型
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
   // public void deleteTemplete(Long[] ids, Integer categoryType) throws BusinessException;
    
    /**
     * 更新模版的停启用状态
     * @param ids 要修改的模版id数组
     * @param state 1 停用/0 启用状态
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
   // public void updateInvalidateTemplete(Long[] ids, Integer state) throws BusinessException;
    
    /**
     * 更新模版的授权信息
     * @param ids 要修改的模版id数组
     * @param value 授权结果
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
  //  public void updateTempleteAuth(Long[] ids, String value, Integer categoryType) throws BusinessException;
    
    /**
     * 更新模版所属的分类信息
     * @param ids 要修改的模版id数组
     * @param pId 分类ID
     * @throws BusinessException
     */
   // public void updateTempletePath(Long[] ids, Long pId) throws BusinessException;
    
    /**
     * 查询个人配置模板
     * @param flipInfo
     * @param params
     * @return
     * @throws BusinessException
     */
  //  public FlipInfo getPersonalTemplete(FlipInfo flipInfo, Map<String, String> params) throws BusinessException;
    
    /**
     * 取消发布到首页的模板
     * @param ids
     * @return
     * @throws BusinessException
     */
   // public void deletePersonalTempleteConfig(Long[] ids) throws BusinessException;
    
    /**
     * 更新我的模板排序
     * @param ids
     * @return
     * @throws BusinessException
     */
    public void updateTempleteConfigSort(Long[] ids) throws BusinessException;

    
    /**
     * 新增模板调用历史记录
     * @param id 模板ID
     * @throws BusinessException
     */
    public void updateTempleteHistory(Long id) throws BusinessException;
    public void updateTempleteHistory(Long id,Long memberId) throws BusinessException ;
    /**
     * 最近调用模板历史信息
     * @param category
     * @param count
     * @return
     */
    //public List<CtpTemplate> getPersonalRencentTemplete(String category, int count) throws BusinessException;
    
    //public List<CtpTemplateCategory> getCategoryByAuth(Long accountId) throws BusinessException;
    
    //public List<CtpTemplateCategory> getCategoryByAuth(Long accountId, int type) throws BusinessException;
    
    /**
     * 根据授权和分类来查找当前登录用户能使用的分类
     */
   // public List<CtpTemplateCategory> getCategorysByAuth(Long accountId, List<ModuleType> types, Long memberId) throws BusinessException;
    
    
    public boolean isAccountAdmin() throws BusinessException;
    
    /**
     * @param  ctpTemplate 模板对象，需要ID和ModuleType
     * @return 模板的授权信息数组
     *         String[0] 为 '张三,李四'
     *         String[1] 为 'Member|123,Member|321'
     * @throws BusinessException
     */
   // public String[] getTemplateAuth(CtpTemplate ctpTemplate) throws BusinessException;
    
    /**
     * @param ctpTemplateCategory 
     * @return 找到当前分类所属的一级分类对象
     * @throws BusinessException
     */
    //public CtpTemplateCategory findRootParent(CtpTemplateCategory ctpTemplateCategory) throws BusinessException;
    
    /**
     * @param accountId 单位ID
     * @param id 分类ID
     * @return 找到当前分类包含的下级分类
     * @throws BusinessException
     */
 //   public List<CtpTemplateCategory> getSubCategorys(Long accountId, Long id) throws BusinessException;
    
    /**
     * 另存为个人模板
     * libing
     * @throws BusinessException 
     * @throws Exception 
     */
	public String saveTemplate() throws BusinessException;
	
    /**
     * 查询模板
     * @param flipInfo
     * @param params
     * @return
     * @throws BusinessException
     */
   /* public List<CtpTemplate> getCtpTemplate(FlipInfo flipInfo, Map<String, String> params)
			throws BusinessException;*/
    
    /**
     * 查询配置的模板，包含新建的（供首页栏目使用）
     * @param category 类型 对应ctp_template表中的module_type或者ctp_template_config表中的type
     * @param count 返回的记录数量
     * @return
     * @throws BusinessException
     */
   // public List<CtpTemplate> getMyConfigCollTemplate(FlipInfo flipInfo, Map<String, Object> param) throws BusinessException;
	
	
	public String checkTargethasDupName(String ids[],String id)throws BusinessException;

	/*
     * colManager 拷贝的方法， 解耦临时方法， 会被废弃
     * @Author      : xuqw
     * @Date        : 2015年11月9日下午1:55:49
     * @param type
     * @param module_id
     * @return
     * @throws BusinessException
     */
	@Deprecated
    public String saveAttachmentFromDomain(ApplicationCategoryEnum type,Long module_id) throws BusinessException;
	
	public String getMoreTemplateCategorys(String category, String fragmentId, String ordinal);
	/**
	 * 保存我的模板个性化查看方式
	 * @param value
	 */
	public void saveCustomViewType(String value);

    /**
     * 触发保存-表单信息
     * @param saveAllParams
     * @throws BusinessException
     */
  //  public void saveFlowTemplate2DB(FormTemplateSaveAllParam saveAllParams) throws BusinessException;


  //  public void templateJob(CtpTemplate template );

}
