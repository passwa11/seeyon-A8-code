package com.seeyon.v3x.edoc.manager;

import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.office.HtmlHandWriteManager;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.phrase.manager.CommonPhraseManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.*;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.common.security.SecurityCheckParam;
import com.seeyon.v3x.edoc.domain.*;
import com.seeyon.v3x.edoc.util.*;
import com.seeyon.v3x.edoc.util.EdocOpenFromUtil.EdocSummaryType;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryBO;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;


/**
  * <p>Title: 公文H5Manager</p>
  * <p>Description: 公文H5Manager</p>
  * <p>Copyright: Copyright (C) 2015 Seeyon, Inc. All rights reserved. </p>
  * <p>Company: 北京致远协创软件有限公司</p>
  * @author muyx
  */
public class EdocH5ManagerImpl implements EdocH5Manager {
	private static final Log LOGGER  = LogFactory.getLog(EdocH5ManagerImpl.class);

	private static final String ERROR_KEY          = "error_msg";

	public static final String AFFAIR_IS_NULL = "isNull";
	public static final String AFFAIR_IS_DELETE = "isDelete";
	public static final String AFFAIR_IS_BACK = "isBack";
	public static final String AFFAIR_IS_STOP = "isStop";
	public static final String AFFAIR_IS_DONE = "isDone";
	public static final String AFFAIR_NEXT_IS_DONE = "isNextDone";

    private OrgManager orgManager;
    private EnumManager enumManagerNew;
    private EdocListManager edocListManager;
    private EdocSummaryManager edocSummaryManager;
    private AffairManager affairManager;
    private EdocManager edocManager;
    private EdocFormManager edocFormManager;
    private TemplateManager templateManager;
    private V3xHtmDocumentSignatManager htmSignetManager;
    private PermissionManager permissionManager;
    private HtmlHandWriteManager htmlHandWriteManager;
    private FileManager fileManager;
    private AttachmentManager attachmentManager;
    private CommonPhraseManager phraseManager;
    private WorkflowApiManager wapi    ;
    private EdocZcdbManager edocZcdbManager;
    private EdocMessagerManager edocMessagerManager;
    private IndexManager indexManager;
    private EdocOpinionManager edocOpinionManager;
    private WorkTimeManager workTimeManager;
    private AppLogManager appLogManager;
    private EdocMarkManager edocMarkManager;
    private SuperviseManager superviseManager;

	@Override
	public EdocSummaryBO getEdocSummaryBO(Long summaryId, Long affairId, String openFrom, Map<String,Object> params) throws BusinessException {
		String listType = EdocOpenFromUtil.getListType(openFrom);

		User user = AppContext.getCurrentUser();
		EdocSummaryBO summary = new EdocSummaryBO();
		CtpAffair ctpAffair = affairManager.get(affairId);
		//来自文档中心、关联文档、督办时，不需要校验Affair对象
		if(!"docLib".equals(openFrom) && !"lenPotent".equals(openFrom) && !"glwd".equals(openFrom) && !"supervise".equals(openFrom)) {
			if(!this.isValidAffair(affairId, ctpAffair.getNodePolicy(),summary.getErrorRet())) {
				return summary;
			}
		}
		if(ctpAffair==null && summaryId!=-1) {
			ctpAffair = this.getAffairByIdOrSummaryId(affairId, summaryId);
        }
		if(ctpAffair == null) {
			summary.getErrorRet().put(ERROR_KEY, ResourceUtil.getString("edoc.alert.right.noThemeView"));
			return summary;
		}
		if(ctpAffair!=null && ctpAffair.getState().intValue()== StateEnum.col_done.key()) {//如果从待办中打开已经处理过的公文
			if(EdocSummaryType.listPending.name().equals(listType)) {
				listType = EdocSummaryType.listDone.name();
			}
		}

		//借阅不进行权限校验，和PC保持一致
		if(!"lenPotent".equals(openFrom)){
		  //权限校验
	        SecurityCheckParam param = new SecurityCheckParam(ApplicationCategoryEnum.edoc, user, ctpAffair.getId());
	        param.setAffair(ctpAffair);
	        param.addExt("openFrom", openFrom);
	        param.addExt("docResId", ParamUtil.getString(params, "docResId", ""));
	        param.addExt("baseObjectId", ParamUtil.getString(params, "baseObjectId", ""));
	        param.addExt("baseApp", ParamUtil.getString(params, "baseApp", ""));

	        /*
	        param.addExt("fromEditor", request.getParameter("fromEditor"));
	        param.addExt("eventId", request.getParameter("eventId"));
	        param.addExt("relativeProcessId", request.getParameter("relativeProcessId"));*/

	        SecurityCheck.isLicit(param);
	        if(!param.getCheckRet()){
	            String msg = param.getCheckMsg();
	            if(Strings.isBlank(msg)){
	                msg = ResourceUtil.getString("edoc.error.security.warning");//越权访问
	            }
	            summary.getErrorRet().put(ERROR_KEY, msg);
	            return summary;
	        }
		}


		EdocSummary edocSummary = edocManager.getEdocSummaryById(ctpAffair.getObjectId(),true);

		//查找公文单意见元素显示。
	    //公文处理意见回显到公文单,排序
        long flowPermAccout = EdocHelper.getFlowPermAccountId(user.getLoginAccount(), edocSummary, templateManager);
        summary.setFlowPermAccout(flowPermAccout);
		FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(edocSummary.getFormId(), flowPermAccout);
		Map<String, EdocOpinionModel> map = edocManager.getEdocOpinion(edocSummary, displayConfig);
        List<V3xHtmDocumentSignature> signatuers = htmSignetManager.findBySummaryIdAndType(edocSummary.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
        Map<String, Object> strMap = EdocOpinionDisplayUtil._convertOpinionToString(map,displayConfig,ctpAffair,false,signatuers, true, false);
        if(null!=edocSummary){
        	int edocType = edocSummary.getEdocType();
        	String category = "";
        	if(edocType == EdocEnum.edocType.sendEdoc.ordinal()){
        		category = "edoc_send_permission_policy";
        	}else if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
        		category = "edoc_rec_permission_policy";
        	}else if(edocType == EdocEnum.edocType.signReport.ordinal()){
        		category = "edoc_qianbao_permission_policy";
        	}

        	String opins = this.getOpinionName(category, user.getAccountId());
        	summary.setOpins(opins);
        }

      //获取发起人姓名
        String memberName = Functions.showMemberName(edocSummary.getStartUserId());
        //获取发起人头像
        String memberPicture = Functions.getAvatarImageUrl(edocSummary.getStartUserId());
        summary.setMemberdName(memberName);
        summary.setMemberPictureSrc(memberPicture);
        summary.setEdocSummary(edocSummary);
        summary.setCreateTime(EdocUtil.showDate(edocSummary.getCreateTime()));
        CtpEnumBean secretLeveleMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());//得到公文密级的枚举
		String secretLevel= this.getLabel(edocSummary.getSecretLevel(), secretLeveleMetadata) ;
		summary.setSecretLevel(secretLevel);
		EdocOpinion edocOpinion = edocManager.findBySummaryIdAndAffairId(edocSummary.getId(), affairId);
		//查询当前处理人意见附件
		if(edocOpinion != null){
			if(edocOpinion.getOpinionType().intValue() != EdocOpinion.OpinionType.provisionalOpinoin.ordinal()
					&& edocOpinion.getOpinionType().intValue()!= EdocOpinion.OpinionType.draftOpinion.ordinal()) {
				edocOpinion = null;
			}
			if(edocOpinion != null) {
				List<Attachment> attList = attachmentManager.getByReference(edocSummary.getId(), edocOpinion.getId());
				edocOpinion.setOpinionAttachments(attList);
			}
		}
		summary.setNowNodeOpinion(edocOpinion);

      //只查找正文的附件。
        List<Attachment> attachments = attachmentManager.getByReference(edocSummary.getId(),edocSummary.getId());
        List<Attachment>  attachmentList = new ArrayList<Attachment>();
        int attachmentSize = 0;
        int relatedDocSize = 0;
        String trimFileName = "";
        for(Attachment att : attachments){
        	if(att.getFilename().contains("<br/>")){
        		trimFileName = att.getFilename().replace("<br/>", "");
        	}else{
        		trimFileName = att.getFilename();
        	}
        	if(0==att.getType()){
        		attachmentSize++;
        	}else if(2==att.getType()){
        		relatedDocSize++;
        	}
        	att.setFilename(trimFileName);
        	attachmentList.add(att);
        }
        summary.setAttachmentSize(attachmentSize);
        summary.setRelatedDocSize(relatedDocSize);
        summary.setAttachmentList(attachmentList);

		String nodePermissionPolicy = "shenpi";
		String[] nodePolicyFromWorkflow = null;
		//affair的已发事项没有activityId
		//从待分发列表打开 登记单，点收文关联发文，出现关联的发文列表，再打开发文时，affair为null,没有传affairId，这里加上非空判断
		if(ctpAffair != null && ctpAffair.getActivityId() !=null){
		    nodePolicyFromWorkflow= wapi.getNodePolicyIdAndName(ApplicationCategoryEnum.edoc.name(), edocSummary.getProcessId(), String.valueOf(ctpAffair.getActivityId()));
		}
		//得到当前处理权限录入意见的显示位置
        if(nodePolicyFromWorkflow != null){
        	nodePermissionPolicy = nodePolicyFromWorkflow[0];
        	//流程取过来的权限名，替换特殊空格[160 -> 32]
        	if(nodePermissionPolicy != null){nodePermissionPolicy=nodePermissionPolicy.replaceAll(new String(new char[]{(char)160}), " ");}
        	//为了解决bug，将协同的知会英文名 inform改为 公文的zhihui
        	if("inform".equals(nodePermissionPolicy)){
        	    nodePermissionPolicy = "zhihui";
        	}

        }
        if ("qianfa".equals(nodePermissionPolicy) && edocSummary.getSigningDate() == null) {
        	edocSummary.setSigningDate(new java.sql.Date(System.currentTimeMillis()));
        }
		EnumNameEnum edocTypeEnum= EdocUtil.getEdocMetadataNameEnum(edocSummary.getEdocType());
		Long senderId = edocSummary.getStartUserId();
        V3xOrgMember sender = orgManager.getMemberById(senderId);
		Long flowPermAccountId = EdocHelper.getFlowPermAccountId(sender.getOrgAccountId(),edocSummary,  templateManager);
		Permission permission = permissionManager.getPermission(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
		//将公文单解析成html
		FormParseExtInfo formParseExtInfo = FormParseUtil.formatFormContentForH5(edocSummary, "", permission.getFlowPermId(),ctpAffair);

		summary.setEdocFormContent(formParseExtInfo.getContent());

		Boolean hasOtherOpinion = edocFormManager.hasFormElement(edocSummary.getFormId(), "otherOpinion");
		Map<String, Object> opHtml = this.optionToJs(strMap, hasOtherOpinion);
        opHtml.put("opAtts", getOpinionAtt(map, hasOtherOpinion));
        summary.setOpinions(opHtml);

		summary.setOpinionPolicy(String.valueOf(permission.getNodePolicy().getOpinionPolicy()));
		summary.setCancelOpinionPolicy(String.valueOf(permission.getNodePolicy().getCancelOpinionPolicy()));
		summary.setDisAgreeOpinionPolicy(String.valueOf(permission.getNodePolicy().getDisAgreeOpinionPolicy()));
		summary.setAttribute(permission.getNodePolicy().getAttitude());
		summary.setSubmitStyleCfg(permission.getNodePolicy().getSubmitStyle());

		if(ctpAffair.getState().intValue() == StateEnum.col_pending.key() && null != ctpAffair.isFinish()
				&& ctpAffair.isFinish()){
			ctpAffair.setFinish(false);
		}
		summary.setCtpAffair(ctpAffair);
		List<String> actions = new ArrayList<String>();
		 //基本草
		 List<String> baseActions  = permissionManager.getBasicActionList(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
		 //常用操作
         List<String> commonActions  = permissionManager.getCommonActionList(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
         //高级操作
         List<String> advancedActions  = permissionManager.getAdvaceActionList(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
         actions.addAll(baseActions);
         actions.addAll(commonActions);
         actions.addAll(advancedActions);
         String actionStr = StringUtil.arrayToString(actions.toArray(new String[actions.size()]));
         summary.setActions(actionStr);
         if(edocSummary.getTempleteId()!=null){
        	 CtpTemplate ctpTemplate = templateManager.getCtpTemplate(edocSummary.getTempleteId());
			if (ctpTemplate!=null) {
				CtpTemplate pTemplate = templateManager.getCtpTemplate(ctpTemplate.getFormParentid());
				Long templeteProcessId = ctpTemplate.getWorkflowId();
				if (ctpTemplate.getFormParentid() != null && !"text".equals(pTemplate.getType())) {// 系统模板的个人模板
					templeteProcessId = pTemplate.getWorkflowId();
				}
				if(ctpTemplate.getWorkflowId()!=null){
					summary.setTemplateProcessId(ctpTemplate.getWorkflowId());
				}
				if(null!=templeteProcessId){
					summary.setProcessTemplate(true);
				}
			}

         }
       //设置公文已读
         Integer sub_state = ctpAffair.getSubState();
         if (sub_state == null || sub_state.intValue() == SubStateEnum.col_pending_unRead.key()) {
        	 ctpAffair.setSubState(SubStateEnum.col_pending_read.key());
             try {

               //更新第一次查看时间
                 Date nowTime = new Date();
                 long firstViewTime = workTimeManager.getDealWithTimeValue(ctpAffair.getReceiveTime(), nowTime, ctpAffair.getOrgAccountId());
                 ctpAffair.setFirstViewPeriod(firstViewTime);
                 ctpAffair.setFirstViewDate(nowTime);

                 affairManager.updateAffair(ctpAffair);

               //要把已读状态写写进流程
                 if (ctpAffair.getSubObjectId() != null) {
                     wapi.readWorkItem(ctpAffair.getSubObjectId());
                 }
             } catch (BusinessException e) {
                 LOGGER.error("更新公文已读状态错误", e);
             }
         }

         //获取默认节点
         int appEnumCode = EdocEnum.getTemplateCategory(edocSummary.getEdocType());
         Map<String, String> defaultNode = edocSummaryManager.getEdocDefaultNode(ApplicationCategoryEnum.valueOf(appEnumCode).name(), edocSummary.getOrgAccountId());
         summary.setDefaultNode(defaultNode);

         //验证当前流程是否是指定回退流程
         boolean specialStepBack = (edocSummary.getCaseId()==null||Long.valueOf(0).equals(edocSummary.getCaseId()))?false:wapi.isInSpecialStepBackStatus(edocSummary.getCaseId());
         summary.setSpecialStepBack(specialStepBack);

         summary.setListType(listType);
         summary.setIsCanComment(EdocOpenFromUtil.isCanComment(openFrom));

         if(formParseExtInfo.getFormOpinionConfig() != null) {
        	 summary.setOptionType(formParseExtInfo.getFormOpinionConfig().getOpinionType());
         }

         return summary;
	}

	/**
	 * 获取公文意见对应的附件
	 * @param map
	 * @return
	 */
	public Map<String,Object> getOpinionAtt(Map<String, EdocOpinionModel> map, Boolean hasOtherOpinion) {
		Map<String,Object> results = new HashMap<String,Object>();
		for(Iterator<String> it = map.keySet().iterator();it.hasNext();){
			String element = it.next();
			if(hasOtherOpinion) {
				continue;
			}
			EdocOpinionModel model = map.get(element);
			List<EdocOpinion> opinions = model.getOpinions();
			for(EdocOpinion opinion : opinions) {
				if (opinion.getOpinionType().intValue() == EdocOpinion.OpinionType.draftOpinion.ordinal())
					continue;
				List<Attachment> tempAtts = null;
				if(null != opinion.getPolicy() && opinion.getPolicy().equals(EdocOpinion.FEED_BACK)){
					Long subOpinionId = opinion.getSubOpinionId();
					if(subOpinionId != null){
						tempAtts = EdocHelper.getOpinionAttachmentsNotRelationDoc(opinion.getSubEdocId(),subOpinionId);
					}
				}else{
					tempAtts = opinion.getOpinionAttachments();
				}
				if(tempAtts != null){
					if("senderOpinion".equals(element)){
						Long id = opinion.getId();
						results.put(id.toString(), tempAtts);
					}else{
						Long affairId = opinion.getAffairId();
						results.put(affairId.toString(), tempAtts);
					}
				}
			}
		}
		return results;
	}

	 @Override
	public String transDealEdoc(EdocManagerModel edocManagerModel, Map<String, String> wfParamMap) throws BusinessException {
        EdocSummary summary =null;
        CtpAffair affair = null;
    	EdocOpinion signOpinion = null;
	    affair = affairManager.get(edocManagerModel.getAffairId());
	    if(affair != null && Integer.valueOf(StateEnum.col_done.key()).equals(affair.getState().intValue())){
	    	throw new BusinessException(ResourceUtil.getString("edoc.error.do.with"));//此公文已被处理
	    }

	    signOpinion = edocManagerModel.getSignOpinion();
	    signOpinion.setCreateUserId(affair.getMemberId());
	    signOpinion.setPolicy(affair.getNodePolicy());
        if(!edocManagerModel.getAffairTrack().equals(affair.getTrack())){
        	affair.setTrack(edocManagerModel.getAffairTrack());
        	affairManager.updateAffair(affair);
        }
       	if(Integer.valueOf(TrackEnum.all.ordinal()).equals(edocManagerModel.getAffairTrack())){
        	edocManager.setTrack(edocManagerModel.getAffairId(), true, "");
        }else if(Integer.valueOf(TrackEnum.no.ordinal()).equals(edocManagerModel.getAffairTrack())){
        	edocManager.setTrack(edocManagerModel.getAffairId(), false, "");
        }
        signOpinion.setIdIfNew();

        //由暂存提交时，先删除暂存时的意见记录
        String oldOpinionIdStr=edocManagerModel.getOldOpinionIdStr();
        if(null != oldOpinionIdStr && !"".equals(oldOpinionIdStr)){
        	Long oldOpinionId=Long.parseLong(oldOpinionIdStr);
        	edocManager.deleteEdocOpinion(oldOpinionId);
        }

        //保存附件
        String relateInfo = ParamUtil.getString(wfParamMap, "fileJson", "[]");
        List<Map> files = JSONUtil.parseJSONString(relateInfo, List.class);
        try {
        	List<Attachment> attList = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.edoc, edocManagerModel.getSummaryId(), signOpinion.getId(), files);
        	if(!attList.isEmpty()){
        		attachmentManager.create(attList);
        		signOpinion.setHasAtt(true);
        		signOpinion.setOpinionAttachments(attList);
        	}
		} catch (Exception ex) {
			LOGGER.error(ex);
			throw new BusinessException(ex);
		}

        summary = edocManager.getEdocSummaryById(edocManagerModel.getSummaryId(), true);
        edocManagerModel.setOldOpinionIdStr("");

        //是否停留最后一次意见
        edocManager.deleteOpinionByWay(affair, edocManagerModel.getOptionWay());

        Map<String, Map<String,Object>> map = new HashMap<String,Map<String,Object>>();
        Map<String, String> condition = new HashMap<String,String>();
		try {
			condition = WorkflowUtil.getPopNodeConditionValues(edocManagerModel.getPopNodeCondition(), map);
		} catch (JSONException e1) {
			LOGGER.error(e1);
		}
		edocManagerModel.setProcessId(summary.getProcessId());
	    edocManagerModel.setAffair(affair);
	    edocManagerModel.setNodeId(affair.getActivityId());
	    edocManagerModel.setEdocSummary(summary);
        String processXml = wfParamMap.get("process_xml");
        String readyObjectJSON = wfParamMap.get("readyObjectJSON");
        String workflowNodePeoplesInput = wfParamMap.get("workflow_node_peoples_input");
		String workflowNodeConditionInput = wfParamMap.get("workflow_node_condition_input");
		String processChangeMessage =  wfParamMap.get("processChangeMessage");

		processXml = WorkflowUtil.getTempProcessXml(processXml);

        String ret = null;

        //记录流程日志，并发送消息
        String messageDataListJSON = wfParamMap.get("process_message_data");
        edocMessagerManager.sendOperationTypeMessage(messageDataListJSON, summary,affair);

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("opinion", edocManagerModel.getSignOpinion());
        params.put("userId", AppContext.currentUserId());
        params.put("docMark", summary.getDocMark());
        params.put("docMark2", summary.getDocMark2());
        params.put("serialNo", summary.getSerialNo());
        params.put("process_xml", processXml);
        params.put("readyObjectJSON", readyObjectJSON);
        params.put("workflow_node_peoples_input", workflowNodePeoplesInput);
        params.put("workflow_node_condition_input", workflowNodeConditionInput);
        params.put("process_message_data",messageDataListJSON);
        params.put("processChangeMessage", processChangeMessage);
        try {
            edocManager.transFinishWorkItem(affair, params);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage(), e);
        }

		return ret;
	}

	 @Override
	 public boolean transDoZCDB(EdocManagerModel edocManagerModel, User user, Map<String, String> wfParamMap) throws BusinessException {
		 	boolean status = true;
	        long summaryId = edocManagerModel.getSummaryId();
	        long affairId = edocManagerModel.getAffairId();
	        String processId = edocManagerModel.getProcessId();
	        boolean isRelieveLock = true;
	        EdocSummary summary = null;
	        try{
	        	String spMemberId = "";
		        String superviseDate = "";
		        String supervisorNames = "";
		        String title = "";
		        summary = edocManager.getEdocSummaryById(summaryId, true);
		        CtpAffair affair = affairManager.get(affairId);
		        if(affair != null && Integer.valueOf(StateEnum.col_done.key()).equals(affair.getState())){
		        	throw new BusinessException(ResourceUtil.getString("edoc.error.do.with"));//此公文已被处理
		        }

	  	        if(!edocManagerModel.getAffairTrack().equals(affair.getTrack())){
		        	affair.setTrack(edocManagerModel.getAffairTrack());
		        	affairManager.updateAffair(affair);
		        }
	  	       	if(Integer.valueOf(TrackEnum.all.ordinal()).equals(edocManagerModel.getAffairTrack())){
		        	edocManager.setTrack(edocManagerModel.getAffairId(), true, "");
		        }else if(Integer.valueOf(TrackEnum.no.ordinal()).equals(edocManagerModel.getAffairTrack())){
		        	edocManager.setTrack(edocManagerModel.getAffairId(), false, "");

		        }
		        summary = edocManager.getEdocSummaryById(summaryId, true);

		        String oldOpinionIdStr=edocManagerModel.getOldOpinionIdStr();
		        if(null != oldOpinionIdStr && !"".equals(oldOpinionIdStr))
		        {//删除原来意见,上传附件等
		        	Long oldOpinionId=Long.parseLong(oldOpinionIdStr);
		        	edocManager.deleteEdocOpinion(oldOpinionId);
		        }
	        	String process_xml = wfParamMap.get("process_xml");
	        	String readyObjectJSON = wfParamMap.get("readyObjectJSON");
	        	String processChangeMessage = wfParamMap.get("processChangeMessage");

	  	        EdocOpinion edocOpinion = edocManagerModel.getSignOpinion();
	  	        if(null!=edocOpinion && Strings.isBlank(edocOpinion.getPolicy())){
	  	        	edocOpinion.setPolicy(affair.getNodePolicy());
	  	        }

	            //保存附件
	            String relateInfo = ParamUtil.getString(wfParamMap, "fileJson", "[]");
	            List<Map> files = JSONUtil.parseJSONString(relateInfo, List.class);
	            try {
	            	List<Attachment> attList = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.edoc, edocManagerModel.getSummaryId(), edocOpinion.getId(), files);
	            	if(!attList.isEmpty()){
	            		attachmentManager.create(attList);
	            		edocOpinion.setHasAtt(true);
	            		edocOpinion.setOpinionAttachments(attList);
	            	}
	    		} catch (Exception ex) {
	    			LOGGER.error(ex);
	    			throw new BusinessException(ex);
	    		}

	  	        //记录流程日志，并发送消息
	            String messageDataListJSON = wfParamMap.get("process_message_data");
	            edocMessagerManager.sendOperationTypeMessage(messageDataListJSON, summary,affair);

		        if(null!=supervisorNames && !"".equals(supervisorNames) && null!=spMemberId && !"".equals(spMemberId) && null!=superviseDate && !"".equals(superviseDate)){
		        	this.edocManager.zcdb(affair, edocOpinion, title, spMemberId, supervisorNames,superviseDate, summary,
		        	        processId, user.getId()+"",process_xml,readyObjectJSON, processChangeMessage,new ArrayList<Long[]>());
		        }else{
		        	this.edocManager.zcdb(summary, affair, edocManagerModel.getSignOpinion(), processId, user.getId()+"",process_xml,readyObjectJSON, processChangeMessage,false,new ArrayList<Long[]>());
		        }
		        //设置新的当前待办人
		        EdocHelper.updateCurrentNodesInfo(summary, false);
		        edocManager.update(summary);
		        //暂存待办提醒时间设置--start
		        String zcdbTimeStr="";
		        EdocZcdb edocZcdb=edocZcdbManager.getEdocZcdbByAffairId(affairId);
		        if(zcdbTimeStr!=null && !("".equals(zcdbTimeStr))){
		        	Date zcdbTime = Datetimes.parse(zcdbTimeStr, Datetimes.datetimeWithoutSecondStyle);
			        if(edocZcdb!=null){
			        	edocZcdbManager.updateEdocZcdbByAffairId(affairId, zcdbTime);
			        	//新增定时消息
			        	edocManager.deleteZcdbQuartz(affairId,zcdbTime);
			        	edocManager.createZcdbQuartz(affairId,zcdbTime);
			        }else{
			        	edocZcdb=new EdocZcdb();
			        	edocZcdb.setIdIfNew();
			        	edocZcdb.setAffairId(affairId);
			        	edocZcdb.setZcdbTime(new Timestamp(zcdbTime.getTime()));
			        	edocZcdbManager.saveEdocZcdb(edocZcdb);
			        	//新增定时消息
			        	edocManager.createZcdbQuartz(affairId,zcdbTime);
			        }

		        }else{//如果没设置，也要保存一条记录，因为查询在办列表需要连接此表
		        	if(edocZcdb!=null){
		        		edocZcdbManager.deleteEdocZcdb(edocZcdb.getId());
			        	edocZcdb=new EdocZcdb();
			        	edocZcdb.setIdIfNew();
			        	edocZcdb.setAffairId(affairId);
			        	edocZcdb.setZcdbTime(null);
			        	edocZcdbManager.saveEdocZcdb(edocZcdb);
		        	}else{
			        	edocZcdb=new EdocZcdb();
			        	edocZcdb.setIdIfNew();
			        	edocZcdb.setAffairId(affairId);
			        	edocZcdb.setZcdbTime(null);
			        	edocZcdbManager.saveEdocZcdb(edocZcdb);
		        	}
		        }
	        }catch(Exception e){
	        	status = false;
	        	LOGGER.error("H5暂存待办时抛出异常：", e);
	        }finally{
	        	if(isRelieveLock)
	        		wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));

	        }
	        return status;

	 }
	 public String getEdocBodyContentRoot(Long affairId) throws BusinessException {

			if(null==affairId){
				return "";
			}
			CtpAffair ctpAffair = affairManager.get(affairId);
			EdocSummary edocSummary = edocManager.getEdocSummaryById(ctpAffair.getObjectId(),true);
			EdocBody firstBody = null;
			if(edocSummary != null && edocSummary.getEdocBodies()!=null && edocSummary.getEdocBodies().size()>0) {
            	Iterator<EdocBody> it = edocSummary.getEdocBodies().iterator();
            	while(it.hasNext()) {
            		EdocBody body = (EdocBody)it.next();
            		if(body.getContentNo() == 0) {
            			firstBody = body;
            		}
            	}
            }
			String bodyType = firstBody.getContentType();
			String path  = "";
			if(!"HTML".equals(bodyType)){
				String contentId =  firstBody.getContent();
				V3XFile tempFile = fileManager.getV3XFile(Long.valueOf(contentId));
				String filePath = this.fileManager.getFolder(tempFile.getCreateDate(), true)+ File.separator ;
				try {
					path = CoderFactory.getInstance().decryptFileToTemp(filePath);
				} catch (Exception e) {
				    LOGGER.error("", e);
				}
			}

		 return path;
	 }

	private Map<String,Object> optionToJs(Map<String, Object> hs, Boolean hasOtherOpinion) {
        Map<String,Object> map = new HashMap<String, Object>();
        List<List<String>> opinionsArray = new ArrayList<List<String>>();
        Iterator<String> it = hs.keySet().iterator();
        String szTemp=null;
        while(it.hasNext()) {
            String key = it.next();
        	//拟文意见
        	if("senderOpinionList".equals(key)||"senderOpinionAttStr".equals(key))
        		continue;

        	if(!hasOtherOpinion) {
        		szTemp = hs.get(key).toString();
            	List<String> list = new ArrayList<String>();
            	list.add(key);
            	list.add(szTemp);
        		opinionsArray.add(list);
        	}
        }
        List<EdocOpinion> sendOpinionObj = (List<EdocOpinion>) hs.get("senderOpinionList");
        List<EdocOpinion> sendOpinionObjCopy = new ArrayList<EdocOpinion>();
        for(EdocOpinion edocOpinion : sendOpinionObj){
        	//edocOpinion.setEdocSummary(null);//别这么写， hibernate坑死你
        	EdocOpinion edocOpinion2 = new EdocOpinion();
//        	edocOpinion2 = edocOpinion.cloneEdocOpinion();
        	edocOpinion2.setId(edocOpinion.getId());
        	edocOpinion2.setCreateTime(edocOpinion.getCreateTime());
        	edocOpinion2.setContent(edocOpinion.getContent());
        	edocOpinion2.setStrCreateTime(EdocUtil.showDate(edocOpinion.getCreateTime()));
        	sendOpinionObjCopy.add(edocOpinion2);
        }
        map.put("opinions", opinionsArray);
        map.put("sendOpinionStr", sendOpinionObjCopy);

        return map;
	}

	private String getOpinionName(String category , long accountId)throws BusinessException {

		List<Permission> list = permissionManager.getPermissionsByCategory(category, accountId);
		StringBuilder opinions = new StringBuilder();
		for(Permission perm : list){
		    opinions.append(perm.getName());
		    opinions.append(",");
		}
		if(opinions.length() > 0){
		    opinions.deleteCharAt(opinions.length() - 1);
		}
		return opinions.toString();
	}

	private String getLabel(String itemValue, CtpEnumBean metadata){
		CtpEnumItem itms = metadata.getItem(itemValue);

		if (itms==null) return null;
		String label = "";
		if(itemValue != null) {

			if(Strings.isNotBlank(metadata.getResourceBundle())){ //在原数据中定义了resourceBundle
				label = ResourceBundleUtil.getString(metadata.getResourceBundle(), itms.getLabel());
			}else{
				label = ResourceUtil.getString(itms.getLabel());
			}

			if(label == null){
				return itms.getLabel();
			}
		}


		return label;
	}


	/**
	 * 用于文档中心中弹出协同，因协同有部门/单位归档之分
	 * @param affairId
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	private CtpAffair getAffairByIdOrSummaryId(Long affairId, Long summaryId) throws BusinessException {
		CtpAffair affair = null;
		if(!Long.valueOf(-1).equals(affairId)){
			affair = affairManager.get(affairId);
			return affair;
		}
		affair = affairManager.getSenderAffair(Long.valueOf(summaryId));
		if(affair == null){
			//获得从已发删除的affair
    			DetachedCriteria criteria = DetachedCriteria
                .forClass(CtpAffair.class)
                .add(Restrictions.eq("objectId", Long.valueOf(summaryId)))
                .add(Restrictions.eq("delete", true))
                .add(Restrictions.in("state",
                        new Object[] { StateEnum.col_sent.key(),
                                StateEnum.col_waitSend.key() }));
	        List<CtpAffair> list = DBAgent.findByCriteria(criteria);

	        if (Strings.isNotEmpty(list)) {
	        	affair = list.get(0);
	        }else{
	        	//在文档中心打开部门归档的公文，由于部门归档doc_resource表的source_id是affairId，所以这里传的summaryId实际是affairId
	        	affair=affairManager.get(Long.valueOf(summaryId));
	        }
		}
		return affair;

	}

	@Override
	public String transStepback(EdocManagerModel edocManagerModel, Map params) throws BusinessException {
        long summaryId = edocManagerModel.getSummaryId();
        long affairId = edocManagerModel.getAffairId();
        String processId = edocManagerModel.getProcessId();
        boolean isRelieveLock = true;
        User user = edocManagerModel.getUser();
        EdocSummary summary = null;
        try{
	        CtpAffair affair = affairManager.get(affairId);
	        if(affair == null) {
	        	return AFFAIR_IS_NULL;
	        } else if(affair.isDelete()) {
	        	return AFFAIR_IS_DELETE;
	        } else if(affair.getState().intValue()== StateEnum.col_stepBack.key()) {
	        	return AFFAIR_IS_BACK;
	        } else if(affair.getState().intValue()== StateEnum.col_stepStop.key()) {
	        	return AFFAIR_IS_STOP;
	        } else if(affair.getState().intValue()== StateEnum.col_done.key()) {
	        	return AFFAIR_IS_DONE;
	        }
	        if(summaryId == -1) {
	        	summaryId = affair.getObjectId();
	        	edocManagerModel.setSummaryId(summaryId);
	        }
	        summary = edocManager.getEdocSummaryById(summaryId, true);
  	        //若当前节点人员已经回复，则修改将原来的回复意见
  	        EdocOpinion oldOpinion = edocManager.findBySummaryIdAndAffairId(summaryId, affairId);
  	        if(oldOpinion != null) {
  	        	edocManagerModel.setSignOpinion(oldOpinion);
  	        }
  	        EdocOpinion edocOpinion = saveFiles(params, edocManagerModel);
  	        edocOpinion.affairIsTrack = false;//EdocOpinion里面的affairIsTrack 看到想哭，怎么能这么设计~~~

  	        Map<String, Object> paramMap = new HashMap<String, Object>();
  	        paramMap.put("oldOpinion", oldOpinion);
  	        paramMap.put("trackWorkflowType", "false");
  	        Long _templeteId = summary.getTempleteId();
  	        if(null != summary && null != _templeteId){
  	        	CtpTemplate ctpTemplate = templateManager.getCtpTemplate(_templeteId);
  	        	if(null != ctpTemplate){
  	        		Integer canTrackWorkflow = ctpTemplate.getCanTrackWorkflow();
  	        		if(null != canTrackWorkflow && canTrackWorkflow.intValue() == 1){
  	        			paramMap.put("trackWorkflowType", "1");
  	        		}
  	        	}
  	        }
	        //true:成功回退 false:不允许回退
	        boolean ok = edocManager.stepBack(summaryId, affairId, edocOpinion, paramMap);
	        //设置新的当前待办人
	        EdocHelper.updateCurrentNodesInfo(summary, Boolean.TRUE);
	        //记录应用日志
	        if(ok){
	        	appLogManager.insertLog(user, 317, user.getName(),summary.getSubject());
	        }
	        //全文检索
	        if (AppContext.hasPlugin("index")) {
	        	indexManager.update(summaryId, ApplicationCategoryEnum.edoc.getKey());
	        }
	        //记录操作时间
            affairManager.updateAffairAnalyzeData(affair);
        } catch(Exception e) {
        	LOGGER.error("H5回退时抛出异常：", e);
        	return "false";
        } finally {
        	if(isRelieveLock) {
        		wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
        		wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(AppContext.currentUserId()));
        	}
        	try {
            	//解锁正文文单
            	edocManager.unLockSummary(user.getId(),summary);
            }catch(Exception e){
            	LOGGER.error("解锁正文文单抛出异常：",e);
            }
        }
        return "true";
	}

	@Override
	public String transRepeal(EdocManagerModel edocManagerModel, Map params) throws BusinessException {
        long summaryId = edocManagerModel.getSummaryId();
        long affairId = edocManagerModel.getAffairId();
        String processId = edocManagerModel.getProcessId();
        boolean isRelieveLock = true;
        User user = edocManagerModel.getUser();
        EdocSummary summary = null;
        try{
	        CtpAffair affair = affairManager.get(affairId);
	        if(affair == null) {
	        	return AFFAIR_IS_NULL;
	        } else if(affair.isDelete()) {
	        	return AFFAIR_IS_DELETE;
	        } else if(affair.getState().intValue()== StateEnum.col_stepBack.key()) {
	        	return AFFAIR_IS_BACK;
	        } else if(affair.getState().intValue()== StateEnum.col_stepStop.key()) {
	        	return AFFAIR_IS_STOP;
	        } else if(affair.getState().intValue()== StateEnum.col_done.key()) {
	        	return AFFAIR_IS_DONE;
	        }
	        if(summaryId == -1) {
	        	summaryId = affair.getObjectId();
	        	edocManagerModel.setSummaryId(summaryId);
	        }
	        summary = edocManager.getEdocSummaryById(summaryId, true);
	        //收文撤销，取回，数据到达待分发。
			if(1==summary.getEdocType()) {
				edocManager.h5CancelRegister(summary);
			}
  	        EdocOpinion edocOpinion = saveFiles(params,edocManagerModel);//edocManagerModel.getSignOpinion();
  	        int result = edocManager.cancelSummary(user.getId(), summaryId, affair, edocOpinion.getContent(), "cancelColl", edocOpinion);
	        if(result == 0){
	        	//全文检索
		        if (AppContext.hasPlugin("index")) {
		        	indexManager.update(summaryId, ApplicationCategoryEnum.edoc.getKey());
		        }
		        //撤销流程事件
				CollaborationCancelEvent event = new CollaborationCancelEvent(this);
				event.setSummaryId(summary.getId());
				event.setUserId(user.getId());
				event.setMessage(edocOpinion.getContent());
				EventDispatcher.fireEvent(event);

				//发送消息给督办人，更新督办状态，并删除督办日志、删除督办记录、删除催办次数
				superviseManager.updateStatus2Cancel(summaryId);
	        } else if(result==1 || result==-1) {
	        	//流程已结束
    	        String alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "edoc.state.end.alert", ResourceUtil.getString("edoc.symbol.opening.chevron")+summary.getSubject()+ ResourceUtil.getString("edoc.symbol.closing.chevron"));
    	        if(alertStr.indexOf("$")>-1){
	        		alertStr = alertStr.replace("$", "");
	        	}
    	        return alertStr;
	        }
        } catch(Exception e) {
        	LOGGER.error("H5回退时抛出异常：", e);
        	return "false";
        } finally {
        	if(isRelieveLock) {
        		wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
        		wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(AppContext.currentUserId()));
        	}
        	try {
            	//解锁正文文单
            	edocManager.unLockSummary(user.getId(),summary);
            }catch(Exception e){
            	LOGGER.error("解锁正文文单抛出异常：",e);
            }
        }
        return "true";
	}

	public boolean isValidAffair(Long affairId,String pageNodePolicy, Map<String, String> ret) {
        boolean canDeal = true;
        //状态检验
        String msg = edocManager.checkAffairValid(affairId==null?"-1":String.valueOf(affairId),pageNodePolicy);
        if(Strings.isNotBlank(msg)){
            canDeal = false;
            ret.put(ERROR_KEY, ResourceUtil.getString(msg));
        }else{//校验公文终止
        	//处理时才需要验证当前节点是否已终止
        	if(ret.get("isOnlyView")!=null && ret.get("isOnlyView")=="false") {
        		CtpAffair affair=null;
				try {
					affair = affairManager.get(affairId);
					if(affair != null){
		    			if(affair.getState().equals(StateEnum.col_done.key())
		    					&& affair.getSubState().equals(SubStateEnum.col_done_stepStop.key())){
		    				canDeal = false;
		    				String forwardMemberId = affair.getForwardMember();
		    				int forwardMemberFlag = 0;
		    				String forwardMember = null;
		    				if(Strings.isNotBlank(forwardMemberId)){
		    					try {
		    						forwardMember = getOrgManager().getMemberById(Long.parseLong(forwardMemberId)).getName();
		    						forwardMemberFlag = 1;
		    					}
		    					catch (Exception e) {
		    						LOGGER.error("", e);
		    					}
		    				}
		    				String appName= ResourceUtil.getString("application."+affair.getApp()+".label");
		    		    	msg = ResourceUtil.getString("collaboration.state.invalidation.alert", affair.getSubject(), ResourceUtil.getString("collaboration.state.10.stepstop"),appName, forwardMemberFlag, forwardMember);
		    	            ret.put(ERROR_KEY, ResourceUtil.getString(msg));
		    	    	}
		    		}
				} catch (BusinessException e) {
					LOGGER.error("", e);
				}
        	}
        }
        return canDeal;
    }
	/**
	 * 公文移交
	 */
	public String transEdocTransfer(EdocManagerModel edocManagerModel, Long transferMemberId, Map<String, String> params) throws BusinessException {
		    long summaryId = edocManagerModel.getSummaryId();
	        long affairId = edocManagerModel.getAffairId();
	        User user = edocManagerModel.getUser();
	        EdocSummary summary = null;
	        CtpAffair affair = affairManager.get(affairId);
	        if(summaryId == -1) {
	        	summaryId = affair.getObjectId();
	        	edocManagerModel.setSummaryId(summaryId);
	        }
	        summary = edocManager.getEdocSummaryById(summaryId, true);
  	        //若当前节点人员已经回复，则修改将原来的回复意见
  	        EdocOpinion oldOpinion = edocManager.findBySummaryIdAndAffairId(summaryId, affairId);
  	        if(oldOpinion != null) {
  	        	edocManagerModel.setSignOpinion(oldOpinion);
  	        }
  	        EdocOpinion transferOpinion = saveFiles(params, edocManagerModel);
  	        transferOpinion.setEdocSummary(summary);
  	        transferOpinion.setOpinionType(EdocOpinion.OpinionType.transferOpinion.ordinal());
  	        transferOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
  	       String result =  edocManager.transTransferEdoc(user, affair, summary, transferOpinion, transferMemberId,null);
  	       if(Strings.isNotBlank(result)){
  	    	   return result;
  	       }
  	       return "";

	}
	//保存附件
	private EdocOpinion saveFiles(Map<String,String> param, EdocManagerModel edocManagerModel) throws BusinessException {

		EdocOpinion edocOpinion = edocManagerModel.getSignOpinion();
		String fileJson = ParamUtil.getString(param, "fileJson","[]");
		List<Map> files = JSONUtil.parseJSONString(fileJson, List.class);
        try {
        	List<Attachment> attList = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.edoc, edocManagerModel.getSummaryId(), edocOpinion.getId(), files);
        	if(!attList.isEmpty()){
        		attachmentManager.create(attList);
        		edocOpinion.setHasAtt(true);
        		edocOpinion.setOpinionAttachments(attList);
        	}
        } catch (Exception ex) {
        	LOGGER.error(ex);
        	throw new BusinessException(ex);
        }
        return edocOpinion;
	}

	public boolean checkEdocMarkisUsed(String markStr, String edocId, String summaryOrgAccountId) {
		return edocMarkManager.isUsed(markStr, edocId, summaryOrgAccountId);
	}

	public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

    public void setEdocListManager(EdocListManager edocListManager) {
        this.edocListManager = edocListManager;
    }

    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public EdocManager getEdocManager() {
		return edocManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public EdocFormManager getEdocFormManager() {
		return edocFormManager;
	}

	public void setEdocFormManager(EdocFormManager edocFormManager) {
		this.edocFormManager = edocFormManager;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public V3xHtmDocumentSignatManager getHtmSignetManager() {
		return htmSignetManager;
	}

	public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
		this.htmSignetManager = htmSignetManager;
	}


	public PermissionManager getPermissionManager() {
		return permissionManager;
	}


	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}


	public OrgManager getOrgManager() {
		return orgManager;
	}


	public EnumManager getEnumManagerNew() {
		return enumManagerNew;
	}


	public EdocListManager getEdocListManager() {
		return edocListManager;
	}


	public EdocSummaryManager getEdocSummaryManager() {
		return edocSummaryManager;
	}


	public HtmlHandWriteManager getHtmlHandWriteManager() {
		return htmlHandWriteManager;
	}


	public void setHtmlHandWriteManager(HtmlHandWriteManager htmlHandWriteManager) {
		this.htmlHandWriteManager = htmlHandWriteManager;
	}


	public FileManager getFileManager() {
		return fileManager;
	}


	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}


	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}


	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}


	public CommonPhraseManager getPhraseManager() {
		return phraseManager;
	}


	public void setPhraseManager(CommonPhraseManager phraseManager) {
		this.phraseManager = phraseManager;
	}

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public EdocZcdbManager getEdocZcdbManager() {
		return edocZcdbManager;
	}

	public void setEdocZcdbManager(EdocZcdbManager edocZcdbManager) {
		this.edocZcdbManager = edocZcdbManager;
	}

	public EdocMessagerManager getEdocMessagerManager() {
		return edocMessagerManager;
	}

	public void setEdocMessagerManager(EdocMessagerManager edocMessagerManager) {
		this.edocMessagerManager = edocMessagerManager;
	}

	public IndexManager getIndexManager() {
		return indexManager;
	}

	public void setIndexManager(IndexManager indexManager) {
		this.indexManager = indexManager;
	}

	public EdocOpinionManager getEdocOpinionManager() {
		return edocOpinionManager;
	}

	public void setEdocOpinionManager(EdocOpinionManager edocOpinionManager) {
		this.edocOpinionManager = edocOpinionManager;
	}

	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}


	public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
		this.edocMarkManager = edocMarkManager;
	}

	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
}
