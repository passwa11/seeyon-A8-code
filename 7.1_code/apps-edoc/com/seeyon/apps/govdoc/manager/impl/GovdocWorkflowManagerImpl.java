package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocWorkflowTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocAffairHelper;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.listener.GovdocWorkflowEventListener;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.WFInfo;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.vo.BPMSeeyonPolicyVO;
import com.seeyon.ctp.workflow.wapi.RunCaseResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * 新公文流程相关管理类
 * 
 * @author 唐桂林
 * 
 */
public class GovdocWorkflowManagerImpl implements GovdocWorkflowManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocWorkflowManagerImpl.class);

	private GovdocSummaryManager govdocSummaryManager;
	private GovdocLogManager govdocLogManager;
	private WorkflowApiManager wapi;
	private TemplateManager templateManager;
	private PermissionManager permissionManager;
	private OrgManager orgManager;
	private AffairManager affairManager;

	/************************* 流程操作相关 11111 start ********************************/
	/**
     * 运行流程,当时子流程时
     * @param sendtype
     * @param user
     * @param summary
     * @return
     * @throws Exception
     */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String,String> runcase(EdocConstant.SendType sendtype, User user, EdocSummary summary, CtpTemplate template) throws BusinessException {
    	Map<String,String> wfdef = new HashMap<String, String>();
        if (AppContext.getRawRequest() != null) {//流程触发时，流程参数不从前端获取
            wfdef = ParamUtil.getJsonDomain("workflow_definition");
    	}
        String process_xml = wfdef.get("process_xml");
        String process_id = wfdef.get("processId");
        String popNodeSubProcessJson = wfdef.get("workflow_newflow_input");
        String selectedPeoplesOfNodes = wfdef.get("workflow_node_peoples_input");
        String conditionsOfNodes = wfdef.get("workflow_node_condition_input");
        return runcase( process_id, process_xml, popNodeSubProcessJson, selectedPeoplesOfNodes, conditionsOfNodes, user, summary, sendtype, template, false);
    }
	@Override
	public Map<String,String> runcase(String process_id, String process_xml, String popNodeSubProcessJson, String selectedPeoplesOfNodes, String conditionsOfNodes,
			User user, EdocSummary summary, EdocConstant.SendType sendtype, CtpTemplate template,boolean addFirstNode) throws BusinessException {
		Long flowPermAccountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary);
		
		AffairData affairData = GovdocAffairHelper.getNewAffairData(summary);
		
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setAppName(ModuleType.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setDebugMode(false);
		if (Strings.isNotBlank(process_id) && !"-1".equals(process_id)) {
			context.setProcessId(process_id);
		}
		context.setAddFirstNode(addFirstNode);
		context.setProcessXml(process_xml);
		context.setStartUserId(String.valueOf(user.getId()));
		context.setCurrentUserId(String.valueOf(user.getId()));
		context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
		context.setStartUserName(user.getName());
		context.setStartAccountId(String.valueOf(user.getLoginAccount()));
		context.setStartAccountName("seeyon");
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, summary.getStartMemberId());
		context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
		context.setBusinessData("bizObject", summary);
		context.setBusinessData(GovdocWorkflowEventListener.EDOCSUMMARY_CONSTANT, summary);
		context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, GovdocWorkflowEventListener.ASSIGN);
		context.setBusinessData(EdocConstant.FlowPermAccountId, flowPermAccountId);
		context.setPopNodeSubProcessJson(popNodeSubProcessJson);
		context.setSelectedPeoplesOfNodes(selectedPeoplesOfNodes);
		context.setConditionsOfNodes(conditionsOfNodes);
		context.setMastrid("" + affairData.getFormRecordId());
		context.setFormData("" + affairData.getFormAppId());
		context.setAppObject(summary);
		context.setVersion("2.0");
		// 触发子流程和新流程, 要让工作流增加一个发起者节点
		if (sendtype == EdocConstant.SendType.auto || sendtype == EdocConstant.SendType.child) {
			context.setAddFirstNode(true);
			context.setSelectedPeoplesOfNodes("{\"nodeAdditon\" : []}");
		} else {
			context.setSelectedPeoplesOfNodes(selectedPeoplesOfNodes);
		}
		if(sendtype == EdocConstant.SendType.child){
			boolean isSubProcessSkipFSender=AppContext.getThreadContext(EventDataContext.CTP_WORKFLOW_SUBPROCESS_SKIP_FSENDER)==null ? false:
				(Boolean)AppContext.getThreadContext(EventDataContext.CTP_WORKFLOW_SUBPROCESS_SKIP_FSENDER);
			context.setCanSubProcessSkipFSender(isSubProcessSkipFSender);
		}
		
		// 指定回退到发起者后发起者再发送
		if (affairData.getCaseId() != null) {
			context.setCurrentActivityId("start");
			context.setCaseId(affairData.getCaseId());
		}
		int moduleKey = ModuleType.edoc.getKey();
		if(CurrentUser.get() == null){
			AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
		}
		/*String[] result;*/
		RunCaseResult runCaseResult = null;
		if (null != template && null != template.getWorkflowId()) {
			context.setProcessTemplateId(String.valueOf(template.getWorkflowId()));
			runCaseResult = wapi.transRunCaseFromTemplate(context);
			this.updatePermissinRef4Govdoc(moduleKey, summary.getGovdocType(), process_xml, "-1", String.valueOf(template.getWorkflowId()), template.getOrgAccountId());
		} else {// 缓存caseId
			runCaseResult = wapi.transRunCase(context);
			this.updatePermissinRef4Govdoc(moduleKey, summary.getGovdocType() , process_xml, process_id, "-1", summary.getOrgAccountId());
		}
		Map<String,String> wfRetMap =  new HashMap<String,String>();
        wfRetMap.put("caseId", runCaseResult.getCaseId());
        wfRetMap.put("prcocessId", runCaseResult.getProcessId());
        wfRetMap.put("nextMembers", runCaseResult.getNextMembers());
        wfRetMap.put("isTriggerNewFlow", runCaseResult.getHasSubprocess()); //是否触发了子流程。
        wfRetMap.put("RelationDataId", Strings.isNotBlank(runCaseResult.getStartDR()) && Strings.isDigits(runCaseResult.getStartDR()) ? runCaseResult.getStartDR() :  "");
        return wfRetMap;
	}
	@Override
	public WFInfo draftRuncase(GovdocNewVO newVo) throws BusinessException {
		WFInfo wfInfo = new WFInfo();
		wfInfo.setProcessId(workflowDraf());
		wfInfo.setCaseId(-1L);
		wfInfo.setNextNodeNames(null);
		
		newVo.getSummary().setCaseId(wfInfo.getCaseId());
		newVo.getSummary().setProcessId(wfInfo.getProcessId());
		
		//添加调用模板进入缓存
		EdocSummary summary = newVo.getSummary();
		if (summary.getGovdocType() != ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
			if (null != newVo.getCurTemId()) {
				templateManager.updateTempleteRecent(newVo.getCurrentUser().getId(),  newVo.getCurTemId());
			} else if (summary.getTempleteId() != null) {
				templateManager.updateTempleteRecent(newVo.getCurrentUser().getId(),  summary.getTempleteId());
			}
		}
		return wfInfo;
	}
	@Override
	public WFInfo sendRuncase(SendType sendType, GovdocNewVO newVo) throws BusinessException {
		// 发起流程
		EdocSummary summary = newVo.getSummary();
		//获取发送前的processId
		String oldProcessId = summary.getProcessId();
		
		RunCaseResult runCaseResult = workflowNew(newVo, newVo.getCurrentUser(), GovdocAffairHelper.getNewAffairData(summary), summary);
		
		WFInfo wfInfo = new WFInfo();
		wfInfo.setCaseId(Long.valueOf(runCaseResult.getCaseId()));
		wfInfo.setProcessId(runCaseResult.getProcessId());
		wfInfo.setNextNodeNames(runCaseResult.getNextMembers().split("[,]"));// 设置下一个节点的节点名和节点权限
		wfInfo.setProcessLogDetails(wfInfo.getProcessLogDetails());
		newVo.setWfInfo(wfInfo);
		
		if(SendType.resend.ordinal() != sendType.ordinal()) {
			if (Strings.isNotBlank(oldProcessId) && Strings.isNotBlank(summary.getProcessId()) && !summary.getProcessId().equals(oldProcessId)) {
				govdocLogManager.updateByHQL(Long.valueOf(summary.getProcessId()), Long.parseLong(oldProcessId));
			}	
		}
		
		newVo.getSummary().setCaseId(wfInfo.getCaseId());
		newVo.getSummary().setProcessId(wfInfo.getProcessId());
		
		List<CtpAffair> affairs = affairManager.getAffairs(newVo.getSummary().getId());
		for (CtpAffair ctpAffair : affairs) {
			ctpAffair.setProcessId(wfInfo.getProcessId());
			ctpAffair.setCaseId(wfInfo.getCaseId());
		}
		affairManager.updateAffairs(affairs);
		
		//添加调用模板进入缓存
		if (summary.getGovdocType() != ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
			if (null != newVo.getCurTemId()) {
				templateManager.updateTempleteRecent(newVo.getCurrentUser().getId(),  newVo.getCurTemId());
			} else if (summary.getTempleteId() != null) {
				templateManager.updateTempleteRecent(newVo.getCurrentUser().getId(),  summary.getTempleteId());
			}
		}
		
		return wfInfo;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public WFInfo zcdbRuncase(ColHandleType handleType, GovdocDealVO dealVo) throws BusinessException {
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(dealVo.getSummary());
		
		AffairData affairData = GovdocAffairHelper.getZcdbAffairData(dealVo.getSummary());
		affairData.setMemberId(dealVo.getAffair().getMemberId());// 事项接收人id，用于工作流回调中处理代理
		
		
		
		Map<String, Object> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		String subOjectId = (String) wfdef.get("subObjectId");
		Long itemId = Long.valueOf(subOjectId);
		workflowWait(dealVo.getCurrentUser(), affairData, itemId, null, wfdef, dealVo);
		 
		WFInfo wfInfo = new WFInfo();
		wfInfo.setCaseId(dealVo.getCaseId());
		wfInfo.setProcessId(dealVo.getProcessId());
		wfInfo.setNextNodeNames(null);
		
		return wfInfo;
	}
	
	@Override
	public WFInfo finishRuncase(ColHandleType handleType, GovdocDealVO dealVo) throws BusinessException {
		EdocSummary summary = dealVo.getSummary();
		CtpAffair affair = dealVo.getAffair();
		
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(dealVo.getSummary());
		
		AffairData affairData = GovdocAffairHelper.getZcdbAffairData(dealVo.getSummary());
		affairData.setSmsAlert(dealVo.isSmsAlert());
		affairData.setMemberId(dealVo.getAffair().getMemberId());// 事项接收人id，用于工作流回调中处理代理
		WFInfo wfInfo = workflowFinish(dealVo.getComment(), affairData, affair.getSubObjectId(), affair, summary, dealVo);
		return wfInfo;
	}
	
	/**
	 * 保存草稿
	 */
	@SuppressWarnings("unchecked")
	private String workflowDraf() throws BusinessException {
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		String processId = wfdef.get("processId");
		String processXml = wfdef.get("process_xml");
		String moduleType = wfdef.get("moduleType");

		// H5参数处理：从流程临时表中取出流程变更之后的processXml
		processXml = wapi.getTempProcessXml(processXml);

		if (Strings.isBlank(processXml) && (Strings.isBlank(processId) || (Strings.isNotBlank(processId) && "-1".equals(processId))))
			return "";
		String result = wapi.saveProcessXmlDraf(processId, processXml, moduleType);
		updatePermissinRef(Integer.parseInt(moduleType), processXml, processId, "", AppContext.currentAccountId());
		return result;
	}

	@SuppressWarnings("unchecked")
	private RunCaseResult workflowNew(GovdocNewVO newVo, User user, AffairData affairData, Object wfContextBizObject) throws BusinessException {
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		String process_xml = wfdef.get("process_xml");
		String processId = wfdef.get("processId");
		String caseId = wfdef.get("caseId");
		String popNodeSubProcessJson = wfdef.get("workflow_newflow_input");
		String selectedPeoplesOfNodes = wfdef.get("workflow_node_peoples_input");
		String conditionsOfNodes = wfdef.get("workflow_node_condition_input");
	//	String conditionsOfNodes = "";//新建的时候设置为空字符串 --xucb 目的是修复撤销后nodepolicy始终是上一节点权限
		Integer moduleType = ParamUtil.getInt(wfdef, "moduleType", ModuleType.edoc.getKey());

		// H5参数处理：从流程临时表中取出流程变更之后的processXml
		process_xml = wapi.getTempProcessXml(process_xml);
		
		return workflowNew(newVo, user, affairData, wfContextBizObject, process_xml, moduleType, processId, caseId, popNodeSubProcessJson, selectedPeoplesOfNodes, conditionsOfNodes);
	}
	private RunCaseResult workflowNew(GovdocNewVO newVo, User user, AffairData affairData, Object wfContextBizObject, String process_xml, Integer moduleType, String process_id, String caseId2, String popNodeSubProcessJson, String selectedPeoplesOfNodes, String conditionsOfNodes) throws BusinessException {
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setAppName(ModuleType.getEnumByKey(moduleType).name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setDebugMode(false);
		if (process_id != null && !"".equals(process_id.trim()) && !"-1".equals(process_id)) {
			context.setProcessId(process_id);
		}
		context.setProcessXml(process_xml);
		context.setStartUserId(String.valueOf(user.getId()));
		context.setStartUserName(user.getName());
		context.setStartAccountId(String.valueOf(user.getLoginAccount()));
		context.setStartAccountName("seeyon");
		context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
		context.setBusinessData("bizObject", wfContextBizObject);
		context.setBusinessData(GovdocWorkflowEventListener.EDOCSUMMARY_CONSTANT, wfContextBizObject);
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, user.getId());
		context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, affairData.getBusinessData().get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE));
		context.setPopNodeSubProcessJson(popNodeSubProcessJson);
		context.setSelectedPeoplesOfNodes(selectedPeoplesOfNodes);
		context.setConditionsOfNodes(conditionsOfNodes);
		context.setMastrid("" + affairData.getFormRecordId());
		// context.setFormData("" + affairData.getFormAppId());
		context.setFormData("-1");
		context.setAppObject(wfContextBizObject);
		// 指定回退到发起者后发起者再发送
		if (affairData.getCaseId() != null) {
			context.setCurrentActivityId("start");
			context.setCaseId(affairData.getCaseId());
			context.setProcessId(affairData.getProcessId());
		}
		/*String caseId[];*/
		
		RunCaseResult runCaseResult = null;
		CtpTemplate ct = null;
		if (null != affairData.getTemplateId()) {
			ct = templateManager.getCtpTemplate(affairData.getTemplateId());
		}
		Long flowAccount = AppContext.currentAccountId();
		if (newVo.getFlowPermAccountId() != null) {
			flowAccount = newVo.getFlowPermAccountId();
		}
		EdocSummary summary = (EdocSummary) wfContextBizObject;
		if (null != affairData.getTemplateId() && null != ct && null != ct.getWorkflowId()) {
			context.setProcessTemplateId(ct.getWorkflowId().toString());
			runCaseResult = wapi.transRunCaseFromTemplate(context);
			updatePermissinRef4Govdoc(moduleType, summary.getGovdocType(), process_xml, process_id, ct.getWorkflowId().toString(), flowAccount);
		} else {
			// 缓存caseId
			runCaseResult = wapi.transRunCase(context);
			updatePermissinRef4Govdoc(moduleType, summary.getGovdocType(), process_xml, process_id, null, flowAccount);
		}
		newVo.setProcessLogDetails(context.getProcessLogDetails());
		return runCaseResult;
	}
	@SuppressWarnings("unchecked")
	private void workflowWait(User user, AffairData affairData, Long itemId, EdocSummary summary, Map<String, Object> params, GovdocDealVO dealVo) throws BusinessException {
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setAppName(ApplicationCategoryEnum.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setAppObject(summary);
		context.setDebugMode(false);
//		context.setAppName("edoc");
		context.setCurrentWorkitemId(itemId);
		context.setCurrentUserId(String.valueOf(user.getId()));
		context.setCurrentUserName(user.getName());
		context.setCurrentAccountId(String.valueOf(user.getAccountId()));
		context.setCurrentAccountName("");
		context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, GovdocWorkflowEventListener.ZCDB);
		context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, user.getId());
		context.setBusinessData(GovdocWorkflowEventListener.EDOCSUMMARY_CONSTANT, summary);
		context.setBusinessData(EdocConstant.FlowPermAccountId, dealVo.getFlowPermAccountId());
		context.setVersion("2.0");
		// 保存修改后的流程数据
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		String processXml = wfdef.get("process_xml");
		String readyObjectJSON = wfdef.get("readyObjectJSON");
		String processChangeMessage = wfdef.get("processChangeMessage");
		context.setChangeMessageJSON(processChangeMessage);
		boolean isMobile = "true".equals((String) params.get("isMobile"));
		context.setMobile(isMobile);
		// H5参数处理：从流程临时表中取出流程变更之后的processXml
		processXml = wapi.getTempProcessXml(processXml);

		if (Strings.isNotBlank(processXml)) {
			context.setProcessXml(processXml);
		}
		if (Strings.isNotBlank(readyObjectJSON)) {
			context.setReadyObjectJson(readyObjectJSON);
		}
		wapi.temporaryPending(context);
	}
	private WFInfo workflowFinish(Comment c, AffairData affairData, long subObjectId, CtpAffair affair, Object appObj, GovdocDealVO dealVo, Object... param)
			throws BusinessException {
		EdocSummary summary= (EdocSummary)appObj;
		User user = AppContext.getCurrentUser();
		WorkflowBpmContext context = new WorkflowBpmContext();
//		context.setApp("4");
		context.setAppName(ApplicationCategoryEnum.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setDebugMode(false);
		context.setAppObject(appObj);
		context.setBusinessData("CtpAffair", affair);
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, affair.getMemberId());
		if (c != null) {
			context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_COMMENT_ID, c.getId());
		}
		context.setBusinessData("bizObject", appObj);
		context.setBusinessData(GovdocWorkflowEventListener.EDOCSUMMARY_CONSTANT, appObj);
		context.setCurrentWorkitemId(subObjectId);
		if (!affair.getMemberId().equals(user.getId())) {
			context.setCurrentUserId(String.valueOf(affair.getMemberId()));
			V3xOrgMember affairMember = orgManager.getMemberById(affair.getMemberId());
			context.setCurrentUserName(affairMember.getName());
			context.setCurrentAccountId(String.valueOf(affairMember.getOrgAccountId()));
		} else {
			context.setCurrentUserId(String.valueOf(user.getId()));
			context.setCurrentUserName(user.getName());
			context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
		}
		context.setCurrentAccountName("");
		context.setBusinessData("comment", c);
		context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, GovdocWorkflowEventListener.COMMONDISPOSAL);
		context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
		context.setBusinessData(EdocConstant.FlowPermAccountId, dealVo.getFlowPermAccountId());
		if(!StringUtils.isBlank(dealVo.getCustomDealWithActivitys()) && !"undefined".equals(dealVo.getCustomDealWithActivitys())){
			context.setBusinessData("isXuBan",true);
		}
		
		@SuppressWarnings("unchecked")
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		String processXml = wfdef.get("process_xml");
		if (processXml != null && !"".equals(processXml.trim())) {
			context.setProcessXml(processXml);
		}
		String readyObjectJSON = wfdef.get("readyObjectJSON");
		if (readyObjectJSON != null && !"".equals(readyObjectJSON.trim())) {
			context.setReadyObjectJson(readyObjectJSON);
		}
		String popNodeSubProcessJson = wfdef.get("workflow_newflow_input");
		String selectedPeoplesOfNodes = wfdef.get("workflow_node_peoples_input");
		String conditionsOfNodes = wfdef.get("workflow_node_condition_input");
		String processChangeMessage = wfdef.get("processChangeMessage");
		context.setPopNodeSubProcessJson(popNodeSubProcessJson);
		context.setSelectedPeoplesOfNodes(selectedPeoplesOfNodes);
		if (param != null && param.length == 1 && null != param[0]) {
			conditionsOfNodes = param[0].toString();
		}
		if(AppContext.getRawRequest().getAttribute("fromDistribute") != null){
			context.setConditionsOfNodes(null);
		}else{
			context.setConditionsOfNodes(conditionsOfNodes);
		}
		context.setMastrid(affairData.getFormRecordId() == null ? null : String.valueOf(affairData.getFormRecordId()));
		// context.setFormData(affairData.getFormAppId() == null ? null :
		// String.valueOf(affairData.getFormAppId()));
		context.setFormData("-1");
		context.setVersion("2.0");
		context.setChangeMessageJSON(processChangeMessage);
		String[] result = wapi.finishWorkItem(context);
		Long flowAccount = AppContext.currentAccountId();
		if (dealVo.getFlowPermAccountId() != null) {
			flowAccount = dealVo.getFlowPermAccountId();
		}
		updatePermissinRef4Govdoc(affairData.getModuleType(), ((EdocSummary) appObj).getGovdocType(), processXml, "-1", "-1", flowAccount);
		
		WFInfo wfInfo = new WFInfo();
		wfInfo.setSubmitConfirmMsg(context.getSubmitConfirmMsg());
		// 设置下一个节点的节点名和节点权限
		wfInfo.setMemberAndPolicys(result[0]);
		wfInfo.setCaseId(summary.getCaseId());
		wfInfo.setProcessId(summary.getProcessId());
		wfInfo.setNextNodeNames(null);
		wfInfo.setProcessLogDetails(context.getProcessLogDetails());
		return wfInfo;
	}

	/************************* 流程操作相关 ********************************/
	@Override
	public int cancelCase(AffairData affairData, Long caseId) throws BPMException {
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setCaseId(caseId);
		context.setAppName(ApplicationCategoryEnum.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setBusinessData(affairData.getBusinessData());
		context.setMastrid(affairData.getFormRecordId() == null ? null : String.valueOf(affairData.getFormRecordId()));
		context.setFormData(affairData.getFormAppId() == null ? null : String.valueOf(affairData.getFormAppId()));
		return wapi.cancelCase(context);
	}
	
	@Override
	public String[] stepBackCase(GovdocDealVO dealVo) throws BusinessException {
		WorkflowTraceEnums.workflowTrackType trackType = WorkflowTraceEnums.workflowTrackType.step_back_normal;
		Long activityId = dealVo.getAffair().getActivityId();
		Long workitemId = dealVo.getAffair().getSubObjectId();
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setCurrentActivityId(activityId.toString());
		context.setCurrentWorkitemId(workitemId);
		context.setSelectTargetNodeId(dealVo.getSelectTargetNodeId());// 参数可选
		context.setAppName(ApplicationCategoryEnum.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setAppObject(dealVo.getSummary());
		context.setVersion("2.0");
		
		AffairData affairData = GovdocAffairHelper.getStepbackAffairData(dealVo.getSummary());
		context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
		context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, GovdocWorkflowEventListener.WITHDRAW);
		//context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_TRACK_FLOW_TYPE, trackType.getKey());
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_TRACK_FLOW, dealVo.getIsWFTrace());
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_AFFAIR_ID, dealVo.getAffairId());
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, dealVo.getAffair().getMemberId());
		context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_SUMMARY_ID, dealVo.getSummaryId());
		context.setBusinessData(GovdocWorkflowEventListener.CURRENTUSER_CONSTANT, dealVo.getCurrentUser());
		return wapi.stepBack(context);
	}
	
	@Override
	public String[] appointStepBackCase(GovdocDealVO dealVo) throws BusinessException {
		// 供发消息
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(dealVo.getSummary());
		WorkflowTraceEnums.workflowTrackType trackType = WorkflowTraceEnums.workflowTrackType.special_step_back_repeal;
		if(!"start".equals(dealVo.getSelectTargetNodeId())) {//指定回退到普通节点
			trackType = WorkflowTraceEnums.workflowTrackType.special_step_back_normal;
		}
        if("1".equals(dealVo.getIsCircleBack())) {//环形回退
        	trackType = WorkflowTraceEnums.workflowTrackType.circle_step_back_repeal;
        }
		// 工作流回退
	    WorkflowBpmContext context = new WorkflowBpmContext();
	    context.setCurrentWorkitemId(dealVo.getWorkitemId());
	    context.setCaseId(dealVo.getCaseId());
	    context.setProcessId(dealVo.getProcessId());
	    context.setCurrentUserId(String.valueOf(dealVo.getCurrentUser().getId()));
	    context.setCurrentAccountId(String.valueOf(dealVo.getCurrentUser().getLoginAccount()));
	    context.setCurrentActivityId(String.valueOf(dealVo.getActivityId()));
	    context.setSelectTargetNodeId(String.valueOf(dealVo.getSelectTargetNodeId()));
	    context.setSubmitStyleAfterStepBack(String.valueOf(dealVo.getSubmitStyle()));
	    context.setAppName(ApplicationCategoryEnum.edoc.name());
	    context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
	    context.setAppObject(dealVo.getSummary());
	    context.setVersion("2.0");
	    
	    // 流程回退及其回调
	 	AffairData affairData = GovdocAffairHelper.getAppointStepBackAffairData(dealVo.getSummary());
	 	context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
	 	context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, "1".equals(dealVo.getSubmitStyle()) ? GovdocWorkflowEventListener.SPECIAL_BACK_SUBMITTO : GovdocWorkflowEventListener.SPECIAL_BACK_RERUN);
	 	context.setBusinessData(GovdocWorkflowEventListener.EDOCSUMMARY_CONSTANT, dealVo.getSummary());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_SUMMARY_ID, dealVo.getSummary().getId());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, dealVo.getAffair().getMemberId());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_COMMENT_ID, dealVo.getSummary().getId());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_AFFAIR_ID, dealVo.getAffairId());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_TRACK_FLOW_TYPE, trackType.getKey());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_TRACK_FLOW, dealVo.getIsWFTrace());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_CIRCLE_BACK, dealVo.getIsCircleBack());
	 	context.setBusinessData(GovdocWorkflowEventListener.CURRENTUSER_CONSTANT, dealVo.getCurrentUser());
	    String[] retValue = wapi.stepBack(context);
	    return retValue;
	}
	@Override
	public void stepStopCase(User user, Long workItemId, Long formRecordId, Long formAppId) throws BPMException {
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setCurrentWorkitemId(workItemId);
		context.setAppName(ApplicationCategoryEnum.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, 8);
		context.setCurrentUserId(String.valueOf(user.getId()));
		context.setMastrid(formRecordId == null ? null : String.valueOf(formRecordId));
		context.setFormData(formAppId == null ? null : String.valueOf(formAppId));
		wapi.stopCase(context);
	}
	@Override
	public int takeBackCase(Long workItemId) throws BPMException {
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setAppName(ApplicationCategoryEnum.edoc.name());
		context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
		context.setCurrentWorkitemId(workItemId);
		context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, GovdocWorkflowEventListener.TAKE_BACK);
		return wapi.takeBack(context);
	}
	/************************* 流程操作相关 11111   end ********************************/
	
	
	/************************* 节点权限相关 ********************************/
	/**
	 * 获得节点权限名称
	 * 
	 * @param affair
	 * @return
	 * @throws BusinessException
	 */
	public SeeyonPolicy getPolicyByAffair(CtpAffair affair, String processId) throws BusinessException {
		try {
			SeeyonPolicy seeyonPolicy = getPolicy(affair, processId);
			if (seeyonPolicy == null) {
				return new SeeyonPolicy("collaboration", ResourceUtil.getString("node.policy.collaboration"));
			} else if (Strings.isBlank(seeyonPolicy.getId())) {
				return new SeeyonPolicy("collaboration", ResourceUtil.getString("node.policy.collaboration"));
			}
			return seeyonPolicy;
		} catch (Exception e) {
			return new SeeyonPolicy("collaboration", ResourceUtil.getString("node.policy.collaboration"));
		}
	}

	private SeeyonPolicy getPolicy(CtpAffair affair, String processId) throws BusinessException, BPMException {
		if (affair != null && (affair.getState().equals(StateEnum.col_waitSend.getKey()) || affair.getState().equals(StateEnum.col_sent.getKey()))) {
			if("niwen".equals(affair.getNodePolicy())){
				return new SeeyonPolicy("niwen",ResourceUtil.getString("node.policy.niwen"));
			}
			if("dengji".equals(affair.getNodePolicy())){
				return new SeeyonPolicy("dengji",ResourceUtil.getString("node.policy.dengji"));
			}
			return new SeeyonPolicy("newCol", ResourceUtil.getString("node.policy.newCol"));
		}
		if (Strings.isNotBlank(affair.getNodePolicy())) {
			return new SeeyonPolicy(affair.getNodePolicy(), BPMSeeyonPolicy.getShowName(affair.getNodePolicy()));
		}
		String[] result = wapi.getNodePolicyIdAndName(ModuleType.edoc.name(), processId, String.valueOf(affair.getActivityId()));
		if (result == null)
			return new SeeyonPolicy("collaboration", ResourceUtil.getString("node.policy.collaboration"));
		return new SeeyonPolicy(result[0], result[1]);
	}

	/**
	 * 更新节点权限引用状态
	 * 
	 * @param modulType
	 *            应用类型
	 * @param processXml
	 *            工作流流程ID
	 * @param processId
	 *            工作流流程ID
	 * @param processTemplateId
	 *            工作流流程模版ID
	 * @throws BusinessException
	 */
	public void updatePermissinRef(Integer modulType, String processXml, String processId, String processTemplateId, Long accountId) throws BusinessException {
		// 更新节点权限引用状态
		ModuleType type = ModuleType.getEnumByKey(modulType);
		String configCategory = "";
		if (ModuleType.collaboration.name().equals(type.name()) || ModuleType.form.name().equals(type.name())) {
			configCategory = EnumNameEnum.col_flow_perm_policy.name();
		} else if (ModuleType.edocSend.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
		} else if (ModuleType.edocRec.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
		} else if (ModuleType.edocSign.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
		} else if (ModuleType.info.name().equals(type.name())) {
			configCategory = EnumNameEnum.info_send_permission_policy.name();
		}
		List<String> list = wapi.getWorkflowUsedPolicyIds(type.name(), processXml, processId, processTemplateId);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				permissionManager.updatePermissionRef(configCategory, list.get(i), accountId);
			}
		}
	}

	/**
	 * 更新节点权限引用状态
	 * 
	 * @param modulType
	 *            应用类型
	 * @param processXml
	 *            工作流流程ID
	 * @param processId
	 *            工作流流程ID
	 * @param processTemplateId
	 *            工作流流程模版ID
	 * @throws BusinessException
	 */
	private void updatePermissinRef4Govdoc(Integer modulType, Integer subApp, String processXml, String processId, String processTemplateId, Long accountId)
			throws BusinessException {
		// 更新节点权限引用状态
		ModuleType type = ModuleType.getEnumByKey(modulType);
		String configCategory = "";
		List<String> list = new ArrayList<String>();
		if (subApp == ApplicationSubCategoryEnum.edoc_fawen.getKey()) {
			configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
			list.add("niwen");
		} else if (subApp == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
			configCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
			list.add("dengji");
		} else if (subApp == ApplicationSubCategoryEnum.edoc_qianbao.getKey()) {
			configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
			list.add("niwen");
		} else if (subApp == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
			configCategory = EnumNameEnum.edoc_new_change_permission_policy.name();
			list.add("fenban");
		}
		list.addAll(wapi.getWorkflowUsedPolicyIds(type.name(), processXml, processId, processTemplateId));
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				permissionManager.updatePermissionRef(configCategory, list.get(i), accountId);
			}
		}
	}

	public void updatePermissinRef(Integer modulType, String[] policyLists, Long accountId) throws BusinessException {
		// 更新节点权限引用状态
		ModuleType type = ModuleType.getEnumByKey(modulType);
		String configCategory = "";
		if (ModuleType.collaboration.name().equals(type.name()) || ModuleType.form.name().equals(type.name())) {
			configCategory = EnumNameEnum.col_flow_perm_policy.name();
		} else if (ModuleType.edocSend.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_send_permission_policy.name();
		} else if (ModuleType.edocRec.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
		} else if (ModuleType.edocSign.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
		} else if (ModuleType.info.name().equals(type.name())) {
			configCategory = EnumNameEnum.info_send_permission_policy.name();
		}
		// List<String> list = wapi.getWorkflowUsedPolicyIds(type.name(),
		// processXml, processId, processTemplateId);
		if (policyLists != null && policyLists.length > 0) {
			for (int i = 0; i < policyLists.length; i++) {
				permissionManager.updatePermissionRef(configCategory, policyLists[i], accountId);
			}
		}
	}

	/**
	 * 更新文单中节点权限引用状态
	 * @param modulType  应用类型
	 * @param flowperm_name 权限名
	 * @throws BusinessException
	 */
	public void updatePermissinRef(Integer modulType, String flowperm_name, Long accountId) throws BusinessException {
		// 更新节点权限引用状态
		ModuleType type = ModuleType.getEnumByKey(modulType);
		String configCategory = "";
		if (ModuleType.edoc.name().equals(type.name()) || ModuleType.form.name().equals(type.name())) {
			configCategory = EnumNameEnum.col_flow_perm_policy.name();
		} else if (ModuleType.edocSend.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_send_permission_policy.name();
		} else if (ModuleType.edocRec.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
		} else if (ModuleType.edocSign.name().equals(type.name())) {
			configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
		} else if (ModuleType.info.name().equals(type.name())) {
			configCategory = EnumNameEnum.info_send_permission_policy.name();
		}
		String[] boundList = flowperm_name.split(",");
		if (boundList != null && boundList.length > 0) {
			for (int i = 0; i < boundList.length; i++) {
				permissionManager.updatePermissionRef(configCategory, boundList[i], accountId);
			}
		}
	}

	@Override
	public void deleteNode(CtpAffair currentAffair, List<String> activityIdList) throws BusinessException {
		EdocSummary summary = govdocSummaryManager.getSummaryById(currentAffair.getObjectId());
		wapi.deleteNodeFromDB(summary.getProcessId(), activityIdList, 
				currentAffair.getActivityId(), currentAffair.getMemberId());
	}

	public void updateCaseRunState(Long caseId, int state) throws BusinessException {
		String hql = "update CaseRunDAO set state =:state where id=:caseId";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("state", state);
		map.put("caseId", caseId);
		DBAgent.bulkUpdate(hql, map);
	}
	/**
	 * 回退分吧把已经执行完毕的流程重新放开
	 * @param caseId
	 * @return
	 * @throws BusinessException
	 */
	public String transBackWfRunCase(String caseId) throws BusinessException {
		String msg = null;
		if (Strings.isNotBlank(caseId)) {
			Long caseIdLong= Long.parseLong(caseId); 
			wapi.moveHistoryCaseToRun(caseIdLong);
		} else {
			msg = "前台传入数据丢失！";
		}
		return msg;
	}
	
	/**
	 * @param summary
	 * @param affair
	 * @throws BusinessException
	 */
	public void fillSummaryVoByWf(GovdocSummaryVO summaryVO) throws BusinessException {
		EdocSummary summary = summaryVO.getSummary();
		CtpAffair affair = summaryVO.getAffair();
		HttpServletRequest request = (HttpServletRequest) AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
		ContentViewRet context = new ContentViewRet();
		context.setModuleId(summary.getId());
		context.setModuleType(ModuleType.collaboration.getKey());
		Long accountId = AppContext.currentAccountId();
		PermissionVO defaultPermission = new PermissionVO();
		String subAppName = "";
		if (summary.getGovdocType() == 1) {
			subAppName = ApplicationCategoryEnum.govdocSend.name();
			defaultPermission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.edoc_new_send_permission_policy.name(), accountId);
		} else if (summary.getGovdocType() == 2) {
			subAppName = ApplicationCategoryEnum.govdocRec.name();
			defaultPermission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.edoc_new_rec_permission_policy.name(), accountId);
		} else if (summary.getGovdocType() == 3) {
			subAppName = ApplicationCategoryEnum.govdocSign.name();
			defaultPermission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.edoc_new_qianbao_permission_policy.name(), accountId);
		} else if (summary.getGovdocType() == 4) {
			subAppName = ApplicationCategoryEnum.govdocExchange.name();
			defaultPermission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.edoc_new_qianbao_permission_policy.name(), accountId);
		}
		if (summary.getGovdocType() != null) {
			request.setAttribute("subAppName", subAppName);
			if(defaultPermission!=null)
			request.setAttribute("defaultPolicyId", defaultPermission.getName());
		}
		context.setAffairId(affair.getId());
		context.setWfActivityId(affair.getActivityId());
		if (Strings.isNotBlank(summary.getProcessId())) {
			if (summary.getCaseId() == null && summary.getState() != EdocConstant.flowState.cancel.ordinal() 
					&& summary.getState() != EdocConstant.flowState.tracked.ordinal()) {
				//TODO 在GovdocWorkflowEventListener的onProcessCanceled中
				//govdocManager.transRepealCallback(baseVo)代码summary.setCaseId(null);
			}
			context.setWfCaseId(summary.getCaseId());
			context.setWfItemId(affair.getSubObjectId());
			context.setWfProcessId(summary.getProcessId());
			AppContext.putRequestContext("scene", 3);// 查运行中
		} else if (null != summary.getTempleteId() && Strings.isNotBlank(String.valueOf(summary.getTempleteId()))) {// 待发列表模板查看流程图
			CtpTemplate cp = templateManager.getCtpTemplate(summary.getTempleteId());
			if (null != cp && !"text".equals(cp.getType())) {
				context.setWfProcessId(cp.getWorkflowId().toString());
				AppContext.putRequestContext("scene", 2);// 查模板的
			}
		}
		context.setContentSenderId(summary.getStartMemberId());
		ContentConfig contentCfg = ContentConfig.getConfig(ModuleType.edoc);
		request.setAttribute("contentCfg", contentCfg);
		request.setAttribute("contentContext", context);
		
		//超期节点
		int superNodeStatus = 0;
		if (null != affair.getActivityId()) {
			superNodeStatus = this.getSuperNodeStatus(summary.getProcessId(), String.valueOf(affair.getActivityId()));
			summaryVO.getSwitchVo().setSuperNodestatus(superNodeStatus);
		}
		AppContext.putRequestContext("superNodestatus", superNodeStatus);
		
	}
	
	/**
     * 获得节点权限名称
     * @param affair
     * @return
     * @throws BusinessException
     */
    public SeeyonPolicy getPolicyByAffair(CtpAffair affair, EdocSummary summary) throws BusinessException {
    	try {
    		SeeyonPolicy seeyonPolicy = getPolicy(affair, summary);
    		if(seeyonPolicy == null){
    			return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
    		}else if(Strings.isBlank(seeyonPolicy.getId())){
    			return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
    		}
    		return seeyonPolicy;
    	} catch(Exception e) {
    		LOGGER.error("",e);
    		return new SeeyonPolicy("collaboration",ResourceUtil.getString("node.policy.collaboration"));
    	}
    }
	private SeeyonPolicy getPolicy(CtpAffair affair, EdocSummary summary) throws BusinessException, BPMException {
		if (affair != null
        		&& (affair.getState().equals(StateEnum.col_waitSend.getKey())
        		|| affair.getState().equals(StateEnum.col_sent.getKey()))){
			if("niwen".equals(affair.getNodePolicy())) {
				return new SeeyonPolicy("niwen",ResourceUtil.getString("node.policy.niwen"));
			}
			if("dengji".equals(affair.getNodePolicy())) {
				return new SeeyonPolicy("dengji",ResourceUtil.getString("node.policy.dengji"));
			}
            return new SeeyonPolicy("newCol",ResourceUtil.getString("node.policy.newCol"));
        }
        if(affair != null && Strings.isNotBlank(affair.getNodePolicy())) {
            return new SeeyonPolicy(affair.getNodePolicy(), BPMSeeyonPolicy.getShowName(affair.getNodePolicy()));
        }
        if(summary != null && affair != null) {
            String[] result = this.getNodePolicyIdAndName(ModuleType.collaboration.name(),summary.getProcessId(),String.valueOf(affair.getActivityId()));
            if(result==null) {
                return new SeeyonPolicy("yuedu",ResourceUtil.getString("node.policy.read"));
            }
            return new SeeyonPolicy(result[0],result[1]);
        } else {
        	return new SeeyonPolicy("yuedu",ResourceUtil.getString("node.policy.read"));
        }
	}
	
	@Override
	public void releaseWorkFlowProcessLock(String processId, String userId) throws BPMException {
		wapi.releaseWorkFlowProcessLock(processId, userId);
	}
	@Override
	public List<ProcessLogDetail> getAllWorkflowMatchLogAndRemoveCache() {
		return wapi.getAllWorkflowMatchLogAndRemoveCache();
	}
	@Override
	public List<ProcessLogDetail> getAllWorkflowMatchLogAndRemoveCache(String conditionInput) {
		return wapi.getAllWorkflowMatchLogAndRemoveCache(conditionInput);
	}
	@Override
	public BPMProcess getTemplateProcess(Long processTempleteId) throws BPMException {
        return wapi.getTemplateProcess(processTempleteId);
    }
	@Override
	@SuppressWarnings("deprecation")
	public BPMProcess getBPMProcessForM1(String processId) throws BPMException {
		return wapi.getBPMProcess(processId);
	}	
	@Override
	public int getSuperNodeStatus(String processId, String activityId) throws BPMException {
		return wapi.getSuperNodeStatus(processId, activityId);
	}
	@Override
	public String getNodeFormViewAndOperationName(Long templateId, String nodeId) throws BPMException {
		return wapi.getNodeFormViewAndOperationName(templateId, nodeId);
	}
	@Override
	public String getNodeFormViewAndOperationName(BPMProcess process, String nodeId) throws BPMException {
		return wapi.getNodeFormViewAndOperationName(process, nodeId);
	}
	@Override
	public String getWorkflowNodesInfo(String processId, String appName, CtpEnumBean subAppEnum) throws BPMException {
		return wapi.getWorkflowNodesInfo(processId, appName, subAppEnum);
	}
	@Override
	public String selectWrokFlowTemplateXml(String templateId) throws BPMException {
		return wapi.selectWrokFlowTemplateXml(templateId);
	}
	@Override
	public String selectWrokFlowXml(String templateId) throws BPMException {
		return wapi.selectWrokFlowXml(templateId);
	}
	@Override
	public String[] getStartNodeFormPolicy(Long templateId) throws BPMException {
		String[] result= new String[5];
		BPMSeeyonPolicyVO startPolicy= wapi.getStartNodeFormPolicy(templateId);
		if (startPolicy != null) {
            result[0] = startPolicy.getFormApp();
            result[1] = startPolicy.getFormViewId();
            result[2] = startPolicy.getFormViewOperation();
            result[3] = startPolicy.getName();
            result[4] = startPolicy.getDR();
        } else {
            LOGGER.info("templateId=" + templateId + "的模版xml无法转换为BPMProcess对象！");
        }
        return result;
	}
	@Override
	public void readWorkItem(Long workitemId) throws BPMException {
		wapi.readWorkItem(workitemId);
	}
	@Override
	public String[] getNodePolicyIdAndName(String appName, String processId, String activityId) throws BPMException {
		return wapi.getNodePolicyIdAndName(appName, processId, activityId);
	}
	@Override
	public String getNodeFormOperationNameFromRunning(String processId, String nodeId) throws BPMException {
		return wapi.getNodeFormOperationNameFromRunning(processId, nodeId);
	}
	@Override
	public String getNodeFormOperationNameFromRunning(String processId, String nodeId,boolean isHistoryFlag) throws BPMException {
		return wapi.getNodeFormOperationNameFromRunning(processId, nodeId, isHistoryFlag);
	}
	@Override
	public String getNodeFormOperationName(Long templateId, String nodeId) throws BPMException {
		return wapi.getNodeFormOperationName(templateId, nodeId);
	}
	@Override
	public boolean isInSpecialStepBackStatus(long caseId, boolean isHistoryFlag) throws BPMException {
		return wapi.isInSpecialStepBackStatus(caseId, isHistoryFlag);
	}
	@Override
    public Object[] replaceWorkItemMembers(boolean isUpdateProcess,Long memberId,String processId, long workitemId, 
            String activityId, List<V3xOrgMember> nextMember,boolean isTransfer) throws BPMException {
    	return wapi.replaceWorkItemMembers(isUpdateProcess, memberId, processId, workitemId, activityId, nextMember, isTransfer);
    }
	@Override
	public String[] getNodePolicyInfos(String processId, String nodeId) throws BPMException {
		return wapi.getNodePolicyInfos(processId, nodeId);
	}
	@Override
	public String[] getNodePolicyInfosFromTemplate(Long templateId, String nodeId) throws BPMException {
		return wapi.getNodePolicyInfosFromTemplate(templateId, nodeId);
	}
	
	@AjaxAccess
	@Override
	public String[] lockWorkflowForRepeal(String appName, String processIds,
			String activityId, String currentUserId, int action)
			throws BPMException {
		String[] repealResult = new String[2];
		if(Strings.isNotBlank(processIds)){
			String[] processArr = processIds.split(",");
			for (int i = 0; i < processArr.length; i++) {
				//1.判断是否可以回车
				repealResult = wapi.canRepeal(appName, processArr[i], activityId);
				if("true".equals(repealResult[0])){
					//2.进行加锁
					repealResult = wapi.lockWorkflowProcess(processArr[i], currentUserId, action);
				}else{
					break;
				}
			}
		}else{
			repealResult[0]= "true";
			repealResult[1]= "";
		}
		return repealResult;
	}
	
    /*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public AffairManager getAffairManager() {
		return affairManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/
	
}
