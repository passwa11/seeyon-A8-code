package com.seeyon.apps.govdoc.manager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormAuthViewFieldBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.form.po.FromPrintBind;
import com.seeyon.ctp.form.po.GovdocFormDefault;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

/**
 * 新公文表单接口
 * @author 唐桂林
 *
 */
public interface GovdocFormManager {

	/**
	 * 设置了动态标题
	 * 目前修改文单时修改所有affair的subject(govdocPubManager.fillSummaryByMapping)
	 * 发送/暂存/提交/触发交换/触发公文流程/触发子流程/转发文/转收文都会用到fillSummaryByMapping
	 * 回退/指定回退生成新的Affair时，设置新的动态标题
	 * @param template
	 * @param summary
	 * @return
	 * @throws BusinessException
	 */
	public String makeSubject(CtpTemplate template, EdocSummary summary) throws BusinessException;
	public String makeSubject(Long summaryId) throws BusinessException;
	/**
	 * 查看公文封装公文单数据
	 * @param summaryVO
	 * @throws BusinessException
	 */
	public void fillSummaryVoByForm(GovdocComponentVO summaryVO) throws BusinessException;
	/**
	 * 获取公文单意见配置
	 * @param formAppId
	 * @return
	 * @throws BusinessException
	 */
	public FormOpinionConfig getFormOpinionConfig(Long formAppId) throws BusinessException;
	
	public void dealCantEditFilesmName(GovdocNewVO govdocVo) throws BusinessException;
	
	/**
	 * 获取文单对象
	 * @param formAppId
	 * @return
	 */
	public FormBean getForm(Long formAppId);
	/**
	 * 文单是否可用
	 * @param moduleId
	 * @return
	 */
	public boolean formIsEnable(Long moduleId);
	@SuppressWarnings("rawtypes")
	public Long getFormViewId(Map para) throws BusinessException;
	public String getGovdocFormViewRight(Map<String, Object> vomMap, CtpAffair affair) throws BusinessException;
	
	public FormDataMasterBean getDataMasterBeanById(Long masterId, FormBean formBean, String[] fields) throws BusinessException, SQLException;     
	public void unlockFormData(Long masterDataId);
	
	 /**
     * 更新表单filesm控件中的值
     * @param filesmName
     * @param contentDataId
     */
	public void updateFilesmValue(String filesmName,Long formAppId,Long contentDataId,String fieldName);

	public FormOptionExtend findByFormId(long formId);
	
	public void updateDataState(CtpAffair affair, EdocSummary summary, ColHandleType type) ;
	
	public Object getMasterFieldValue(Long formId, Long masterId, String fieldName, boolean isShowValue) throws BusinessException, SQLException;

	/**
	 * 根据表单id获取配置信息
	 * @param formId
	 * @return
	 */
	public FormPermissionConfig getConfigByFormId(Long formId);
	
	public FormBean getFormByTemplate4Govdoc(CtpTemplate ctpTemplate) throws BusinessException;
	
	public FormAuthViewBean getAuth(long id);
	
	public FormViewBean getView(long id);
	
	public FormOptionExtend findOptionExtendByFormId(long formId);
	
	public List<FormOptionSort> findOptionSortByFormId(Long id);
	
	public List<FormOptionSort> findBoundByFormId(long formId, String processName, long accountId);
	
	public void saveOrUpdateList(List<FormOptionSort> list);
	
	public void saveOrUpdate(FormOptionExtend govdocFormExtend);
	
	public FormBean getEditingForm();
	
	public Lock getLock(Long masterDataId);
	
	public void removeSessionMasterDataBean(Long formMasterId);
	
	
	public void procDefaultValue(Long formId, Long dataId, Long authId,Long moduleId) throws SQLException, BusinessException;
	
	public List<FormAuthViewFieldBean> getEditFieldByFormAndPermission(Long formId,Long permissionId);
	
	public void insertOrUpdateMasterData(FormDataMasterBean masterData) throws BusinessException, SQLException;
	
	public String findRightIdbyAffairIdOrTemplateId(CtpAffair affair,CtpTemplate template,boolean isFromDocLibByAccountPighole,String wfOperationId) throws BusinessException;
	public String findRightIdbyAffairIdOrTemplateId(CtpAffair affair, Long templateId) throws BusinessException;
	
	public void saveByFormData(EdocSummary summary, CtpAffair affair) throws BusinessException;
	
	public List<Map<String,Object>> selectByFlipInfo(FlipInfo fi, Map<String,Object> params) throws BusinessException;
	
	public GovdocFormDefault getGovdocFormDefaultBySubApp(long AccountId, String subApp, int isQuickSend) throws BusinessException;
	
	public Long getDefaultSendFormId(long nowCategoryId) throws BusinessException;
	
	public void putSessioMasterDataBean(FormBean formBean, FormDataMasterBean cacheMasterData, boolean needAdd2Seeion,boolean needCloneMasterData)  throws BusinessException;
	
	public Long getTemplateIdByFormId(Long formId) throws BusinessException;
	
	public FromPrintBind findPrintMode(Long unitId, Long edocXsnId) throws BusinessException;
	
}
