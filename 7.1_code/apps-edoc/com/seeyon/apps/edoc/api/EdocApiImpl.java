package com.seeyon.apps.edoc.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.edoc.bo.EdocBodyBO;
import com.seeyon.apps.edoc.bo.EdocElementBO;
import com.seeyon.apps.edoc.bo.EdocListCountBO;
import com.seeyon.apps.edoc.bo.EdocOptionBO;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.apps.edoc.bo.EdocSummaryComplexBO;
import com.seeyon.apps.edoc.bo.GovdocTemplateBO;
import com.seeyon.apps.edoc.bo.GovdocTemplateDepAuthBO;
import com.seeyon.apps.edoc.bo.SimpleEdocSummary;
import com.seeyon.apps.edoc.bo.TemplateMarkInfo;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OldExchangeNodePolicyEnum;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.helper.GovdocSwitchHelper;
import com.seeyon.apps.govdoc.manager.GovdocDocTemplateManager;
import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.manager.QwqpManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionExtendManager;
import com.seeyon.apps.govdoc.option.manager.FormOptionSortManager;
import com.seeyon.apps.govdoc.option.util.EdocOptionDisplayUtil;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.apps.govdoc.po.QwqpEdocFormFileRelation;
import com.seeyon.apps.ocip.util.OrgUtil;
import com.seeyon.apps.trustdo.constants.SDKConstants;
import com.seeyon.apps.trustdo.constants.XRDPhoneConstants;
import com.seeyon.apps.trustdo.exceptions.XRDUserException;
import com.seeyon.apps.trustdo.model.sdk.AccountData;
import com.seeyon.apps.trustdo.model.sdk.LoginData;
import com.seeyon.apps.trustdo.model.sdk.Result;
import com.seeyon.apps.trustdo.model.sdk.SDKLoginEventData;
import com.seeyon.apps.trustdo.utils.XRDAppUtils;
import com.seeyon.apps.trustdo.utils.XRDHttpUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.HtmlHandWriteManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.usermessage.Constants.LinkOpenType;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.exceptions.MessageException;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum.EdocV5ListTypeEnum;
import com.seeyon.v3x.edoc.dao.EdocBodyDao;
import com.seeyon.v3x.edoc.dao.EdocFormDao;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocElementCacheManager;
import com.seeyon.v3x.edoc.manager.EdocElementManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocListManager;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMessageHelper;
import com.seeyon.v3x.edoc.manager.EdocObjTeamManager;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.manager.EdocUpgradeManager;
import com.seeyon.v3x.edoc.util.DataTransUtil;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.exchange.dao.EdocRecieveRecordDao;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.enums.EdocExchangeMode.EdocExchangeModeEnum;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import com.seeyon.v3x.exchange.util.Constants;
import com.seeyon.v3x.exchange.util.ExchangeUtil;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

public class EdocApiImpl extends AbstractEdocApi implements EdocApi{
    
    private static final Log LOGGER = CtpLogFactory.getLog(EdocApiImpl.class);
    
    private EdocManager edocManager = null;
    private EdocListManager edocListManager = null;
    private EdocSummaryManager edocSummaryManager = null;
    private EdocElementManager edocElementManager = null;
    private GovdocElementManager govdocElementManager = null;
    private OrgManager orgManager = null;
    private EdocBodyDao edocBodyDao = null;
    private EdocFormDao edocFormDao = null;
    private EdocRecieveRecordDao edocRecieveRecordDao = null;
    private AffairManager affairManager = null;
    private EdocRegisterManager edocRegisterManager = null;
    private UserMessageManager userMessageManager = null;
    private AppLogManager appLogManager = null;
    private RecieveEdocManager recieveEdocManager = null;
    private AttachmentManager attachmentManager = null;
    private GovdocMarkManager govdocMarkManager;
    private FormApi4Cap3 formApi4Cap3;
    private FormOptionExtendManager formOptionExtendManager;
    private FormOptionSortManager formOptionSortManager;
    private HtmlHandWriteManager htmlHandWriteManager;
	private V3xHtmDocumentSignatManager htmSignetManager;
	private PermissionManager permissionManager;
	private GovdocDocTemplateManager govdocDocTemplateManager;
    private GovdocPishiManager govdocPishiManager;
    private EdocElementCacheManager edocElementCacheManager = (EdocElementCacheManager)AppContext.getBean("edocElementCacheManager");
    
	private CollaborationApi collaborationApi;
	private GovdocApi govdocApi;
	private QwqpManager qwqpManager;
	


	@Override
    public String getCurrentNodesInfo(Long summaryId) throws BusinessException {
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false, false);
        return EdocHelper.parseCurrentNodesInfo(summary);
    }
	
	/**
	 * M3公文处理意见
	 */
	public Map<String,Object> getEdocOpinionsMap(EdocOptionBO optionBO) throws NumberFormatException, BusinessException{
		EdocOptionBO retOptionBO = new EdocOptionBO();
		//String openFrom = optionBO.getOpenFrom();
		String formAppid = optionBO.getFormAppid();
		if(Strings.isNotBlank(formAppid)){
			FormBean formBean = formApi4Cap3.getForm(Long.valueOf(formAppid));
			long formId =-1l;
			if(formBean!=null){
		    	formId = formBean.getId();
			}
			if(-1!=formId){
				String nodePermissionPolicy = "";
				String affairId = optionBO.getAffairId();
				CtpAffair affair = null;
				if (Strings.isNotBlank(affairId)) {
					affair = affairManager.get(Long.valueOf(affairId));
				}
				EdocSummary summay = null;
				String summaryId = null;
				if(Strings.isNotBlank(affairId)){
					affair = affairManager.get(Long.parseLong(affairId));
					nodePermissionPolicy = affair.getNodePolicy();
					summaryId = affair.getObjectId().toString();
				
		    		if(Strings.isNotBlank(summaryId)){
		    			summay = edocManager.getEdocSummaryById(Long.parseLong(summaryId),false);
		    			if(null==summay){
		    				summay = new EdocSummary();
		        			summay.setId(Long.parseLong(summaryId));
		        			summay.setFormAppid(formId);
		        			summay.setOrgAccountId(AppContext.currentAccountId());
		        		}else{
		        			summay.setFormAppid(formId);
		        		}
		    		}
				}
	    		FormOptionExtend govdocFormExtend = formOptionExtendManager.findByFormId(formId);
				FormOpinionConfig displayConfig = null;
	    		// 公文单显示格式
	    		if(null!=govdocFormExtend){
		    	    displayConfig = JSONUtil.parseJSONString(govdocFormExtend.getOptionFormatSet(), FormOpinionConfig.class);
	    		}
	    		if(displayConfig == null){
	    		    displayConfig = new FormOpinionConfig();
	    		}
	    		Map<String,EdocOpinionModel> maps = formOptionSortManager.getGovdocOpinion(formId,summay, displayConfig);
	    		List<FormOptionSort> policyList = formOptionSortManager.findBoundByFormId(formId,nodePermissionPolicy, AppContext.currentAccountId() );
	    		String policy = "";
	    		if(Strings.isNotEmpty(policyList)) {
	    			policy = policyList.get(0).getFlowPermName();
	    		}
	    		if(Strings.isBlank(policy)) {
	    			policy = nodePermissionPolicy;
	    		}
	    		retOptionBO.setPolicy(policy);
	            
	            //是否是退回的状态
	            String affairState = EdocOptionDisplayUtil.getAffairReturnState(maps, policy, affair);
	            retOptionBO.setAffairState(affairState);
	            if(govdocFormExtend != null)
	            	retOptionBO.setOptionId(govdocFormExtend.getId());
	            retOptionBO.setOpinionType(displayConfig.getOpinionType());
	            
	            boolean isFromPending = "Pending".equals(optionBO.getFrom());
	            List<V3xHtmDocumentSignature> signatuers = htmSignetManager.findBySummaryIdAndType(summay.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
	            Map<String, Object> strMap = EdocOptionDisplayUtil.convertOpinionToString(maps,displayConfig,affair,isFromPending,signatuers);
	           //按照PC逻辑处理文单签批数据
	            String hwjs=htmlHandWriteManager.getHandWritesString(Long.parseLong(summaryId), AppContext.getCurrentUser().getName());
	            strMap.put("htmlSign", hwjs);
	            return strMap;
			}

		}
		return new HashMap<String, Object>();
	}

	public EdocOptionBO getMainBodyLogic(EdocOptionBO optionBO) throws NumberFormatException, BusinessException{
		EdocOptionBO retOptionBO = new EdocOptionBO();
		String openFrom = optionBO.getOpenFrom();
		String formAppid = optionBO.getFormAppid();
		if(Strings.isNotBlank(formAppid)){
			FormBean formBean = formApi4Cap3.getForm(Long.valueOf(formAppid));
			long formId =-1l;
			if(formBean!=null){
		    	formId = formBean.getId();
			}
			if(-1!=formId){
				String nodePermissionPolicy = "";
				String affairId = optionBO.getAffairId();
				CtpAffair affair = null;
				if (Strings.isNotBlank(affairId)) {
					affair = affairManager.get(Long.valueOf(affairId));
				}
				EdocSummary summay = null;
				String summaryId = null;
				if(Strings.isNotBlank(affairId)){
					affair = affairManager.get(Long.parseLong(affairId));
					nodePermissionPolicy = affair.getNodePolicy();
					summaryId = affair.getObjectId().toString();
				
		    		if(Strings.isNotBlank(summaryId)){
		    			summay = edocManager.getEdocSummaryById(Long.parseLong(summaryId),false);
		    			if(null==summay){
		    				summay = new EdocSummary();
		        			summay.setId(Long.parseLong(summaryId));
		        			summay.setFormAppid(formId);
		        			summay.setOrgAccountId(AppContext.currentAccountId());
		        		}else{
		        			summay.setFormAppid(formId);
		        		}
		    		}
		    		
		            //查找公文单意见元素显示。
		    		FormOptionExtend govdocFormExtend = formOptionExtendManager.findByFormId(formId);
		    		FormOpinionConfig displayConfig = null;
		    		// 公文单显示格式
		    		if(null!=govdocFormExtend){
			    	    displayConfig = JSONUtil.parseJSONString(govdocFormExtend.getOptionFormatSet(), FormOpinionConfig.class);
			    	    if (govdocFormExtend.getOptionFormatSet().indexOf("showAtt") < 0) {
							displayConfig.setShowAtt(true);
						}
		    		}
		    		if(displayConfig == null){
		    		    displayConfig = new FormOpinionConfig();
		    		}
		    		Map<String,EdocOpinionModel> maps = formOptionSortManager.getGovdocOpinion(formId,summay, displayConfig);
		    		List<FormOptionSort> policyList = formOptionSortManager.findBoundByFormId(formId,nodePermissionPolicy, AppContext.currentAccountId() );
		    		String policy = "";
		    		if(Strings.isNotEmpty(policyList)) {
		    			policy = policyList.get(0).getFlowPermName();
		    		}
		    		if(Strings.isBlank(policy)) {
		    			policy = nodePermissionPolicy;
		    		}
		    		retOptionBO.setPolicy(policy);
		            
		            //是否是退回的状态
		            String affairState = EdocOptionDisplayUtil.getAffairReturnState(maps, policy, affair);
		            retOptionBO.setAffairState(affairState);
		            if(govdocFormExtend != null)
		            	retOptionBO.setOptionId(govdocFormExtend.getId());
		            retOptionBO.setOpinionType(displayConfig.getOpinionType());
		            
		            boolean isFromPending = "Pending".equals(optionBO.getFrom());
		            List<V3xHtmDocumentSignature> signatuers = htmSignetManager.findBySummaryIdAndType(summay.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
		            Map<String, Object> strMap = EdocOptionDisplayUtil.convertOpinionToString(maps,displayConfig,affair,isFromPending,signatuers);
		            retOptionBO.setOpinionsJs(EdocOptionDisplayUtil.optionToJs(strMap));
		            
		            //发起人意见
		            
		            retOptionBO.setSenderOpinion(strMap.get("senderOpinionList"));
		            retOptionBO.setSenderOpinionAttStr(strMap.get("senderOpinionAttStr"));
		            
		            List<String> ols = formOptionSortManager.getOpinionElementLocationNames(summay.getFormAppid());
		            String reOption = "";			            
		            for(int i=0;i<ols.size();i++){
		            	if(i == ols.size()-1){
		            	 reOption=reOption+ols.get(i);
		            	}else{
		            	 reOption=reOption+ols.get(i)+",";
		            	}
		            }
		            retOptionBO.setOls(reOption);
		            if(affair != null && affair.getState()==3 && "listPending".equals(openFrom)) {
			            //得到判断是否允许在文单里面编辑意见
			            GovdocOpenManager govdocOpenManager = (GovdocOpenManager)AppContext.getBean("govdocOpenManager");
			            if(govdocOpenManager != null && govdocOpenManager.isAllowCommentInForm()) {
			            	retOptionBO.setAllowCommentInForm("yes");
			            } else {
			            	retOptionBO.setAllowCommentInForm("no");
			            }
		            }
		            String hwjs=htmlHandWriteManager.getHandWritesJs(Long.parseLong(summaryId), AppContext.getCurrentUser().getName(),ols);
		           	retOptionBO.setHwjs(hwjs);
		    	
				}
			}
		}
		
		return retOptionBO;
	}
	
    @Override
    public EdocSummaryBO getEdocSummary(Long summaryId)
            throws BusinessException {
        
        try {
            EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false, false);
            return DataTransUtil.transEdocSummary2BO(summary);
        } catch (Exception e) {
            LOGGER.error("获取公文异常，id=" + summaryId, e);
            throw new BusinessException(e.getMessage(), e);
        }
    }
    
//    public EdocSummaryPO getSummaryPoById(Long summaryId) throws BusinessException {
//    	try {
//            EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false, false);
//            return summary;
//        } catch (Exception e) {
//            LOGGER.error("获取公文异常，id=" + summaryId, e);
//            throw new BusinessException(e.getMessage(), e);
//        }
//    }

    @Override
    public List<EdocBodyBO> getEdocBodys(long summaryId)
            throws BusinessException {
        
        List<EdocBodyBO> bodys = new ArrayList<EdocBodyBO>();
        
        List<EdocBody> edocBodys = edocSummaryManager.findEdocBodys(summaryId);
        if(Strings.isNotEmpty(edocBodys)){
            for(EdocBody body : edocBodys){
                bodys.add(DataTransUtil.transEdocBody2BO(body));
            }
        }
        return bodys;
    }

    @Override
    public List<EdocSummaryComplexBO> findMyPendingEdocByExpectedProcessTime(
            Map<String, Object> tempMap) throws BusinessException {
        
        try {
            List<EdocSummaryModel> summarys = edocManager.getMyEdocDeadlineNotEmpty(tempMap);
            
            List<EdocSummaryComplexBO> cBOs = new ArrayList<EdocSummaryComplexBO>();
            if(Strings.isNotEmpty(summarys)){
                for(EdocSummaryModel s : summarys){
                    cBOs.add(DataTransUtil.transEdocSumary2CBO(s));
                }
            }
            return cBOs;
        } catch (Exception e) {
            LOGGER.error("获取超期公文异常", e);
            throw new BusinessException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isEdocCreateRole(Long memberId, Long accountId, Integer edocType) throws BusinessException {
        try {
            return GovdocRoleHelper.isEdocCreateRole(accountId, memberId, edocType);
        } catch (Exception e) {
            LOGGER.error("判断是否有发起权限异常， memberId=" + memberId + ", accountId=" + accountId + ", edocType=" + edocType, e);
            throw new BusinessException(e.getMessage(), e);
        }
    }

    @Override
    public List<EdocElementBO> findEdocElementsByStatus4Doc(Long accountId, Integer status) throws BusinessException {
        try {
            List<EdocElement> es = govdocElementManager.getEdocElementsByStatusForDoc(accountId, status);
            List<EdocElementBO> bos = new ArrayList<EdocElementBO>();
            if(Strings.isNotEmpty(es)){
                for(EdocElement e : es){
                    bos.add(DataTransUtil.truansEdocElement2BO(e));
                }
            }
            return bos;
        } catch (Exception e) {
            LOGGER.error("通过状态获取单位公文元素异常， accountId=" + accountId + ", status=" + status, e);
            throw new BusinessException(e.getMessage(), e);
        }
    }

    public List<EdocElementBO> findAllEdocElements(Long accountId)  throws BusinessException {
        try {
        	GovdocElementManager govdocElementManager = (GovdocElementManager)AppContext.getBean("govdocElementManager");
        	List<EdocElement> es = govdocElementManager.listElementByAccount(accountId);
            List<EdocElementBO> bos = new ArrayList<EdocElementBO>();
            if(Strings.isNotEmpty(es)){
                for(EdocElement e : es){
                    bos.add(DataTransUtil.truansEdocElement2BO(e));
                }
            }
            return bos;
        } catch (Exception e) {
            LOGGER.error("获取单位公文元素异常， accountId=" + accountId, e);
            throw new BusinessException(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isEdoc(int appKey) throws BusinessException {
        return EdocUtil.isEdocCheckByAppKey(appKey);
    }

    @Override
    public List<CtpEnumItem> findEdocElementEnumItems4Doc(Long elementId)
            throws BusinessException {
        
        return edocElementManager.getDocElementEnumListForDoc(elementId);
    }

    @Override
    public EdocElementBO getEdocElementByFiledName(String fieldName)
            throws BusinessException {
        
        EdocElement e = edocElementManager.getByFieldName(fieldName);
        
        return DataTransUtil.truansEdocElement2BO(e);
        
    }

	@Override
	public List<Integer> findEdocAllAppEnumKeys() throws BusinessException {
		return EdocUtil.getAllEdocApplicationCategoryEnumKey();
	}

	@Override
	public List<AttachmentVO> getAttachmentsBySummaryId(Long summaryId, String attmentList) throws BusinessException {
		
	    if(summaryId == null){
	        throw new BusinessException("edoc summaryId IS NULL");
	    }
	    
	    return edocManager.getAttachmentListBySummaryId(summaryId, attmentList);
	}

	@Override
	public boolean isExchangeRole(Long memberId, Long accountId) throws BusinessException {
		try {
            return GovdocRoleHelper.isExchangeRole(memberId, accountId);
        } catch (Exception e) {
            LOGGER.error("判断是否为交换角色异常， accountId=" + accountId + ", memberId=" + memberId, e);
            throw new BusinessException(e.getMessage(), e);
        }
	}

	@Override
	public EdocSummaryBO getEdocSummaryByProcessId(Long processId) throws BusinessException {
	    
	    try {
            EdocSummary summary = edocManager.getEdocSummaryByProcessId(processId);
            return DataTransUtil.transEdocSummary2BO(summary);
        } catch (Exception e) {
            LOGGER.error("公文公文异常，ProcessId=" + processId, e);
            throw new BusinessException(e.getMessage(), e);
        }
	}


    @Override
    public void receiveEdoc(ReceiveEdocParam param) throws BusinessException {

        
        EdocSummary edocSummary = new EdocSummary();

        // 设置ID
        edocSummary.setId(param.getId());
        edocSummary.setIdIfNew();

        String createUnitName = param.getSendUnitName();
        V3xOrgAccount sendUnit = null;

        // 根据单位名称获取OA单位
        if (createUnitName != null) {

            sendUnit = orgManager.getAccountByName(createUnitName);

            if (sendUnit == null) {
                LOGGER.info("发文单位不存在");
                // return RETURNS_CONTINUE;
                throw new BusinessException(ResourceUtil.getString("edoc.error.get.sendaccount"));//获取发文单位错误
            }

            
            edocSummary.setSendUnit(sendUnit.getName());
            edocSummary.setSendUnitId("Account|" + sendUnit.getId());
            
        }

        // 读取预置的收文单
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("domainId", 1L);
        condition.put("type", 1);
        condition.put("isSystem", Boolean.TRUE);

        List<EdocForm> ls = edocFormDao.findForms(condition, null);
        if (Strings.isNotEmpty(ls)) {
            edocSummary.setFormId(ls.get(0).getId());
        }

        // 将书生xml对象中的值放到默认收文单的SUMMARY对象
        edocSummary.setHasArchive(param.isHasArchive());
        edocSummary.setDeadline(param.getDeadline());
        edocSummary.setEdocType(param.getEdocType());
        edocSummary.setCanTrack(param.getCanTrack());
        edocSummary.setIdentifier(param.getIdentifier());
        edocSummary.setSubject(param.getSubject());
        edocSummary.setKeywords(param.getKeywords());
        edocSummary.setIssuer(param.getIssuer());
        edocSummary.setDocMark(param.getDocMark());
        edocSummary.setCreatePerson(param.getCreatePerson());
        edocSummary.setStartTime(param.getStartTime());
        edocSummary.setCreateTime(param.getCreateTime());
        edocSummary.setState(param.getState());
        edocSummary.setDocType(param.getDocType());
        edocSummary.setSendType(param.getSendType());
        edocSummary.setKeepPeriod(param.getKeepPeriod());
        edocSummary.setSecretLevel(param.getSecretLevel());
        edocSummary.setUrgentLevel(param.getUrgentLevel());

        // 处理主送单位
        if (Strings.isNotBlank(param.getSendTo())) {
            V3xOrgAccount sendToUnits = orgManager.getAccountByName(param.getSendTo());
            if (sendToUnits != null) {
                edocSummary.setSendTo(sendToUnits.getName());
                edocSummary.setSendToId("Account|" + sendToUnits.getId());
            }
        }

        // 抄送单位
        if (Strings.isNotBlank(param.getCopyTo())) {
            edocSummary.setCopyTo(param.getCopyTo());
            String[] cts = param.getCopyTo().split("、");
            StringBuilder ids = new StringBuilder();
            for (int i = 0; i < cts.length; i++) {
                V3xOrgAccount sendCopy = orgManager.getAccountByName(cts[i]);

                if (sendCopy != null) {
                    if (ids.length() > 0) {
                        ids.append(",");
                    }
                    ids.append("Account|" + sendCopy.getId());
                }
            }
            edocSummary.setCopyToId(ids.toString());
        }

        Long agentToId = null;// 被代理人ID
        if (sendUnit != null) {// 发文单位
            List<V3xOrgMember> orgMembers = this.getAccountExchangeUsers(sendUnit.getId());
            V3xOrgMember createUser = null;
            User user = null;

            if (Strings.isNotEmpty(orgMembers)) {
                for (V3xOrgMember v3xOrgMember : orgMembers) {
                    if (v3xOrgMember.getName().equals(param.getCreatePerson())) {
                        createUser = v3xOrgMember;
                        break;
                    }
                }
            }
            if (createUser == null && orgMembers != null) {
                createUser = orgMembers.get(0);
                LOGGER.info("发文人没找到，使用默认发文人：" + createUser.getName());
            }

            // 由于很多接口都是用USER对象，所以在此处根据member对象封装了一个
            // USER对象传递到接口中
            if (createUser != null) {
                user = this.getOrgUser(createUser);
            }
            
            edocSummary.setOrgAccountId(user.getAccountId());

            LOGGER.info("创建EdocSendRecord数据");
            // 构建发文
            EdocSendRecord esr = new EdocSendRecord();
            esr.setNewId();
            if (edocSummary.getSendToId() == null) {
                edocSummary.setSendToId("1");
            }

            esr.setSubject(edocSummary.getSubject());
            esr.setSecretLevel(edocSummary.getSecretLevel());
            esr.setUrgentLevel(edocSummary.getUrgentLevel());
            esr.setDocType(edocSummary.getDocType());
            esr.setDocMark(edocSummary.getDocMark());
            esr.setSendUnit(edocSummary.getSendUnit());
            esr.setSendTime(edocSummary.getCreateTime());

            esr.setSendedTypeIds(edocSummary.getSendToId());
            esr.setSendedNames(edocSummary.getSendTo());
            esr.setSendUserNames(param.getSendUserNames());
            esr.setIssueDate(new Date());
            esr.setEdocId(edocSummary.getId());
            esr.setStatus(Constants.C_iStatus_Sent);
            esr.setContentNo(0);
            String sendUnitId = edocSummary.getSendUnitId();
            if (sendUnitId != null) {
                int o = sendUnitId.indexOf("|");
                if (o > -1) {
                    esr.setExchangeOrgId(Long.valueOf(edocSummary.getSendUnitId().substring(o + 1)));
                } else {
                    esr.setExchangeOrgId(Long.valueOf(edocSummary.getSendUnitId()));
                }
            }
            esr.setCreateTime(edocSummary.getCreateTime());

            LOGGER.info("创建EdocSendDetail数据");

            List<EdocSendDetail> edocSendDetails = new ArrayList<EdocSendDetail>();// EdocSendDetail集合
            // 构建发文详细
            if (edocSummary.getSendTo() != null && !"".equals(edocSummary.getSendTo())) {
                
                String[] sendTos = edocSummary.getSendTo().split("、");
                String[] ids = edocSummary.getSendToId().split("、");
                if (sendTos.length > 0) {
                    for (int i = 0; i < sendTos.length; i++) {
                        EdocSendDetail esd = new EdocSendDetail();
                        esd.setNewId();

                        esd.setSendRecordId(esr.getId());
                        esd.setRecOrgId(getAccountID(ids[i]).toString());
                        esd.setRecOrgName(sendTos[i]);
                        esd.setRecUserName(param.getSendUserNames());
                        edocSendDetails.add(esd);
                    }
                }
            }

            LOGGER.info("创建EdocRecieveRecord数据");
            // 构建收文
            if (edocSendDetails.size() > 0) {

                List<EdocRecieveRecord> edocRecieveRecords = new ArrayList<EdocRecieveRecord>();
                
                // 这个逻辑应该是有问题的
                EdocRecieveRecord lasRecieveRecord = null;

                for (int c = 0; c < edocSendDetails.size(); c++) {

                    EdocSendDetail esd = edocSendDetails.get(c);

                    EdocRecieveRecord edocRecieveRecord = new EdocRecieveRecord();
                    edocRecieveRecord.setNewId();
                    edocRecieveRecord.setReplyId(esd.getId().toString());
                    edocRecieveRecord.setSubject(edocSummary.getSubject());
                    edocRecieveRecord.setSendTo(edocSummary.getSendTo());
                    edocRecieveRecord.setExchangeType(1);
                    edocRecieveRecord.setSecretLevel(edocSummary.getSecretLevel());
                    edocRecieveRecord.setUrgentLevel(edocSummary.getUrgentLevel());
                    edocRecieveRecord.setCopyTo(edocSummary.getCopyTo());
                    edocRecieveRecord.setSendUnitType(1);
                    edocRecieveRecord.setDocType(edocSummary.getDocType());
                    edocRecieveRecord.setDocMark(edocSummary.getDocMark());
                    edocRecieveRecord.setSender(param.getSendUserNames());
                    edocRecieveRecord.setRegisterUserId(user.getId());
                    edocRecieveRecord.setSendUnit(edocSummary.getSendUnit());
                    edocRecieveRecord.setIssueDate(new Date());
                    edocRecieveRecord.setIsRetreat(0);
                    edocRecieveRecord.setEdocId(edocSummary.getId());
                    edocRecieveRecord.setFromInternal(true);
                    edocRecieveRecord.setStatus(Constants.C_iStatus_Recieved);
                    edocRecieveRecord.setRecTime(new Timestamp(System.currentTimeMillis()));
                    edocRecieveRecord.setContentNo(0);
                    edocRecieveRecord.setExchangeOrgId(Long.valueOf(edocSendDetails.get(c).getRecOrgId()));
                    edocRecieveRecord.setCreateTime(edocSummary.getCreateTime());
                    edocRecieveRecord.setEdocId(edocSummary.getId());
                    edocRecieveRecord.setExchangeMode(EdocExchangeModeEnum.sursen.getKey());
                    edocRecieveRecord.setIssuer(param.getIssuer());
                    edocRecieveRecords.add(edocRecieveRecord);

                    lasRecieveRecord = edocRecieveRecord;
                }

                edocRecieveRecordDao.saveAll(edocRecieveRecords);

                if (!GovdocHelper.isG6Version() || !"1".equals(GovdocSwitchHelper.isOpenRegister(user.getAccountId()))) {
                    CtpAffair reAffair = new CtpAffair(); // 登记人代办事项
                    ExchangeUtil.createRegisterAffair(reAffair, user,
                            lasRecieveRecord, edocSummary,
                            StateEnum.edoc_exchange_register.getKey());
                    
                    reAffair.setSummaryState(edocSummary.getState());
                    affairManager.save(reAffair);

                    // 发消息给待登记人
                    sendMessageToRegister(user, agentToId, reAffair, edocSummary);

                    // A8签收时也生成登记数据
                    if (!EdocHelper.isG6Version() && lasRecieveRecord != null) {
                        EdocRegister register = ExchangeUtil.createAutoRegisterData(lasRecieveRecord,
                                        edocSummary,
                                        lasRecieveRecord.getRegisterUserId(),
                                        orgManager);
                        
                        register.setExchangeMode(EdocExchangeModeEnum.sursen.getKey());
                        register.setOrgAccountId(user.getAccountId());
                        register.setDistributerId(user.getId());
                        register.setState(EdocNavigationEnum.EdocDistributeState.Distributed.ordinal());
                        register.setDistributeState(EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal());
                        edocRegisterManager.createEdocRegister(register);
                    }

                } else {// 当G6版本关闭登记开关时，那么签收时，还要生成登记数据，并生成待分发affair
                    CtpAffair reAffair = new CtpAffair(); // 登记人已办事项
                    ExchangeUtil.createRegisterAffair(reAffair, user,
                            lasRecieveRecord, edocSummary,
                            StateEnum.edoc_exchange_registered.getKey());

                    reAffair.setSummaryState(edocSummary.getState());
                    affairManager.save(reAffair);
                    
                    // 原来G6自动登记中有这个
                    if(lasRecieveRecord != null){
                    	appLogManager.insertLog(user, AppLogAction.Edoc_RegEdoc, user.getName(), lasRecieveRecord.getSubject());
                    }
                    // 分发人(当登记开关关闭时，在签收时就可直接选择分发人了)
                    long distributerId = lasRecieveRecord != null ? lasRecieveRecord.getRegisterUserId() : null;
                    
                    // 生成登记数据
                    EdocRegister register = ExchangeUtil.createAutoRegisterData(lasRecieveRecord,
                                    edocSummary, distributerId, orgManager);
                    
                    edocRegisterManager.createEdocRegister(register);
                    
                    if (lasRecieveRecord != null) {
                        lasRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
                        lasRecieveRecord.setRegisterName(register.getRegisterUserName());
                        register.setExchangeMode(EdocExchangeModeEnum.sursen.getKey());
                        recieveEdocManager.update(lasRecieveRecord);
                    }
                    
                    Integer urgentLevel = null;
                    if (Strings.isNotBlank(register.getUrgentLevel()) && NumberUtils.isNumber(register.getUrgentLevel())) {
                        urgentLevel = Integer.parseInt(register.getUrgentLevel());
                    }
                    
                    // v3x_affair表中增加待分发事项
                    distributeAffair(register.getSubject(),register.getDocMark(), register.getSendUnit(), register.getSendUnitId(),
                            register.getRegisterUserId(),
                            register.getDistributerId(), register.getId(),
                            register.getDistributeEdocId(), "create",
                            urgentLevel);
                    
                    // 这个消息是 已成功签收，请分发
                    String key = "edoc.auto.registered";
                    long registerId = register.getId();
                    distributerId = register.getDistributerId();
                    String subject = register.getSubject();
                    String url = "message.link.exchange.distribute";
                    com.seeyon.ctp.common.usermessage.Constants.LinkOpenType linkOpenType = com.seeyon.ctp.common.usermessage.Constants.LinkOpenType.href;
                    String agentApp = "agent";
                    String agentToName = "";
                    String recieverName = orgManager.getMemberById(user.getId()).getName();
                    // 给分发人的代理人发消息
                    sendRegisterMessage(key, subject, recieverName,
                            agentToName, register.getRegisterUserId(),
                            registerId, distributerId, "", "", url,
                            linkOpenType, registerId, "");

                    Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(
                                    ApplicationCategoryEnum.edoc.key(),
                                    register.getDistributerId());
                    
                    if (agentMemberId != null) {
                        sendRegisterMessage(key, subject, recieverName,
                                agentToName, register.getRegisterUserId(),
                                registerId, agentMemberId, "col.agent", "",
                                url, linkOpenType, registerId, agentApp);
                    }
                }
            }
            
            
            LOGGER.info("正文内容保存");
            V3XFile bodyFile = param.getBodyFile();
            Set<EdocBody> edocBodies = new HashSet<EdocBody>();
            if (bodyFile != null) {
                EdocBody body = new EdocBody();

                // 书生交换过来为GD格式，所以此处设置正文类型为GD加密格式，并且问小写
                body.setNewId();
                body.setContentType(param.getBodyType());
                body.setContent(bodyFile.getId().toString());
                body.setContentNo(0);
                body.setEdocId(edocSummary.getId());
                body.setCreateTime(edocSummary.getCreateTime());
                body.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                edocBodyDao.saveOrUpdate(body);
                edocBodies.add(body);
                edocSummary.setEdocBodies(edocBodies);
            }
            
            LOGGER.info("保存附件信息");
            List<Attachment> attachments = param.getAttachments();
            if (Strings.isNotEmpty(attachments)) {
                AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
                attachmentManager.create(attachments);
            }
            
            // 保存summary对象
            edocSummaryManager.saveEdocSummary(edocSummary);
        }
    }
    
    @Override
    public EdocSummaryBO getSummaryFromTemplate(CtpTemplate template) throws BusinessException {

        EdocSummaryBO bo = null;
        
        if(template != null && isEdoc(template.getModuleType())){
            EdocSummary summary = (EdocSummary) XMLCoder.decoder(template.getSummary());
            bo = DataTransUtil.transEdocSummary2BO(summary);
        }
        
        return bo;
    }
    
    private List<V3xOrgMember> getAccountExchangeUsers(Long loginAccout)
            throws BusinessException {
        V3xOrgRole exchangeRole= null;
        if(!EdocHelper.isG6Version()){
            exchangeRole=orgManager.getRoleByName("RecEdoc",loginAccout);
        }else{
            exchangeRole=orgManager.getRoleByName("RegisterEdoc",loginAccout);
        }
        return orgManager.getMembersByRole(loginAccout, exchangeRole.getId());
    }
    
    private User getOrgUser(V3xOrgMember member) {
        User user = null;
        if (member != null) {
            user = new User();
            user.setId(member.getId());
            user.setName(member.getName());
            user.setAccountId(member.getOrgAccountId());
            user.setDepartmentId(member.getOrgDepartmentId());
        }
        return user;
    }
    
 // 获取ID
    private Long getAccountID(String accountId) {
        Long id = -1L;
        if (Strings.isNotBlank(accountId)) {
            String ids[] = accountId.split("\\|");
            if (ids.length == 2) {
                id = Long.parseLong(ids[1]);
            }
        }
        return id;
    }
    
    private void sendMessageToRegister(User user, Long agentToId,
            CtpAffair reAffair, EdocSummary summary) throws MessageException {
        String url = "message.link.exchange.register.pending"
                + Functions.suffix();
        List<String> messageParam = new ArrayList<String>();
        if (EdocHelper.isG6Version()) {
            url = "message.link.exchange.register.govpending";
            /**
             * G6点登记消息，转到登记页面地址
             * http://localhost:8088/seeyon/edocController.do?method
             * =newEdocRegister&comm=create&edocType=1&registerType=1
             * &recieveId=
             * 179946784204145713&edocId=6526010927409218030&sendUnitId
             * =8958087796226541112&listType=registerPending
             */

            messageParam.add(reAffair.getSubObjectId().toString());
            messageParam.add(reAffair.getObjectId().toString());
            // 设置来文单位id
            messageParam.add(String.valueOf(summary.getOrgAccountId()));

        } else {
            /**
             * A8点登记消息，转到收文新建页面地址
             * /edocController.do?method=entryManager&amp;entry
             * =recManager&amp;listType=newEdoc&amp;comm=register&amp;
             * edocType={0}&amp;recieveId={1}&amp;edocId={2}
             * 
             */
            messageParam.add(String
                    .valueOf(EdocEnum.edocType.recEdoc.ordinal()));
            messageParam.add(reAffair.getSubObjectId().toString());
            messageParam.add(reAffair.getObjectId().toString());
        }

        String key = "edoc.register";
        String userName = user.getName();
        // 代理
        LinkOpenType linkOpenType = com.seeyon.ctp.common.usermessage.Constants.LinkOpenType.href;
        if (agentToId != null) {
            String agentToName = "";
            try {
                agentToName = orgManager.getMemberById(agentToId).getName();
            } catch (Exception e) {
                LOGGER.error("获取代理人名字抛出异常", e);
            }
            MessageReceiver receiver = new MessageReceiver(reAffair.getId(),
                    reAffair.getMemberId(), url, linkOpenType, messageParam
                            .get(0), messageParam.get(1), messageParam.get(2),
                    "", reAffair.getAddition());
            try {
                userMessageManager.sendSystemMessage(new MessageContent(key,
                        reAffair.getSubject(), agentToName).add(
                        "edoc.agent.deal", user.getName()),
                        ApplicationCategoryEnum.edocRegister, agentToId,
                        receiver,EdocMessageHelper.getSystemMessageFilterParam(reAffair).key);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }

            Long agentMemberId = MemberAgentBean.getInstance()
                    .getAgentMemberId(ApplicationCategoryEnum.edoc.key(),
                            reAffair.getMemberId());
            if (agentMemberId != null) {
                MessageReceiver agentReceiver = new MessageReceiver(reAffair
                        .getId(), agentMemberId, url, linkOpenType,
                        messageParam.get(0), messageParam.get(1), messageParam
                                .get(2), "agent", reAffair.getAddition());
                try {
                    userMessageManager.sendSystemMessage(
                            new MessageContent(key, reAffair.getSubject(),
                                    agentToName).add("edoc.agent.deal",
                                    user.getName()).add("col.agent"),
                            ApplicationCategoryEnum.edocRegister, agentToId,
                            agentReceiver,EdocMessageHelper.getSystemMessageFilterParam(reAffair).key);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            }
        } else {
            // 非代理
            MessageReceiver receiver = new MessageReceiver(reAffair.getId(),
                    reAffair.getMemberId(), url, linkOpenType, messageParam
                            .get(0), messageParam.get(1), messageParam.get(2),
                    "", reAffair.getAddition());
            try {
                userMessageManager.sendSystemMessage(new MessageContent(key,
                        reAffair.getSubject(), userName),
                        ApplicationCategoryEnum.edocRegister, user.getId(),
                        receiver,EdocMessageHelper.getSystemMessageFilterParam(reAffair).key);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }

            Long agentMemberId = MemberAgentBean.getInstance()
                    .getAgentMemberId(ApplicationCategoryEnum.edoc.key(),
                            reAffair.getMemberId());
            if (agentMemberId != null) {
                MessageReceiver agentReceiver = new MessageReceiver(reAffair
                        .getId(), agentMemberId, url, linkOpenType,
                        messageParam.get(0), messageParam.get(1), messageParam
                                .get(2), "agent", reAffair.getAddition());
                try {
                    userMessageManager.sendSystemMessage(new MessageContent(
                            key, reAffair.getSubject(), userName)
                            .add("col.agent"),
                            ApplicationCategoryEnum.edocRegister, user.getId(),
                            agentReceiver,EdocMessageHelper.getSystemMessageFilterParam(reAffair).key);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }
    
    private void sendRegisterMessage(String key, String subject, String userName, String agentName, long fromUserId, long registerId,
            long toUserId, String colAgent, String agentDeal, String url, com.seeyon.ctp.common.usermessage.Constants.LinkOpenType linkType, 
            long param, String agentApp) throws BusinessException { 
           MessageReceiver receiver = new MessageReceiver(registerId, toUserId, url, linkType, EdocEnum.edocType.recEdoc.ordinal(),
                   String.valueOf(registerId), agentApp);
           MessageContent messageContent = new MessageContent(key, subject, userName);
           messageContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
           messageContent.add(colAgent);
           messageContent.add(agentDeal, agentName);
           userMessageManager.sendSystemMessage(messageContent, ApplicationCategoryEnum.edocRecDistribute, fromUserId, receiver);
           //XX已成功登记公文《XX》，请速进行分发处理’点击消息提醒链接到收文分发页面（用户在个人设置-消息提示设置里关闭收文的提示消息后不弹出该消息）
//         userMessageManager.sendSystemMessage(messageContent, ApplicationCategoryEnum.edocRec, fromUserId, receiver);
       }
    
    /**
     * G6 V1.0 SP1后续功能_签收时自动登记功能  --- 待分发事项存入v3x_affair表
     * @param subject
     * @param senderId
     * @param memberId
     * @param objectId
     * @param subObjectId
     * @param comm
     * @param importantLevel
     * @param bodyType
     * @throws BusinessException 
     */
     private void distributeAffair(String subject, String docMark, String sendUnit, Long sendUnitId, long senderId, long memberId, Long objectId, Long subObjectId, String comm, Integer importantLevel, String...bodyType) throws BusinessException {
            CtpAffair reAffair = null;
            if("delete".equals(comm)) {
                affairManager.deleteByObjectId(ApplicationCategoryEnum.edocRegister, objectId);
            } else if("create".equals(comm)) {//这里注意，登记ojbectId, subState  分发：subObjectId, state
                reAffair = new CtpAffair();
                reAffair.setIdIfNew();
                reAffair.setDelete(false);
                reAffair.setSubject(subject);
                reAffair.setMemberId(memberId);
                reAffair.setSenderId(senderId);         
                reAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
                reAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
                reAffair.setObjectId(objectId);//登记的id
                reAffair.setSubObjectId(subObjectId);//分发的id
                // wangjingjing begin 发文分发 等于 交换中心的 待发送，所以这里的分发一定是 收文分发
                reAffair.setApp(ApplicationCategoryEnum.edoc.getKey());
                reAffair.setSubApp(ApplicationSubCategoryEnum.old_edocRecDistribute.getKey());
                reAffair.setNodePolicy(OldExchangeNodePolicyEnum.oldfenfa.name());
                //reAffair.setApp(31);
                // wangjingjing end
                reAffair.setState(StateEnum.col_pending.key());//分发的状态
                reAffair.setSubState(SubStateEnum.col_normal.key());
                
                //-------------待分发列表没有显示word图标bug 修复  changyi add
                if(bodyType !=null && bodyType.length == 1)
                    reAffair.setBodyType(bodyType[0]);
                reAffair.setImportantLevel(importantLevel);
                
                Map<String, Object> extParam = EdocUtil.createExtParam(docMark, sendUnit, String.valueOf(sendUnitId));
                AffairUtil.setExtProperty(reAffair, extParam);
                
                affairManager.save(reAffair);
            }
        }

	@Override
	public List<SimpleEdocSummary> findSimpleEdocSummarysByIds(List<Long> ids) throws BusinessException {
		
		return edocSummaryManager.findSimpleEdocSummarysByIds(ids);
	}
	
	/**
	 * 
	 * @param memberId
	 * @param edocType
	 * @return
	 * @throws BusinessException
	 */
	public EdocListCountBO getEdocListSize(int type, Map<String, Object> condition) throws BusinessException {
		EdocListCountBO countObj = new  EdocListCountBO();
		try {
			countObj.setEdocType((Integer)condition.get("edocType"));
			
			condition.put("isPagination", Boolean.FALSE);
			
			condition.put("listType", EdocV5ListTypeEnum.listPending.getKey());
			countObj.setListPendingSize(edocListManager.findEdocPendingCount(EdocNavigationEnum.LIST_TYPE_PENDING,  condition));
        	
			condition.put("listType", EdocV5ListTypeEnum.listZcdb.getKey());
        	countObj.setListZcdbSize(edocListManager.findEdocPendingCount(EdocNavigationEnum.LIST_TYPE_PENDING, condition));
        	
        	condition.put("listType", EdocV5ListTypeEnum.listExchangeSent.getKey());
        	countObj.setListWaitSize(edocListManager.findEdocPendingCount(EdocNavigationEnum.LIST_TYPE_SENT, condition));
        	
        	condition.put("listType", EdocV5ListTypeEnum.listWaitSend.getKey());
        	countObj.setListDoneAllSize(edocListManager.findEdocPendingCount(EdocNavigationEnum.LIST_TYPE_WAIT_SEND, condition));

        	condition.put("listType", EdocV5ListTypeEnum.listDoneAll.getKey());
        	countObj.setListSentSize(edocListManager.findEdocPendingCount(EdocNavigationEnum.LIST_TYPE_DONE, condition));
        	
        } catch (BusinessException e) {
        	LOGGER.error("获得发文条数报错!",e);
		}
		return countObj;
	}
	
	public Map<String,String> getEdocListItemValue(long objectId) throws BusinessException{
		EdocSummary summary=edocManager.getEdocSummaryById(objectId, false);
		Map<String, String> data = new HashMap<String,String>();
		if (null != summary) { 
			FormOpinionConfig displayConfig = edocManager.getEdocFormOpinionConfig(summary);
			if (null != displayConfig) {
				Map<String, EdocOpinionModel> opinionModelMap = edocManager.getEdocOpinion(summary, displayConfig);
				if (opinionModelMap != null) {
					int size = 0 ; 
					Set<Entry<String, EdocOpinionModel>> entrys = opinionModelMap.entrySet();
					if (entrys != null && !entrys.isEmpty()) {
						for (Entry<String, EdocOpinionModel> entry : entrys) {
							EdocOpinionModel edocOpinions =  opinionModelMap.get(entry.getKey());
							if (edocOpinions != null && edocOpinions.getOpinions() != null && !edocOpinions.getOpinions().isEmpty()) {
								for (EdocOpinion opinion : edocOpinions.getOpinions()) {
									if (opinion.getOpinionType().intValue() == 1) {
				            			size++;
				            		}
								}
							}
						}
					}
		            data.put("opinions",String.valueOf(size));
				}
			}
		}
		data.put("deadline", summary.getDeadline()==null?"":summary.getDeadline().toString());
		return data;
	}
	
	public Map<String, String>	getEdocListItemValue(Long objectId, Long affairId) throws BusinessException {
		Map<String, String> data = new HashMap<String,String>();
		String replyCount = "0";//回复条数
		String isWorklfowTimeout = "false";//是否流程超期
		String isZcdb = "false";//是否暂存状态
		try {
			EdocSummary summary = edocManager.getEdocSummaryById(objectId, false);
			if (null != summary) {
				FormOpinionConfig displayConfig = edocManager.getEdocFormOpinionConfig(summary);
				if (null != displayConfig) {
					Map<String, EdocOpinionModel> opinionModelMap = edocManager.getEdocOpinion(summary, displayConfig);
					if (opinionModelMap != null) {
						int size = 0 ; 
						Set<Entry<String, EdocOpinionModel>> entrys = opinionModelMap.entrySet();
						if (entrys != null && !entrys.isEmpty()) {
							for (Entry<String, EdocOpinionModel> entry : entrys) {
								EdocOpinionModel edocOpinions =  opinionModelMap.get(entry.getKey());
								if (edocOpinions != null && edocOpinions.getOpinions() != null && !edocOpinions.getOpinions().isEmpty()) {
									for (EdocOpinion opinion : edocOpinions.getOpinions()) {
										if (opinion.getOpinionType().intValue() == 1) {
					            			size++;
					            		}
									}
								}
							}
						}
						replyCount = String.valueOf(size);
					}
				}
				
				java.sql.Timestamp startDate = summary.getCreateTime();
				java.sql.Timestamp finishDate = summary.getCompleteTime();
				Date now = new Date(System.currentTimeMillis());
				if(summary.getDeadline() != null && summary.getDeadline() > 0) {
					Long deadline = summary.getDeadline()*60000;
					if(finishDate == null) {
						if((now.getTime()-startDate.getTime()) > deadline) {
							isWorklfowTimeout = "true";
						}
					} else {
						Long expendTime = finishDate.getTime() - summary.getCreateTime().getTime();
						if((deadline-expendTime) < 0) {
							isWorklfowTimeout = "true";
						}
					}
				}
				
				CtpAffair affair = affairManager.get(affairId);
				if(affair != null) {
					if(affair.getState()!=null && affair.getState().intValue()==StateEnum.col_pending.ordinal()) {
						if(affair.getSubState()!=null && affair.getSubState().intValue()==SubStateEnum.col_pending_ZCDB.ordinal()) {
							isZcdb = "true";
						}
					}
				}	
			}
		} catch(Exception e) {
			LOGGER.info("m3首页获取公文状态出错", e);
		}
		data.put("replyCount", replyCount);
		data.put("isWorklfowTimeout", isWorklfowTimeout);
		data.put("isZcdb", isZcdb);
		return data;
	}

	@Override
	public List<EdocElementBO> getListElementByAccountId(Long accountId) throws BusinessException {
		try {
	
			List<EdocElement> es = edocElementManager.listElementByAccount(accountId);
			
			List<EdocElementBO> bos = new ArrayList<EdocElementBO>();
			if(Strings.isNotEmpty(es)){
				for(EdocElement e : es){
					bos.add(DataTransUtil.truansEdocElement2BO(e));
				}
			}
			return bos;
			
		} catch (Exception e) {
			LOGGER.error("通过单位ID获取单位公文元素异常， accountId=" + accountId, e);
			throw new BusinessException(e.getMessage(), e);
		}
	}
	
	public String getTemplateMarkInfoXmlByParams(User user, Map params) throws BusinessException {
		return govdocMarkManager.getTemplateMarkInfoXmlByParams(user, params);
	}
	
	public String getTemplateSummary(Map params,String type) throws BusinessException {
		String summary="";
		EdocSummary edocSummary  =(EdocSummary) ParamUtil.mapToBean(params, new EdocSummary(),false);
		edocSummary.setId(null);
		//add by rz 2017-12-06[添加对续办模板的保存] start
		if(params.get("customDealWith")!=null && "true".equals(params.get("customDealWith"))
				&& params.get("memberRange")!=null && !"".equals(params.get("memberRange"))){
			edocSummary.setIsCustomDealWith(Boolean.parseBoolean((String) params.get("customDealWith")));
			edocSummary.setCustomDealWithNode((String)params.get("permissionRange"));
			Long id = Long.valueOf((String)params.get("memberRange"));
			V3xOrgMember orgMember=orgManager.getMemberById(id);//人员
			if(orgMember != null){
				edocSummary.setCustomDealWithMemberId(String.valueOf(orgMember.getId()));
				edocSummary.setCustomDealWithMemberName(orgMember.getName());
			}else{
				V3xOrgAccount orgAccount = orgManager.getAccountById(id);//单位
				if(orgAccount != null){
					edocSummary.setCustomDealWithMemberId(String.valueOf(orgAccount.getId()));
					edocSummary.setCustomDealWithMemberName(orgAccount.getName());
				}else{
					V3xOrgDepartment orgDepartment = orgManager.getDepartmentById(id);//部门
					if(orgDepartment != null){
						edocSummary.setCustomDealWithMemberId(String.valueOf(orgDepartment.getId()));
						edocSummary.setCustomDealWithMemberName(orgDepartment.getName());
					}else{
						V3xOrgTeam orgTeam = orgManager.getTeamById(id);//组
						if(orgTeam != null){
							edocSummary.setCustomDealWithMemberId(String.valueOf(orgTeam.getId()));
							edocSummary.setCustomDealWithMemberName(orgTeam.getName());
						}else{
							V3xOrgPost orgPost = orgManager.getPostById(id);//岗位
							if(orgPost != null){
								edocSummary.setCustomDealWithMemberId(String.valueOf(orgPost.getId()));
								edocSummary.setCustomDealWithMemberName(orgPost.getName());
							}else {
								V3xOrgLevel orgLevel = orgManager.getLevelById(id);
								if(orgLevel != null){
									edocSummary.setCustomDealWithMemberId(String.valueOf(orgLevel.getId()));
									edocSummary.setCustomDealWithMemberName(orgLevel.getName());
								}
							}
						}
					}
				}
			}
		}
		//add by rz 2017-12-06[添加对续办模板的保存] end
		//流程超期
		if (params.get("remindIntervalCheckBox") == null || (params.get("deadLineselect") != null && params.get("deadLineselect") == "0")){
			edocSummary.setRemindInterval(-1L);
		}
		if (params.get("processTermTypeCheck") == null || (params.get("deadLineselect") != null && params.get("deadLineselect") == "0")){
			edocSummary.setProcessTermType(-1);
		}
		if (TemplateEnum.Type.template.name().equals(type)) {
			summary = XMLCoder.encoder(edocSummary);
		}else if (TemplateEnum.Type.workflow.name().equals(type)) {
			edocSummary.setCanArchive(false);//允许操作
			edocSummary.setCanAutostopflow(false);
			edocSummary.setCanEdit(false);
			edocSummary.setCanEditAttachment(false);
			edocSummary.setCanForward(false);
			edocSummary.setProcessId(null);
			edocSummary.setImportantLevel(1);//重要程度
			edocSummary.setDeadline(null);//流程期限
			edocSummary.setAdvanceRemind(null);//提前提醒
			edocSummary.setArchiveId(null);//预归档
			edocSummary.setProjectId(null);//关联项目
			summary = XMLCoder.encoder(edocSummary);
		}else if(TemplateEnum.Type.text.name().equals(type)){
			edocSummary.setCanArchive(false);//允许操作
			edocSummary.setCanAutostopflow(false);
			edocSummary.setCanEdit(false);
			edocSummary.setCanEditAttachment(false);
			edocSummary.setCanForward(false);
			edocSummary.setProcessId(null);
			edocSummary.setImportantLevel(1);//重要程度
			edocSummary.setDeadline(null);//流程期限
			edocSummary.setAdvanceRemind(null);//提前提醒
			edocSummary.setArchiveId(null);//预归档
			edocSummary.setProjectId(null);//关联项目
			summary = XMLCoder.encoder(edocSummary);
		}
		return summary;
	}
	
	public String getTemplateMarkInfoByParams(Map params) throws BusinessException {
		return null;	
	}
	
	public void saveGovdocExtendAndSort() throws BusinessException{
		govdocApi.saveGovdocExtendAndSort();
	}
	
	@Override
	public Map<String, Object> getFormMarkDisplayValue(Map<String, Object> params) throws BusinessException {
		Long formDataId = (Long)params.get("formDataId");
		CtpContentAll formContent = GovdocContentHelper.getFormContentByDataIdAndModuleType(4, formDataId);
		if(formContent != null) {
			params.put("summaryId", formContent.getModuleId());
		}
		return govdocMarkManager.getFormMarkDisplayValue(params);
	}
	
	public List<Map<String, Object>> getFormMarkList(Map<String, Object> params)  throws BusinessException {
		return govdocMarkManager.getFormMarkList(params);
	}
	
	public boolean checkFormMarkIsUsed(Map<String, Object> params)  throws BusinessException {
		String summaryId = params.get("summaryId").toString();
		String orgAccountId = params.get("domainId").toString();
		String govdocType = params.get("govdocType").toString();
		String markType = params.get("markType").toString();
		String markstr = params.get("markstr").toString();
		return govdocMarkManager.checkMarkIsUsed(summaryId, orgAccountId, govdocType, null, null, markType, markstr);
	}
	
	public Map<Integer, List<TemplateMarkInfo>> getFormBindMarkList(String markType, Long domainId)  throws BusinessException {
		return govdocMarkManager.getFormBindMarkList(markType, domainId);
	}

	@Override
	public void setEdocSumamryToTemplate(CtpTemplate template, ColSummary colSummary) throws BusinessException {
		govdocApi.setEdocSumamryToTemplate(template, colSummary);
	}

	@Override
	public ColSummary getColSummaryByEdocSummary(String summaryStr) {
		return govdocApi.getColSummaryByEdocSummary(summaryStr);	
	}

	@Override
	public List<String> getEdocPermissions(CtpAffair affair)
			throws NumberFormatException, BusinessException {
		EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(),false);
		Long accountId = summary.getOrgAccountId();     
        String configItem = collaborationApi.getPolicyByAffair(affair).getId();
        String category ="";
        if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_fawen.getKey()){  //发文
      	  category =EnumNameEnum.edoc_new_send_permission_policy.name();
        }else if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()){  //收文
      	  category =EnumNameEnum.edoc_new_rec_permission_policy.name();
        }else if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_qianbao.getKey()){  //签报
      	  category =EnumNameEnum.edoc_new_qianbao_permission_policy.name();
        }else if(affair.getSubApp() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()){  //交换
      	  category=EnumNameEnum.edoc_new_change_permission_policy.name();
		  }
		return permissionManager.getActionList(category, configItem, accountId);
	}

	@Override
	public List<GovdocTemplateBO> findTemplateByType(int officeType)
			throws BusinessException {
		List<EdocDocTemplate> list = govdocDocTemplateManager.findTemplateByType(officeType);
		if(list != null && !list.isEmpty()){
			List<GovdocTemplateBO> tempList = new ArrayList<GovdocTemplateBO>();
			GovdocTemplateBO bo = null;
			for (EdocDocTemplate temp : list) {
				bo = new GovdocTemplateBO();
				bo.setId(temp.getId());
				bo.setName(temp.getName());
				tempList.add(bo);
			}
			return tempList;
		}
		return null;
	}

	@Override
	public String getGovdocContentType(ModuleType moduleType)
			throws BusinessException {
		if(moduleType == null){
			moduleType = ModuleType.collaboration;
		}
		ContentConfig contenTypeAll= ContentConfig.getConfig(moduleType);
		return contenTypeAll.getMainbodyTypeListJSONStr();
	}
	
	@Override
	public List<GovdocTemplateBO> getEdocDocTemplateList(String isFromAdmin, Long orgAccountId,User user,String edocType,String bodyType) throws BusinessException{
		List<GovdocTemplateBO> templateBOs = null;
		List<EdocDocTemplate> list = govdocDocTemplateManager.getEdocDocTemplateList(isFromAdmin, orgAccountId, user, edocType, bodyType);
		if(list != null && !list.isEmpty()){
			templateBOs = new ArrayList<GovdocTemplateBO>();
			GovdocTemplateBO bo = null;
			for (EdocDocTemplate temp : list) {
				bo = new GovdocTemplateBO();
				bo.setId(temp.getId());
				bo.setName(temp.getName());
				templateBOs.add(bo);
			}
			return templateBOs;
		}
		
		return null;
	}

	@Override
	public String[] getOrgTeamPuisneAccount(Long teamId) {
		EdocObjTeamManager edocObjTeamManager = (EdocObjTeamManager) AppContext.getBean("edocObjTeamManager");
      	EdocObjTeam edocObjTeam = edocObjTeamManager.getById(teamId);
      	if(edocObjTeam!=null){
      		String[] ids = edocObjTeam.getSelObjsStr().split(",");
      		return ids;
         }else{
     		return null;
         }
	}
	
	@Override
	public boolean isEdocSendToUnitEdit(Long accountId) {
		try {
			GovdocOpenManager govdocOpenManager = (GovdocOpenManager) AppContext.getBean("govdocOpenManager");
			return govdocOpenManager.isEdocSendToUnitEdit(accountId);
		} catch(Exception e) {
			return false;
		}
	}
	
	/**
     * 公文流程被触发
     * @param oldSubject
     * @param sendType
     * @param templateId
     * @param senderId
     * @param formMasterId
     * @param recieveOrgId
     * @param bodyType
     * @param hasAtts
     * @throws BusinessException
     */
	 public void transSendColl(SendType sendType, 
	    		Long templateId, 
	    		Long senderId,
	    		Long formMasterId, 
	    		Long parentSummaryId,
	    		Long newSumamryId) throws BusinessException {
    	try {
    		GovdocPubManager govdocPubManager = (GovdocPubManager) AppContext.getBean("govdocPubManager");
    		govdocPubManager.transSendColl(sendType, templateId, senderId, formMasterId, parentSummaryId, newSumamryId, null, null, null, null);
		} catch(Exception e) {
			LOGGER.error("公文流程被触发出错", e);
		}
    }
	@Override
	public boolean isPlatformEntity(Object entity) {
		 return OrgUtil.isPlatformEntity((V3xOrgEntity) entity);
	}
	@Override
	public int getOpinionCountByAffair(Long edocId) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String checkLeaderPishi(Long userId, Long affairMemberId) {
		return govdocPishiManager.checkLeaderPishi(userId, affairMemberId);
	}

	@Override
	public String getExchangeReciveContentType(Long recieveId){
		return recieveEdocManager.getEdocExchangeContentType(recieveId);
	}
	
	private static final Map<String,String> edocElementBOFieldNameRelation = new HashMap<String, String>(){
		{
			put("sign_mark","docMark2");
			put("sign_person","signPerson");
			put("receive_unit","receiveUnit");
		}
	};
 	
	@Override
	public String getEdocElementBOFieldName(String poFieldName){
		String boFieldName = "";
		String tempFieldName = edocElementBOFieldNameRelation.get(poFieldName);
		if(tempFieldName != null){
			boFieldName = tempFieldName;
		}
		return boFieldName;
	}
    
    

	@Override
	public int getEdocSummaryGovdocTypeBySummaryID(long summaryId) {
        return edocManager.getGovdocTypeBySummaryId(summaryId);
	}

	@Override
	public EdocElementBO getEdocElementByFieldName4upgrade(String fieldName, Long userAccountId) {
		 EdocElement e = edocElementCacheManager.getEdocElementCacheByFieldName(fieldName, userAccountId);
		 if(null != e){
			 EdocElementBO  b = new EdocElementBO();
			 b.setName(e.getName());
			 b.setMetadataId(e.getMetadataId());
			 return b;
		 }
		 return null;
	}

	@Override
	public int isV5EdocUpgrade() {
		EdocUpgradeManager upgradeManager = (EdocUpgradeManager)AppContext.getBean("edocUpgradeManager");
		int govdocUpgrad = upgradeManager.getUpgradState();
    	return govdocUpgrad;
	}

	@Override
	public List<GovdocTemplateDepAuthBO> findDepAuthListByTemplateId(long templateId) {
		return govdocApi.findDepAuthListByTemplateId(templateId);
	}

	@Override
	public void deleteDepAuthByTemplateAndAuthType(Long id, int authTypeExchange) {
		govdocApi.deleteDepAuthByTemplateAndAuthType(id, authTypeExchange);
	}

	@Override
	public void deleteDepAuthByOrgIdAndTypeId(Long id, int authTypeExchange) {
		govdocApi.deleteDepAuthByOrgIdAndTypeId(id, authTypeExchange);
	}

	@Override
	public void saveDepAuthList(List<GovdocTemplateDepAuthBO> list) {
		govdocApi.saveDepAuthList(list);
	}
	
	@Override
	public String getGovDocElementName(String i18n, String code) {
		if(Strings.isBlank(i18n)){//没有的话 表示扩展元素
			EdocApi edocApi = (EdocApi) AppContext.getBean("edocApi");
			EdocElementBO element = null;
			try {
				element = edocApi.getEdocElementByFiledName(code);
			} catch (BusinessException e) {
				LOGGER.error("根据单位ID获取公文元素出错"+e);
			}
			if(element==null){
				return i18n;
			}else{
				return element.getName();
			}
		}else{
			if((Boolean)SysFlag.sys_isGovVer.getFlag() && "edoc.element.dengji".equals(i18n)){
				//G6版本登记改为分发
                //客开 项目名称： [修改功能：GOVA-2450] 作者：fzc 修改日期：2018-5-18 start
				//this.setI18nStr("edoc.element.fenfa");
                //客开 项目名称： [修改功能：GOVA-2450] 作者：fzc 修改日期：2018-5-18 end
			}
			return ResourceUtil.getString(i18n);
		}
	}
	/**
	 * 
	 * @param accountId 组织管理员 传入0
	 * @return
	 */
	public List<EdocElementBO> getEdocElementsByAccount(Long accountId){
		return edocElementManager.getEdocElementsByAccount(accountId);
	}
	
	public Long getQwqpFile(long formId) {
		List<QwqpEdocFormFileRelation> list = qwqpManager.findByEdocFormId(formId);
		if(CollectionUtils.isNotEmpty(list)){
			return list.get(0).getFileId();
		}
		return null;
	}
	
	@Override
	public String getSDKLoginEvent() {
		String appKeySign = null;
		String result = "";
		String url = SDKConstants.SDK_EVENT_LOGIN_URL;
		try {
			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("appKey", XRDPhoneConstants.APP_KEY);
			try {
				appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
			} catch (XRDUserException e1) {
				LOGGER.error(e1.getMessage(),e1);
				return null;
			}

			NameValuePair[] trstdoData = {
					new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
					new NameValuePair("sign", appKeySign) };

			Result<?> sdkLoginEventResult = XRDHttpUtils.sdkConnectMobileShieldServer(trstdoData, url, SDKLoginEventData.class);
			if (sdkLoginEventResult != null) {
				result = JSONUtil.toJSONString(sdkLoginEventResult);
			}
		} catch(Exception e) {
			return null;
		}
		return result;
	}

	@Override
	public String getSDKKeyId(String m3Account) {
		String appKeySign = null;
		String result = "";
		String url = SDKConstants.SDK_KEYID_URL;
		try {
			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("appKey", XRDPhoneConstants.APP_KEY);
			signMap.put("account", m3Account);
			try {
				appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
			} catch (XRDUserException e1) {
				LOGGER.error(e1.getMessage(),e1);
				return null;
			}

			NameValuePair[] data = {
					new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
					new NameValuePair("account", m3Account),
					new NameValuePair("sign", appKeySign) };

			Result<?> keyIdResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, AccountData.class);
			if (keyIdResult != null) {
				result = JSONUtil.toJSONString(keyIdResult);
			}
		} catch(Exception e) {
			LOGGER.error(e.getMessage(),e);
			return null;
		}
		return result;
	}

	@Override
	public String getSDKUpdateCertEvent(String m3Account) {
		String appKeySign;
		String result = "";
		String url = SDKConstants.SDK_EVENT_UPDATE_CERT_URL;
		try {
			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("appKey", XRDPhoneConstants.APP_KEY);
			signMap.put("account", m3Account);
			try {
				appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
			} catch (XRDUserException e1) {
				LOGGER.error(e1.getMessage(),e1);
				return null;
			}

			NameValuePair[] data = {
					new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
					new NameValuePair("account", m3Account),
					new NameValuePair("sign", appKeySign) };

			Result<?> updateCertEventResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, AccountData.class);
			if (updateCertEventResult != null) {
				result = JSONUtil.toJSONString(updateCertEventResult);
			}
		} catch(Exception e) {
			LOGGER.error(e.getMessage(),e);
			return null;
		}
		return result;
	}

	@Override
	public String getSDKLoginName(String accToken) {
		String appKeySign;
		String result = "";
		String url = SDKConstants.SDK_GET_TOKEN_URL;
		try {
			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("appKey", XRDPhoneConstants.APP_KEY);
			signMap.put("accToken", accToken);
			try {
				appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
			} catch (XRDUserException e1) {
				return null;
			}
			NameValuePair[] data = {
					new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
					new NameValuePair("accToken", accToken),
					new NameValuePair("sign", appKeySign) };

			Result<?> loginResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, LoginData.class);
			if (loginResult != null) {
				result = JSONUtil.toJSONString(loginResult);
			}
		} catch(Exception e) {
			LOGGER.error(e.getMessage(),e);
			return null;
		}
		return result;
	}

	@Override
	public String getServerUrlAndCert() {
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("url", SDKConstants.REQUEST_RUL);
		signMap.put("cert", SDKConstants.SDK_COMMUNICATION_CERT);
		return JSONUtil.toJSONString(signMap);
	}
	
	public void setGovdocApi(GovdocApi govdocApi) {
		this.govdocApi = govdocApi;
	}
	public void setQwqpManager(QwqpManager qwqpManager) {
		this.qwqpManager = qwqpManager;
	}
	public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
	public void setGovdocDocTemplateManager(GovdocDocTemplateManager govdocDocTemplateManager) {
		this.govdocDocTemplateManager = govdocDocTemplateManager;
	}
	public void setEdocListManager(EdocListManager edocListManager) {
		this.edocListManager = edocListManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
    public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
        this.recieveEdocManager = recieveEdocManager;
    }
    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }
    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }
    public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
        this.edocRegisterManager = edocRegisterManager;
    }
    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }
    public void setEdocRecieveRecordDao(
            EdocRecieveRecordDao edocRecieveRecordDao) {
        this.edocRecieveRecordDao = edocRecieveRecordDao;
    }
    public void setEdocBodyDao(EdocBodyDao edocBodyDao) {
		this.edocBodyDao = edocBodyDao;
	}
	public void setEdocFormDao(EdocFormDao edocFormDao) {
        this.edocFormDao = edocFormDao;
    }
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
    public void setEdocManager(EdocManager edocManager) {
        this.edocManager = edocManager;
    }
    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }
    public void setEdocElementManager(EdocElementManager edocElementManager) {
        this.edocElementManager = edocElementManager;
    }
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	public void setFormOptionExtendManager(FormOptionExtendManager formOptionExtendManager) {
		this.formOptionExtendManager = formOptionExtendManager;
	}
	public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
		this.htmSignetManager = htmSignetManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setHtmlHandWriteManager(HtmlHandWriteManager htmlHandWriteManager) {
		this.htmlHandWriteManager = htmlHandWriteManager;
	}
	public void setFormOptionSortManager(FormOptionSortManager formOptionSortManager) {
		this.formOptionSortManager = formOptionSortManager;
	}
	public void setGovdocMarkManager(GovdocMarkManager govdocMarkManager) {
		this.govdocMarkManager = govdocMarkManager;
	}
	public void setGovdocElementManager(GovdocElementManager govdocElementManager) {
		this.govdocElementManager = govdocElementManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
}
