package com.seeyon.apps.govdoc.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;
import com.seeyon.v3x.edoc.exception.EdocException;

/**
 * 新公文套红模板接口
 * @author 唐桂林
 *
 */
public interface GovdocDocTemplateManager {
	
	/**
	 * 查询出所有得公文套红模版
	 * @return
	 */
	public List<EdocDocTemplate> findAllTemplate(String condition,String textfield) throws BusinessException;
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

	/**
	 * 根据ID获取模版对象
	 * @param edocTemplateId
	 * @return
	 */
	public EdocDocTemplate getEdocDocTemplateById(long edocTemplateId) throws BusinessException;
	
	/**
	 * 添加公文套红模版
	 */
	public String addEdocTemplate(EdocDocTemplate po) throws EdocException;
	
	/**
	 * 修改公文套红模板
	 * 
	 */
	public String modifyEdocTemplate(EdocDocTemplate po, String name)throws BusinessException;
	
	/**
	 * 根据公文套红模版ID删除具体的对象
	 * @param edocTemplateIds
	 */
	public void deleteEdocTemtlate(List<Long> edocTemplateIds) throws BusinessException;
	
	/**
	 * 
	 * @param type
	 * @param name
	 * @param templateId
	 * @param accountId
	 * @return
	 */
	public boolean checkHasName(int type,String name,Long templateId,Long accountId) throws BusinessException;
	
	/**
	 * 
	 * @param templateId
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocDocTemplateAcl> getEdocDocTemplateAcl(String templateId) throws BusinessException;
	
	/**
	 * 
	 * @param id
	 * @param templateId
	 * @param departmentIds
	 * @throws BusinessException
	 */
	public void saveEdocDocTemplateAcl(Long id,Long templateId,String[] departmentIds) throws BusinessException;
	
	/**
	 * 
	 * @param id
	 * @param templateId
	 * @param departmentIds
	 * @throws BusinessException
	 */
	public void updateEdocDocTemplateAcl(Long id,Long templateId,String[] departmentIds) throws BusinessException;
	
	/**
	 * 
	 * @param templateId
	 * @throws BusinessException
	 */
	public void deleteAclByTemplateId(Long templateId) throws BusinessException;
	
	/**
	 * 根据类型查找出公文套红模版
	 * @param type
	 * @return
	 */
	public List<EdocDocTemplate> findTemplateByType(int type) throws BusinessException;
	
	/**
	 * Ajax前台页面调用，判断是否存在套红模板
	 * @param edocType 类型（正文/文单）
	 * @param bodyType Officeword:word正文/Wpsword:wps正文
	 * @return "0":没有套红模板，“1”：有套红模板
	 */
	public String hasEdocDocTemplate(Long orgAccountId, String edocType, String bodyType);
	public String hasEdocDocTemplate(String isFromAdmin, Long orgAccountId, String edocType,String bodyType);

	/**
	 * 套红时,根据传入的处理人Id,查找该处理人所属单位被授权的套红模板
	 * @param userId : 当前处理人的Id	
	 * @Param type : 套红模板类型
	 * @return
	 * @throws EdocException
	 */
	public List<EdocDocTemplate> findGrantedListForTaoHong(Long userId, Integer type, String textType) throws BusinessException;
	
	/**
	 * 获取能够使用的模板，过滤掉停用的。
	 * @param user     ：用户
	 * @param edocType ：类型（正文/文单）
	 * @param bodyType : Officeword:word正文/Wpsword:wps正文
	 * @return
	 * @throws Exception
	 */
	public List<EdocDocTemplate> getEdocDocTemplateList(String isFromAdmin, Long orgAccountId, User user, String edocType, String bodyType) throws BusinessException;
	
	/**
	 * 根据表单ID获取绑定的文单模板
	 * @param formId
	 * @param userId
	 * @return
	 */
	public String getTemplateIdByFormId(Long formId,long userId) throws BusinessException;
	/**
	 *  根据表单ID获取下载到本地的打印模板
	 * @param formId
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public String getLocalPrintTemplate(Long formId) throws IOException, FileNotFoundException;
	
	
	
}
