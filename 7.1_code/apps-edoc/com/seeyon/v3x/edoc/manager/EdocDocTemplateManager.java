package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.exception.EdocException;

public interface  EdocDocTemplateManager {
	/**
	 * 添加公文套红模版
	 * @param name
	 * @param type
	 * @param templateId
	 * @param domainId
	 * @param status
	 */
	public String addEdocTemplate(String name,String desc,int type,long templateId,long domainId,int status) throws EdocException;
	
	/**
	 * 用于在organizationController的createAccount中调用，每新建一个单位，就要增加相应的套红模板(模板的文件有系统预制)
	 * @return
	 * @throws EdocException
	 */
	public void addEdocTemplate(long accountId) throws Exception;
	
	/**
	 * 修改公文套红模板
	 * 
	 */
	public String modifyEdocTemplate(EdocDocTemplate edocTemplate,String name)throws EdocException;
	
	/**
	 * 根据ID获取模版对象
	 * @param edocTemplateId
	 * @return
	 */
	public EdocDocTemplate getEdocDocTemplateById(long edocTemplateId);
	
	/**
	 * 根据公文套红模版ID删除具体的对象
	 * @param edocTemplateIds
	 */
	public void deleteEdocTemtlate(List<Long> edocTemplateIds);
	
	/**
	 * 查询出所有得公文套红模版
	 * @return
	 */
	public List<EdocDocTemplate> findAllTemplate() throws EdocException;
	
	/**
	 * 查询出所有得公文套红模版
	 * @return
	 */
	public List<EdocDocTemplate> findAllTemplate(String condition,String textfield) throws EdocException;
	
	/**
	 * 根据类型查找出公文套红模版
	 * @param type
	 * @return
	 */
	public List<EdocDocTemplate> findTemplateByType(int type) throws EdocException ;
	
	/**
	 * 根据用户ID得到用户能够获取得所有公文套红模板
	 * @param userId
	 * @param obj 正文类型，如果为空，即普通查询，否则为公文处理时套红弹出页面的查询
	 * @return
	 */
	public List<EdocDocTemplate> findTemplateByDomainId(long userId,int type, Object... obj)throws EdocException;
	
	/**
	 * 添加公文套红模版
	 */
	public String addEdocTemplate(EdocDocTemplate edocTemplate) throws EdocException;
	
	/**
	 * 套红时,根据传入的处理人Id,查找该处理人所属单位被授权的套红模板
	 * @param userId : 当前处理人的Id	
	 * @Param type : 套红模板类型
	 * @return
	 * @throws EdocException
	 */
	public List<EdocDocTemplate> findGrantedListForTaoHong(long userId, int type, String textType)throws Exception;
	/**
	 * 套红时,根据传入的处理人Id,查找该处理人所属单位被授权的套红模板
	 * @param orgAccountId : 需要查找套红模板的单位
	 * @param userId 	   : 当前处理人的Id	
	 * @Param type 		   : 套红模板类型
	 * @return
	 * @throws EdocException
	 */
	public List<EdocDocTemplate> findGrantedListForTaoHong(Long orgAccountId,long userId, int type, String textType)throws Exception;
	public boolean checkHasName(int type,String name);
	
	public boolean checkHasName(int type,String name,Long templateId,Long accountId);
	
	public List<EdocDocTemplate> getAllTemplateByAccountId(long accountId);
	
	public String hasEdocDocTemplate(Long orgAccountId,String edocType,String bodyType);
	
}
