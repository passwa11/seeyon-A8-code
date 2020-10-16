package com.seeyon.apps.govdoc.manager.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionExtendManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionSortManager;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormAuthViewFieldBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.manager.GovdocFormTemplateRelationManager;
import com.seeyon.ctp.form.modules.bindprint.manager.FormPrintBindManager;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.form.po.FromPrintBind;
import com.seeyon.ctp.form.po.GovdocFormDefault;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ocip.exchange.exceptions.BussinessException;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

/**
 * 新公文表单管理类
 * @author 唐桂林
 *
 */
public class GovdocFormManagerImpl implements GovdocFormManager {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocFormManagerImpl.class);
	
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocWorkflowManager govdocWorkflowManager;
	private GovdocCommentManager govdocCommentManager;
	
	private CAPFormManager capFormManager;
	private FormApi4Cap3 formApi4Cap3;
	private FormOptionExtendManager formOptionExtendManager;
	private FormOptionSortManager formOptionSortManager;
	private GovdocFormTemplateRelationManager govdocFormTemplateRelationManager;
	private FormPrintBindManager formPrintBindManager;
	private EnumManager enumManagerNew;
	private TemplateManager templateManager;
	private AffairManager affairManager;
	
	public String makeSubject(Long summaryId) throws BusinessException {
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
		if(summary == null) {
			return "";
		}
		if(summary.getTempleteId() == null) {
			return summary.getSubject();
		}
		CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
		return makeSubject(template, summary);
	}
	
	@Override
	public String makeSubject(CtpTemplate template, EdocSummary summary) throws BusinessException {
		if (template == null || Strings.isBlank(template.getColSubject())) {
			return summary.getSubject();
		}
		String subject = summary.getSubject();
		if (Strings.isNotBlank(template.getColSubject())) {
			String[] policy = govdocWorkflowManager.getStartNodeFormPolicy(template.getWorkflowId());

			if (policy != null && policy[1] != null) {

				LOGGER.info(AppContext.currentUserLoginName() + "，表单接口,协同标题生成：Param:appid:" + summary.getFormAppid() + ",template.getColSubject()："
						+ template.getColSubject() + ",recordId:" + summary.getFormRecordid() + ",policy:" + Long.parseLong(policy[1]));
				subject = formApi4Cap3.getCollSubjuet(summary.getFormAppid(), template.getColSubject(), summary.getFormRecordid(),false);
				LOGGER.info("协同标题生成成功, 协同ID=：" + summary.getId());
				// 转移换行符，标题中不能用换行符
				subject = Strings.toText(subject);
				if (subject.length() > 300) {
					subject = Strings.toText(subject).substring(0, 295) + "...";
				}
			}
		}
		return subject;
	}
	
	@Override
	public void fillSummaryVoByForm(GovdocComponentVO summaryVO) throws BusinessException {
		String rightId = "-1";
		//如果是文档中心打开的、模板设置的 预归档公文，需要取显示明细中的rightId
		if((Strings.isNotBlank(summaryVO.getIsGovArchive()) && "1".equals(summaryVO.getIsGovArchive()))) {
			CtpTemplate template = summaryVO.getTemplate();
			rightId = GovdocHelper.findRightIdByTemplate(template);
		}
		if("-1".equals(rightId)) {
			EdocSummary summary = summaryVO.getSummary();
			CtpAffair affair = summaryVO.getAffair();
			Map<String, Object> vomMap = new HashMap<String, Object>();
			vomMap.put("formAppid", summary.getFormAppid());
			vomMap.put("govdocType", summary.getGovdocType());
			rightId = this.getGovdocFormViewRight(vomMap, affair);
		}
		AppContext.putRequestContext("rightId", rightId);
		addFormRightIdToFormCache(rightId);
	}
	
	/**
     *  将表单ID加入到表单权限缓存中去
     */
	private void addFormRightIdToFormCache(String rightId) {
		if(rightId != null){
        	String[] groupViewAndRightId = rightId.split("[_]");
        	if(groupViewAndRightId!=null && groupViewAndRightId.length != 0){
        		for(String viewAndRightId : groupViewAndRightId){
        			String[] viewAndRightIdArr = viewAndRightId.split("[.]");
        			if(viewAndRightIdArr!= null){
        				if( viewAndRightIdArr.length == 2 && Strings.isNotBlank(viewAndRightIdArr[1])&&!"null".equals(viewAndRightIdArr[1])){
        					capFormManager.addRightId(Long.valueOf(viewAndRightIdArr[1]));
        				}
        				else if(viewAndRightIdArr.length == 1 && Strings.isNotBlank(viewAndRightIdArr[0])&&!"null".equals(viewAndRightIdArr[0])){
        					capFormManager.addRightId(Long.valueOf(viewAndRightIdArr[0]));
        				}
        			}
        		}
        	}
        }
	}
	
	public FormOpinionConfig getFormOpinionConfig(Long formAppId) throws BusinessException {
		FormOptionExtend govdocFormExtend = this.findByFormId(formAppId);
		FormOpinionConfig displayConfig = null;
		// 公文单显示格式
		if(null != govdocFormExtend) {
    	    displayConfig = JSONUtil.parseJSONString(govdocFormExtend.getOptionFormatSet(), FormOpinionConfig.class);
		}
		if(displayConfig == null){
		    displayConfig = new FormOpinionConfig();
		}
		return displayConfig;
	}
	
	/**
     * 如果是非编辑状态的附件说明，则需要更新主表记录
     * @param govdocVo
     * @throws BussinessException
     */
    public void dealCantEditFilesmName(GovdocNewVO govdocVo) throws BusinessException{
        String spanFilesName = govdocVo.getCantEditFilesmName();
        if(Strings.isNotBlank(spanFilesName)){
        	EdocSummary summary = govdocVo.getSummary();
        	summary.setFilesm(spanFilesName);
        	String filesmFieldName = govdocVo.getFilesmFieldName();
        	if(Strings.isNotBlank(spanFilesName) && Strings.isNotBlank(filesmFieldName)){
        	    formApi4Cap3.updateFilesmValue(spanFilesName, summary.getFormAppid(), summary.getFormRecordid(), filesmFieldName);
        	} 	
        }
    }
    
    @Override
    public FormBean getForm(Long formAppId) {
    	return formApi4Cap3.getForm(formAppId);
    }
    
    @Override
    public boolean formIsEnable(Long moduleId){
		CtpContentAll content = GovdocContentHelper.getFormContentByModuleId(moduleId);
		if(content != null) {
			FormBean fb = formApi4Cap3.getForm(content.getContentTemplateId());
			if(fb.isEnabled()) {
				return true;
			}
		}
		return false;
	}
    
    @SuppressWarnings("rawtypes")
	public Long getFormViewId(Map para) throws BusinessException {
    	Long formViewId = null;
    	Long contentTemplateId = Long.valueOf((String) para.get("contentTemplateId"));
    	FormBean formBean =  formApi4Cap3.getForm(contentTemplateId);
    	if(formBean != null) {
    		Long contentRightId = Long.valueOf((String) para.get("contentRightId"));
    		formBean.getAuthViewBeanById(contentRightId).getFormViewId();
    	}
		return formViewId;
    }
    

    public void updateDataState(CtpAffair affair, EdocSummary summary, ColHandleType type) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("subject", summary.getSubject());
			map.put("id", summary.getId());
			map.put("formId", summary.getFormId());
			map.put("formAppId", summary.getFormAppid());
			map.put("formRecordId", summary.getFormRecordid());
			map.put("startMemberId", summary.getStartMemberId());
			map.put("vouch", summary.getVouch());
			map.put("audited", summary.isAudited());
			map.put("state", summary.getState());
			map.put("startMemberId", summary.getStartMemberId());
			//客开 项目名称： 作者：fzc 修改日期：2018-4-9 [修改功能：待触发列表]start
            List<Comment> commentList = new ArrayList<Comment>();
            commentList = govdocCommentManager.getCommentList(ModuleType.edoc, summary.getId());
            if (summary.getFormRecordid() != null && affair.getFormRecordid() == null) {
            	affair.setFormRecordid(summary.getFormRecordid());
                Map<String, Object> columnValue = new HashMap<String, Object>();
                columnValue.put("formRecordid", summary.getFormRecordid());
				affairManager.update(affair.getId(), columnValue );
            }
            formApi4Cap3.updateDataState(map, affair, type, commentList);
            //客开 项目名称： 作者：fzc 修改日期：2018-4-9 [修改功能：待触发列表]end
        } catch (Exception e) {
			LOGGER.error("更新表单相关信息异常", e);
		}
	}
    
    public String getGovdocFormViewRight(Map<String, Object> vomMap, CtpAffair affair) throws BusinessException {
    	return formApi4Cap3.getGovdocFormViewRight(vomMap, affair);
    }
    
    public FormDataMasterBean getDataMasterBeanById(Long masterId, FormBean formBean, String[] fields) throws BusinessException, SQLException {
        return formApi4Cap3.getDataMasterBeanById(masterId, formBean, fields);
    }
    
    public void unlockFormData(Long masterDataId) {
        formApi4Cap3.unlockFormData(masterDataId);
    }
    
    /**
     * 更新表单filesm控件中的值
     * @param filesmName
     * @param contentDataId
     */
	public void updateFilesmValue(String filesmName,Long formAppId,Long contentDataId,String fieldName) {
	    formApi4Cap3.updateFilesmValue(filesmName, formAppId, contentDataId, fieldName);
	}
	
    public Object getMasterFieldValue(Long formId, Long masterId, String fieldName, boolean isShowValue) throws BusinessException, SQLException {
    	return formApi4Cap3.getMasterFieldValue(formId, masterId, fieldName, isShowValue);
    }
    
    /**
	 * 根据表单id获取配置信息
	 * @param formId
	 * @return
	 */
	public FormPermissionConfig getConfigByFormId(Long formId) {
		return formApi4Cap3.getConfigByFormId(formId);
	}

	public FormBean getFormByTemplate4Govdoc(CtpTemplate ctpTemplate) throws BusinessException {
		return formApi4Cap3.getFormByTemplate4Govdoc(ctpTemplate);
	}
	
	public FormOptionExtend findByFormId(long formId) {
		return formOptionExtendManager.findByFormId(formId);
	}
    
	public FormAuthViewBean getAuth(long id) {
		return formApi4Cap3.getAuth(id);
	}
	
	public FormViewBean getView(long id) {
		return formApi4Cap3.getView(id);
	}
	
	public FormOptionExtend findOptionExtendByFormId(long formId) {
		return formOptionExtendManager.findByFormId(formId);
	}
	
	public List<FormOptionSort> findOptionSortByFormId(Long id) {
		return formOptionSortManager.findByFormId(id);
	}
	
	public List<FormOptionSort> findBoundByFormId(long formId, String processName, long accountId) {
		return formOptionSortManager.findBoundByFormId(formId, processName, accountId);
	}
	
	public void saveOrUpdateList(List<FormOptionSort> list) {
		formOptionSortManager.saveOrUpdateList(list);
	}
	
	public void saveOrUpdate(FormOptionExtend govdocFormExtend) {
		formOptionExtendManager.saveOrUpdate(govdocFormExtend);
	}
	
	public FormBean getEditingForm() {
		return formApi4Cap3.getEditingForm();
	}

	public Lock getLock(Long masterDataId) {
		return formApi4Cap3.getLock(masterDataId);
	}
	
	public void removeSessionMasterDataBean(Long formMasterId) {
	    formApi4Cap3.removeSessionMasterDataBean(formMasterId);
	}
	
	public void procDefaultValue(Long formId, Long dataId, Long authId,Long moduleId) throws SQLException, BusinessException {
	    formApi4Cap3.procDefaultValue(formId, dataId, authId, moduleId,null);
	}
	
	public List<FormAuthViewFieldBean> getEditFieldByFormAndPermission(Long formId,Long permissionId) {
		return formApi4Cap3.getEditFieldByFormAndPermission(formId, permissionId);
	}
	
	 public void insertOrUpdateMasterData(FormDataMasterBean masterData) throws BusinessException, SQLException {
	     formApi4Cap3.insertOrUpdateMasterData(masterData);
	 }
	 
	/**
     * 待办，已办直接从Affair中去，如果取不到从工作流中取
     * 已发新建从工作流中取
     * @param affair
     * @param TEMPLATEID
     * @param isFromDocLib :是否从文档中心打开的预归档文件
     * @return 
     * @throws BusinessException
     */
    public String findRightIdbyAffairIdOrTemplateId(CtpAffair affair,CtpTemplate template,boolean isFromDocLibByAccountPighole,String wfOperationId) throws BusinessException {
    	String operationId = "-1";
	        try{
	        	if( isFromDocLibByAccountPighole
	        		&& String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())
	        		&& Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())){
	        		
	        		if(null != template){
						ColSummary tsummary =  (ColSummary)XMLCoder.decoder(template.getSummary());
						if(null != tsummary.getArchiveId()){
							String s = (String)tsummary.getExtraAttr("archiverFormid");
							if(Strings.isNotBlank(s)){
								if(s.endsWith("|")){
									s = s.substring(0,s.length()-1);
								}
								operationId = s;
							}
						}
					}
	        	}
	        	if("-1".equals(operationId)){
	        		if(affair != null 
	                    && (Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState()) 
	                    		|| Integer.valueOf(StateEnum.col_done.getKey()).equals(affair.getState())
	                    		|| Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())
	                    		|| Integer.valueOf(StateEnum.col_pending_repeat_auto_deal.getKey()).equals(affair.getState()))){
	                    
	        			if(affair.getFormOperationId()!=null) {
	        				if(Strings.isNotBlank(affair.getMultiViewStr()) && !"0".equals(affair.getMultiViewStr())){
	        					String _multiViewStr = affair.getMultiViewStr();
	        					_multiViewStr= _multiViewStr.replace("|",".");
	        					_multiViewStr= _multiViewStr.replace(",","|");
	        					operationId = affair.getFormOperationId().toString()+"|"+_multiViewStr;
	        				}else{
	        					operationId = affair.getFormOperationId().toString();
	        				}
	                    }else{
	                        EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
	                        if(summary!= null){
	                        	
	                        	if(Strings.isBlank(wfOperationId)){
	                        		String nodeId = affair.getActivityId() == null? null : String.valueOf( affair.getActivityId());
	                            	String [] arr = govdocWorkflowManager.getNodePolicyInfos(summary.getProcessId(),nodeId );
	                            	operationId = arr[0];
	                        	}else{
	                        		operationId = wfOperationId;
	                        	}
	                        }
	                    }
	                    
	                }else{//待发
	                    if (template != null) {
	                		if(template.getWorkflowId() != null){
	                			
	                			if(Strings.isBlank(wfOperationId)){
	                            	String [] arr = govdocWorkflowManager.getNodePolicyInfosFromTemplate(template.getWorkflowId(), null);
	                            	operationId = arr[0];
	                        	}else{
	                        		operationId = wfOperationId;
	                        	}
	                		}
	                    }
	                }
	        	}
	            
	        }catch(Throwable t){
	            LOGGER.error("",t);
	            throw new BusinessException(t);
	        }
	        operationId =operationId==null ? null : operationId.replaceAll("[|]","_");
	        return operationId;
	}
    
    @Override
	public void putSessioMasterDataBean(FormBean formBean, FormDataMasterBean cacheMasterData, boolean needAdd2Seeion,
			boolean needCloneMasterData) throws BusinessException {
		this.formApi4Cap3.putSessioMasterDataBean(formBean, cacheMasterData, needAdd2Seeion, needCloneMasterData);
	}
	    
    @Override
	public String findRightIdbyAffairIdOrTemplateId(CtpAffair affair, Long templateId) throws BusinessException {
		CtpTemplate template = templateManager.getCtpTemplate(templateId);
		return findRightIdbyAffairIdOrTemplateId(affair, template, false, "");
	}
	    
	public void saveByFormData(EdocSummary summary, CtpAffair affair) throws BusinessException {
		FormBean formBean = this.getForm(summary.getFormAppid());
		if (formBean != null) {
			try {
				FormDataBean formDataBean = this.getDataMasterBeanById(summary.getFormRecordid(), formBean, null);
				List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();
				for (FormFieldBean formFieldBean : formFieldBeans) {
					if (formFieldBean.isMasterField() && Strings.isNotBlank(formFieldBean.getMappingField())) {
						if ("urgent_level".equals(formFieldBean.getMappingField())) {// 紧急程度
							Object value = formDataBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, Object> selectParams = new HashMap<String, Object>();
							selectParams.put("bizModel", true);
							selectParams.put("isFinalChild", formFieldBean.getIsFinalChild());
							selectParams.put("enumId", formFieldBean.getEnumId());
							selectParams.put("enumLevel", formFieldBean.getEnumLevel());
							List<CtpEnumItem> enumList = enumManagerNew.getFormSelectEnumItemList(selectParams);
							for (CtpEnumItem item : enumList) {
								if (item.getId().longValue() == Long.valueOf(value.toString())) {
									summary.setImportantLevel(Integer.valueOf(item.getEnumvalue()));
									affair.setImportantLevel(Integer.valueOf(item.getEnumvalue()));
									break;
								}
							}
						}
					}
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	public List<Map<String,Object>> selectByFlipInfo(FlipInfo fi, Map<String,Object> params) throws BusinessException {
		return formApi4Cap3.selectByFlipInfo(fi, params);
	}
	
	public GovdocFormDefault getGovdocFormDefaultBySubApp(long AccountId, String subApp, int isQuickSend) throws BusinessException {
		return formApi4Cap3.getGovdocFormDefaultBySubApp(AccountId, subApp, isQuickSend);
	}
	
	public Long getDefaultSendFormId(long nowCategoryId) throws BusinessException {
		return formApi4Cap3.getDefaultSendFormId(nowCategoryId);
	}
	
	@Override
	public Long getTemplateIdByFormId(Long formId) throws BusinessException {
		return govdocFormTemplateRelationManager.getTemplateIdByFormId(formId);
	}
	
	@Override
	public FromPrintBind findPrintMode(Long unitId, Long edocXsnId) throws BusinessException {
		return formPrintBindManager.findPrintMode(unitId, edocXsnId);
	}
	
	
	/*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
    public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}	
	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}
	public void setFormOptionExtendManager(FormOptionExtendManager formOptionExtendManager) {
		this.formOptionExtendManager = formOptionExtendManager;
	}
	public void setFormOptionSortManager(FormOptionSortManager formOptionSortManager) {
		this.formOptionSortManager = formOptionSortManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setGovdocFormTemplateRelationManager(GovdocFormTemplateRelationManager govdocFormTemplateRelationManager) {
		this.govdocFormTemplateRelationManager = govdocFormTemplateRelationManager;
	}	
	public void setFormPrintBindManager(FormPrintBindManager formPrintBindManager) {
		this.formPrintBindManager = formPrintBindManager;
	}	
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/

}
