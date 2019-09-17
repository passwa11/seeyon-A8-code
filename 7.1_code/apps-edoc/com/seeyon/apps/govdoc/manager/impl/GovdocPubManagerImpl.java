package com.seeyon.apps.govdoc.manager.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.edoc.bo.EdocOptionBO;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.bo.SendGovdocResult;
import com.seeyon.apps.govdoc.constant.GovdocEnum.EdocTypeEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OperationType;
import com.seeyon.apps.govdoc.constant.GovdocEnum.TransferStatus;
import com.seeyon.apps.govdoc.helper.GovdocAffairHelper;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocNewHelper;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.helper.GovdocSummaryHelper;
import com.seeyon.apps.govdoc.manager.GovdocAffairManager;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocContentManager;
import com.seeyon.apps.govdoc.manager.GovdocDocManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.manager.GovdocSignetManager;
import com.seeyon.apps.govdoc.manager.GovdocStatManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.manager.QwqpManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocBodyVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.index.api.IndexApi;
//import com.seeyon.apps.ocip.util.OrgUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.AttachmentEditUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.enums.PermissionAction;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.constants.WorkFlowConstants;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.vo.CPMatchResultVO;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.manager.EdocManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GovdocPubManagerImpl implements GovdocPubManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocManagerImpl.class);
	
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocExchangeManager govdocExchangeManager;
	private GovdocStatManager govdocStatManager;
	private GovdocMarkManager govdocMarkManager;
	private GovdocFormManager govdocFormManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocWorkflowManager govdocWorkflowManager;
	private GovdocContentManager govdocContentManager;
	private GovdocDocManager govdocDocManager;
	private GovdocSignetManager govdocSignetManager;
	private GovdocLogManager govdocLogManager;
	private GovdocPishiManager govdocPishiManager;
	private GovdocAffairManager govdocAffairManager;
	private QwqpManager qwqpManager;
	
	private EdocManager edocManager;
	private AffairManager affairManager;
	private SuperviseManager superviseManager;
	private TemplateManager templateManager;
	private PermissionManager permissionManager;
	private MainbodyManager ctpMainbodyManager;
	private EnumManager enumManagerNew;
    private AttachmentManager attachmentManager;
	private FileManager fileManager;
	private IndexApi indexApi;
	private OrgManager orgManager;
    private DocApi docApi;
    private CollaborationApi collaborationApi;
    private FormApi4Cap3 formApi4Cap3;
    private EdocApi edocApi;
    private ProcessLogManager processLogManager;
    
	/*************************** 11111 新建界面填充数据 start ***************************/
	@Override
	public String getTemplateSubject(String subject) {
		User curUser = AppContext.getCurrentUser();
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String formatDate = format.format(date);
		subject = subject + "(" + curUser.getName() + " " + formatDate + ")";
		return subject;
	}
	@Override
	public void CopySuperviseFromSummary(GovdocNewVO newVo, Long summaryId) throws BusinessException {
		SuperviseSetVO ssvo = new SuperviseSetVO();
		CtpSuperviseDetail detail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summaryId);
		if (detail != null) {
			ssvo.setTitle(detail.getTitle());// 主题
			Long terminalDate = detail.getTemplateDateTerminal();
			if (null != terminalDate) {
				Date superviseDate = Datetimes.addDate(new Date(), terminalDate.intValue());
				String date = Datetimes.format(superviseDate, Datetimes.datetimeWithoutSecondStyle);
				newVo.setSuperviseDate(date);
			} else if (detail.getAwakeDate() != null) {
				newVo.setSuperviseDate(Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));
				ssvo.setAwakeDate(Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));// 日期
			}
			List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
			Set<String> sIdSet = new HashSet<String>();
			for (CtpSupervisor supervisor : supervisors) {
				sIdSet.add(supervisor.getSupervisorId().toString());
			}
			if (!sIdSet.isEmpty()) {
				StringBuilder names = new StringBuilder();
				StringBuilder ids = new StringBuilder();
				String forshow = "";
				for (String s : sIdSet) {
					V3xOrgMember mem = orgManager.getMemberById(Long.valueOf(s));
					if (mem != null) {
						if (ids.length() > 0) {
							ids.append(",");
							names.append(",");
						}
						ids.append(mem.getId());
						names.append(mem.getName());
						forshow += "Member|" + mem.getId() + ",";
					}
				}
				if (forshow.length() > 0) {
					newVo.setForShow(forshow.substring(0, forshow.length() - 1));
				}
				EdocSummary byId = govdocSummaryManager.getSummaryById(summaryId);
				if (null != byId && null != byId.getTempleteId()) {
					// newVo.setUnCancelledVisor(ids.toString());
					Set<Long> _tempSur = superviseManager.parseTemplateSupervisorIds(byId.getTempleteId(), AppContext.getCurrentUser().getId());
					StringBuilder sb = new StringBuilder();
					if (null != _tempSur) {
						for (Long lid : _tempSur) {
							sb.append(lid + ",");
						}
						String sbs = sb.toString();
						if (sbs.length() > 1) {
							ssvo.setUnCancelledVisor(sbs.substring(0, sbs.length() - 1));
						}
					}
				}
				newVo.setColSupervisors(ids.toString());
				newVo.setColSupervisorNames(names.toString());
				ssvo.setSupervisorNames(names.toString());
				ssvo.setSupervisorIds(ids.toString());
			}
			newVo.setColSupervise(detail);
			AppContext.putRequestContext("_SSVO", ssvo);
		}
	}
	@SuppressWarnings("incomplete-switch")
	@Override
	public Map getAttributeSettingInfo(Map<String, String> args) throws BusinessException {
		Map map = new HashMap();
		if (args == null) {
			return map;
		}
		// 无
		String isNull = ResourceUtil.getString("collaboration.project.nothing.label");
		String affairId = args.get("affairId");
		if (Strings.isBlank(affairId)) {
			return map;
		}
		boolean isHistoryFlag = false;
		if (args.get("isHistoryFlag") != null && "true".equals((String) args.get("isHistoryFlag"))) {
			isHistoryFlag = true;
		}
		CtpAffair affair = null;
		if (isHistoryFlag) {
			affair = affairManager.getByHis(Long.parseLong(affairId));
		} else {
			affair = affairManager.get(Long.parseLong(affairId));
		}
		if (affair == null) {
			return map;
		}
		// 是否超期
		Boolean cOverTime = affair.isCoverTime(); // 未超期、已超期
		Timestamp now = DateUtil.currentTimestamp();
		if (affair.getExpectedProcessTime() != null && now.after(affair.getExpectedProcessTime())) {
			cOverTime = true;
		}
		map.put("cOverTime", cOverTime == true ? ResourceUtil.getString("pending.overtop.true.label") : ResourceUtil.getString("pending.overtop.false.label"));
		// 流程状态
		String state = "";
		switch (StateEnum.valueOf(affair.getState().intValue())) {
		case col_waitSend:
			state = ResourceUtil.getString("collaboration.state.11.waitSend");
			break;
		case col_sent:
			state = ResourceUtil.getString("collaboration.state.12.col_sent");
			break;
		case col_pending:
			state = ResourceUtil.getString("collaboration.state.13.col_pending");
			break;
		case col_done:
			state = ResourceUtil.getString("collaboration.state.14.done");
			break;
		}
		map.put("flowState", state);
		// 模版id
		String processId = null;
		Long templateWorkFlowID = null;
		// 查看协同时，特有的属性
		EdocSummary edocSummary = this.govdocSummaryManager.getSummaryById(affair.getObjectId());
		processId = edocSummary.getProcessId();
		// 待发中没有PROCESSID的时候，读取模板的流程ID。
		if (Strings.isBlank(processId) && Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState())) {
			if (edocSummary.getTempleteId() != null) {
				CtpTemplate t = templateManager.getCtpTemplate(edocSummary.getTempleteId());
				templateWorkFlowID = t.getWorkflowId();
			}
		}
		Long projectId = edocSummary.getProjectId();
		// 关联项目
		if (projectId == null || projectId == -1) {
			map.put("projectName", isNull);// 无
		} else {
			// ProjectSummary project = projectManager.getProject(projectId);
			// if (null != project) {
			// String projectName = project.getProjectName();
			// map.put("projectName", projectName);
			// }
		}
		// 归档,显示预归档目录(预归档的summary才会有archiveId)
		Long archiveId = edocSummary.getArchiveId();
		if(archiveId != null){
			String archiveName;
			DocResourceBO res = docApi.getDocResource(archiveId);
			archiveName=res.getFrName();
			//获取完整文字路径
			String fullPath= docApi.getPhysicalPath(res.getLogicalPath(), "\\", false, 0);
			if (GovdocHelper.needI18n(res.getFrType())){
				archiveName = ResourceUtil.getString(archiveName);
			}

			if(res.getLogicalPath()!=null && res.getLogicalPath().split("\\.").length>1){
				archiveName=fullPath;
			}
			map.put("archiveName", archiveName);
		}else{
			map.put("archiveName", isNull);// "无"
		}

		// 重要程度
		Integer importantLevel = edocSummary.getImportantLevel();
		if (affair.getImportantLevel() != null) {
			importantLevel = affair.getImportantLevel();
		}
		map.put("importantLevel", importantLevel == null ? isNull : WFComponentUtil.getImportantLevel(importantLevel.toString()));
		// 流程期限
		Date deadline = edocSummary.getDeadlineDatetime();
		map.put("deadline", deadline == null ? isNull : WFComponentUtil.getDeadLineName(deadline));

		if (deadline == null && edocSummary.getDeadline() != null) {
			map.put("deadline", edocSummary.getDeadline() == null ? isNull : WFComponentUtil.getDeadLineName(edocSummary.getDeadline()));
		}

		// 能否转发
		Boolean canForward = edocSummary.getCanForward();
		map.put("canForward", (canForward == null || canForward == false) ? "0" : "1");
		// 能否修改流程
		Boolean canModify = edocSummary.getCanModify();
		map.put("canModify", (canModify == null || canModify == false) ? "0" : "1");

		//合并处理设置
		boolean canStartMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,edocSummary);
		boolean canPreDealMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,edocSummary);
		boolean canAnyDealMerge = GovdocUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,edocSummary);
		if(canStartMerge){//处理人就是发起人
			map.put("canStartMerge", (canStartMerge == false) ? "0" : "1");
		}
		if(canPreDealMerge){//处理人和上一步相同
			map.put("canPreDealMerge", (canPreDealMerge == false) ? "0" : "1");
		}
		if(canAnyDealMerge){//处理人已处理过
			map.put("canAnyDealMerge", (canAnyDealMerge == false) ? "0" : "1");
		}
		// 能否编辑正文
		Boolean canEdit = edocSummary.getCanEdit();
		map.put("canEdit", (canEdit == null || canEdit == false) ? "0" : "1");
		// 能否编辑附件
		Boolean canEditAttachment = edocSummary.getCanEditAttachment();
		map.put("canEditAttachment", (canEditAttachment == null || canEditAttachment == false) ? "0" : "1");
		// 是否归档
		Boolean canArchive = edocSummary.getCanArchive();
		map.put("canArchive", (canArchive == null || canArchive == false) ? "0" : "1");
		
		//流程到期自动终止和撤销
        Integer processTermType = edocSummary.getProcessTermType();
        Boolean hasprocessTermType = Boolean.TRUE.equals(edocSummary.getCanAutostopflow())||null!=processTermType;
        map.put("processTermType", processTermType);
        if(hasprocessTermType){
        	if(Boolean.TRUE.equals(edocSummary.getCanAutostopflow())){
        		processTermType = 0;
        	}
        }
        map.put("processTermTypeName", ResourceUtil.getString("collaboration.auto.show.title0"));
        
		// 提醒
		Long advanceRemind = edocSummary.getAdvanceRemind();
		map.put("canDueReminder", advanceRemind == null ? isNull : WFComponentUtil.getAdvanceRemind(advanceRemind.toString()));
		Long remindInterval = edocSummary.getRemindInterval();
		map.put("remindInterval", remindInterval == null ? isNull : WFComponentUtil.getAdvanceRemind(remindInterval.toString()));
		// 发起时间
		Date startDate = edocSummary.getCreateTime();
		map.put("startDate", startDate);
		// 显示督办信息
		CtpSuperviseDetail detail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), affair.getObjectId());
		if (detail != null) {
			map.put("awakeDate", detail.getAwakeDate());
			map.put("supervisors", detail.getSupervisors());
			map.put("supervise", "supervise");
		}
		// fb 显示流程密级信息
		if (AppContext.hasPlugin("secret")) {
			String secretLevel = edocSummary.getSecretLevel();
			if (secretLevel != null) {
				map.put("secretLevel", enumManagerNew.getEnumItemLabel(EnumNameEnum.edoc_secret_level, String.valueOf(secretLevel)));
			} else {
				map.put("secretLevel", isNull);// "无"
			}
		}

		// 表单绑定
		if (String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())) {
			Long operationId = null;
			// 已办和待办时直接从Affair中取值
			if (Integer.valueOf(StateEnum.col_done.getKey()).equals(affair.getState())
					|| Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())) {
				operationId = affair.getFormOperationId();
				if (operationId == null) {
					try {
						// operationId =
						// Long.valueOf(ContentUtil.findRightIdbyAffairIdOrTemplateId(affair,
						// affair.getTempleteId()));
					} catch (Exception e) {

					}
				}
			} else {
				String oId = "";
				if (templateWorkFlowID != null) {
					oId = govdocWorkflowManager.getNodeFormOperationName(templateWorkFlowID, null);
				} else {
					oId = govdocWorkflowManager.getNodeFormOperationNameFromRunning(processId, null, isHistoryFlag);
				}
				if (Strings.isNotBlank(oId)) {
					operationId = Long.parseLong(oId);
				}
			}
			if (operationId != null) {
				FormAuthViewBean authViewBean = govdocFormManager.getAuth(operationId);
				// BDGW-1636快速发文后，点击属性状态，报错
				if (authViewBean != null) {
					FormViewBean viewBean = govdocFormManager.getView(authViewBean.getFormViewId());
					// 操作名
					String operationName = authViewBean.getName();
					// 视图名
					String viewName = viewBean.getFormViewName();
					map.put("formOperation", viewName + "." + operationName);
				}
			}
		}
		map.put("app", affair.getApp());
		return map;
	}
	/*************************** 11111 新建界面填充数据   end ***************************/

	
	/*************************** 22222 新建界面发送保存 start ***************************/
	@Override
	public void fillSendObj(GovdocNewVO newVo, SendType sendType) throws BusinessException {
		EdocSummary summary = newVo.getSummary();
		summary.setStartUserId(newVo.getCurrentUser().getId());
		summary.setCanDueReminder(false);
		summary.setAudited(false);
		summary.setVouch(EdocConstant.vouchState.defaultValue.ordinal());
		
		User user = newVo.getCurrentUser();
		Long senderId = user.getId();

		Long templateId = summary.getTempleteId();
		CtpTemplate template = null;
		if (templateId != null) {
			template = templateManager.getCtpTemplate(templateId);
			newVo.setTemplate(template);
		}

		if (summary.getGovdocType() != ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
			// 新增模板调用历史记录
			if (null != newVo.getCurTemId()) {
				this.templateManager.updateTempleteRecent(newVo.getCurTemId(), senderId);
			} else if (summary.getTempleteId() != null) {
				this.templateManager.updateTempleteRecent(summary.getTempleteId(), senderId);
			}
		}
		
		if (template != null) {
			String[] formInfo = govdocWorkflowManager.getStartNodeFormPolicy(template.getWorkflowId());
			if (null != formInfo) {
				if (Strings.isBlank(formInfo[0]) || Strings.isBlank(formInfo[1])) {
					summary.setFormAppid(newVo.getSummary().getFormId());// 表单ID
				} else {
					summary.setFormAppid(Long.valueOf(formInfo[0]));// 表单ID
					summary.setFormId(Long.valueOf(formInfo[1]));// 视图ID
				}
			}
		} else {
			summary.setFormAppid(newVo.getSummary().getFormId());// 表单ID
		}

		summary.setIdIfNew();
		Timestamp now = new Timestamp(newVo.getCurrentDate().getTime());
		summary.setCreateTime(now);
		summary.setStartTime(now);
		summary.setState(EdocConstant.flowState.run.ordinal());
		summary.setStartMemberId(senderId);
		summary.setOrgAccountId(user.getLoginAccount());
		summary.setOrgDepartmentId(user.getDepartmentId());

		// fb 分送产生最终的密级
		if (summary.getSecretLevel() == null) {
			Object temp = AppContext.getThreadContext("GovdocExchangeMain.SECRETLEVEL");
			if (temp != null) {
				summary.setSecretLevel((String) temp);
				AppContext.removeThreadContext("GovdocExchangeMain.SECRETLEVEL");// 保存密级枚举
			}
			LOGGER.info("最终密级产生成功!");
		}
	}
	
	public void fillDraftObj(GovdocNewVO newVo, Map para) throws BusinessException {
		Timestamp nowTime = new Timestamp(newVo.getCurrentDate().getTime());
		
		boolean isNew = true;
		boolean isSpecialBacked = false;//表示当前公是否被指定回退
		EdocSummary summaryFromUE = newVo.getSummary();
		EdocSummary summary = newVo.getSummary();
		CtpAffair senderAffair = new CtpAffair();
		
		if (!summary.isNew()) {
			summary = govdocSummaryManager.getSummaryById(summary.getId(), false);
			if(summary != null) {//编辑
				try {
					EdocSummary oldSummary = (EdocSummary)summary.clone(); 
					oldSummary.setId(summary.getId());
					newVo.setOldSummary(oldSummary);
				} catch(CloneNotSupportedException cse) {
					LOGGER.error("保存待发clone原公文对象出错", cse);
				}
				senderAffair = affairManager.getSenderAffair(summary.getId());
				isSpecialBacked = senderAffair==null ? false : GovdocHelper.isSpecialBacked(senderAffair.getSubState().intValue());
				isNew = false;//表示编辑
			} else {//新建
				summary = newVo.getSummary();
			}
			//从前端拷贝数据
			summary = GovdocNewHelper.cloneSummaryByUE(summary, summaryFromUE);
		}
		
		if (para.get("bodyType") != null) {
			summary.setFormId(govdocFormManager.getFormViewId(para));// 视图ID
			summary.setFormAppid(Long.valueOf((String) para.get("contentTemplateId")));// 表单ID
			summary.setFormRecordid(Long.valueOf((String) para.get("contentDataId")));// form主数据ID
		}
		summary.setStartUserId(newVo.getCurrentUser().getId());
		summary.setOrgAccountId(newVo.getCurrentUser().getLoginAccount());
		summary.setOrgDepartmentId(newVo.getCurrentUser().getDepartmentId());
		summary.setTempleteId(newVo.gettId());
		if (!isSpecialBacked) {
			summary.setCreateTime(nowTime);
		}
		summary.setAudited(false);
		summary.setVouch(EdocConstant.vouchState.defaultValue.ordinal());
		summary.setState(EdocConstant.flowState.cancel.ordinal());
		summary.setOrgAccountId(newVo.getCurrentUser().getLoginAccount());
		//流程超期
		if (para.get("remindIntervalCheckBox") == null 
				|| (para.get("deadLineselect") != null && para.get("deadLineselect") == "0")
				|| para.get("remindInterval") == null ){
			summary.setRemindInterval(-1L);
		}else{
			summary.setRemindInterval(Long.valueOf(para.get("remindInterval").toString()));
		}
		if (para.get("processTermTypeCheck") == null 
				|| (para.get("deadLineselect") != null && para.get("deadLineselect") == "0")
				|| para.get("processTermType") == null ){
			summary.setProcessTermType(-1);
		}else{
			summary.setProcessTermType(Integer.valueOf(para.get("processTermType").toString()));
		}
		newVo.setSummary(summary);
		newVo.setSenderAffair(senderAffair);
		newVo.setSenderMember(orgManager.getMemberById(newVo.getCurrentUser().getId()));
		if (newVo.gettId() != null) {
			CtpTemplate template = templateManager.getCtpTemplate(newVo.gettId());
			if(template != null) {
				newVo.setTemplate(template);
				newVo.setTemplateId(template.getId());
			}
		}
		if(senderAffair != null){			
			newVo.setAffairId(senderAffair.getId());
		}
		newVo.setSummaryId(summary.getId());
		newVo.setNew(isNew);
		newVo.setSpecialBacked(isSpecialBacked);
	}
	
	@Override
	public void fillSummaryByMapping(GovdocBaseVO newVo) throws BusinessException {
		EdocSummary summary = newVo.getSummary();
		
		FormBean formBean = govdocFormManager.getForm(summary.getFormAppid());
		FormDataMasterBean formDataMasterBean = null;
		try {
			formDataMasterBean = formApi4Cap3.findDataById(summary.getFormRecordid(), summary.getFormAppid());
		} catch (SQLException e) {
			LOGGER.error("公文单数据映射到主表时获取文单数据出错 formRecordId=" + summary.getFormRecordid() + " formAppId=" + summary.getFormAppid(), e);
		}

		boolean hasSendMemberEle = false;
		boolean hasSendDeptEle = false;
		boolean hasSendUnitEle = false;
		
		if (null != formBean && null != formDataMasterBean) {
			Map<String,Object> affairUpdateMap = new HashMap<String,Object>();
			List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();

			for (FormFieldBean formFieldBean : formFieldBeans) {
				if (formFieldBean.isMasterField()) {
					if (Strings.isNotBlank(formFieldBean.getMappingField())) {
						if ("doc_mark".equals(formFieldBean.getMappingField())) {// 公文文号
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								summary.setDocMark(null);
								continue;
							}
							String[] marks = value.toString().split("[|]");
							if(marks.length == 1) {
								summary.setDocMark(marks[0]);
							} else if(marks.length > 1) {
								summary.setDocMark(marks[1]);
							}
							if(newVo.getDocMarkVo() != null) {
								newVo.getDocMarkVo().setIsMapping(true);
								newVo.getDocMarkVo().setIsEnable(true);
							}
						} else if ("serial_no".equals(formFieldBean.getMappingField())) {// 内部文号
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								summary.setSerialNo(null);
								continue;
							}
							String[] marks = value.toString().split("[|]");
							if(marks.length == 1) {
								summary.setSerialNo(marks[0]);
							} else if(marks.length > 1) {
								summary.setSerialNo(marks[1]);
							}
							if(newVo.getSerialNoVo() != null) {
								newVo.getSerialNoVo().setIsMapping(true);
								newVo.getSerialNoVo().setIsEnable(true);
							}
						} else if ("sign_mark".equals(formFieldBean.getMappingField())) {// 公文文号B
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							summary.setSignAccountId(formBean.getOwnerAccountId());
							if (value == null || Strings.isBlank(value.toString())) {
								summary.setDocMark2(null);
								continue;
							}
							String[] marks = value.toString().split("[|]");
							if(marks.length == 1) {
								summary.setDocMark2(marks[0]);
							} else if(marks.length > 1) {
								summary.setDocMark2(marks[1]);
							}
							if(newVo.getSignMarkVo() != null) {
								newVo.getSignMarkVo().setIsMapping(true);
								newVo.getSignMarkVo().setIsEnable(true);
							}
						} else if ("subject".equals(formFieldBean.getMappingField())) {
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							//如果自动发起的，并且标题没有变化
							if (summary.getSubject().contains("(自动发起)")) {
								summary.setSubject("(自动发起)" + value.toString());
								summary.setDynamicSubject("(自动发起)" + value.toString());
							} else {
								summary.setSubject(value.toString());
								summary.setDynamicSubject(value.toString());
							}
						} else if ("urgent_level".equals(formFieldBean.getMappingField())) {// 紧急程度
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							CtpEnumItem item = enumManagerNew.getCacheEnumItem(Long.valueOf(value.toString()));
							if (item != null) {
								//需要更新待办affair的密级
								if (!(newVo instanceof GovdocNewVO && newVo.isNewBusiness()) && !item.getEnumvalue().equals(summary.getUrgentLevel())) {
									/*Map<String, Object> params = new HashMap<String, Object>();
									params.put("importantLevel", Integer.parseInt(item.getEnumvalue()));
									params.put("summaryId", summary.getId());
									affairManager.update("update CtpAffair c set c.importantLevel=:importantLevel where c.objectId=:summaryId", params );
									 */
									affairUpdateMap.put("importantLevel", Integer.parseInt(item.getEnumvalue()));
								}
								summary.setUrgentLevel(item.getEnumvalue());
								summary.setImportantLevel(Integer.parseInt(item.getEnumvalue()));
							}
						} else if ("secret_level".equals(formFieldBean.getMappingField())) {// 文件密级
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								summary.setSecretLevel(null);
								//affairUpdateMap.put("secretLevel", null);
								continue;
							}
							CtpEnumItem item = enumManagerNew.getCacheEnumItem(Long.valueOf(value.toString()));
							if (item != null) {
								summary.setSecretLevel(item.getEnumvalue());
								//affairUpdateMap.put("secretLevel", item.getEnumvalue());
							}	
						} else if ("sign_person".equals(formFieldBean.getMappingField())) {// 签收人
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Long memberId = Long.valueOf(value.toString());
								V3xOrgMember member = orgManager.getMemberById(memberId);
								if (member != null) {
									summary.setSignPerson(member.getName());
								}
							} catch (Exception e) {
								summary.setSignPerson(value.toString());
							}
						} else if ("create_person".equals(formFieldBean.getMappingField())) {// 拟稿人
							hasSendMemberEle = true;
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Long memberId = Long.valueOf(value.toString());
								V3xOrgMember member = orgManager.getMemberById(memberId);
								if (member != null) {
									summary.setCreatePerson(member.getName());
								}
							} catch (Exception e) {
								summary.setCreatePerson(value.toString());
							}
						} else if ("send_unit".equals(formFieldBean.getMappingField())) {// 发文单位
							hasSendUnitEle = true;
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setSendUnit(result.get("unitName"));
							summary.setSendUnitId(result.get("unitId"));
						} else if ("issuer".equals(formFieldBean.getMappingField())) {// 签发人
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Long memberId = Long.valueOf(value.toString());
								V3xOrgMember member = orgManager.getMemberById(memberId);
								if (member != null) {
									summary.setIssuer(member.getName());
								}
							} catch (Exception e) {
								summary.setIssuer(value.toString());
							}
						} else if ("send_to".equals(formFieldBean.getMappingField())) {// 主送单位
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setSendTo(result.get("unitName"));
							summary.setSendToId(result.get("unitId"));
						} else if ("copy_to".equals(formFieldBean.getMappingField())) {// 抄送单位
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setCopyTo(result.get("unitName"));
							summary.setCopyToId(result.get("unitId"));
						} else if ("report_to".equals(formFieldBean.getMappingField())) {// 抄报单位
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setReportTo(result.get("unitName"));
							summary.setReportToId(result.get("unitId"));
						} else if ("keyword".equals(formFieldBean.getMappingField())) {// 主题词
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							summary.setKeywords(value.toString());
						} else if ("print_unit".equals(formFieldBean.getMappingField())) {// 印发单位
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setPrintUnit(result.get("unitName"));
							summary.setPrintUnitId(result.get("unitId"));
						} else if ("printer".equals(formFieldBean.getMappingField())) {// 印发人
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Long memberId = Long.valueOf(value.toString());
								V3xOrgMember member = orgManager.getMemberById(memberId);
								if (member != null) {
									summary.setPrinter(member.getName());
								}
							} catch (Exception e) {
								summary.setPrinter(value.toString());
							}
						} else if ("send_to2".equals(formFieldBean.getMappingField())) {// 主送单位B
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setSendTo2(result.get("unitName"));
							summary.setSendToId2(result.get("unitId"));
						} else if ("copy_to2".equals(formFieldBean.getMappingField())) {// 抄送单位B
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setCopyTo2(result.get("unitName"));
							summary.setCopyToId2(result.get("unitId"));
						} else if ("report_to2".equals(formFieldBean.getMappingField())) {// 抄报单位B
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setReportTo2(result.get("unitName"));
							summary.setReportToId2(result.get("unitId"));
						} else if ("send_department".equals(formFieldBean.getMappingField())) {// 发文部门
							hasSendDeptEle = true;
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setSendDepartment(result.get("unitName"));
							summary.setSendDepartmentId(result.get("unitId"));
						} else if ("send_department2".equals(formFieldBean.getMappingField())) {// 发文部门B
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setSendDepartment2(result.get("unitName"));
							summary.setSendDepartmentId2(result.get("unitId"));
						} else if ("filesm".equals(formFieldBean.getMappingField())) {// 附件说明
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							summary.setFilesm(value.toString());
						} else if ("filefz".equals(formFieldBean.getMappingField())) {// 附注
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							summary.setFilefz(value.toString());
						} else if ("phone".equals(formFieldBean.getMappingField())) {// 联系电话
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							summary.setPhone(value.toString());
						} else if ("auditor".equals(formFieldBean.getMappingField())) {// 审核人
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Long memberId = Long.valueOf(value.toString());
								V3xOrgMember member = orgManager.getMemberById(memberId);
								if (member != null) {
									summary.setAuditor(member.getName());
								}
							} catch (Exception e) {
								summary.setAuditor(value.toString());
							}
						} else if ("review".equals(formFieldBean.getMappingField())) {// 复核人
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Long memberId = Long.valueOf(value.toString());
								V3xOrgMember member = orgManager.getMemberById(memberId);
								if (member != null) {
									summary.setReview(member.getName());
								}
							} catch (Exception e) {
								summary.setReview(value.toString());
							}
						} else if ("undertaker".equals(formFieldBean.getMappingField())) {// 承办人
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Long memberId = Long.valueOf(value.toString());
								V3xOrgMember member = orgManager.getMemberById(memberId);
								if (member != null) {
									summary.setUndertaker(member.getName());
								}
							} catch (Exception e) {
								summary.setUndertaker(value.toString());
							}
						} else if ("undertakenoffice".equals(formFieldBean.getMappingField())) {// 承办机构
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							Map<String, String> result = GovdocOrgHelper.getUnitNameAndUnitId1(value.toString());
							summary.setUndertakenoffice(result.get("unitName"));
							summary.setUndertakenofficeId(result.get("unitId"));
						} else if ("attachments".equals(formFieldBean.getMappingField())) {// 附件
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							summary.setAttachments(value.toString());
						} else if ("copies".equals(formFieldBean.getMappingField())) {// 印发份数
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							summary.setCopies(Integer.valueOf(value.toString()));
						} else if ("copies2".equals(formFieldBean.getMappingField())) {// 印发份数B
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							summary.setCopies2(Integer.valueOf(value.toString()));
						} else if ("signing_date".equals(formFieldBean.getMappingField())) {// 签发日期
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Date date = (Date) value;
								summary.setSigningDate(date);
							} catch (Exception e) {
								LOGGER.error(e);
							}
						} else if (formFieldBean.getMappingField().equals("createdate")) {// 拟稿日期
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Date date = new SimpleDateFormat("yyyy-MM-dd").parse(value.toString());
								summary.setStartTime(new Timestamp(date.getTime()));
							} catch (Exception e) {
								LOGGER.error(e);
							}
						} else if ("packdate".equals(formFieldBean.getMappingField())) {// 封发日期
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Date date = (Date) value;
								summary.setPackTime(new Timestamp(date.getTime()));
							} catch (Exception e) {
								LOGGER.error(e);
							}
						} else if ("receipt_date".equals(formFieldBean.getMappingField())) {// 签收日期
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Date date = (Date) value;
								summary.setReceiptDate(new java.sql.Date(date.getTime()));
							} catch (Exception e) {
								LOGGER.error(e);
							}
						} else if ("registration_date".equals(formFieldBean.getMappingField())) {// 登记日期
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							try {
								Date date = (Date) value;
								summary.setRegistrationDate(new java.sql.Date(date.getTime()));
							} catch (Exception e) {
								LOGGER.error(e);
							}
						} else if ("doc_type".equals(formFieldBean.getMappingField())) {// 公文种类
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}
							CtpEnumItem item = enumManagerNew.getCacheEnumItem(Long.valueOf(value.toString()));
							if (item != null) {
								summary.setDocType(item.getEnumvalue());
							}
						} else if ("send_type".equals(formFieldBean.getMappingField())) {// 行文类型
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}

							CtpEnumItem item = enumManagerNew.getCacheEnumItem(Long.valueOf(value.toString()));
							if (item != null) {
								summary.setSendType(item.getEnumvalue());
							}
						} else if ("keep_period".equals(formFieldBean.getMappingField())) {// 保密期限
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}

							CtpEnumItem item = enumManagerNew.getCacheEnumItem(Long.valueOf(value.toString()));
							if (item != null) {
								summary.setKeepPeriod(Integer.valueOf(item.getEnumvalue()));
							}
						} else if ("unit_level".equals(formFieldBean.getMappingField())) {// 公文级别
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())) {
								continue;
							}

							CtpEnumItem item = enumManagerNew.getCacheEnumItem(Long.valueOf(value.toString()));
							if (item != null) {
								summary.setUnitLevel(item.getEnumvalue());
							}
						} else if("nibanyijian".equalsIgnoreCase(formFieldBean.getMappingField())){
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null) {
								continue;
							}
							if(!value.toString().equals(summary.getDealSuggestion())){
								newVo.setModifyDealSuggestion(true);
							}
							summary.setDealSuggestion(value.toString());
						}else{//扩展字段
							GovdocNewHelper.fillSummaryExtendParam(summary, formFieldBean,formDataMasterBean);
						}
					} else {//公文文号没有映射
						if ("edocDocMark".equals(formFieldBean.getInputType())) {
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())
									|| Strings.isBlank(value.toString())) {
								continue;
							}
							if(newVo.getDocMarkVo() != null) {
								newVo.getDocMarkVo().setIsMapping(false);
								newVo.getDocMarkVo().setIsEnable(true);
							}
						}
						if ("edocInnerMark".equals(formFieldBean.getInputType())) {
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())
									|| Strings.isBlank(value.toString())) {
								continue;
							}
							if(newVo.getSerialNoVo() != null) {
								newVo.getSerialNoVo().setIsMapping(false);
								newVo.getSerialNoVo().setIsEnable(true);
							}
						}
						if ("edocSignMark".equals(formFieldBean.getInputType())) {
							Object value = formDataMasterBean.getFieldValue(formFieldBean.getName());
							if (value == null || Strings.isBlank(value.toString())
									|| Strings.isBlank(value.toString())) {
								continue;
							}
							if(newVo.getSignMarkVo() != null) {
								newVo.getSignMarkVo().setIsMapping(false);
								newVo.getSignMarkVo().setIsEnable(true);
							}
						}
					}
				}
			}
			
			//当公文单上没有映射拟稿人/拟稿部门/拟稿单位时，默认为当前公文发起人的部门/单位
			if(!hasSendMemberEle || !hasSendDeptEle || hasSendUnitEle) {
				V3xOrgMember member = orgManager.getMemberById(summary.getStartMemberId());
				if(member != null) {
					if(!hasSendMemberEle) {
						summary.setCreatePerson(member.getName());
					}
					if(!hasSendDeptEle) {
						V3xOrgDepartment dept = orgManager.getDepartmentById(member.getOrgDepartmentId());
						summary.setSendDepartment(dept == null ? null : dept.getName());
						summary.setSendDepartmentId(dept == null ? null : "Department|" + dept.getId());
					}
					/*if(!hasSendUnitEle) {
						V3xOrgAccount account = orgManager.getAccountById(member.getOrgAccountId());
						summary.setSendUnit(account == null ? null : account.getName());
						summary.setSendUnitId(account == null ? null : "Account|" + account.getId());
					}*/
				}
			}
			
			summary.setJianbanType(newVo.getJianbanType());
			if(newVo.getDocMarkVo() != null && newVo.getDocMarkVo().getIsEnable()) {
				newVo.getDocMarkVo().setSubject(summary.getSubject());
				newVo.getDocMarkVo().setSummaryId(summary.getId());
				newVo.getDocMarkVo().setDomainId(summary.getOrgAccountId());
				newVo.getDocMarkVo().setFormDataId(formDataMasterBean.getId());
				newVo.getDocMarkVo().setGovdocType(summary.getGovdocType());
				newVo.getDocMarkVo().setNewflowType(summary.getNewflowType());
			}
			if(newVo.getSerialNoVo() != null && newVo.getSerialNoVo().getIsEnable()) {
				newVo.getSerialNoVo().setSubject(summary.getSubject());
				newVo.getSerialNoVo().setSummaryId(summary.getId());
				newVo.getSerialNoVo().setDomainId(summary.getOrgAccountId());
				newVo.getSerialNoVo().setFormDataId(formDataMasterBean.getId());
				newVo.getSerialNoVo().setGovdocType(summary.getGovdocType());
				newVo.getSerialNoVo().setNewflowType(summary.getNewflowType());
			}
			if(newVo.getSignMarkVo() != null && newVo.getSignMarkVo().getIsEnable()) {
				newVo.getSignMarkVo().setSubject(summary.getSubject());
				newVo.getSignMarkVo().setSummaryId(summary.getId());
				newVo.getSignMarkVo().setDomainId(summary.getOrgAccountId());
				newVo.getSignMarkVo().setFormDataId(formDataMasterBean.getId());
				newVo.getSignMarkVo().setGovdocType(summary.getGovdocType());
				newVo.getSignMarkVo().setNewflowType(summary.getNewflowType());
			}
			
			String dynamicSubject = "";
			if(newVo.getTemplate() != null) {
				//若触发无标题映射时，将模板名称赋值
				if(Strings.isBlank(summary.getSubject())) {
					summary.setSubject(newVo.getTemplate().getSubject());
				}
				if(Strings.isNotBlank(newVo.getTemplate().getColSubject())) { 
					dynamicSubject = govdocFormManager.makeSubject(newVo.getTemplate(), summary);
				}
			}
			//若有动态标题则更新Affair标题为动态标题 或 无动态标题但标题变更过则更新Affair为更改过的标题
			if(Strings.isBlank(dynamicSubject)) {
				dynamicSubject = summary.getSubject();
			}
			//若动态标题为空则将subject赋值
			summary.setDynamicSubject(dynamicSubject);
			
			//判断公文标题或文号是否修改过
			boolean subjectIsChange = false;
			boolean docMarkIsChange = false;
			if(newVo.getOldSummary() != null) {
				String oldsubject = newVo.getOldSummary().getSubject()==null ? "" : newVo.getOldSummary().getSubject();
				String olddocMark = newVo.getOldSummary().getDocMark()==null ? "" : newVo.getOldSummary().getDocMark();
				
				if (!oldsubject.equals(summary.getSubject())) {
					subjectIsChange = true;
				}
				if (!olddocMark.equals(summary.getDocMark())) {
					docMarkIsChange = true;
				}
			}
			
			
			//同步公文标题到正文表ctp_content_all
			if(subjectIsChange) {
				affairUpdateMap.put("subject", summary.getDynamicSubject());
				
				AppContext.putThreadContext("contentTitle", summary.getDynamicSubject());
				if (newVo.getBodyVo()!= null && newVo.getBodyVo().getFormContent() != null) {
					ctpMainbodyManager.updateContentTitle(newVo.getBodyVo().getFormContent().getId(), summary.getDynamicSubject());
				}
				
				superviseManager.updateSubjectByEntityId(summary.getSubject(), summary.getId());
			}
			
			if(!affairUpdateMap.isEmpty()) {
				affairManager.update(affairUpdateMap, new Object[][]{{"objectId", summary.getId()}});
			}
			
			//同步文号到Affair
			if(docMarkIsChange) {
				List<CtpAffair> affairList = affairManager.getAffairs(ApplicationCategoryEnum.edoc, summary.getId());
				if(Strings.isNotEmpty(affairList)) {
					for(CtpAffair affair : affairList) {
						AffairUtil.addExtProperty(affair, AffairExtPropEnums.edoc_edocMark, summary.getDocMark());
					}
					affairManager.updateAffairs(affairList);
				}
			}
		}
	}

	@Override
	public void delSummary(GovdocNewVO info) throws BusinessException {
		boolean isNew = info.isNewBusiness();
		EdocSummary summary = info.getSummary();
		if (!isNew) {
			// 非新建环节，删除原有的
			govdocSummaryManager.deleteEdocSummary(summary.getId());
			List _senderist = new ArrayList();
			_senderist.add(CommentType.sender);
			govdocCommentManager.deleteCommentAllByModuleIdAndCtypes(ModuleType.edoc, summary.getId(), _senderist);
			CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
			if (sendAffair == null) {
				return;
			}
			sendAffair.setBodyType(info.getBodyVo().getContentType().toString());
			info.setSenderAffair(sendAffair);
			
			int subState = sendAffair.getSubState().intValue();
			boolean isSpecialBackedToSender = (subState == SubStateEnum.col_pending_specialBacked.getKey() 
																	|| subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()); 
			info.setSubState(subState);
			if (isSpecialBackedToSender) {
				govdocContentManager.deleteAttachment4SpecialBack(summary.getId());
			} else {
				/*if (Strings.isNotBlank(summary.getProcessId())) {
					govdocLogManager.deleteProcessLog(Long.parseLong(summary.getProcessId()));
				}*/
				affairManager.deleteByObjectId(ApplicationCategoryEnum.edoc, summary.getId());
				// 删除催办日志
				this.superviseManager.deleteLogs(summary.getId());
				// 删除意见
				govdocCommentManager.deleteCommentAllByModuleId(ModuleType.edoc, summary.getId());
				// 删除附件
				List<Attachment> attachment = attachmentManager.getByReference(summary.getId());
				for (Attachment att : attachment) {
					if (!AttachmentEditUtil.CONTENT_ATTACHMENTSUBRE.equals(att.getSubReference().toString())
							&& !Integer.valueOf(ModuleType.form.getKey()).equals(att.getCategory())) {
						this.attachmentManager.deleteById(att.getId());
					}
				}

			}
			// 删除意见
			if (subState != SubStateEnum.col_pending_specialBacked.getKey()) {
				govdocCommentManager.deleteOpinionBySummaryId(summary.getId());
				govdocSignetManager.deleteBySummaryId(summary.getId());
			} else {
				info.setSpecialBacked(true);
			}

		}
	}
	
	@Override
	public void saveSupervise(GovdocNewVO info, SendType sendType, boolean sendMessage) throws BusinessException {
		EdocSummary summary = info.getSummary();
		Map superviseMap = ParamUtil.getJsonDomain("colMainData");
		// 新公文没有contentType，而是govdocContentType,需要重置一下
		superviseMap.put("contentType", superviseMap.get("govdocContentType"));
		User user = info.getCurrentUser();
		if (user == null) {
			user = AppContext.getCurrentUser();
		}
		SuperviseMessageParam smp = new SuperviseMessageParam();
		if (sendMessage) {
			smp.setSendMessage(true);
			smp.setMemberId(summary.getStartMemberId());
			smp.setImportantLevel(summary.getImportantLevel());
			smp.setSubject(summary.getSubject());
		}

		if (sendType == null) {// 保存待发
			SuperviseSetVO ssvo = (SuperviseSetVO) ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
			smp.setSaveDraft(true);
			superviseManager.saveOrUpdateSupervise4Process(ssvo, smp, summary.getId(), SuperviseEnum.EntityType.govdoc);
		} else if (sendType == EdocConstant.SendType.resend || sendType == EdocConstant.SendType.normal) {
			SuperviseSetVO ssvo = (SuperviseSetVO) ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
			superviseManager.saveOrUpdateSupervise4Process(ssvo, smp, summary.getId(), SuperviseEnum.EntityType.govdoc);
		} else if (sendType == EdocConstant.SendType.auto || sendType == EdocConstant.SendType.child) {
			V3xOrgMember sender = orgManager.getMemberById(user.getId());
			copyAndSaveSuperviseFromTemplete(sender, smp, summary.getId(), summary.getTempleteId(), superviseMap);
		} else if (sendType == EdocConstant.SendType.forward) {
			// 转发不保存督办，先把这个分支留着吧。
		} else if (sendType == EdocConstant.SendType.immediate) {
			boolean specialback = false;
			boolean isspecialbackrerun = false;
			// 保存已发个人事务表
			CtpAffair affair = info.getSenderAffair();
			if (affair != null) {
				if (Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState())) {
					specialback = true;
				}
				if (Integer.valueOf(SubStateEnum.col_pending_specialBackToSenderCancel.getKey()).equals(affair.getSubState())) {
					isspecialbackrerun = true;
				}
			}
			if (specialback || isspecialbackrerun) {
				smp = null;// 不发送督办信息
			}

			superviseManager.updateStatus(summary.getId(), SuperviseEnum.EntityType.govdoc, SuperviseEnum.superviseState.supervising, smp);
		}
	}
	/*************************** 22222 新建界面发送保存   end ***************************/
	
	
	/*************************** 33333 公文查看界面显示 start ***************************/
	@Override
	public boolean fillSummaryObj(GovdocSummaryVO summaryVO) throws BusinessException {
		long l1 = System.currentTimeMillis();
		
		CtpAffair affair = null;
		if (summaryVO.getAffairId() != null) {
			affair = affairManager.get(summaryVO.getAffairId());
			if(affair == null) {
				summaryVO.setErrorMsg(ResourceUtil.getString("govdoc.summary.cannotacc.msg2"));
				return false;
			}
			//当前Affair状态为回退
			if(GovdocHelper.isAffairBacked(affair)) {
				summaryVO.setErrorMsg(WFComponentUtil.getErrorMsgByAffair(affair) + ResourceUtil.getString("collaboration.state.6.stepback"));
				return false;
			}
			//文档中心、督办 收藏和关联文档 借阅打开不校验删除状态
			if (affair.isDelete() && GovdocHelper.isNotCheckAffairDeleteOfOpenFrom(summaryVO.getOpenFrom())) {
				summaryVO.setErrorMsg(ResourceUtil.getString("govdoc.summary.cannotacc.msg1"));
				return false;
			}
			if(!GovdocHelper.isNotCheckAffairValidOfOpenFrom(summaryVO.getOpenFrom())) {
				if (!AffairUtil.isAfffairValid(affair)) {
					String errorMsg = WFComponentUtil.getErrorMsgByAffair(affair);
					if (!Strings.isBlank(errorMsg)) {
						summaryVO.setErrorMsg(errorMsg);
						return false;
					}
				}
			}
		} else {
			if(StringUtils.isNotBlank(summaryVO.getOpenFrom()) && "subFlow".equals(summaryVO.getOpenFrom())) {
				//查看子流程或主流程
				if(summaryVO.getSummaryId()==null && StringUtils.isNotBlank(summaryVO.getProcessId())) {//打开子流程或主流程时只有proccessid
					//根据流程ID查询summary
					EdocSummary summary = govdocSummaryManager.getSummaryByProcessId(summaryVO.getProcessId());
					if(summary != null) {
						affair = affairManager.getSenderAffair(summary.getId());
					}
				}
			}
			if(affair==null) {
				affair = govdocAffairManager.getSummaryAffairWhenAffairIsNull(summaryVO.getOpenFrom(), summaryVO.getSummaryId());
			}
		}
		if (affair == null) {
			summaryVO.setErrorMsg(ResourceUtil.getString("govdoc.summary.cannotacc.msg2"));
			return false;
		}
		
		String stepBack = summaryVO.getStepBack();
		if ("stepBack".equals(stepBack)) {
			summaryVO.setErrorMsg(stepBack);
			return false;
		}
		long l2 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取Affair耗时：" + (l2 -l1));
		
		//设置当前Affair对象
		summaryVO.setAffair(affair);
		//设置Summary主表对象
		EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
		
		long l3 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取Summary耗时：" + (l3 -l2));
		
		summaryVO.setSummary(summary);
		if (summaryVO.getSummaryId() == null) {
			summaryVO.setSummaryId(summary.getId());
		}
		summaryVO.setGovdocType(summary.getGovdocType());
		summaryVO.setProcessId(summary.getProcessId());
		summaryVO.setQuickSend(summary.getIsQuickSend());
		summaryVO.setActivityId(affair.getActivityId());
		summaryVO.setGovdocType(affair.getSubApp());
		
		//设置文单对象
		summaryVO.setFormBean(govdocFormManager.getForm(summary.getFormAppid()));
		long l4 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取FormBean耗时：" + (l4 -l3));
		//设置文单权限配置对象
		summaryVO.setFormPermConfig(formApi4Cap3.getConfigByFormId(summaryVO.getFormBean().getId()));
		long l5 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取FormBean文单权限配置耗时：" + (l5 -l4));
		//设置文单意见对象
		summaryVO.setFormOpinionConfig(govdocFormManager.getFormOpinionConfig(summaryVO.getFormBean().getId()));
		long l6 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取FormBean文单权限配置耗时：" + (l6 -l5));
		//设置模板对象
		if (summary.getTempleteId() != null) {
			CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
			summaryVO.setTemplate(template);
			if (template != null && template.isSystem()) {
				summaryVO.setSysTemplate(true);
			}
		}
		long l7 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取模板耗时：" + (l7 -l6));
		//设置发起人员
		V3xOrgMember member = orgManager.getMemberById(summary.getStartMemberId());
		if (member != null) {
			summaryVO.setStartMemberName(Functions.showMemberName(member));
			summaryVO.setStartMemberDepartmentId(member.getOrgDepartmentId());
			if (member.getIsInternal()) {
				summaryVO.setStartMemberPostId(member.getOrgPostId());
				String postName = "";
				if (member.getOrgPostId() != null) {
					V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
					if (post != null) {
						postName = post.getName();
					}
				}
				summaryVO.setStartMemberPostName(postName);
			} else {// 外部人员
				summaryVO.setStartMemberPostId(null);
				summaryVO.setStartMemberPostName(OrgHelper.getExtMemberPriPost(member));
			}
		}
		summaryVO.setSenderMember(member);
		long l8 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取发起人耗时：" + (l8 -l7));
		
		//设置当前所属Affair人员
        V3xOrgMember orgMember =  orgManager.getMemberById(affair.getMemberId());
        if(orgMember!=null){
        	summaryVO.setAffairMemberName(orgMember.getName());
        }
        summaryVO.setMember(orgMember);
        
        long l9 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取所属人耗时：" + (l9 -l8));
		
		//督办,督办列表页面因为要显示修改流程按钮，所以需要预先记载督办信息，其他协同展现页面延迟加载，即点督办设置的时候再加载。
        if(ColOpenFrom.supervise.name().equals(summaryVO.getOpenFrom())
        		||ColOpenFrom.listDone.name().equals(summaryVO.getOpenFrom())){
	        SuperviseSetVO ssvo = superviseManager.parseProcessSupervise(summary.getId(), summary.getTempleteId(), summary.getStartMemberId(),SuperviseEnum.EntityType.govdoc);
	        if (Strings.isNotBlank(ssvo.getSupervisorIds()) && ssvo.getSupervisorIds().indexOf(AppContext.getCurrentUser().getId().toString())!= -1) {
	            summaryVO.setIsCurrentUserSupervisor(true);
	        }
        }
        long l10 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillSummaryObj获取督办耗时：" + (l10 -l9));

        //当前公文打开为可回复状态
        summaryVO.getSwitchVo().setHasDealArea("listPending".equals(summaryVO.getOpenFrom()) && affair.getState().intValue()==StateEnum.col_pending.key());
        
        if(null != summary.getGovdocType() && summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
			GovdocExchangeDetail exchangeDetail = govdocExchangeManager.findDetailBySummaryId(summary.getId());
			summaryVO.setExchangeDetail(exchangeDetail);
			long l11 = System.currentTimeMillis();
			LOGGER.info("公文查看summary界面fillSummaryObj获取交换耗时：" + (l11 -l10));
        }
        
		return true;
	}
	
	@Override
	public void fillSummaryVoByPermission(GovdocSummaryVO summaryVO) throws BusinessException {
		EdocSummary summary = summaryVO.getSummary();
		CtpAffair affair = summaryVO.getAffair();
		
		//设置公文实际单位ID
		Long flowPermAccountId = GovdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary);
		summaryVO.setFlowPermAccountId(flowPermAccountId);
		
		// 封装节点权限
		String configItem = collaborationApi.getPolicyByAffair(affair).getId();
		String category = PermissionFatory.getPermBySubApp(summary.getGovdocType(), configItem).getCategorty();
		Permission permission = null;
		NodePolicy nodePolicy = null;
		try {
			permission = permissionManager.getPermission(category, configItem, flowPermAccountId);
			nodePolicy = permission.getNodePolicy();
			if("newCol".equals(configItem)) {//纠错
				if(summary.getGovdocType().intValue() == 2) {
					configItem = "dengji";
				} else {
					configItem = "niwen";
				}
			}
			// 用于判断当前节点权限是否存在，如果不存在则给知会的提示
			if (permission != null) {
				if (!configItem.equals(permission.getName()) 
						&& affair.getState() == StateEnum.col_pending.getKey()
						&& ApplicationCategoryEnum.collaboration.name().equals(PermissionFatory.getPermBySubApp(summary.getGovdocType()).getSubAppName())) {
					summaryVO.getSwitchVo().setNoFindPermission(true);
				}
			}
			summaryVO.setPermission(permission);
			summaryVO.setNodePolicy(configItem);
			summaryVO.setNodePolicyObj(nodePolicy);
			summaryVO.setNodePolicyLabel(permissionManager.getPermissionName(permission));
		} catch (Exception e) {
			LOGGER.error("获取节点权限报错category:" + category + " caonfigItem:" + configItem + " accountId:" + flowPermAccountId, e);
		}
		
		List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
		List<String> commonActionList = permissionManager.getActionList(permission, PermissionAction.common);
		List<String> advanceActionList = permissionManager.getActionList(permission, PermissionAction.advanced);
		
		// 控制节点权限的条件
		GovdocSummaryHelper.checkCanAction(summaryVO, affair, summary, basicActionList, commonActionList, advanceActionList, summaryVO.isSysTemplate(), permission);
		
		summaryVO.getSwitchVo().setCanTrack(basicActionList.contains("Track"));//是否允许跟踪
		if(summaryVO.getSwitchVo().isHasDealArea()) {
			//根据节点权限判断意见显示
			summaryVO.getSwitchVo().setCanComment(basicActionList.contains("Comment"));//是否有提交
			summaryVO.getSwitchVo().setCanShowAttitude(Strings.isNotBlank(nodePolicy.getAttitude()));//是否显示态度
			summaryVO.getSwitchVo().setCanShowOpinion(basicActionList.contains("Opinion"));//是否显示意见
			summaryVO.getSwitchVo().setCanShowCommonPhrase(basicActionList.contains("CommonPhrase"));//是否显示常用语
			summaryVO.getSwitchVo().setCanUploadAttachment(basicActionList.contains("UploadAttachment"));//是否允许上传附件
			summaryVO.getSwitchVo().setCanUploadRel(basicActionList.contains("UploadRelDoc"));//是否允许关联文档
			summaryVO.getSwitchVo().setFengsong(basicActionList.contains("FaDistribute"));//是否分送节点
			summaryVO.getSwitchVo().setCanModifyWorkFlow(summary.getCanModify());//是否允许修改流程
			summaryVO.getSwitchVo().setCanEditBody(commonActionList.contains("Edit") || advanceActionList.contains("Edit"));// 是否可以修改正文
			summaryVO.getSwitchVo().setCanContentSign(commonActionList.contains("ContentSign") || advanceActionList.contains("ContentSign"));
			summaryVO.getSwitchVo().setCanTransToBul(commonActionList.contains("TransmitBulletin") || advanceActionList.contains("TransmitBulletin"));
			summaryVO.getSwitchVo().setCanTransToEvent(commonActionList.contains("Transform") || advanceActionList.contains("Transform"));
			summaryVO.getSwitchVo().setCanTransToCol(commonActionList.contains("Zhuanshiwu") || advanceActionList.contains("Zhuanshiwu"));
			//是否有转事务权限
			if(AppContext.hasPlugin("collaboration") && summaryVO.getSwitchVo().isCanTransToCol()) {
				summaryVO.getSwitchVo().setHasNewColRole(summaryVO.getCurrentUser().hasResourceCode("F01_newColl"));
		    }
			//是否允许修改附件
			if("listSent".equals(summaryVO.getOpenFrom()) && (commonActionList.contains("allowUpdateAttachment") || advanceActionList.contains("allowUpdateAttachment"))) {
				summaryVO.getSwitchVo().setCanEditAttachment(true);	
			}
			//是否允许点赞
			if (Strings.isNotEmpty(basicActionList) && basicActionList.contains("Praise")) {
				summaryVO.getSwitchVo().setHasPraise("1");
			}
		}
		
		summaryVO.setCommonActionList(commonActionList);
		summaryVO.setBasicActionList(basicActionList);
		summaryVO.setAdvanceActionList(advanceActionList);
		//一屏式布局策略重置
		GovdocSummaryHelper.fillSummaryVoByAction(summaryVO);
		//指定回退态度的
		GovdocSummaryHelper.fillSummaryVoByAttitude(permission);
		//根据权限配置控制正文是否显示
		GovdocSummaryHelper.fillSummaryVoByShowContentSwitch(summaryVO);
		//签收分办按钮相关
		GovdocSummaryHelper.fillSummaryVoShowButton(summaryVO);
		//封装公文归档&收藏开关
		GovdocSummaryHelper.fillSummaryVoByArchive(summaryVO);
		//公文打印开关处理
		GovdocSummaryHelper.fillSummaryVoByPrint(summaryVO);
		
		if (GovdocHelper.isNodePolicyShowCustomDealWith(nodePolicy) && affair.getSubState() != SubStateEnum.col_pending_specialBack.key()) {
			summaryVO.getSwitchVo().setCanXuban(true);
		}
		
		//因以上ActionList可能会被重置，设置NodePerm_只能放到方法的最后
		summaryVO.setNodePerm_baseActionList(JSONUtil.toJSONString(summaryVO.getBasicActionList()));
		summaryVO.setNodePerm_commonActionList(JSONUtil.toJSONString(summaryVO.getCommonActionList()));
		summaryVO.setNodePerm_advanceActionList(JSONUtil.toJSONString(summaryVO.getAdvanceActionList()));
	}
	
	@Override
	public void fillSummaryVoByOpinions(GovdocSummaryVO summaryVO) throws NumberFormatException, BusinessException {
		Long formAppid = summaryVO.getSummary().getFormAppid();
		Long affairId = summaryVO.getAffair().getId();
		String openFrom = summaryVO.getOpenFrom();
		
		EdocOptionBO optionBO = new EdocOptionBO();
		optionBO.setOpenFrom(openFrom);
		optionBO.setFormAppid(formAppid + "");
		optionBO.setAffairId(affairId + "");
		EdocOptionBO edocOptionBO = edocApi.getMainBodyLogic(optionBO);
		if(edocOptionBO!=null){
			AppContext.putRequestContext("policy", edocOptionBO.getPolicy());
			AppContext.putRequestContext("affairState", edocOptionBO.getAffairState());
			AppContext.putRequestContext("optionId",edocOptionBO.getOptionId());
			AppContext.putRequestContext("opinionType", edocOptionBO.getOpinionType());
			AppContext.putRequestContext("opinionsJs", edocOptionBO.getOpinionsJs());
			AppContext.putRequestContext("senderOpinion", edocOptionBO.getSenderOpinion());
			AppContext.putRequestContext("senderOpinionAttStr", edocOptionBO.getSenderOpinionAttStr());
			AppContext.putRequestContext("ols",edocOptionBO.getOls());
			AppContext.putRequestContext("allowCommentInForm", edocOptionBO.getAllowCommentInForm());
		}
		
		Map<String, String> opMap = govdocCommentManager.getOptionPo(summaryVO);
		AppContext.putRequestContext("disPosition", opMap.get("disPosition"));
		AppContext.putRequestContext("nodePermissionPolicy", opMap.get("nodePermissionPolicy"));
		//mav.addObject("disPosition", opMap.get("disPosition"));
		//mav.addObject("nodePermissionPolicy", opMap.get("nodePermissionPolicy"));
	}
	
	@Override
	public void fillSummaryVoByExchange(GovdocSummaryVO summaryVO) throws BusinessException {
		GovdocExchangeMain zsfMain = null;
		//是否有转收文信息
    	zsfMain = govdocExchangeManager.findBySummaryId(summaryVO.getSummary().getId(),GovdocExchangeMain.EXCHANGE_TYPE_ZHUANSHOUWEN);
		if(zsfMain != null) {//并且要有转办节点权限
			if(zsfMain.getStartUserId().longValue() == summaryVO.getCurrentUser().getId() ||
					"pishi".equals(govdocPishiManager.checkLeaderPishi(summaryVO.getCurrentUser().getId(), zsfMain.getStartUserId().longValue()))){
				AppContext.putRequestContext("haveTurnRecEdoc", zsfMain.getId());
			}
		}
		
		GovdocExchangeDetail detail = govdocExchangeManager.findDetailBySummaryId(summaryVO.getSummaryId());
		if(detail != null) {//并且当前节点是登记
			zsfMain = govdocExchangeManager.getGovdocExchangeMainById(detail.getMainId());
			//是否来自转收文
			if(zsfMain.getType() == GovdocExchangeMain.EXCHANGE_TYPE_ZHUANSHOUWEN){
				AppContext.putRequestContext("haveTurnRecEdoc2", detail.getId());
			}
			//如果当前公文是交换类型带有正文文件，获取交换之前的公文正文id(公文交换、公文触发、公文转发等除联合发文外，修改正文时都需要复制正文，联合发文主办与协办流程共用一个正文)
			if (zsfMain.getOriginalFileId() != null && zsfMain.getType()!=GovdocExchangeMain.EXCHANGE_TYPE_LIANHE) {
				summaryVO.setExchangeContentId(zsfMain.getOriginalFileId().toString());
			}
		}
		
		//是否是分办后的收文
		GovdocExchangeDetail recDetail = govdocExchangeManager.findDetailByRecSummaryId(summaryVO.getSummaryId());
		if(recDetail!=null){
			GovdocExchangeMain main = govdocExchangeManager.getGovdocExchangeMainById(recDetail.getMainId());
			if(main!=null && main.getOriginalFileId()!=null){
				summaryVO.setExchangeContentId(main.getOriginalFileId().toString());
			}
		}
		
    	//是否有转发文的信息。
		List<GovdocExchangeMain> turnSendEdocRelations = govdocExchangeManager.findByReferenceIdId(summaryVO.getSummary().getId(),GovdocExchangeMain.EXCHANGE_TYPE_ZHUANFAWEN);
		if (turnSendEdocRelations != null && turnSendEdocRelations.size() > 0 && !"1".equals(summaryVO.getIsRecSendRel())) {
			if (summaryVO.getSummary().getGovdocType() != null && summaryVO.getSummary().getGovdocType() == ApplicationSubCategoryEnum.edoc_fawen.getKey()) {
				Long sumID = turnSendEdocRelations.get(0).getSummaryId();
				EdocSummary turnSummary=govdocSummaryManager.getSummaryById(sumID);
				if(turnSummary!=null){
					AppContext.putRequestContext("haveTurnSendEdoc1", sumID);
				}
			}else if(summaryVO.getSummary().getGovdocType() != null && summaryVO.getSummary().getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()){
				AppContext.putRequestContext("haveTurnSendEdoc2", "true");
			}
		}
		// 是否有联合发文消息  打开的主办流程(只会出现其中一种)
		GovdocExchangeMain lhMains = govdocExchangeManager.findBySummaryId(summaryVO.getSummaryId(), GovdocExchangeMain.EXCHANGE_TYPE_LIANHE);
		// 是否有联合发文消息  打开的协办流程(只会出现其中一种)
		GovdocExchangeDetail lhDetail = govdocExchangeManager.findDetailBySummaryId(summaryVO.getSummaryId());
		if (lhMains == null && lhDetail != null) {
			lhMains = govdocExchangeManager.getGovdocExchangeMainById(detail.getMainId());
			if (lhMains.getType() != GovdocExchangeMain.EXCHANGE_TYPE_LIANHE) {
				lhMains = null;
			}
		}
		if(lhMains != null){
			Map<String, String> conditionMap = new HashMap<String, String>();
			conditionMap.put("mainId", lhMains.getId().toString());
			List<GovdocExchangeDetail> details = govdocExchangeManager.getGovdocExchangeDetailList(new FlipInfo(), conditionMap);
			StringBuilder jointlyIssyed_value = new StringBuilder("");
			StringBuilder jointlyIssyed_text = new StringBuilder("");
			Set<Long> s = new HashSet<Long>();
			for (GovdocExchangeDetail vo : details) {
				Long orgId = vo.getRecAccountId();// 去重
				if (s.contains(orgId)) {
					continue;
				} else {
					s.add(orgId);
				}
				if (jointlyIssyed_value.length() > 0) {
					jointlyIssyed_value.append(",");
					jointlyIssyed_text.append("、");
				}
				jointlyIssyed_value.append("Account").append("|").append(orgId);
				jointlyIssyed_text.append(vo.getRecOrgName());
			}
			AppContext.putRequestContext("jointlyIssyed_value", jointlyIssyed_value.toString());
			AppContext.putRequestContext("jointlyIssyed_text", jointlyIssyed_text.toString());
			AppContext.putRequestContext("_jointlyIssued", "1");
		}
		
		// 交换穿透查看
		boolean chuantouchakan1 = false;
		PluginDefinition definition = SystemEnvironment.getPluginDefinition("govdoc");
		if (definition != null && "true".equals(definition.getPluginProperty("govdoc.chuantouchakan1"))) {
			chuantouchakan1 = true;
		}
		//如果是ocip外系统登陆进来，则不显示穿透
		//String ocipCas = request.getParameter("ocipCas");
		String ocipCas = AppContext.getRawRequest().getParameter("ocipCas");
		if (chuantouchakan1&&!"1".equals(ocipCas)) {
			Map<String, Object> chuantouchakanId = govdocExchangeManager.getChuantouchakanId(summaryVO);
			//mav.addAllObjects(chuantouchakanId);
			//TODO OCIP的内容
		}
			
		// 设置处室承办
		String isChuShi = "no";
		AppContext.putRequestContext("isChuShi", isChuShi);
		
		if(!"0".equals(summaryVO.getSwitchVo().getNewGovdocView())) {
			govdocExchangeManager.showExchangeState(summaryVO);
		}
	}
	@Override
	public boolean fillComponentObj(GovdocComponentVO summaryVO) throws BusinessException {
		long l1 = System.currentTimeMillis();
		CtpAffair affair = null;
		EdocSummary summary = null;
		if (summaryVO.isHistoryFlag()) {
			affair = affairManager.getByHis(summaryVO.getAffairId());
		
			long l2 = System.currentTimeMillis();
			LOGGER.info("公文查看summary界面fillComponentObj获取Affair耗时：" + (l2 -l1));
			summary = govdocSummaryManager.getSummaryByIdHistory(affair.getObjectId());// 流程追溯的数据
																						// 这个防止的objectId值可能有问题
			long l3 = System.currentTimeMillis();
			LOGGER.info("公文查看summary界面fillComponentObj获取Summary耗时：" + (l3 -l2));
		} else {
			affair = affairManager.get(summaryVO.getAffairId());
			long l2 = System.currentTimeMillis();
			LOGGER.info("公文查看summary界面fillComponentObj获取Affair耗时：" + (l2 -l1));
			
			
			summary = govdocSummaryManager.getSummaryById(affair.getObjectId());// 流程追溯的数据
																				// 这个防止的objectId值可能有问题
			long l3 = System.currentTimeMillis();
			LOGGER.info("公文查看summary界面fillComponentObj获取Summary耗时：" + (l3 -l2));
		}
		long l4 = System.currentTimeMillis();
		
		String openFrom = summaryVO.getOpenFrom();
		boolean isFormQuery = EdocOpenFrom.formQuery.name().equals(openFrom);
		boolean isFormStatistical = EdocOpenFrom.formStatistical.name().equals(openFrom);
		boolean ifFromstepBackRecord = EdocOpenFrom.stepBackRecord.name().equalsIgnoreCase(openFrom);
		boolean isFromrepealRecord = EdocOpenFrom.repealRecord.name().equalsIgnoreCase(openFrom);
		boolean isFromF8Reprot = EdocOpenFrom.F8Reprot.name().equalsIgnoreCase(openFrom);
		boolean isEdocStatics = "edocStatics".equals(openFrom);
		boolean isGlwd = EdocOpenFrom.glwd.name().equals(openFrom);
		boolean isGovLenPotent = "lenPotent".endsWith(openFrom) && "1".equals(summaryVO.getIsGovArchive());
		boolean isDocLib = "docLib".equals(openFrom);
		boolean isleaderPishi = "2".equals(summaryVO.getLeaderPishiType());
		boolean exchangeRelation = EdocOpenFrom.exchangeRelation.name().equals(openFrom);
		boolean exchangeFallback = EdocOpenFrom.exchangeFallback.name().equals(openFrom);
		if ("1".equals(summaryVO.getIsRecSendRel())) {
			isGlwd = true;
		}
		// SECURITY 访问安全检查
		ApplicationCategoryEnum moduleType = ApplicationCategoryEnum.edoc;
		if (!isFormQuery && !isFormStatistical && !ifFromstepBackRecord && !isFromrepealRecord && !isEdocStatics && !isGlwd && !isGovLenPotent && !isDocLib
				&& !isleaderPishi && !exchangeRelation && !exchangeFallback && !isFromF8Reprot) {
			Object summaryObj = DateSharedWithWorkflowEngineThreadLocal.getColSummary();
			if (summaryObj == null) {
				DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
			}
			if (!SecurityCheck.isLicit(AppContext.getRawRequest(), AppContext.getRawResponse(), moduleType, summaryVO.getUser(), affair.getObjectId(), affair,
					summary.getArchiveId())) {
				summaryVO.setErrorMsg("你没权限查看主题");
				return false;
			}
		}
		long l5 = System.currentTimeMillis();
		LOGGER.info("公文查看summary界面fillComponentObj获取验证是否有查看主题权限耗时：" + (l5 -l4));
		
		summaryVO.setSummary(summary);
		summaryVO.setAffair(affair);
		
		if("1".equals(summaryVO.getIsGovArchive())) {
			if (summary.getTempleteId() != null) {
				summaryVO.setTemplate(templateManager.getCtpTemplate(summary.getTempleteId()));
			}			
		}
		
		//当前公文打开为可回复状态
        summaryVO.getSwitchVo().setHasDealArea("listPending".equals(summaryVO.getOpenFrom()) && affair.getState().intValue()==StateEnum.col_pending.key());
		
		return true;
	}
	/*************************** 33333 公文查看界面显示    end ***************************/
	
	
	/*************************** 44444 查看界面处理保存 start ***************************/
	@Override
	public boolean fillFinishObj(GovdocDealVO dealVo) throws BusinessException {
		CtpAffair affair = affairManager.get(dealVo.getAffairId());
		EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
		// 待办校验
		if (affair == null || affair.getState() != StateEnum.col_pending.key()) {
			String errorMsg = WFComponentUtil.getErrorMsgByAffair(affair);
			if (Strings.isNotBlank(errorMsg)) {
				dealVo.setErrorMsg(errorMsg);
				return false;
			}
		}
		
		Comment comment = null;
		//从签收流程而来，则先将分办流程处理，意见为签收/分办的处理意见
		Object fromDistribute = AppContext.getRawRequest().getAttribute("fromDistribute");
		if (fromDistribute == null) {//非分办意见
			comment = govdocCommentManager.getCommnetFromRequest(OperationType.finish, affair.getMemberId(), affair.getObjectId());
		}
		if(summary.getTempleteId() != null) {
			CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
			dealVo.setTemplate(template);
		}
		
		dealVo.setAffair(affair);
		dealVo.setSummary(summary);
		dealVo.setComment(comment);
		try {
			dealVo.setOldSummary((EdocSummary)summary.clone());
		} catch(CloneNotSupportedException cse) {
			LOGGER.error("保存待发clone原公文对象出错", cse);
		}
		return true;
	}
	
	@Override
	public boolean fillZcdbObj(GovdocDealVO dealVo) throws BusinessException {
		CtpAffair affair = affairManager.get(dealVo.getAffairId());
		EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
		
		// 待办校验
		if (affair == null || affair.getState() != StateEnum.col_pending.key()) {
			String errorMsg = WFComponentUtil.getErrorMsgByAffair(affair);
			if (Strings.isNotBlank(errorMsg)) {
				dealVo.setErrorMsg(errorMsg);
				return false;
			}
		}
		// 从request对象中对象中获取意见
		Comment comment = govdocCommentManager.getCommnetFromRequest(OperationType.wait, affair.getMemberId(), affair.getObjectId());
		//直接分办时设置分办意见为草稿
		if("normal".equals(dealVo.getDistributeType())) {
			comment.setCtype(Comment.CommentType.draft.getKey());
		}
		
		if(summary.getTempleteId() != null) {
			CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
			dealVo.setTemplate(template);
		}
		
		dealVo.setAffair(affair);
		dealVo.setSummary(summary);
		dealVo.setComment(comment);
		try {
			dealVo.setOldSummary((EdocSummary)summary.clone());
		} catch(CloneNotSupportedException cse) {
			LOGGER.error("保存待发clone原公文对象出错", cse);
		}
		
		String configItem = collaborationApi.getPolicyByAffair(affair).getId();
		String category = PermissionFatory.getPermBySubApp(summary.getGovdocType(), configItem).getCategorty();
		Permission permission = permissionManager.getPermission(category, configItem,AppContext.currentAccountId());
		if (permission != null) {
			String baseAction = permission.getBasicOperation();
			if (baseAction.indexOf("FaDistribute") != -1) {
				dealVo.setDealAction("fensong");
			}
			if (baseAction.indexOf("ReSign") != -1) {
				dealVo.setDealAction("qianshou");
			}
		}
		
		return true;
	}
	
	@Override
	public boolean fillStepBackObj(GovdocDealVO dealVo) throws BusinessException {
		CtpAffair currentAffair = affairManager.get(dealVo.getAffairId());
		if(currentAffair == null) {
			dealVo.setErrorMsg("该公文已被撤销！");
			return false;
		}
		int state = currentAffair.getState().intValue();
		if(state != StateEnum.col_pending.key()) {
			if (state == StateEnum.col_stepBack.key()) {
				dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被回退！");
			} else if (state == StateEnum.col_takeBack.key()) {
				dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被取回！");
			} else {
				dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被撤销！");
			}	
			return false;
		}
		if(currentAffair.isDelete()) {
			dealVo.setErrorMsg("该公文已被删除！");
			return false;
		}
		String pishiFlag = "";
		if(!currentAffair.getMemberId().equals(dealVo.getCurrentUser().getId())){
			pishiFlag = govdocPishiManager.checkLeaderPishi(AppContext.currentUserId(), currentAffair.getMemberId());
			try {
				if(!"pishi".equals(pishiFlag)){
					boolean canDeal = GovdocHelper.checkAgent(currentAffair, true);
					if (!canDeal) {
						dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》不能被代理人回退！");
						return false;
					}
				}else{
	        		AffairUtil.addExtProperty(currentAffair,AffairExtPropEnums.dailu_pishi_mark, pishiFlag+AppContext.currentUserId());
	        	}
			} catch(Exception e) {
				LOGGER.error("", e);
			}
		}
		
        EdocSummary summary = govdocSummaryManager.getSummaryById(dealVo.getSummaryId());
        if(summary == null) {
        	dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被撤销！");
        	return false;
        }
        if(summary.getState() != 0) {
        	dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被撤销！");
        	return false;
        }
        dealVo.setSummary(summary);
        dealVo.setAffair(currentAffair);
        
        //回退意见
        Comment comment = dealVo.getComment();
        if(dealVo.getComment() == null) {
	        comment  = new Comment();
        }
        ParamUtil.getJsonDomainToBean("comment_deal", comment);
        Map parm = ParamUtil.getJsonDomain("comment_deal");
		if(parm.get("content_coll")!= null){
			String content_coll=(String)parm.get("content_coll");
			comment.setContent(content_coll);
        }
        comment.setModuleType(ModuleType.edoc.getKey());
		comment.setModuleId(summary.getId());
		comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
		comment.setCreateId(currentAffair.getMemberId());
		comment.setAffairId(currentAffair.getId());
		comment.setExtAtt3("collaboration.dealAttitude.rollback");
		if (!dealVo.getCurrentUser().getId().equals(currentAffair.getMemberId())) {
			comment.setExtAtt2(dealVo.getCurrentUser().getName());
		}
		comment.setPid(0L);
		comment.setPushMessage(false);
		if("pishi".equals(pishiFlag)){
			comment.setContent(comment.getContent()+"(由"+AppContext.currentUserName()+"代录)");
		}
		dealVo.setComment(comment);
		
		if(summary.getTempleteId() != null) {
			CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
			dealVo.setTemplate(template);
		}
		
        return true;
	}
	
	@Override
	public boolean fillAppointStepBackObj(GovdocDealVO dealVo) throws BusinessException {
		CtpAffair currentAffair = affairManager.get(dealVo.getAffairId());
		if(currentAffair == null) {
			dealVo.setErrorMsg("该公文已被撤销！");
			return false;
		}
		int state = currentAffair.getState().intValue();
		if(state != StateEnum.col_pending.key()) {
			if (state == StateEnum.col_stepBack.key()) {
				dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被回退！");
			} else if (state == StateEnum.col_takeBack.key()) {
				dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被取回！");
			} else {
				dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被撤销！");
			}	
			return false;
		}
		if(currentAffair.isDelete()) {
			dealVo.setErrorMsg("该公文已被删除！");
			return false;
		}
		if(!currentAffair.getMemberId().equals(dealVo.getCurrentUser().getId())){
			try {
				String pishiFlag = govdocPishiManager.checkLeaderPishi(AppContext.currentUserId(), currentAffair.getMemberId());
				if(!"pishi".equals(pishiFlag)){
					boolean canDeal = GovdocHelper.checkAgent(currentAffair, true);
					if (!canDeal) {
						dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》不能被代理人回退！");
						return false;
					}
				}else{
	        		AffairUtil.addExtProperty(currentAffair,AffairExtPropEnums.dailu_pishi_mark, pishiFlag+AppContext.currentUserId());
	        	}
			} catch(Exception e) {
				LOGGER.error("", e);
			}
		}
		
        EdocSummary summary = govdocSummaryManager.getSummaryById(dealVo.getSummaryId());
        if(summary == null) {
        	dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被撤销！");
        	return false;
        }
        if(summary.getState() != 0) {
        	dealVo.setErrorMsg("公文《" + currentAffair.getSubject() + "》已经被撤销！");
        	return false;
        }
        dealVo.setSummary(summary);
        dealVo.setAffair(currentAffair);
        dealVo.setMember(orgManager.getMemberById(dealVo.getAffair().getMemberId()));
        
        //指定回退意见
        Comment comment = dealVo.getComment();
        if(dealVo.getComment() == null) {
	        comment  = new Comment();
        }
        ParamUtil.getJsonDomainToBean("comment_deal", comment);
        Map parm = ParamUtil.getJsonDomain("comment_deal");
		if(parm.get("content_coll")!= null){
			String content_coll=(String)parm.get("content_coll");
			comment.setContent(content_coll);
        }
        comment.setModuleType(ModuleType.edoc.getKey());
        comment.setModuleId(summary.getId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        if(!dealVo.getCurrentUser().getId().equals(currentAffair.getMemberId())){
            comment.setExtAtt2(dealVo.getCurrentUser().getName());
        }
        comment.setCreateId(currentAffair.getMemberId());
        comment.setExtAtt3("collaboration.dealAttitude.rollback");
        comment.setPid(0L);
        dealVo.setComment(comment);
		
        if(summary.getTempleteId() != null) {
			CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
			dealVo.setTemplate(template);
		}
        return true;
	}
	
	@Override
	public boolean fillStepStopObj(GovdocDealVO dealVo) throws BusinessException {
		Long affairId = dealVo.getAffairId();
        if(affairId == null){
        	return false;
        }
        
        CtpAffair affair = affairManager.get(affairId);
        if (affair.getState().intValue() != StateEnum.col_pending.key()  && affair.getState().intValue() != StateEnum.col_pending_repeat_auto_deal.key()) {
        	if("zhuanfawenstop".equals(dealVo.getExtAtt1())) {
        		List<CtpAffair> list = affairManager.getAffairs(affair.getObjectId(),StateEnum.col_pending);
                if(Strings.isNotEmpty(list)) {
                	affair = list.get(0);
                } else {
                	dealVo.setErrorMsg(WFComponentUtil.getErrorMsgByAffair(affair));
                    return false;
                }
        	} else {
        		dealVo.setErrorMsg(WFComponentUtil.getErrorMsgByAffair(affair));
                return false;
        	}
        }
        //非转发文流程自动办结，才执行该逻辑
        if(!"zhuanfawenstop".equals(dealVo.getExtAtt1())) {
	        if(!affair.getMemberId().equals(dealVo.getCurrentUser().getId())){
	            // 检查代理，避免不是处理人也能处理了。
	            String pishiFlag = govdocPishiManager.checkLeaderPishi(AppContext.currentUserId(), affair.getMemberId());
	            if(!"pishi".equals(pishiFlag)){
	            	boolean canDeal = GovdocHelper.checkAgent(affair, true);
	            	if (!canDeal) {
	            		dealVo.setErrorMsg("当前代理人不能处理");
	            		return false;
	            	}
	            }else{
	        		AffairUtil.addExtProperty(affair,AffairExtPropEnums.dailu_pishi_mark, pishiFlag+AppContext.currentUserId());
	        	}
	        }
        }
        dealVo.setAffair(affair);
        
        //处理意见
        Comment comment = dealVo.getComment();
        if(!"new".equals(dealVo.getFrom())) {//公文处理界面终止
        	comment = new Comment();
        	ParamUtil.getJsonDomainToBean("comment_deal", comment, false);
        	Map<String,Object> commentDeal = ParamUtil.getJsonDomain("comment_deal");
        	if(commentDeal.get("content_coll")!= null){
				String content_coll=(String)commentDeal.get("content_coll");
				comment.setContent(content_coll);
	        }
        	comment.setExtAtt1(commentDeal.get("extAtt1").toString());//态度
        	comment.setExtAtt3("collaboration.dealAttitude.termination");
        }
        User user = dealVo.getCurrentUser();
        if (user.isAdmin()) {
            comment.setCreateId(user.getId());
        } else {
            comment.setCreateId(affair.getMemberId());
            comment.setAffairId(affairId);
        }
        // 由代理人终止需要写入处理人ID
		if(!user.getId().equals(affair.getMemberId()) && !user.isAdmin()) {
            comment.setExtAtt2(user.getName());
        }
        comment.setModuleType(ModuleType.edoc.getKey());
        comment.setPid(0L);
        comment.setPushMessage(false);
        comment.setModuleId(affair.getObjectId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
		// 保存终止时的意见
        comment.setCtype(CommentType.comment.getKey());
        dealVo.setComment(comment);
        
        EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
        dealVo.setSummary(summary);
        
		return true;
	}
	/**
	 * 修改状态和sendDate
	 * 
	 * @param edocSummaryId
	 * @param transferStatus
	 * @param signSummaryId
	 * @throws BusinessException
	 */
	public void directUpdateEdocSummaryAttr(Long edocSummaryId, TransferStatus transferStatus, Long signSummaryId) throws BusinessException {
		Map<String, Object> param = new HashMap<String, Object>();
		if (signSummaryId != null) {
			EdocSummary edocSummary1 = govdocSummaryManager.getSummaryById(signSummaryId);
			if (edocSummary1 != null && edocSummary1.getSendDate() != null) {
				param.put("sendDate", edocSummary1.getSendDate());
			} else {
				param.put("sendDate", new java.sql.Timestamp(System.currentTimeMillis()));
			}
			if (edocSummary1 != null && edocSummary1.getSignPerson() != null) {
				param.put("signPerson", edocSummary1.getSignPerson());
			}
		}
		param.put("transferStatus", transferStatus == null ? TransferStatus.defaultStatus.getKey() : transferStatus.getKey());
		govdocSummaryManager.update(edocSummaryId, param);
	}
	@Override
	public void saveSuperviseByDeal(EdocSummary summary, CtpAffair affair) throws BusinessException {
    	Map<String, Object> superviseMap = (Map<String, Object>) ParamUtil.getJsonDomain("superviseDiv");
		String isModifySupervise = (String) superviseMap.get("isModifySupervise");
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
		if ("1".equals(isModifySupervise)) {
			SuperviseMessageParam smp = new SuperviseMessageParam(true, summary.getImportantLevel(), summary.getSubject(), summary.getForwardMember(), summary.getStartMemberId());
			SuperviseEnum.EntityType superviseType;
			if (summary.getGovdocType() != null) {
				superviseType = SuperviseEnum.EntityType.govdoc;
			} else {
				superviseType = SuperviseEnum.EntityType.summary;
			}
			this.superviseManager.saveOrUpdateSupervise4Process(smp, summary.getId(), superviseType);
			// 督办写入流程日志中
			String supervisorIds = (String) superviseMap.get("supervisorIds");
			if (Strings.isNotBlank(supervisorIds)) {
				String[] ids = supervisorIds.split(",");
				String names = "";
				for (String str : ids) {
					names += orgManager.getMemberById(Long.valueOf(str)).getName() + ",";
				}
				govdocLogManager.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.govdocDuban, names.substring(0, names.length() - 1));
			}
		}
    }
	
    @Override
	public void saveSuperviseByZcdb(EdocSummary summary, CtpAffair affair) throws BusinessException {
    	Map<String, Object> superviseMap = (Map<String, Object>) ParamUtil.getJsonDomain("superviseDiv");
		String isModifySupervise = (String) superviseMap.get("isModifySupervise");
		if ("1".equals(isModifySupervise)) {
			DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
			SuperviseMessageParam smp = new SuperviseMessageParam(true, summary.getImportantLevel(), summary.getSubject(), summary.getForwardMember(), summary.getStartMemberId());
			if (summary.getGovdocType() != null) {
				this.superviseManager.saveOrUpdateSupervise4Process(smp, summary.getId(), SuperviseEnum.EntityType.govdoc);
			} else {
				this.superviseManager.saveOrUpdateSupervise4Process(smp, summary.getId(), SuperviseEnum.EntityType.summary);
			}
		}
    }
	
    
	/*************************** 44444 查看界面处理保存   end ***************************/
		
	
	
	/*************************** 66666 公文交换相关方法 start ***************************/
	/**
	 * 处理快速发文时在收文单位收文的触发
	 * @param info
	 */
	@Override
	public void sendIsQuickGovdoc(GovdocNewVO info) throws BusinessException {
		if(info.getIsQuickSend()){
			govdocExchangeManager.exchangeSend(info.getSummary(), info.getCurrentUser().getId(), info.getSenderAffair().getId(), null);
		}
	}

	/**
	 * 公文交换/转发文/转收文/联合发文/触发子流程/触发公文流程
     * 发起新流程，表单数据请提前保存好, 然后把formMasterId给我
     * 
     * @param templateId 子(新)流程所属模板Id
     * @param senderId 发起者Id
     * @param formMasterId 表单数据记录主键Id值
     * @param parentSummaryId 
     * @param newSumamryId :当前流程的summaryID
     * @throws Exception
     */
    public SendGovdocResult transSendColl(
    	EdocConstant.SendType sendType, 
    	Long templateId, 
    	Long senderId, 
    	Long formMasterId, 
    	Long parentSummaryId,
    	Long summaryId,
    	Long recieveOrgId, 
    	Integer bodyType, 
    	Boolean hasAtts,
    	Integer exchangeType) throws BusinessException {
    	
    	try {
    	
	        //1 校验模板是否存在
	        CtpTemplate template = templateManager.getCtpTemplate(templateId);
	        if(template == null){
	            throw new BusinessException();
	        }
	        
	        //2 获取公文发起人
	        V3xOrgMember sender = orgManager.getMemberById(senderId);
	        if(sender == null){
	            throw new BusinessException(ResourceUtil.getString("collaboration.error.common.create.flow.sender") + "senderId=" + senderId);//发起新流程失败，原因：发起者不存在。
	        }
	        
	        Date currentDate =  new Date();
	        GovdocNewVO newVo = new GovdocNewVO();
	        
	        //3 获取当前人
	        User user = new User();
	        user.setId(sender.getId());
	        user.setName(sender.getName());
	        user.setDepartmentId(sender.getOrgDepartmentId());
	        user.setLoginAccount(sender.getOrgAccountId());
//	        if(OrgUtil.isPlatformEntity(sender)){
//	        	user.setLoginAccount(template.getOrgAccountId());
//	        }
	        user.setAccountId(sender.getOrgAccountId());
	        newVo.setCurrentUser(user);
	        newVo.setCurrentDate(currentDate);
	        
	        //4 公文触发时发文单位参数为空，或部门交换时设置接收单位ID
	        if(recieveOrgId == null) {//触发
	        	recieveOrgId = sender.getOrgAccountId();
	        } else {
	        	V3xOrgUnit unit = orgManager.getUnitById(recieveOrgId);
	        	//若接收单位为部门，则接收单位为该部门所属单位
	        	if(unit!=null && "Department".equals(unit.getEntityType())) {
	        		recieveOrgId = unit.getOrgAccountId();
	        	}
	        }
	        
	        //5 构造新的summary对象
	        EdocSummary summary =(EdocSummary) XMLCoder.decoder(template.getSummary());
	        if (bodyType != null) {
	        	summary.setBodyType(bodyType + "");
			}
	        if(summaryId != null) {
	        	summary.setId(summaryId);
	        }else{
	        	summary.setIdIfNew();
	        	summary.putExtraAttr(GovdocHelper._IsNewGovdoc, "true");//是否是新建的公文，理论上这个方法都是新建的公文，不需要saveOrUpdate 减少开销；
	        }
			summary.setTempleteId(templateId);
	        summary.setStartUserId(senderId);
	        summary.setOrgAccountId(recieveOrgId);
	        summary.setOrgDepartmentId(user.getDepartmentId());
	        summary.setExchangeType(exchangeType);
	        summary.setGovdocType(GovdocUtil.getGovdocTypeByModuleType(template.getModuleType()));
	        summary.setCanEditAttachment(true);
	        //老公文类型没有交换，把交换归类于收文
	        if(summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.key()){
	        	summary.setEdocType(1);
	        }else{
	        	summary.setEdocType(GovdocUtil.getEdocTypeByGovdoc(summary.getGovdocType()));
	        }
			summary.setTransferStatus(GovdocUtil.getTransferStatusByModuleType(template.getModuleType()));
			summary.setState(EdocConstant.flowState.run.ordinal());
			summary.setFormRecordid(formMasterId);
	        summary.setFormAppid(template.getFormAppId());
	        //项目ID
	        if(GovdocParamUtil.isNull(summary.getProjectId()) && GovdocParamUtil.isNotNull(template.getProjectId())) {
	        	summary.setProjectId(template.getProjectId());
	        }
	        
	        //子流程
	        if(sendType == EdocConstant.SendType.child) {
	            summary.setNewflowType(EdocConstant.NewflowType.child.ordinal());
	        } else if(sendType == EdocConstant.SendType.auto) {
	        	summary.setNewflowType(EdocConstant.NewflowType.auto.ordinal());
	        } else {
	        	summary.setNewflowType(EdocConstant.NewflowType.main.ordinal());
	        }
	        newVo.setNewflowType(summary.getNewflowType());
	          
	        summary.setCreateTime(new Timestamp(currentDate.getTime()));
	        summary.setStartTime(new Timestamp(currentDate.getTime()));
	        //计算流程超期时间。把时间段换成具体时间点
	        GovdocHelper.setDeadlineData(summary);
	                
	        //复制模板的附件给当前协同
	        if(TemplateUtil.isHasAttachments(template)) {
	            String result = attachmentManager.copy(template.getId(), template.getId(), summary.getId(), summary.getId(), ApplicationCategoryEnum.edoc.key(),sender.getId(),sender.getOrgAccountId());//附件
	            if (hasAtts != null && (hasAtts || Strings.isNotBlank(result))) {
	            	summary.setHasAttachments(true);
				}
	        }
	        
	        //保存公文归档数据
	        if(templateId != null && Strings.isNotBlank(summary.getAdvancePigeonhole())) {
	        	try {
		        	JSONObject jo = new JSONObject(summary.getAdvancePigeonhole());
					String advancePigeonhole = jo.toString();
		            summary.setAdvancePigeonhole(advancePigeonhole);
	        	} catch(Exception e) {
	        		LOGGER.error("公文高级归档报错", e);
	        		throw new BusinessException(e);
	        	}
	        }
	        
	        newVo.setNewBusiness("1");
	        newVo.setTrackType(TrackEnum.all.ordinal());
	        newVo.setSummary(summary);
	        newVo.setTemplate(template);
	        
	        //保存新公文单数据ctp_content_all，用于保存公文触发数据
	        GovdocBodyVO bodyVo = new GovdocBodyVO();
	        newVo.setBodyVo(bodyVo);
	        
	        bodyVo.setFormContent(MainbodyType.FORM, null, formMasterId, new Date());
	        if(!isFromNewCollPage(sendType)) {
	        	govdocContentManager.saveExchangeFormContent(newVo);
	        }
	        
	        //保存新公文正文数据ctp_content_all
	        CtpContentAll bodyContent = null;
	        EdocSummary parentSummary = null;
	        if(sendType == EdocConstant.SendType.child || sendType == EdocConstant.SendType.auto) {
	        	if(parentSummaryId != null) {//公文触发/触发子流程，复制原文的正文内容 
	        		parentSummary = govdocSummaryManager.getSummaryById(parentSummaryId);
	        		if(parentSummary != null) {
		        		govdocMarkManager.fillMarkParamByAuto(newVo, parentSummary);

		        		CtpContentAll transContent = GovdocContentHelper.getTransBodyContentByModuleId(parentSummary.getId());
		        		if(transContent != null) {
		        			bodyContent = (CtpContentAll)transContent.clone();
		        			bodyContent.setSort(1);
		        		}
		        	}
	        	} 
	        	//表单触发则复制
	        	if(parentSummary == null) {
	        		CtpContentAll transContent = GovdocContentHelper.getBodyContentByModuleId(template.getId());
	        		if(transContent != null) {
	        			bodyContent = (CtpContentAll)transContent.clone();
	        		}
	        	}
	        	if(bodyContent != null) {
	        		bodyContent.setIdIfNew();
	        		if(bodyContent.getContentType().intValue() != MainbodyType.HTML.getKey()) {
		        		Long fileId = null;
		        		try {
		        			fileId = fileManager.copyFileBeforeModify(Long.valueOf(bodyContent.getContent()));
						} catch (Exception e) {
							LOGGER.info("复制正文或签章错误:" + bodyContent.getTitle(), e);
						}
		        		bodyContent.setContent(String.valueOf(fileId));
	        		}
					
	        		//设置正文类型
	     			summary.setBodyType(String.valueOf(bodyContent.getContentType()));
	     			//保存新公文正文
	     			bodyVo.setBodyContent(bodyContent);
	        		govdocContentManager.saveExchangeBodyContent(newVo);
	        	}
	        }
	        
	        // 7 非流程触发时，从原文单中获取映射数据
	        this.fillSummaryByMapping(newVo);

	        //保存公文文号
	        this.govdocMarkManager.saveSendMark(newVo);
	        
	        //复制签章
	        /*if(parentSummaryId!=null) {
	        	iSignatureHtmlManager.save(parentSummaryId, summary.getId());
	        }*/
	        String subject = "";
	        if(sendType == EdocConstant.SendType.child || sendType == EdocConstant.SendType.auto){
	        	subject = GovdocUtil.makeSubject4NewWF(template, summary, user);
	        }else {
	        	subject = GovdocUtil.makeSubject(template, summary, user);
	        }

	            
	        DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
	        
	        //9 获取已发Affair
	        CtpAffair senderAffair = GovdocAffairHelper.createExchangeAffair(newVo);
	        
	        //10生产最终标题
	        if(Strings.isBlank(subject)){
	        	subject = "{"+ResourceUtil.getString("collaboration.subject.default")+"}";
	        }else {
	        	//存在新标题 且和原来的标题不一致的时候更新撤销回退记录
	        	if(senderAffair != null && !subject.equals(senderAffair.getSubject())) {
	        		affairManager.updateFormCollSubject(summary.getId(),subject);
	        	}
	        }
	        if(null != summary.getAutoRun() && summary.getAutoRun()){
	        	senderAffair.setAutoRun(summary.getAutoRun());
	        }
	        //11 发起流程
	        Map<String,String> wfRetMap =  new HashMap<String,String>();
	        if(sendType == EdocConstant.SendType.immediate 
        			|| (sendType == EdocConstant.SendType.exchange)) {
        		String processId = summary.getProcessId();
        		//没有待发条件的要判断一次是否后续节点是否需要选人，以及是否有分支条件。如果有则进入发起人的待发(参考FlowFactoryImpl)
                //1.流程中有分支2.有节点需要选人 
        		CPMatchResultVO resultVo = isSelectPersonCondition(template,user.getId(), formMasterId, user.getLoginAccount());
        		boolean addFirstNode = false;
        		if(resultVo.isPop()){
        			addFirstNode = true;
        		}
        		String conditon_Str = getConditonStr(resultVo);
        		LOGGER.info("conditon_Str:="+conditon_Str);
        		//将分支条件结果传入触发流程
        		wfRetMap = govdocWorkflowManager.runcase(processId, null, null, null, conditon_Str.toString(), user, summary, sendType, template,addFirstNode);
        	} else if(sendType == EdocConstant.SendType.child || sendType == EdocConstant.SendType.auto) {
        		wfRetMap = govdocWorkflowManager.runcase(sendType, user, summary,template);
        	}
	        
	        String caseId = wfRetMap.get("caseId");
	        String prcocessId = wfRetMap.get("prcocessId");
	        String RelationDataId = wfRetMap.get("RelationDataId");
	        
	        summary.setCaseId(Long.valueOf(caseId));
	        summary.setProcessId(prcocessId);
	        
	        senderAffair.setProcessId(summary.getProcessId());
	        senderAffair.setCaseId(summary.getCaseId());
	        if(Strings.isNotBlank(RelationDataId)){
	        	senderAffair.setRelationDataId(Long.valueOf(RelationDataId));
	        }
	        
	        //12 保存已发数据
	    	this.affairManager.save(senderAffair);
	    	newVo.setSenderAffair(senderAffair);
	        
	    	//13 保存公文主表数据
	    	GovdocHelper.updateCurrentNodesInfo(summary, false);
	    	if(summary.getExtraDataContainer().get(GovdocHelper._IsNewGovdoc) != null){
	    		govdocSummaryManager.saveEdocSummary(summary, true);
	    	}else{
	    		govdocSummaryManager.saveOrUpdateEdocSummary(summary);
	    	}
	    	
	        //14 子流程时保存发起者附言
	        newVo.setComment(ResourceUtil.getString("collaboration.system.auto.start"));
	        Comment comment = newVo.getComment();
	        if(comment != null && (sendType == EdocConstant.SendType.child || sendType == EdocConstant.SendType.auto)) {
	        	comment.setModuleType(ApplicationCategoryEnum.edoc.getKey());
	            comment.setModuleId(summary.getId());
	            comment.setCreateId(senderId);
	            comment.setCreateDate(currentDate);
	            govdocCommentManager.insertComment(comment);
	            //子流程发起需要保存流程日志 OA-171463
	            processLogManager.insertLog(user, Long.parseLong(prcocessId), -1l, ProcessLogAction.sendColl,-1L,DateUtil.currentDate(),new ArrayList<ProcessLogDetail>(), (String)wfRetMap.get("nextMembers"));
            	
	        }
	        
	        //15 创建或者更新公文统计数据
	        govdocStatManager.saveOrUpdateEdocStat(summary, user);
	      
	        //16 没有文档中心插件不归档
	        if(AppContext.hasPlugin("doc")) {
	        	summary.setAdvancePigeonhole(null);
	        	govdocDocManager.savePigeonhole(newVo, sendType);
	        }
	        
	        //17 全文检索 
	        if (AppContext.hasPlugin("index")) {
	            this.indexApi.add(summary.getId(), ApplicationCategoryEnum.edoc.key());
	        }
	        //18 全文签批
	        qwqpManager.saveEdocFormFileRelations(newVo);
	        //定时任务超期提醒和提前提醒
	        //CollaborationJob.createQuartzJobOfSummary(summary, workTimeManager);
			/** 18、发送自动跳过的事件(异步)，异步回调方法CollaborationListener.transOnAutoSkip */
			GovdocHelper.fireAutoSkipEvent(this);
			
	        //19 返回交换对象
	        SendGovdocResult sendGovdocResult = new SendGovdocResult();
	        sendGovdocResult.setSentAffair(senderAffair);
	        sendGovdocResult.setSummary(summary);
	        return sendGovdocResult;
    	} catch(Exception e) {
    		//e.printStackTrace();
            LOGGER.error("触发公文流程报错", e);
            throw new BusinessException(e);
    	}
    }
    private String getConditonStr(CPMatchResultVO resultVo) {
        StringBuilder conditon_Str =new StringBuilder();
        Set<String> allSelectNodes=resultVo.getAllSelectNodes();
        conditon_Str.append("{\"condition\":[");
        for (String value : allSelectNodes) {
            conditon_Str.append("{\"nodeId\":\""+value+"\",");
            conditon_Str.append("\"isDelete\":\"false\"},");
        }
        Set<String> allNotSelectNodes=resultVo.getAllNotSelectNodes();
        for (String value : allNotSelectNodes) {
            conditon_Str.append("{\"nodeId\":\""+value+"\",");
            conditon_Str.append("\"isDelete\":\"true\"},");
        }
        String vauleString=StringUtils.removeEnd(conditon_Str.toString(), ",");
        return vauleString+"]}";
    }
    private CPMatchResultVO isSelectPersonCondition(CtpTemplate template, long sender,long masterId,long loginAccountId) throws BPMException {
        //选分支,选人的判断,isPop 为true则保存待发
        WorkflowBpmContext wfContext = new WorkflowBpmContext();
        wfContext.setProcessId(null);
        wfContext.setCaseId(-1L);
        wfContext.setCurrentActivityId(null);
        wfContext.setCurrentWorkitemId(-1L);
        wfContext.setMastrid(String.valueOf(masterId));
        wfContext.setFormData(String.valueOf(masterId));
        wfContext.setStartUserId(String.valueOf(sender));
        wfContext.setCurrentUserId(String.valueOf(sender));
        wfContext.setProcessTemplateId(String.valueOf(template.getWorkflowId()));
        wfContext.setAppName(ModuleType.edoc.name());
//      wfContext.setStartAccountId(String.valueOf(member.getOrgAccountId()));
//      wfContext.setCurrentAccountId(String.valueOf(member.getOrgAccountId()));
        wfContext.setStartAccountId(String.valueOf(loginAccountId));
        wfContext.setCurrentAccountId(String.valueOf(loginAccountId));
        WorkflowApiManager wapi=(WorkflowApiManager) AppContext.getBean("wapi");
        CPMatchResultVO result = wapi.transBeforeInvokeWorkFlow(wfContext, new CPMatchResultVO());
        
        return result;
    }
	private boolean isFromNewCollPage(EdocConstant.SendType sendType){
    	if(sendType == EdocConstant.SendType.resend
    			||sendType == EdocConstant.SendType.normal
    			){
    		return true;
    	}else{
    		return false;
    	}
    }
	/**
	 * 更新edocSummary流转状态
	 * @param colSummaryId
	 * @param transferStatus
	 * @throws BusinessException
	 */
	public void updateEdocSummaryTransferStatus(Long colSummaryId, TransferStatus transferStatus) throws BusinessException {
		EdocSummary edocSummary = govdocSummaryManager.getSummaryById(colSummaryId);
		if (edocSummary != null) {
			int transferStatusValue = transferStatus.getKey();
			if (edocSummary.getEdocType() == EdocTypeEnum.sendEdoc.ordinal()) {// 发文
				// 已发行的发文，如果要求更新为已结束，则不更新状态。
				if (transferStatusValue == TransferStatus.sendEnd.getKey()
						&& edocSummary.getTransferStatus() == TransferStatus.sendPublished.getKey()) {
					transferStatusValue = -1;
				}
			}
			if (transferStatusValue != -1 && edocSummary.getTransferStatus() != TransferStatus.takebackWaitSend.getKey()
					&& edocSummary.getTransferStatus() != TransferStatus.takebackZCDB.getKey()) {
				edocSummary.setTransferStatus(transferStatusValue);
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("transferStatus", transferStatusValue);
				if (transferStatusValue == TransferStatus.receiveSigned.getKey()) {
					if (Strings.isBlank(edocSummary.getSignPerson())) {
						param.put("signPerson", AppContext.currentUserName());
					}
				}
				if (edocSummary.getPackTime() == null && transferStatus.getKey() == TransferStatus.sendPublished.getKey()) {// 分发时间为空并且需要修改已分发状态
					param.put("packTime", new java.sql.Timestamp(System.currentTimeMillis()));
				}
				govdocSummaryManager.update(edocSummary.getId(), param);
			}
		}
	}
	
	@Override
	public void fillSummaryBeforeSave(GovdocBaseVO baseVo) throws BusinessException {
		if (baseVo instanceof GovdocNewVO) {
			GovdocNewVO newInfo = (GovdocNewVO)baseVo;
			if(newInfo.getIsQuickSend()){
				baseVo.getSummary().setPackTime(new java.sql.Timestamp(System.currentTimeMillis()));
				baseVo.getSummary().setExchangeSendAffairId(baseVo.getSenderAffair().getId());
			}
			if(baseVo.getSummary().getArchiveId() != null){//简单设置归档的属性，后面设置没有做更新；
				baseVo.getSummary().setHasArchive(true);
			}
		} else if(baseVo instanceof GovdocDealVO) {
			GovdocDealVO dealInfo = (GovdocDealVO)baseVo;
			if(dealInfo.isFensong()) {
				baseVo.getSummary().setPackTime(new java.sql.Timestamp(System.currentTimeMillis()));
				baseVo.getSummary().setExchangeSendAffairId(baseVo.getAffair().getId());
			}
		}
	}
	public boolean copyAndSaveSuperviseFromTemplete(V3xOrgMember sender,SuperviseMessageParam smp, Long summaryId, Long templeteId,Map<String, Object> param) throws BusinessException {
        CtpSuperviseDetail detail = superviseManager.getSupervise(SuperviseEnum.EntityType.template.ordinal(), templeteId);
        if(detail != null) {
            Date superviseDate = null;
            Long terminalDate = detail.getTemplateDateTerminal();
            if(null!=terminalDate){
                superviseDate = Datetimes.addDate(new Date(), terminalDate.intValue());
            }else if(detail.getAwakeDate() != null) {
                superviseDate = detail.getAwakeDate();
            }
            List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
            Set<Long> sIdSet = new HashSet<Long>();
            for(CtpSupervisor supervisor:supervisors){
                sIdSet.add(supervisor.getSupervisorId());
            }
            List<CtpSuperviseTemplateRole> roleList = superviseManager.findRoleByTemplateId(templeteId);

            V3xOrgRole orgRole = null;
            for(CtpSuperviseTemplateRole role : roleList){
                if(null==role.getRole() || "".equals(role.getRole())){
                    continue;
                }
                if(role.getRole().toLowerCase().equals(WorkFlowConstants.ORGENT_META_KEY_SEDNER.toLowerCase())){
                    sIdSet.add(sender.getId());
                }
                if(role.getRole().toLowerCase().equals(WorkFlowConstants.ORGENT_META_KEY_SEDNER.toLowerCase() + OrgConstants.Role_NAME.DepManager.name().toLowerCase())){
                    orgRole = orgManager.getRoleByName(OrgConstants.Role_NAME.DepManager.name(), sender.getOrgAccountId());
                    if(null!=orgRole){
                        List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(sender.getId());
                        for(V3xOrgDepartment dep : depList){
                            List<V3xOrgMember> managerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
                            for(V3xOrgMember mem : managerList){
                                sIdSet.add(mem.getId());
                            }
                        }
                    }
                }
                if(role.getRole().toLowerCase().equals(WorkFlowConstants.ORGENT_META_KEY_SEDNER.toLowerCase() + WorkFlowConstants.ORGENT_META_KEY_SUPERDEPMANAGER.toLowerCase())){
                    orgRole = orgManager.getRoleByName(WorkFlowConstants.ORGENT_META_KEY_SUPERDEPMANAGER, sender.getOrgAccountId());
                    if(null!=orgRole){
                        List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(sender.getId());
                        for(V3xOrgDepartment dep : depList){
                        List<V3xOrgMember> superManagerList = orgManager.getMembersByRole(dep.getId(), orgRole.getId());
                            for(V3xOrgMember mem : superManagerList){
                                sIdSet.add(mem.getId());
                            }
                        }
                    }
                }
            }
            if(!sIdSet.isEmpty()){
                StringBuilder idBf = new StringBuilder();
                StringBuilder nameBf = new StringBuilder();
                int i = 0;
                for (Long id : sIdSet) {
                    V3xOrgMember mem = orgManager.getMemberById(id);
                    if(mem!=null){
                        if(nameBf.length() > 0){
                            nameBf.append(",");
                            idBf.append(",");
                        }
                        idBf.append(mem.getId());
                        nameBf.append(mem.getName());
                    }
                }
				SuperviseSetVO ssvo = new SuperviseSetVO();
				ssvo.setTitle(detail.getTitle());
				ssvo.setSupervisorIds(idBf.toString());
				ssvo.setSupervisorNames(nameBf.toString());
				ssvo.setAwakeDate(DateUtil.format(new Date(), DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
				superviseManager.saveOrUpdateSupervise4Process(ssvo, smp, summaryId, SuperviseEnum.EntityType.govdoc, param);
                return true;
            }
        }
        return false;
    }
	/*************************** 66666 公文交换相关方法   end ***************************/
	
	@Override
	public void saveUpdateAttInfo(int attSize,Long summaryId,List<ProcessLog> logs) throws BusinessException {
		this.edocManager.saveUpdateAttInfo(attSize, summaryId, logs);
	}
	
	@Override
	public String getFullArchiveNameByArchiveId(Long archiveId) throws BusinessException {
		return this.edocManager.getFullArchiveNameByArchiveId(archiveId);
	}
	
	@Override
	public String getShowArchiveNameByArchiveId(Long archiveId) throws BusinessException {
		return this.edocManager.getShowArchiveNameByArchiveId(archiveId);
	}
	
	@Override
	public void setArchiveIdToAffairsAndSendMessages(EdocSummary summary,CtpAffair affair,boolean needSendMessage) throws BusinessException {
		this.edocManager.setArchiveIdToAffairsAndSendMessages(summary, affair, needSendMessage);
	}
	    
	/*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setGovdocExchangeManager(GovdocExchangeManager govdocExchangeManager) {
		this.govdocExchangeManager = govdocExchangeManager;
	}
	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setGovdocSignetManager(GovdocSignetManager govdocSignetManager) {
		this.govdocSignetManager = govdocSignetManager;
	}
	public void setGovdocDocManager(GovdocDocManager govdocDocManager) {
		this.govdocDocManager = govdocDocManager;
	}
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setGovdocContentManager(GovdocContentManager govdocContentManager) {
		this.govdocContentManager = govdocContentManager;
	}
	public void setGovdocStatManager(GovdocStatManager govdocStatManager) {
		this.govdocStatManager = govdocStatManager;
	}
	public void setGovdocMarkManager(GovdocMarkManager govdocMarkManager) {
		this.govdocMarkManager = govdocMarkManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
	public void setGovdocAffairManager(GovdocAffairManager govdocAffairManager) {
		this.govdocAffairManager = govdocAffairManager;
	}
	public void setQwqpManager(QwqpManager qwqpManager) {
		this.qwqpManager = qwqpManager;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}
	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/
	
}
