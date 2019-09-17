package com.seeyon.v3x.edoc.manager;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.po.affair.CtpAffair;
//import com.seeyon.ctp.workflow.vo.WorkflowFormFieldVO;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocFormFlowPermBound;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.webmodel.EdocFormModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
public interface EdocFormManager 
{
	
	public void initialize();
	
	public void createEdocForm(EdocForm edocForm,List<Long> elementIdList);
	
	public boolean hasRquiredElement(Long formId);
	
	public void updateEdocForm(EdocForm edocForm) throws Exception;
	public void updateEdocFormExtendInfo(EdocFormExtendInfo edocForm) throws Exception;

	public EdocForm getEdocForm(long id);
	public EdocFormExtendInfo getEdocFormExtendInfoByForm(EdocForm edocForm ,Long loginAccount);
	public EdocForm getDefaultEdocForm(Long domainId,int edocType);
	public EdocForm getDefaultEdocForm(Long domainId,int edocType, long subType);
	public void setDefaultEdocForm(Long domainId,int edocType,EdocForm edocForm);
	
	public List<EdocForm> getAllEdocForms(Long domainId);
	
	/**
	 * 获取某个单位下需要修复的系统文单
	 * @Author      : xuqw
	 * @Date        : 2015年4月1日下午4:41:53
	 * @param domainId
	 * @return
	 */
	public List<EdocForm> getToFixSysEdocForms(Long domainId);
	
	public List<EdocForm> getAllEdocFormsForWeb(User user,Long domainId,String condition,String textfield);
	public  EdocFormExtendInfo getEdocFormExtendInfo(Long id);
	public List<EdocForm> getAllEdocFormsByStatus(Long domainId,int status);
	
	public List<EdocForm> getAllEdocFormsByType(Long domainId,int type);
	
	public List<EdocForm> getAllEdocFormsByTypeAndStatus(Long domainId,int type, int status);
	
	public List<EdocForm> getEdocForms(long domainId,String domainIds, int type);
	public List<EdocForm> getEdocForms(long domainId,String domainIds, int type, long subType);
	
	public List<EdocForm> getEdocForms(long domainId,String domainIds, int type, long subType,boolean isPage);
	
	public void removeEdocForm(long id) throws Exception;
	
	public List<EdocFormElement> getAllEdocFormElements();
	
	public List<EdocFormElement> getEdocFormElementByFormId(long formId);
	public String getEdocFormXmlData(long formId,EdocSummary edocSummary,long actorId, int edocType);//新增公文单类型
	
	public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId) throws EdocException;
	public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId,boolean isTemplete ,boolean isCallTemplete) throws EdocException;
	public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId,boolean isTemplete ,boolean isCallTemplete, CtpAffair affair) throws EdocException;
	
	public EdocFormModel getEdocFormModel(long formId,long actorId) throws EdocException;
	
	public EdocFormModel getEdocFormModel(long formId,EdocSummary edocSummary,long actorId, CtpAffair affair) throws EdocException;
	
	 public Set<EdocFormElement> saveEdocXmlData(long id,List<Long> elementIdList);
	 
	 public void deleteFormElementByFormId(long formId);
	
	 public String deleteForm(long id) throws Exception;
	 
	 public void updateForm(EdocForm edocForm);
	 
	 public void updateEdocFormElement(List<EdocFormElement> edocFormElements) throws Exception;
	 
	 public String getDirectory(String[] urls,String[] createDates,String[] mimeTypes,String[] names)throws Exception;
	 
	  public void updateDefaultEdocForm(long domainId,int type, Long subType,boolean hasSubType);
	  
	  /**
	   * 取消之前的缺省值，并将当前文单置为缺省
	   * @param newDefaultInfo
	   * @param domainId
	   * @param type
	   * @param subType
	   */
	  public void updateDefaultEdocForm(EdocFormExtendInfo newDefaultInfo,long domainId,int type, Long subType,boolean hasSubType);
	  
	  public List<String> getEdocElementFieldNameByRequired(long formId, Boolean required);
	  public Object[] getEdocFormElementRequiredMsg(EdocForm edocForm, EdocSummary summary) throws Exception;
	  
	  
	  /**
	   * 新建立单位后，生成默认公文单
	   * @param accountId
	   * @throws Exception
	   */
	  public void initAccountEdocForm(long accountId) throws Exception;
	  /**
	   * 复制公文单到当前单位
	   * @param formIds:逗号分割的公文单id
	   * @throws Exception
	   */
	  public void importEdocForm(String formIds) throws Exception;
	  
	  /**
	   * 检查公文单是否有重名
	   * @param name 名称
	   * @param type 类型(发文,收文,签报)
	   * @param status 状态,是否为启用
	   * @param domainId 单位标识
	   * @return
	   */
	  public boolean checkHasName(String name,int type);

	/**
	 * 绑定公文单处理意见与节点权限
	 * 
	 * @param name
	 *            处理意见： shenpi,fuhe,niban...
	 * @param boundName
	 *            节点权限名称 : shenpi, fuhe, niban...
	 * @param edocFormId
	 *            : 公文单ID
	 * @param sortType
	 *            处理意见排序方式 0,1,2,3
	 * @param orgAccountId
	 *            : 单位Id
	 * @throws Exception
	 */
	  public void bound(String name, String boundName, String boundNameLabel, long edocFormId, String sortType,Long accoutId)throws Exception;
	  /**
	   * 公文单Id
	   * @param edocFormId
	   * @return:返回节点权限为KEY,公文元素名称为value的Hashtable
	   */
	  public Hashtable<String,String> getOpinionLocation(Long edocFormId);
	  /**
	   * 公文单Id
	   * @param edocFormId
	   * @param aclAccountId :公文单被授权使用的单位
	   * @return:返回节点权限为KEY,公文元素名称为value的Hashtable
	   */
	  public Hashtable<String,String> getOpinionLocation(Long edocFormId,Long aclAccountId);
	  /**
	   * 返回公文单中，意见绑定元素的名称
	   * @param edocFormId
	   * @return
	   */
	  public List<String> getOpinionElementLocationNames(Long edocFormId);
	  /**
	   * 返回公文单中，意见绑定元素的名称
	   * @param edocFormId
	   * @param aclAccountId :公文单被授权使用的单位
	   * @return
	   */
	  public List<String> getOpinionElementLocationNames(Long edocFormId,Long aclAccountId);
	  
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId)throws Exception;
	  public List<EdocFormFlowPermBound> findBoundByFormIdAndDomainId(long edocFormId,Long accountId)throws Exception;
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId,String processName,long accountId)throws Exception;
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId, String processName)throws Exception;
	  public List<EdocFormFlowPermBound> findBoundByFormId(long edocFormId, long accountId, String flowPermName)throws Exception;
	  public List<EdocElement> getEdocFormElementByFormIdAndFieldName(long edocFormId, String fieldName);
		
	  public void deleteEdocFormFlowPermBoundByFormId(long edocFormId)throws Exception;
	  public void deleteEdocFormFlowPermBoundByFormIdAndAccountId(long edocFormId,long AccountId)throws Exception;

	  public String ajaxIsReferenced(String id)throws Exception;	  
	  
	  public void saveEdocForm(EdocForm form); 
	  
	  public void saveEdocForms(List<EdocForm> forms); 
		
//	  public List<WorkflowFormFieldVO> getElementByEdocForm(Long formId);
	  
	  /**
	   * 
	   * @param formId
	   * @return
	   */
//	  public List<WorkflowFormFieldVO> getAllElementsByEdocForm(Long formId);

	/**
	 * 方法描述：ajax方法，动态判断是否重名
	 * 
	 */
	public boolean ajaxCheckDuplicatedName(String name, String type, String id);

	/**
	 * 方法描述：ajax方法，动态判断本单位是否存在该文单
	 * 
	 */
	public boolean ajaxCheckIsExistInUnit(String formId);

	public void removeDefaultEdocForm(Long domainId, int edocType);

	public boolean ajaxCheckFormIsIdealy(String edocFormId, String isDefault,String isEnabled);
	
	public void saveEdocFormExtendInfo(EdocFormExtendInfo form);
	public List<EdocForm> getEdocFormByAcl(String domainIds);
	public boolean isExsit(Long formId);
	/**
	 * 取得公文单意见显示设置
	 * @param formId  ： 公文单ID
	 * @param accountId ：单位ID
	 * @return
	 */
	public FormOpinionConfig getEdocOpinionDisplayConfig(Long formId,Long accountId);
	/**
	 * lijl添加,通过参数获取EdocFormExtendInfo对象
	 * @param formId 
	 * @param accountId
	 * @return EdocFormExtendInfo对象
	 */
	public EdocFormExtendInfo getEdocOpinionConfig(Long formId,Long accountId);
	
	public void updateFormContentToDBOnly();
	
	/**
	 * 通过workflowid查询公文模板绑定的文单id
	 * @param workflowId    公文模板对应的工作流id
	 * @param system        是否系统模板
	 * @return              {formId:xxxxxx}
	 */
	public String getFormIdByWorkflowId(String workflowId,boolean isSystem);
	/**
	 *验证当前文单是否属于本单位
	 * @param domainId
	 * @param type  
	 * @return
	 */
	public boolean getFormAccountEdoc(Long domainId,long edocFormId);
	
	/**
     * 单子里面有一个BUG，会清空content, 这里做兼容
     * @Author      : xuqw
     * @Date        : 2015年5月20日上午12:01:01
     * @param ef
     * @param formId
     * @param summary
     * @param user
     * @return
     * @throws EdocException
     */
    public String getFormContentWithFix(EdocForm ef, long formId, EdocSummary summary) throws EdocException;
    
    /**
     * 将文单相关信息写入文件
     * @Author      : xuqw
     * @Date        : 2015年6月15日下午2:07:06
     * @param summaryId
     * @param folder 目标路径,如 D:\test
     * @return
     */
    public boolean writeForm2File(Long summaryId, String folder);
    
    /**
     * 将文单相关信息写入文件(新公文)
     * @Author      : xuqw
     * @Date        : 2018年7月16日
     * @param summaryId
     * @param folder 目标路径,如 D:\test
     * @return
     */
    public boolean writeForm2File2(Long summaryId, String folder);
    
    public Boolean hasFormElement(Long formId, String fieldName);
    
    
    /**
     * 获取单位下表单配置数量
     * 
     * @param formId
     * @param accountId
     * @return
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月21日下午5:00:55
     *
     */
    public int countExtendInfo(Long formId,Long accountId);
    
}
