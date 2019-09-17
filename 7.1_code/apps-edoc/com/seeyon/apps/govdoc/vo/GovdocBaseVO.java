package com.seeyon.apps.govdoc.vo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.po.FormPermissionConfig;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;

import net.joinwork.bpm.definition.BPMProcess;

public class GovdocBaseVO {
	
	public String errorMsg;
	
	private String opinionType;
	private boolean isAlertStepbackDialog = false;
	private boolean specifyFallback;//是否指定回退-提交给我状态
	private boolean isInSpecialSB = false;
	
	//公文开关
	private String newGovdocView = "0";
	
	//用于判断是否是新建的业务
	private String newBusiness = "1";
	private String currentPageName;
	private String action = "";
	private String dealAction = "";
	private String from = "";//从哪里来的，枚举NewGovdocFrom
	private boolean isFromDistribute;//是否公文分办
	private Long exchangeFileId;
	//复制正文后原始正文id
	private String exchangeContentIdNewGov;
	
	private Long templateId;//模板Id
	private Long summaryId;
	private Long affairId;
	private String processId;
	private Long caseId;//流程實例ID---caseId
	private Long workitemId;
	private Long activityId;
	private Long signSummaryId;//签收流程summaryId
	private Long distributeAffairId;
	private String distributeType;
	private Long flowPermAccountId;
	
	private GovdocParamVO paramVo;
	private Permission permission;
	private NodePolicy nodePolicyObj;
	private String nodePolicy;//节点权限
	private String nodePolicyLabel;//节点权限名称
	
	private boolean isSysTemplate = false;
	private EdocRecieveRecord oldSignRecord;
	private EdocRegister register;
	private BPMProcess process;//流程对象
	private EdocSummary summary;//当前Summary对象
	private EdocSummary oldSummary;//原Summary对象
	private CtpAffair affair;//当前Affair对象
	private CtpAffair senderAffair;//发起Affair对象
	private V3xOrgMember member;//当前Affaird人员
	private V3xOrgMember senderMember;//发起Affaird人员
	private CtpCommentAll commentAll;
	private Comment comment;
	private CtpTemplate template;
	private CtpTemplate rootTemplate;
	private GovdocSwitchVO switchVo;//
	private GovdocXubanVO xubanVo;//续办对象
	private GovdocPishiVO pishiVo;//领导批示编号
	private GovdocExchangeVO exchangeVo;
	private Map<String, Object> extPropertyMap;
	
	private FormBean formBean;
	private FormPermissionConfig formPermConfig;
	private FormOpinionConfig formOpinionConfig;
		
	//文号传参相关
	private GovdocBodyVO bodyVo;
	private GovdocMarkVO markOpenVo;
	private GovdocMarkVO docMarkVo;
	private GovdocMarkVO templateDocMarkVo;
	private GovdocMarkVO serialNoVo;
	private GovdocMarkVO templateSerialNoVo;
	private GovdocMarkVO signMarkVo;
	private GovdocMarkVO templateSignMarkVo;
	private String oldDocMark;
	private String oldDocMark2;
	private String oldSerialNo;
	private String oldSignMark;
	private boolean isLastNode = false;
	private Integer jianbanType;
	private Integer newflowType;
	private Long parentSummaryId;
	private String childSummaryId;
	
	//当前参数
	private User currentUser;//当前人员
	private Date currentDate;
	
	private Integer summaryState;
	private Integer affairState;
	private Integer subState;
	private boolean isQuickSend;
	private String subApp;	
	private int govDocFormType;
	private Integer isLoadNewFile;
	private String trackWorkflowType;//WorkflowTraceEnums.workflowTrackType
	private String isWFTrace;//1流程追溯 0非流程追溯
	private String isCircleBack = "";//是否环形回退 1是 0否
	private String commentContent;
	
	private boolean isFensong;//是否是分送节点
	private boolean isSign;//是否是签收节点
	
	private boolean isGovdocForm = true;
	
	private boolean isModifyDealSuggestion = false;//拟办意见是否有修改
	
	private Integer trackType;
	private String zdgzry;
	private String trackIds;
	private String trackNames;
	private String displayIds;
	private String displayNames;
	
	private List<String> basicActionList;
	private List<String> commonActionList;
	private List<String> advanceActionList;
	private String nodePerm_baseActionList;
	private String nodePerm_commonActionList;
	private String nodePerm_advanceActionList;
	
	public GovdocBaseVO() {
		//公文新建参数
		GovdocBodyVO bodyVo = new GovdocBodyVO();
		this.setBodyVo(bodyVo);
		GovdocSwitchVO switchVo = new GovdocSwitchVO();
		this.setSwitchVo(switchVo);
		GovdocParamVO paramVo = new GovdocParamVO();
		this.setParamVo(paramVo);
	}
	
	public Boolean isNewBusiness() {
		return "1".equals(newBusiness) ? true : false;
	}
	public int getGovDocFormType() {
		if("1".equals(subApp)){
    		return 5;
    	}else if("2".equals(subApp)){
    		return 7;
    	}else if("3".equals(subApp)){
    		return 8;
    	}
		return govDocFormType;
	}
	public boolean isChildFlow() {
		if(this.newflowType != null && this.newflowType.intValue()==EdocConstant.NewflowType.child.ordinal() && this.getParentSummaryId()!=null) {
			return true;
		}
		return false;
	}
	public boolean isParentFlow() {
		if(this.newflowType != null && this.newflowType.intValue()==EdocConstant.NewflowType.main.ordinal() && Strings.isNotBlank(this.getChildSummaryId())) {
			return true;
		}
		return false;
	}
	
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public Long getCaseId() {
		return caseId;
	}
	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}
	public Long getWorkitemId() {
		return workitemId;
	}
	public void setWorkitemId(Long workitemId) {
		this.workitemId = workitemId;
	}
	public Long getActivityId() {
		return activityId;
	}
	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}
	public Long getAffairId() {
		if(affairId == null && affair != null){
			return affair.getId();
		}
		if (affairId == null && distributeAffairId != null) {
			return distributeAffairId;
		}
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public Long getDistributeAffairId() {
		return distributeAffairId;
	}
	public void setDistributeAffairId(Long distributeAffairId) {
		this.distributeAffairId = distributeAffairId;
	}
	public void setGovDocFormType(int govDocFormType) {
		this.govDocFormType = govDocFormType;
	}
	public EdocRegister getRegister() {
		return register;
	}
	public void setRegister(EdocRegister register) {
		this.register = register;
	}
	public EdocSummary getSummary() {
		return summary;
	}
	public void setSummary(EdocSummary summary) {
		this.summary = summary;
	}
	public EdocSummary getOldSummary() {
		return oldSummary;
	}
	public void setOldSummary(EdocSummary oldSummary) {
		this.oldSummary = oldSummary;
	}
	public CtpAffair getAffair() {
		return affair;
	}
	public void setAffair(CtpAffair affair) {
		this.affair = affair;
	}
	public CtpAffair getSenderAffair() {
		return senderAffair;
	}
	public void setSenderAffair(CtpAffair senderAffair) {
		this.senderAffair = senderAffair;
	}
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	public CtpCommentAll getCommentAll() {
		return commentAll;
	}
	public void setCommentAll(CtpCommentAll commentAll) {
		this.commentAll = commentAll;
	}
	public User getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	public GovdocMarkVO getMarkOpenVo() {
		return markOpenVo;
	}
	public void setMarkOpenVo(GovdocMarkVO markOpenVo) {
		this.markOpenVo = markOpenVo;
	}
	public GovdocMarkVO getDocMarkVo() {
		return docMarkVo;
	}
	public void setDocMarkVo(GovdocMarkVO docMarkVo) {
		this.docMarkVo = docMarkVo;
	}
	public GovdocMarkVO getSerialNoVo() {
		return serialNoVo;
	}
	public void setSerialNoVo(GovdocMarkVO serialNoVo) {
		this.serialNoVo = serialNoVo;
	}
	public GovdocMarkVO getSignMarkVo() {
		return signMarkVo;
	}
	public void setSignMarkVo(GovdocMarkVO signMarkVo) {
		this.signMarkVo = signMarkVo;
	}
	public boolean getIsQuickSend() {
		return isQuickSend;
	}
	public void setQuickSend(boolean isQuickSend) {
		this.isQuickSend = isQuickSend;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getDealAction() {
		return dealAction;
	}
	public void setDealAction(String dealAction) {
		this.dealAction = dealAction;
	}
	public String getNewBusiness() {
		return newBusiness;
	}
	public void setNewBusiness(String newBusiness) {
		this.newBusiness = newBusiness;
	}
	public String getSubApp() {
		return subApp;
	}	
	public void setSubApp(String subApp) {
		this.subApp = subApp;
	}
	public CtpTemplate getTemplate() {
		return template;
	}
	public void setTemplate(CtpTemplate template) {
		this.template = template;
	}
	public boolean isSysTemplate() {
		return isSysTemplate;
	}
	public void setSysTemplate(boolean isSysTemplate) {
		this.isSysTemplate = isSysTemplate;
	}
	public CtpTemplate getRootTemplate() {
		return rootTemplate;
	}
	public void setRootTemplate(CtpTemplate rootTemplate) {
		this.rootTemplate = rootTemplate;
	}
	public FormBean getFormBean() {
		return formBean;
	}
	public void setFormBean(FormBean formBean) {
		this.formBean = formBean;
	}
	public FormPermissionConfig getFormPermConfig() {
		return formPermConfig;
	}
	public void setFormPermConfig(FormPermissionConfig formPermConfig) {
		this.formPermConfig = formPermConfig;
	}
	public FormOpinionConfig getFormOpinionConfig() {
		return formOpinionConfig;
	}
	public void setFormOpinionConfig(FormOpinionConfig formOpinionConfig) {
		this.formOpinionConfig = formOpinionConfig;
	}
	public boolean isFensong() {
		return isFensong;
	}
	public void setFensong(boolean isFensong) {
		this.isFensong = isFensong;
	}
	public boolean isSign() {
		return isSign;
	}
	public void setSign(boolean isSign) {
		this.isSign = isSign;
	}
	public Integer getSummaryState() {
		return summaryState;
	}
	public void setSummaryState(Integer summaryState) {
		this.summaryState = summaryState;
	}
	public Date getCurrentDate() {
		return currentDate;
	}
	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	public Long getTemplateId() {
		return templateId;
	}
	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}
	public V3xOrgMember getMember() {
		return member;
	}
	public void setMember(V3xOrgMember member) {
		this.member = member;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getNodePolicy() {
		return nodePolicy;
	}
	public void setNodePolicy(String nodePolicy) {
		this.nodePolicy = nodePolicy;
	}
	public String getNodePolicyLabel() {
		return nodePolicyLabel;
	}
	public void setNodePolicyLabel(String nodePolicyLabel) {
		this.nodePolicyLabel = nodePolicyLabel;
	}
	public Integer getIsLoadNewFile() {
		return isLoadNewFile;
	}
	public void setIsLoadNewFile(Integer isLoadNewFile) {
		this.isLoadNewFile = isLoadNewFile;
	}
	public String getTrackWorkflowType() {
		return trackWorkflowType;
	}
	public void setTrackWorkflowType(String trackWorkflowType) {
		this.trackWorkflowType = trackWorkflowType;
	}
	public String getCommentContent() {
		return commentContent;
	}
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}
	public Integer getAffairState() {
		return affairState;
	}
	public void setAffairState(Integer affairState) {
		this.affairState = affairState;
	}
	public Integer getSubState() {
		return subState;
	}
	public void setSubState(Integer subState) {
		this.subState = subState;
	}
	public BPMProcess getProcess() {
		return process;
	}
	public void setProcess(BPMProcess process) {
		this.process = process;
	}
	public boolean isLastNode() {
		return isLastNode;
	}
	public void setLastNode(boolean isLastNode) {
		this.isLastNode = isLastNode;
	}
	public String getIsWFTrace() {
		return isWFTrace;
	}
	public void setIsWFTrace(String isWFTrace) {
		this.isWFTrace = isWFTrace;
	}
	public String getIsCircleBack() {
		return isCircleBack;
	}
	public void setIsCircleBack(String isCircleBack) {
		this.isCircleBack = isCircleBack;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public boolean getIsGovdocForm() {
		return isGovdocForm;
	}
	public void setGovdocForm(boolean isGovdocForm) {
		this.isGovdocForm = isGovdocForm;
	}
	public GovdocMarkVO getTemplateDocMarkVo() {
		return templateDocMarkVo;
	}
	public void setTemplateDocMarkVo(GovdocMarkVO templateDocMarkVo) {
		this.templateDocMarkVo = templateDocMarkVo;
	}
	public GovdocMarkVO getTemplateSerialNoVo() {
		return templateSerialNoVo;
	}
	public void setTemplateSerialNoVo(GovdocMarkVO templateSerialNoVo) {
		this.templateSerialNoVo = templateSerialNoVo;
	}
	public GovdocMarkVO getTemplateSignMarkVo() {
		return templateSignMarkVo;
	}
	public void setTemplateSignMarkVo(GovdocMarkVO templateSignMarkVo) {
		this.templateSignMarkVo = templateSignMarkVo;
	}
	public String getDistributeType() {
		return distributeType;
	}
	public void setDistributeType(String distributeType) {
		this.distributeType = distributeType;
	}
	public V3xOrgMember getSenderMember() {
		return senderMember;
	}
	public void setSenderMember(V3xOrgMember senderMember) {
		this.senderMember = senderMember;
	}
	public Long getFlowPermAccountId() {
		return flowPermAccountId;
	}
	public void setFlowPermAccountId(Long flowPermAccountId) {
		this.flowPermAccountId = flowPermAccountId;
	}
	public Integer getJianbanType() {
		return jianbanType;
	}
	public void setJianbanType(Integer jianbanType) {
		this.jianbanType = jianbanType;
	}
	public Integer getNewflowType() {
		return newflowType;
	}
	public void setNewflowType(Integer newflowType) {
		this.newflowType = newflowType;
	}
	public Long getParentSummaryId() {
		return parentSummaryId;
	}
	public void setParentSummaryId(Long parentSummaryId) {
		this.parentSummaryId = parentSummaryId;
	}
	public String getChildSummaryId() {
		return childSummaryId;
	}
	public void setChildSummaryId(String childSummaryId) {
		this.childSummaryId = childSummaryId;
	}
	public String getOldDocMark() {
		return oldDocMark;
	}
	public void setOldDocMark(String oldDocMark) {
		this.oldDocMark = oldDocMark;
	}
	public String getOldDocMark2() {
		return oldDocMark2;
	}
	public void setOldDocMark2(String oldDocMark2) {
		this.oldDocMark2 = oldDocMark2;
	}
	public String getOldSerialNo() {
		return oldSerialNo;
	}
	public void setOldSerialNo(String oldSerialNo) {
		this.oldSerialNo = oldSerialNo;
	}
	public String getOldSignMark() {
		return oldSignMark;
	}
	public void setOldSignMark(String oldSignMark) {
		this.oldSignMark = oldSignMark;
	}
	public EdocRecieveRecord getOldSignRecord() {
		return oldSignRecord;
	}
	public void setOldSignRecord(EdocRecieveRecord oldSignRecord) {
		this.oldSignRecord = oldSignRecord;
	}
	public String getNewGovdocView() {
		return newGovdocView;
	}
	public void setNewGovdocView(String newGovdocView) {
		this.newGovdocView = newGovdocView;
	}
	public GovdocBodyVO getBodyVo() {
		return bodyVo;
	}
	public void setBodyVo(GovdocBodyVO bodyVo) {
		this.bodyVo = bodyVo;
	}
	public GovdocSwitchVO getSwitchVo() {
		return switchVo;
	}
	public void setSwitchVo(GovdocSwitchVO switchVo) {
		this.switchVo = switchVo;
	}
	public GovdocXubanVO getXubanVo() {
		return xubanVo;
	}
	public void setXubanVo(GovdocXubanVO xubanVo) {
		this.xubanVo = xubanVo;
	}
	public GovdocPishiVO getPishiVo() {
		return pishiVo;
	}
	public void setPishiVo(GovdocPishiVO pishiVo) {
		this.pishiVo = pishiVo;
	}
	public GovdocExchangeVO getExchangeVo() {
		return exchangeVo;
	}
	public void setExchangeVo(GovdocExchangeVO exchangeVo) {
		this.exchangeVo = exchangeVo;
	}
	public Map<String, Object> getExtPropertyMap() {
		return extPropertyMap;
	}
	public void setExtPropertyMap(Map<String, Object> extPropertyMap) {
		this.extPropertyMap = extPropertyMap;
	}
	public String getCurrentPageName() {
		return currentPageName;
	}
	public void setCurrentPageName(String currentPageName) {
		this.currentPageName = currentPageName;
	}
	public boolean isModifyDealSuggestion() {
		return isModifyDealSuggestion;
	}
	public void setModifyDealSuggestion(boolean isModifyDealSuggestion) {
		this.isModifyDealSuggestion = isModifyDealSuggestion;
	}
	public Long getSignSummaryId() {
		return signSummaryId;
	}
	public void setSignSummaryId(Long signSummaryId) {
		this.signSummaryId = signSummaryId;
	}
	public boolean isFromDistribute() {
		return isFromDistribute;
	}
	public void setFromDistribute(boolean isFromDistribute) {
		this.isFromDistribute = isFromDistribute;
	}
	public String getOpinionType() {
		return opinionType;
	}
	public void setOpinionType(String opinionType) {
		this.opinionType = opinionType;
	}
	public boolean isSpecifyFallback() {
		return specifyFallback;
	}
	public void setSpecifyFallback(boolean specifyFallback) {
		this.specifyFallback = specifyFallback;
	}
	public boolean isAlertStepbackDialog() {
		return isAlertStepbackDialog;
	}
	public void setAlertStepbackDialog(boolean isAlertStepbackDialog) {
		this.isAlertStepbackDialog = isAlertStepbackDialog;
	}
	public boolean isInSpecialSB() {
		return isInSpecialSB;
	}
	public void setInSpecialSB(boolean isInSpecialSB) {
		this.isInSpecialSB = isInSpecialSB;
	}
	public Long getExchangeFileId() {
		return exchangeFileId;
	}
	public void setExchangeFileId(Long exchangeFileId) {
		this.exchangeFileId = exchangeFileId;
	}
	public String getExchangeContentIdNewGov() {
		return exchangeContentIdNewGov;
	}
	public void setExchangeContentIdNewGov(String exchangeContentIdNewGov) {
		this.exchangeContentIdNewGov = exchangeContentIdNewGov;
	}
	public NodePolicy getNodePolicyObj() {
		return nodePolicyObj;
	}
	public void setNodePolicyObj(NodePolicy nodePolicyObj) {
		this.nodePolicyObj = nodePolicyObj;
	}
	public Permission getPermission() {
		return permission;
	}
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
	public Integer getTrackType() {
		return trackType;
	}
	public void setTrackType(Integer trackType) {
		this.trackType = trackType;
	}
	public String getTrackIds() {
		return trackIds;
	}
	public void setTrackIds(String trackIds) {
		this.trackIds = trackIds;
	}
	public String getZdgzry() {
		return zdgzry;
	}
	public void setZdgzry(String zdgzry) {
		this.zdgzry = zdgzry;
	}
	public String getTrackNames() {
		return trackNames;
	}
	public void setTrackNames(String trackNames) {
		this.trackNames = trackNames;
	}
	public String getDisplayIds() {
		return displayIds;
	}
	public void setDisplayIds(String displayIds) {
		this.displayIds = displayIds;
	}
	public String getDisplayNames() {
		return displayNames;
	}
	public void setDisplayNames(String displayNames) {
		this.displayNames = displayNames;
	}
	public List<String> getBasicActionList() {
		return basicActionList;
	}
	public void setBasicActionList(List<String> basicActionList) {
		this.basicActionList = basicActionList;
	}
	public List<String> getCommonActionList() {
		return commonActionList;
	}
	public void setCommonActionList(List<String> commonActionList) {
		this.commonActionList = commonActionList;
	}
	public List<String> getAdvanceActionList() {
		return advanceActionList;
	}
	public void setAdvanceActionList(List<String> advanceActionList) {
		this.advanceActionList = advanceActionList;
	}
	public String getNodePerm_baseActionList() {
		return nodePerm_baseActionList;
	}
	public void setNodePerm_baseActionList(String nodePerm_baseActionList) {
		this.nodePerm_baseActionList = nodePerm_baseActionList;
	}
	public String getNodePerm_commonActionList() {
		return nodePerm_commonActionList;
	}
	public void setNodePerm_commonActionList(String nodePerm_commonActionList) {
		this.nodePerm_commonActionList = nodePerm_commonActionList;
	}
	public String getNodePerm_advanceActionList() {
		return nodePerm_advanceActionList;
	}
	public void setNodePerm_advanceActionList(String nodePerm_advanceActionList) {
		this.nodePerm_advanceActionList = nodePerm_advanceActionList;
	}
	public GovdocParamVO getParamVo() {
		return paramVo;
	}
	public void setParamVo(GovdocParamVO paramVo) {
		this.paramVo = paramVo;
	}
}
