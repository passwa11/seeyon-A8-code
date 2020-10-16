package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.govdoc.bo.GovdocLockObject;
import com.seeyon.apps.govdoc.dao.GovdocExchangeDao;
import com.seeyon.apps.govdoc.helper.GovdocAffairHelper;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocDocTemplateManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocLockManager;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.apps.wpstrans.listener.WpsTransEvent;
import com.seeyon.apps.wpstrans.manager.WpsTransManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

public class GovdocAjaxManagerImpl extends GovdocManagerImpl {

	private Log LOGGER = LogFactory.getLog(GovdocAjaxManagerImpl.class);
	
	private GovdocManager govdocManager;
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocExchangeManager govdocExchangeManager;
	private GovdocDocTemplateManager govdocDocTemplateManager;
	private GovdocFormManager govdocFormManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocPishiManager govdocPishiManager;
	private GovdocLockManager govdocLockManager;
	private WpsTransManager wpsTransManager;
	private AffairManager affairManager;
	private MainbodyManager ctpMainbodyManager;
	private PermissionManager permissionManager;
	private ConfigManager configManager;
	private CollaborationApi collaborationApi;
	private V3xHtmDocumentSignatManager htmSignetManager;
	private GovdocExchangeDao govdocExchangeDao;
	
	@AjaxAccess
    public String checkAffairValid(String affairId) throws NumberFormatException, BusinessException {
		return GovdocAffairHelper.checkAffairValid(affairId, true,"");
	}

	@AjaxAccess
	public String getSenderAffairBySummaryId(Long summaryId) throws BusinessException {
		CtpAffair ctpAffair = affairManager.getSenderAffair(summaryId);
		if(Strings.isBlank(ctpAffair.getProcessId())) {
			Map<String, Object> map = govdocSummaryManager.getSummaryMapById(summaryId);
			if(map.get("processId") != null) {
				ctpAffair.setProcessId((String)map.get("processId"));
			}
		}
		return JSONUtil.toJSONString(ctpAffair);
	}
	
	@AjaxAccess
	public String getSenderAffairsBySummaryIds(String summaryIds) throws BusinessException{
		if(Strings.isBlank(summaryIds)){
			return "";
		}
		String[] tempIds = summaryIds.split(",");
		Long summaryId = null;
		CtpAffair ctpAffair = null;
		Map<String, CtpAffair> affairMap = new HashMap<String, CtpAffair>();
		List<Long> summaryIdList = new ArrayList<Long>();
		for (int i = 0; i < tempIds.length; i++) {
			summaryId = Long.valueOf(tempIds[i]);
			ctpAffair = affairManager.getSenderAffair(summaryId);
			if(Strings.isBlank(ctpAffair.getProcessId())) {
				summaryIdList.add(summaryId);
			}
			affairMap.put(tempIds[i], ctpAffair);
		}
		
		if(Strings.isNotEmpty(summaryIdList)) {
			List<Map<String, Object>> summaryList = govdocSummaryManager.getSummaryListById(summaryIdList);
			for(Map<String, Object> map : summaryList) {
				String id = map.get("id").toString();
				String processId = (String)map.get("processId");
				if(affairMap.get(id) != null) {
					affairMap.get(id).setProcessId(processId);
				}
			}
		}
		
		return JSONUtil.toJSONString(affairMap);
	}
	
	/**
    * 获取当前事项的所有memberId
    * @param String
    * @return
    */
	@AjaxAccess
    public List<Long> getColAllMemberId(String summaryId){
        return GovdocAffairHelper.getColAllMemberId(summaryId);        
    }
	
	/**
	 *  AJAX方法：校验是否允许回退分办
	 */
	@AjaxAccess
	public boolean isFromDistributed(Long summaryId) throws BusinessException {
		return govdocExchangeManager.findDetailByRecSummaryId(summaryId) != null;
	}
	
	@AjaxAccess
	public String getExchangeContentId(Long summaryId) throws BusinessException {
		GovdocExchangeDetail detail = govdocExchangeManager.findDetailByRecSummaryId(summaryId);
		GovdocExchangeMain main = govdocExchangeManager.getGovdocExchangeMainById(detail.getMainId());
		if(main != null){
			return main.getOriginalFileId() !=null ? String.valueOf(main.getOriginalFileId()) : "" ;
		}
		return "";
	}
	/**
	 * 判断是否是ofd类型转公告
	 * @param summaryId
	 * @return
	 * @throws NumberFormatException
	 * @throws BusinessException
	 */
	@AjaxAccess
	public String isOfdBulIssue(String summaryId) throws NumberFormatException, BusinessException {
		CtpContentAll bodyContent = GovdocContentHelper.getFirstBodyContentByModuleId(Long.parseLong(summaryId));	
		// 580增加判断，如果是ofd，暂时不支持转公告
		if (bodyContent != null && bodyContent.getContentType() != null && bodyContent.getContentType() == 46) {
			return "true";
		}
		return "false";
	}
	
	/**
	 * 获取指定分钟数后的日期
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public Long calculateWorkDatetime(Map<String, String> params) throws BusinessException {
		return GovdocHelper.calculateWorkDatetime(params, AppContext.currentAccountId());
	}
	
	@AjaxAccess
	public String transConvertWordToPdf(Map<String, Object> params) {
		String result = "success";
		//Long processId = Long.valueOf((String) params.get("processId"));
		Long summaryId = Long.valueOf((String) params.get("summaryId"));
		//Long affairId = Long.valueOf((String) params.get("affairId"));
		String pdfId = (String) params.get("pdfId");
		try {
			List<CtpContentAll> contents = GovdocContentHelper.getContentListByModuleId(summaryId);
			CtpContentAll pdfContent = null;
			for (CtpContentAll content : contents) {
				if(content.getContentType().intValue() != 20) {
					if(content.getContentType().intValue() == MainbodyType.Pdf.getKey()) {
						pdfContent = content;
						break;
					}
				}
			}
			if(pdfContent == null) {
				pdfContent = new CtpContentAll();
				pdfContent.setIdIfNew();
				pdfContent.setCreateDate(new Date());
				pdfContent.setCreateId(AppContext.currentUserId());
				pdfContent.setModuleTemplateId(0L);
				pdfContent.setContentTemplateId(0L);
			}
//			pdfContent.setTransId(AppContext.currentUserId());
			pdfContent.setModifyDate(new Date());
			pdfContent.setModifyId(AppContext.currentUserId());
			pdfContent.setModuleType(ApplicationCategoryEnum.edoc.key());
			pdfContent.setModuleId(summaryId);			
			pdfContent.setContentType(MainbodyType.Pdf.getKey());
			pdfContent.setContent(pdfId+"");
			pdfContent.setSort(3);
//			pdfContent.setTransId(summaryId);
			ctpMainbodyManager.saveOrUpdateContentAll(pdfContent);
			
			/*processLogManager.insertLog(AppContext.getCurrentUser(),Long.valueOf(processId),
      	    		affairId, ProcessLogAction.processEdoc, String.valueOf(ProcessEdocAction.wordTransPDF.getKey()));*/
		} catch (Exception e) {
			result = ResourceUtil.getString("govdoc.trans2PdfError");
			LOGGER.error(result, e);
			//e.printStackTrace();
		}
		return result;
	}
	
	/**
     * 前端请求保存正文
     * @param map
     * @return
     * @throws BusinessException
     */
	@AjaxAccess
    public boolean saveDealGovdocBody(Map<String,String> map) {
    	try {
			CtpContentAll newContentAll = new CtpContentAll();
			MainbodyType bodyType = MainbodyType.getEnumByKey(Integer.valueOf(map.get("contentType")));
			CtpContentAll contentAlls = GovdocContentHelper.getBodyContentByModuleIdAndType(Long.valueOf(map.get("summaryId")), bodyType);
			if (null != contentAlls) {
				newContentAll = contentAlls;
			}
			if(newContentAll.getId()==null){
				newContentAll.setIdIfNew();
			}
			newContentAll.setModuleId(Long.valueOf(map.get("summaryId")));
			newContentAll.setCreateId(AppContext.getCurrentUser().getId());
			newContentAll.setCreateDate(new Date());
			newContentAll.setContent(map.get("content"));
			newContentAll.setContentType(Integer.valueOf(map.get("contentType")));
			newContentAll.setModuleType(ApplicationCategoryEnum.edoc.getKey()); //公文类型
			newContentAll.setModifyDate(new Date());
			ctpMainbodyManager.saveOrUpdateContentAll(newContentAll);
		} catch (Exception e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return false;
		}
		return true;
    }

	/**
	 * 前端请求修改正文的修改时间
	 * @param map
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
    public boolean updateContentUpdateDate(Map<String,String> map) throws BusinessException{
    	try {
			CtpContentAll newContentAll = new CtpContentAll();
			CtpContentAll contentAlls = GovdocContentHelper.getBodyContentByModuleId(Long.valueOf(map.get("summaryId")));
			if (null != contentAlls) {
				newContentAll = contentAlls;
				newContentAll.setModifyDate(new Date());
				ctpMainbodyManager.saveOrUpdateContentAll(newContentAll);
			}
		} catch (Exception e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return false;
		}
		return true;
    }
	
	/**
	 * 判断是否存在ofd正文
	 * @param fileId
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public String validateOfdContent(String fileId) throws BusinessException  {
		FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
		V3XFile v3xFile = fileManager.getV3XFile(Long.valueOf(fileId));
		if (v3xFile!=null && "application/ofd".equals(v3xFile.getMimeType())) {
			return "success";
		}
		return "no";
	}
	
	/**
	 * 获取当前流程节点的处理说明
	 * @param AFFAIRID 
	 * @param templeteId 协同模版Id
	 * @param PROCESSID 流程ID
	 * @return 处理说明String
	 */
	@AjaxAccess
	public String getDealExplain(Map<String, String> params) {
	    String affairId = (String) params.get("affairId");
	    String templeteId = (String) params.get("templeteId");
	    String processId = (String) params.get("processId");
        String desc = "";
        if(Strings.isBlank(affairId) || Strings.isBlank(templeteId) || Strings.isBlank(processId)) {
            return desc ;
        }
        /*try{
            CtpAffair affair  = affairManager.get(Long.valueOf(affairId));
            BPMProcess process =  wapi.getBPMProcessForM1(processId);
            BPMActivity activity = process.getActivityById(affair.getActivityId().toString());
            desc = activity.getDesc();
            desc= desc.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("\\s", "&nbsp;");
        }catch(Exception e){
            LOG.error("",e);
        }*/
        return desc;
	}

	/**
	 * 检查模板是否可用
	 * @param strID
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public Map<String, String> checkTemplateCanUse(String strID) throws BusinessException {
		Map<String, String> result = new HashMap<String, String>();
		if (Strings.isNotBlank(strID)) {// 模板检查
			Long tId = Long.parseLong(strID);
			User user = AppContext.getCurrentUser();
			boolean outMsg = super.templateManager.isTemplateEnabled(tId, user.getId());
			if (!outMsg) {
				result.put("flag", "cannot");
				return result;
			}
		}
		result.put("flag", "can");
		return result;
	}
	
    /**
    * 判断当前用户是是否有权限
    * @param resourceCode
    * @return
    * @throws BPMException
    */
	@AjaxAccess
	public boolean checkRoot(String resourceCode) throws BPMException {
		return AppContext.getCurrentUser().hasResourceCode(resourceCode);
	}
	
	@AjaxAccess
	public Boolean checkPishiNo(Integer pishiNo, String pishiName,String pishiYear ,Long summaryId) throws BusinessException {
		return govdocPishiManager.checkPishiNo(pishiNo, pishiName, pishiYear, summaryId);
	}
	
	@SuppressWarnings("rawtypes")
	@AjaxAccess
	public Map checkIsCanRepeal(Map params) throws BusinessException {
		return govdocManager.checkIsCanRepeal(params);
	}
	
	/**
	 * AJAX方法：流程撤销
	 * @param repealVO
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public String transRepal(GovdocRepealVO repealVO) throws BusinessException {
		return govdocManager.transRepal(repealVO);
	}
	
	/**
	 * AJAX方法：删除前验证流程是否允许删除
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public String checkCanDelete(Map<String, String> param) throws BusinessException {
		String affairIds = param.get("affairIds");
		String result = "";
		String from = param.get("fromMethod");
		List<CtpAffair> affairList = null;
		if (Strings.isNotBlank(affairIds)) {
			affairList = new ArrayList<CtpAffair>();
			String[] affairs = affairIds.split("[*]");
			for (String affairId : affairs) {
				Long _affairId = new Long(affairId);
				CtpAffair affair = affairManager.get(_affairId);
				affairList.add(affair);
				int state = affair.getState();
				if (state != StateEnum.col_pending.getKey() && state != StateEnum.col_done.getKey() && state != StateEnum.col_sent.getKey()
						&& (state != StateEnum.col_waitSend.getKey() || "listSent".equals(from))) { // 已发里被回退的不能删除
					result = WFComponentUtil.getErrorMsgByAffair(affair);
				}
			}
		}

		if (!"".equals(result)) {
			return result;
		}
		return "success";
	}
	
	@AjaxAccess
	public void deleteAffair(String pageType, long affairId) throws BusinessException {
		govdocManager.deleteAffair(pageType, affairId);
	}
	
	/**
	 * 检查模板是否可用
	 * @param strID
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@AjaxAccess
	public Map checkTemplateCanModifyProcess(String templateId) throws BusinessException {
		Map resMap = new HashMap();
		if(!AppContext.getCurrentUser().isV5Member()){//V-Join人员不允许设置督办
            resMap.put("canSetSupervise","no");
        }
		if(!Strings.isNotBlank(templateId)){
			resMap.put("canModify","yes");
			return resMap;
		}
		CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
		if(null  == ctpTemplate){
			resMap.put("canModify","yes");
			return resMap;
		}else{
			String summary = ctpTemplate.getSummary();
			Boolean canSupervise = ctpTemplate.getCanSupervise();
			if(canSupervise!=null && !canSupervise){
				resMap.put("canSetSupervise","no");
				return resMap;
			}
			EdocSummary sum = (EdocSummary)XMLCoder.decoder(summary);
			Boolean canModify = sum.getCanModify();
			if(canModify!=null && canModify){
				resMap.put("canModify","yes");
				return resMap;
			}else{
				resMap.put("canModify","no");
				return resMap;
			}
		}
	}
	
	/**
	 * Ajax前台页面调用，判断是否存在套红模板
	 * @param edocType 类型（正文/文单）
	 * @param bodyType Officeword:word正文/Wpsword:wps正文
	 * @return "0":没有套红模板，“1”：有套红模板
	 */
	@AjaxAccess
	public String hasEdocDocTemplate(String isFromAdmin, Long orgAccountId, String edocType,String bodyType) {
		return govdocDocTemplateManager.hasEdocDocTemplate(orgAccountId, edocType, bodyType);
	}
	
	@AjaxAccess
	public List<EdocDocTemplate> getEdocDocTemplateList(String isFromAdmin, Long orgAccountId, String edocType, String bodyType) throws BusinessException {
		try {
			User user = AppContext.getCurrentUser();
			return govdocDocTemplateManager.getEdocDocTemplateList(isFromAdmin, orgAccountId, user, edocType, bodyType);
		} catch(Exception e) {
			LOGGER.error("ajax获取套红模板列表异常：",e);
			throw new BusinessException();
		}
	}
	
	@AjaxAccess
	public String transToOfd(Map<String, Object> params) {
		try {
			WpsTransEvent event = new WpsTransEvent(this);
			String summaryId = (String) params.get("summaryId");
			String affairId = (String) params.get("affairId");
			event.setAffair(affairManager.get(Long.valueOf(affairId)));
			event.setSummary(govdocSummaryManager.getSummaryById(Long.valueOf(summaryId)));
			AppContext.getBean("wpsTransManager");
			if(!wpsTransManager.transToOfd(event)){
				return "转换ofd正文异常";
			}
		} catch (Exception e) {
			LOGGER.error("转换ofd正文异常",e);
			return "转换ofd正文异常";
		}
		return "success";
	}
	
	@SuppressWarnings("unchecked")
	@AjaxAccess
    public boolean isShowContentByAffairId(long affairId,long summaryId) throws BusinessException{
    	CtpAffair affair = affairManager.get(affairId);
    	EdocSummary edocSummary = govdocSummaryManager.getSummaryById(Long.valueOf(summaryId));
    	long formId = edocSummary.getFormAppid();
		String category = PermissionFatory.getPermBySubApp(edocSummary.getGovdocType()).getCategorty();
		String configItem = collaborationApi.getPolicyByAffair(affair).getId();
		Permission permission = permissionManager.getPermission(category, configItem, AppContext.getCurrentUser().getAccountId());
		long permissionId = permission.getFlowPermId();
		FormPermissionConfig formPermissionConfig = govdocFormManager.getConfigByFormId(formId);
		if(null != formPermissionConfig){
			Map<String,String> conentShowMap = (Map<String, String>) JSONUtil.parseJSONString(formPermissionConfig.getShowContentConfig());
    		if(null != conentShowMap){
    			for(String key:conentShowMap.keySet()){
	    			if(permissionId == Long.valueOf(key)){
	    				if("false".equals(conentShowMap.get(key))){
	    					return false;
	    				}
	    			}
	    		}
    		}
		}
		return true;
    }
	
	@AjaxAccess
	public void deleteContentHistoryBeforeSubmit(Long summaryId) throws BusinessException {
		List<CtpContentAll> contentList = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.edoc, summaryId);
		if(contentList!=null&&contentList.size()>0){
			//List<CtpContentAll> formContentList = new ArrayList<CtpContentAll>();
			for(CtpContentAll c:contentList){
				if(MainbodyType.FORM.getKey()==c.getContentType()){
					ctpMainbodyManager.onlyDeleteContentById(c.getId());
				}
			}
		}
	}	
	
	/**
	 * 删除对应的公文意见
	 * @param commentId
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public String delComment(String commentId) throws BusinessException{
		return govdocCommentManager.delComment(commentId);
	}
	
	/**
	 * 是否意见不能为空
	 * @param commentId
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public String needFillOpinion(String commentId) throws BusinessException {
		if(Strings.isNotBlank(commentId)){
			try{
				CommentManager commentManager = (CommentManager)AppContext.getBean("ctpCommentManager");
				Comment comment = commentManager.getComment(Long.parseLong(commentId));
				
				CtpAffair affair = affairManager.get(comment.getAffairId());
				EdocSummary summary = govdocSummaryManager.getSummaryById(comment.getModuleId());
				Permission permission = permissionManager.getPermission(
						PermissionFatory.getPermBySubApp(summary.getGovdocType()).getCategorty(),
						collaborationApi.getPolicyByAffair(affair).getId(), summary.getOrgAccountId());
				Integer opinion = permission.getNodePolicy().getOpinionPolicy();
	        	if(opinion != null && opinion.intValue() == 1){
	        		return "needFillOpinion";
	        	}
			}catch(Exception e){
				LOGGER.error("获取<是否必填意见选项>失败!");
			}
		}
		return "";
	}

	/**
	 * 判断公文在当前节点是否有策略
	 * @param orgAccountId
	 * @param govdocType
	 * @param affairId
	 * @return
	 */
	@AjaxAccess
	public String hasTransCollStrategy(Long orgAccountId, Integer govdocType, Long affairId ,String operation) {
		try {
			String category = "";
			String nodepolicy = "";
			if(orgAccountId == null){
				orgAccountId = AppContext.currentAccountId();
			}
			CtpAffair affair = affairManager.get(affairId);
			if(govdocType == 1){
				category = EnumNameEnum.edoc_new_send_permission_policy.name();
			}else if(govdocType == 2){
				category = EnumNameEnum.edoc_new_rec_permission_policy.name();
			}else if(govdocType == 3){
				category = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
			}else if(govdocType == 4){
				category = EnumNameEnum.edoc_new_change_permission_policy.name();
			}
			nodepolicy = affair.getNodePolicy();
			PermissionManager permissionManager=(PermissionManager)AppContext.getBean("permissionManager");
			PermissionVO permissionVo = permissionManager.getPermissionVO(category, nodepolicy, orgAccountId);
			String autoOperation = ",TurnRecEdoc,TransmitBulletin";
			
			String advanced = permissionVo.getAdvancedOperation();//节点已选中的高级操作
			String common = permissionVo.getCommonOperation();//节点已选中的常用操作 
			
			if(nodepolicy != null && ("niwen".equals(nodepolicy) || "dengji".equals(nodepolicy))){
				advanced += autoOperation;
			}
			if(Strings.isNotBlank(advanced)){
				String[] advancedOperation = advanced.split(",");
				for (int i=0;i<advancedOperation.length;i++) {				
					if(operation.equals(advancedOperation[i].trim())){					
						return operation;
					}
				}
			}
			if(Strings.isNotBlank(common)){
				String[] commonOperation = common.split(",");
				for (int i=0;i<commonOperation.length;i++) {
					if(operation.equals(commonOperation[i].trim())){
						return operation;
					}
				}
			}
			return "0";
		} catch(Exception e) {
			LOGGER.error("判断公文在当前收文节点是否有转发文操作", e);
		}
		return null;
	
	}
	
	/**
	 * 获取最终summary正文类型
	 * @param summaryId
	 * @return
	 */
	@AjaxAccess
	//获取最终summary正文类型
	public String getFirstContentType(Long summaryId) {
		if(null != summaryId){
			CtpContentAll govdocContentAll = GovdocContentHelper.getTransBodyContentByModuleId(summaryId);
			if(null != govdocContentAll){
				return MainbodyType.getEnumByKey(govdocContentAll.getContentType()).name();
			}
		}
		return "";
	}

	@AjaxAccess
	public List<V3xHtmDocumentSignature> getSignaturebySummaryId(Long summaryId) {
		Pagination.setMaxResults(50);
		List<V3xHtmDocumentSignature> signatuers = htmSignetManager.findBySummaryIdAndType(summaryId, V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
		return signatuers;
	}
	
	/**
	 * 查找最后一条已办affair
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public CtpAffair getLastDoneAffair(Long summaryId, Long currentUserId) throws BusinessException {
		Map<String,Object> conditions = new HashMap<String,Object>();
		conditions.put("memberId", currentUserId);
		conditions.put("objectId", summaryId);
		conditions.put("state", StateEnum.col_done.getKey());
		conditions.put("app", 4);
		List<CtpAffair> list = affairManager.getByConditions(new FlipInfo(), conditions );
		CtpAffair affair = list.get(0);
		for (int i=1;i<list.size();i++) {
			if(affair.getCompleteTime() == null){
				affair = list.get(i);
				continue;
			}
			if(list.get(i).getCompleteTime() == null){
				continue;
			}
			if(affair.getCompleteTime().getTime() < list.get(i).getCompleteTime().getTime()){
				affair = list.get(i);
			}
		}
		return affair;
	}
	
	@AjaxAccess
	public String verifyUnitExists(String unit,Long summaryId) throws BusinessException {
		String resultNames = "";
		String resultIds = "";
		GovdocExchangeMain main = govdocExchangeManager.findBySummaryId(summaryId, GovdocExchangeMain.EXCHANGE_TYPE_ZHUANSHOUWEN);
		List<GovdocExchangeDetail> details = new ArrayList<GovdocExchangeDetail>();
		if(main != null){
			Map<String, String> conditionMap = new HashMap<String, String>();
			conditionMap.put("mainId", main.getId().toString());
			details = govdocExchangeDao.findGovdocExchangeDetail(new FlipInfo(), conditionMap);
		}

		Map<Long, GovdocExchangeDetail> detailMap = new HashMap<Long, GovdocExchangeDetail>();
		for (GovdocExchangeDetail govdocExchangeDetail : details) {
			detailMap.put(Long.valueOf(govdocExchangeDetail.getRecOrgId()), govdocExchangeDetail);
		}
		String[] unitIds = unit.split(",");
		for (String string : unitIds) {
			Long unitId = Long.valueOf(string.split("\\|")[1]);
			GovdocExchangeDetail govdocExchangeDetail = detailMap.get(unitId);
			if (govdocExchangeDetail != null) {
				if (!"".equals(resultNames)) {
					resultNames += ",";
				}
				if (!"".equals(resultIds)) {
					resultIds += ",";
				}
				resultNames += govdocExchangeDetail.getRecOrgName();
				resultIds += string;
			}
		}
		Map<String, String> map = new HashMap<String, String>();
		//为接下来用户选择否做准备
		if (!"".equals(resultNames)) {
			String[] resultIds2 = resultIds.split(",");

			for (int i = 0; i < unitIds.length; i++) {
				for (int j = 0; j < resultIds2.length; j++) {
					if (resultIds2[j].equals(unitIds[i])) {
						unitIds[i] = null;
					}
				}
			}
			String finalIds = "";
			for (String string : unitIds) {
				if (Strings.isNotBlank(string)) {
					if (!"".equals(finalIds)) {
						finalIds += ",";
					}
					finalIds += string;
				}
			}
			map.put("names", resultNames);
			map.put("finalIds", finalIds);
			return JSONUtil.toJSONString(map);
		}
		return null;
	}
	
	
	/**
     * AJAX方法：取回前验证是否已分送出去
     * @throws BusinessException
     */
	@AjaxAccess
	public boolean hasDistributed(Long summaryId) throws BusinessException {
		return govdocExchangeManager.findBySummaryId(summaryId, null) != null;
	}
	
	@AjaxAccess
	public Map<String, Object> transTakeBack(Map<String, Object> ma) throws BusinessException {
		return govdocManager.transTakeBack(ma);
	}
	
	@AjaxAccess
	public GovdocLockObject addFormLock(Long affairId) throws BusinessException {
		return govdocLockManager.formAddLock(affairId);
	}
	
	@AjaxAccess
	public void delFormLock(Map<String, String> param) throws BusinessException {
		govdocLockManager.ajaxColDelLock(param);
	}
	
	@AjaxAccess
	public void bindPdfContent(String key, String value1, String value2) throws BusinessException {
		value1 = value1.trim();
		if ("serialNo".equals(key)) {
			configManager.deleteConfigItem("pdfContentSet", "serialNo");
			configManager.addConfigItem("pdfContentSet", "serialNo", value1, 1L);
		}else if ("users".equals(key)) {
			ConfigItem item = configManager.getConfigItem("pdfContentSet", "authUsers");
			if (item == null) {
				configManager.addConfigItem("pdfContentSet", "authUsers", "", 1L);
				item = configManager.getConfigItem("pdfContentSet", "authUsers");
			}
			item.setExtConfigValue(value1);
			item.setConfigValue(value2);
			configManager.updateConfigItem(item);
		}
	}
	
	/**
	 * 已发中修改附件，同步附件说明的值
	 * @param filesmName
	 * @param summaryId
	 * @param filesmFileId
	 * @throws BusinessException
	 */
	@AjaxAccess
	public void senderUpdateFilesm(String filesmName, Long summaryId, String filesmFileId) throws BusinessException {
		if(summaryId!=null && Strings.isNotBlank(filesmName) && Strings.isNotBlank(filesmFileId)){
			EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
			//更新主表记录
			govdocFormManager.updateFilesmValue(filesmName, summary.getFormAppid(), summary.getFormRecordid(), filesmFileId);
			filesmName = filesmName.replaceAll("\n", "<br>");
			summary.setFilesm(filesmName);
			govdocSummaryManager.saveOrUpdateEdocSummary(summary);
		}	
	}
	
	/**
	 * 记录正文修改日志
	 * @param map
	 */
	@AjaxAccess
	public void recoidChangeWord(Map<String, String> map) {
		govdocManager.recoidChangeWord(map);
	}
	
	/**
     * AJAX方法：回退分办
     * @param summaryID
     * @param affairID
     * @return
     * @throws BusinessException
     */
	@AjaxAccess
	public String takeDeal(String summaryID, String affairID) throws BusinessException {
		return govdocManager.takeDeal(summaryID, affairID);
	}
	
	/**
	 * 根据策略名称，检查公文是否有对于的权限
	 * @param affairId 公文id列表
	 * @param actionKey 策略名称 如撤销(Cancel)\修改正文(Edit)
	 * @return 撤销结果信息
	 * @throws BusinessException
	 */
	@AjaxAccess
	public String getRightByAction(String affairId, String actionKey) throws BusinessException {
		return govdocManager.getRightByAction(affairId, actionKey);
	}
	
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public void setGovdocDocTemplateManager(GovdocDocTemplateManager govdocDocTemplateManager) {
		this.govdocDocTemplateManager = govdocDocTemplateManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setWpsTransManager(WpsTransManager wpsTransManager) {
		this.wpsTransManager = wpsTransManager;
	}
	public void setGovdocExchangeManager(GovdocExchangeManager govdocExchangeManager) {
		this.govdocExchangeManager = govdocExchangeManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
	public void setGovdocLockManager(GovdocLockManager govdocLockManager) {
		this.govdocLockManager = govdocLockManager;
	}
	public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
		this.htmSignetManager = htmSignetManager;
	}
	public void setGovdocManager(GovdocManager govdocManager) {
		this.govdocManager = govdocManager;
	}
	public void setGovdocExchangeDao(GovdocExchangeDao govdocExchangeDao) {
		this.govdocExchangeDao = govdocExchangeDao;
	}
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
}
