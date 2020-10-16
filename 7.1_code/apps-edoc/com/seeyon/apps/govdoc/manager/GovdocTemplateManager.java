package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.bo.TemplateMarkInfo;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.template.vo.TemplateCategory;
import com.seeyon.ctp.util.FlipInfo;

public interface GovdocTemplateManager {
	
	public void setLianheTemplateAttr(List<CtpTemplate> templateList);

	public List<CtpTemplate> findLianHeTemplateList(Map<String, Object> map);
	
	FlipInfo selectTempletesNew(FlipInfo flipInfo, Map<String, String> params) throws BusinessException;
	
	/**
     * 根据授权和分类来查找当前登录用户能使用的分类
     */
    public List<CtpTemplateCategory> getCategorysByAuth(Long accountId, List<ModuleType> types) throws BusinessException;

	StringBuffer getCategory2HTMLNew(Long accountId, Long parentCategoryId) throws BusinessException;
    /**
     * 公文另存为个人模板
     * libing
     * @throws BusinessException 
     * @throws Exception 
     */
	public String saveGovDocTemplate() throws BusinessException;
	
	/**
	 * 此方法仅供首页模板栏目调用，对首页模板栏目进行特殊操作
	 * 向模板配置表中插入授权数据，删除未授权数据
	 * @param flipInfo
	 * @param param
	 */
	public void transMergeCtpTemplateConfig(FlipInfo flipInfo, Map<String, Object> param) throws BusinessException;
	
	Integer getMaxSortId(String parentIds) throws BusinessException;
	
	public boolean isCtpTemplateCategoryCanManager(long memberId, long loginAccountId,CtpTemplateCategory c) throws BusinessException;
	
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
	
	public List<CtpTemplate> getSysFormTemplatesByOwnerMemberId(long memberId,List<Integer> moduleTypes)throws BusinessException;

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
     * 保存公文模板
     * @param jsonDomain
     * @return
     * @throws BusinessException
     */
    public Map saveTemplate2Cache(Map jsonDomain)throws BusinessException;
    
	/**
     * 获取文单  文号绑定列表
     * @param user
     * @return
     * @throws BusinessException
     */
    public Map<Integer, List<TemplateMarkInfo>> getFormBindMarkList(User user) throws BusinessException;
	public Map checkTemplateIsDelete(String tid)throws BusinessException;
    /**
     * 更新模版的授权信息
     * @param ids 要修改的模版id数组
     * @param value 授权结果
     * @throws BusinessException com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     */
    public void updateTempleteAuth(Long[] ids, String value, Integer categoryType) throws BusinessException;
    /**
     * 返回指定模板类型和名字的个人模板Id列表
     * @param subject
     * @param type
     * @return
     */
    public List<Long> getPersonTemplateIds(String subject,String type,boolean isEdoc);
    
    /**
     * 删除模版类型
     * @param category 要保存模版类型对象
     * @return
     * @throws BusinessException
     */
 //   public String deleteCategory(Long id) throws BusinessException;

    /**
     * 查询本单位启用的公文系统模板数量
     * @param accountId
     * @return
     * @throws BusinessException
     */
    public Integer getAccountGovdocSysTemplateCount(Long accountId) throws BusinessException;
    
    /**
     * 更加表单Id，更新相关模板的subState状态
     * @param fromId
     * @param subState
     * @throws BusinessException
     */
    public void updateSubStateByFormId(Long fromId,  Integer subState) throws BusinessException;

}
