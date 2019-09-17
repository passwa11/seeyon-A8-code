package com.seeyon.apps.govdoc.manager.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CommentExtAtt3Enum;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocDocManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;

/**
 * 公文归档管理方法实现
 * @author tanggl
 *
 */
public class GovdocDocManagerImpl implements GovdocDocManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocDocManagerImpl.class);
	
	private DocApi docApi;
	private AffairManager affairManager;
	private PermissionManager permissionManager;
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocLogManager govdocLogManager;
	private GovdocFormManager govdocFormManager;
	private GovdocManager govdocManager;
	private CAPFormManager capFormManager;
	
	@Override
	@AjaxAccess
	public String transPigeonholeDeleteStepBackDoc(List<String> collIds, Long destFolderId) throws BusinessException {
		StringBuilder result = new StringBuilder();
		if (CollectionUtils.isEmpty(collIds))
			return result.toString();
		// 归档时同一协同已被回退的文档删除
		List<Long> needDeleteDocs = new ArrayList<Long>();
		Map<Long, List<Long>> objectDocMap = new HashMap<Long, List<Long>>();
		List<String> types = new ArrayList<String>();
		types.add(String.valueOf(ApplicationCategoryEnum.edoc.getKey()));
		List<DocResourceBO> reses = docApi.findDocResourcesByType(destFolderId, types);
		if (!CollectionUtils.isEmpty(reses)) {
			CtpAffair affairPig = null;
			List<Long> affairIds = null;
			for (DocResourceBO docResourceBO : reses) {
				affairPig = affairManager.get(docResourceBO.getSourceId());
				if (affairPig != null && affairPig.getState().intValue() == StateEnum.col_stepBack.getKey()) {
					affairIds = objectDocMap.get(affairPig.getObjectId());
					if (affairIds == null) {
						affairIds = new ArrayList<Long>();
					}
					affairIds.add(affairPig.getId());
					objectDocMap.put(affairPig.getObjectId(), affairIds);
				}
			}
		}
		CtpAffair affair = null;
		for (String id : collIds) {
			affair = affairManager.get(Long.parseLong(id));
			if (objectDocMap.get(affair.getObjectId()) != null) {
				needDeleteDocs.addAll(objectDocMap.get(affair.getObjectId()));
			}
		}
		if (!CollectionUtils.isEmpty(needDeleteDocs)) {
			docApi.deleteDocResources(AppContext.currentUserId(), needDeleteDocs);
		}
		return result.toString();
	}


	@Override
	@AjaxAccess
	public String getPigeonholeRight(List<String> collIds) throws BusinessException {
		StringBuilder result = new StringBuilder();
		CtpAffair affair = null;
		EdocSummary summary = null;
		List<String> permissions = null;
		List<String> actions = null;
		for (String id : collIds) {
			affair = affairManager.get(Long.parseLong(id));
			summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
			long accountId = 0L;
			// 协同的发起人不管在什么地方都能归档
			// 允许操作中不勾选归档：
			// (作废)1、归档人是发起人是可以归档的（从已发列表和待办、已办中都可以归档）
			// 2、归档人是非发起人，不可以归档
			if (affair.getState().intValue() == StateEnum.col_sent.getKey())
				continue;
			if (summary != null) {
				accountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary);
				// 判断协同设置是否允许归档
				if (null != affair.getSubApp() && affair.getSubApp() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_rec_permission_policy.name(), affair.getNodePolicy(), accountId);
				} else if (null != affair.getSubApp() && affair.getSubApp() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_change_permission_policy.name(), affair.getNodePolicy(), accountId);
				} else if (null != affair.getSubApp() && affair.getSubApp() == ApplicationSubCategoryEnum.edoc_qianbao.getKey()) {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_qianbao_permission_policy.name(), affair.getNodePolicy(), accountId);
				} else {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_send_permission_policy.name(), affair.getNodePolicy(), accountId);
				}
			}
			// 不允许归档的情况有：
			// 1. 节点动作不包含处理后归档
			// 2. 协同不允许归档
			// 3. 节点权限设置意见不能为空
			if(actions != null && summary != null){
				if (actions.contains("Archive")
						&& ((summary.getStartMemberId().equals(affair.getMemberId()) && affair.getState()==StateEnum.col_sent.getKey()) || (summary.getCanArchive() != null && summary.getCanArchive()))) {
					permissions = permissionManager.getRequiredOpinionPermissions(EnumNameEnum.col_flow_perm_policy.name(), accountId);
					// 待办的情况下
					if (affair.getState().intValue() == StateEnum.col_pending.getKey() && permissions.contains(affair.getNodePolicy())) {
						result.append("以下事项要求意见不能为空，不能直接归档或删除。").append(summary.getSubject()).append("<br>");
					}
				} else if(summary.getGovdocType().intValue() == 0){
					result.append("公文《" + summary.getSubject() + "》为老数据，不允许归档！\r\n");
				}else{
					result.append("公文《" + summary.getSubject() + "》不允许归档！\r\n");
				}
			}
			
		}
		return result.toString();
	}

	@Override
	@AjaxAccess
	public String getIsSamePigeonhole(List<String> collIds, Long destFolderId) throws BusinessException {
		StringBuilder result = new StringBuilder();
		CtpAffair affair = null;
		for (String id : collIds) {
			affair = affairManager.get(Long.parseLong(id));
			if (affair != null) {
				int _key = ApplicationCategoryEnum.edoc.getKey();
				List<Long> sourceIds = new ArrayList<Long>();
				sourceIds.add(affair.getObjectId());
				if (docApi.hasSamePigeonhole(destFolderId, sourceIds, _key)) {
					result.append("在该文件夹下已存在<" + affair.getSubject() + ">的归档链接，不能归档！");
				}
			}
		}
		return result.toString();
	}

	@Override
	@AjaxAccess
	public String checkPigeonhole(List<String> ids, Long floder, String pageType) {
		StringBuilder sb = new StringBuilder();
		Map<Long, Long> summaryIds = new HashMap<Long, Long>();
		Map<String, EdocSummary> affairIdMappingSummary = new HashMap<String, EdocSummary>();
		if ("govdocdone".equals(pageType) || "govdocsent".equals(pageType)) {
			try {
				for (String id : ids) {
					CtpAffair affair = affairManager.get(Long.valueOf(id));
					EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
					if (null != summary) {
						summaryIds.put(summary.getId(), summary.getId());
						affairIdMappingSummary.put(id, summary);
					}
				}
				if (summaryIds.size() != ids.size()) {
					return "同一流程只能保存一条公文,请勿重复选择!";
				}
				// 同一条公文 在同一目录下 只能归档一次
				Iterator<Map.Entry<String, EdocSummary>> entries = affairIdMappingSummary.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry<String, EdocSummary> entry = entries.next();
					boolean b = docApi.hasSamePigeonhole(floder, Long.valueOf(entry.getKey()), ApplicationCategoryEnum.edoc.key());
					String subject = "";
					EdocSummary edocSummary = entry.getValue();
					if (null != edocSummary) {
						subject = edocSummary.getSubject();
					}
					if (b) {
						sb.append("公文《").append(subject).append("》在该文件夹下已经存在！\r\n");
						return sb.toString();
					}
					// 获取所有edoc_summary_id 相同 的 affair数据
					if(edocSummary != null){
						List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, edocSummary.getId());
						for (CtpAffair aff : affairs) {
							b = docApi.hasSamePigeonhole(floder, aff.getId(), ApplicationCategoryEnum.edoc.key());
							if (b) {
								sb.append("公文《").append(subject).append("》在该文件夹下已经存在！\r\n");
								break;
							}
						}
					}										
				}
			} catch (Exception e) {
				LOGGER.error(e);
				return "归档失败";
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("deprecation")
	@Override
	@AjaxAccess
	public String transPigeonhole(Long affairId, Long destFolderId, String type) throws BusinessException {
		User user = AppContext.getCurrentUser();
		List<Boolean> hasAttachments = new ArrayList<Boolean>();
		List<Long> collIdLongs = new ArrayList<Long>();
		collIdLongs.add(affairId);
		CtpAffair affair = affairManager.get(affairId);
		EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());

		int app = ApplicationCategoryEnum.edoc.key();
		hasAttachments.add(summary.isHasAttachments());
		Integer actionId = GovdocAppLogAction.EDOC_PINGHOLE.key();
		// 归档操作
		List<Long> results = docApi.pigeonhole(user.getId(), app, collIdLongs, hasAttachments, null, destFolderId, null);
		affair.setArchiveId(results.get(0));
		if (summary != null) {
			summary.setHasArchive(true);
			summary.setArchiveId(destFolderId);
			govdocSummaryManager.updateEdocSummary(summary,false);
		}
		if (!StringUtil.checkNull(type) && "pending".equals(type)) {
			Comment c = govdocCommentManager.getNullDealComment(affair.getId(), affair.getObjectId());
			c.setExtAtt3(CommentExtAtt3Enum.pighole_pending_skip.getI18nLabel());
			DateSharedWithWorkflowEngineThreadLocal.setIsNeedAutoSkip(false);
			GovdocDealVO dealVo = new GovdocDealVO();
			dealVo.setSummary(summary);
			dealVo.setAffair(affair);
			dealVo.setComment(c);
			govdocManager.transFinishWorkItemPublic(dealVo, ColHandleType.finish);
		}
		DocResourceBO res = docApi.getDocResource(results.get(0));
		String forderName = docApi.getDocResourceName(res.getParentFrId());

		govdocLogManager.insertAppLog(user, actionId, user.getName(), res.getFrName(), forderName);
		affair.setArchiveId(res.getParentFrId());
		affairManager.updateAffair(affair);
		if (actionId.intValue() == GovdocAppLogAction.EDOC_PINGHOLE.key()) {// 老公文暂时不做推送到门户
																			// TODO
			return "";
		}
		// 归档推送到门户,本期不做 TODO
		// if(OutersapceSystemPropertyUtil.isOuterspaceState()){
		// String otype = String.valueOf(ApplicationCategoryEnum.doc.getKey());
		// ModuleType mtype;
		// if(summary.getGovdocType()!=null){
		// mtype=ModuleType.edoc;
		// }else{
		// mtype=ModuleType.collaboration;
		// }
		// OuterspaceSectionConfig config =
		// outerspaceSectionConfigManager.findOuterspaceByBusiness(destFolderId,otype);
		// if(config!=null){
		// OuterspaceSectionList sectionList =
		// outerspaceSectionListManager.findSectionByObjectId(res.getId(),otype);
		// if(sectionList ==null){
		// CtpContentAll contentAll = this.getCtpContentAllTypeAndId(mtype,
		// summary.getId());
		// sectionList = new OuterspaceSectionList();
		// sectionList.setId(UUIDLong.absLongUUID());
		// sectionList.setSectionId(config.getSectionId());
		// sectionList.setObjectType(otype);
		// sectionList.setBusinessId(destFolderId);
		// sectionList.setUnitId(config.getUnitId());
		// sectionList.setObjectId(res.getId());
		// if(contentAll!=null){
		// sectionList.setContent(contentAll.getContent());
		// sectionList.setContentType(ContentTypeUtil.typeConvert(contentAll.getContentType()));
		// }
		// sectionList.setCreateTime(new Timestamp(System.currentTimeMillis()));
		// sectionList.setSubject(res.getFrName());
		// outerspaceManager.saveOuterspaceSectionData(sectionList);
		// }
		// }
		//
		// }
		return "";
	}

	

	@Override
	public String transPigeonhole(EdocSummary summary, CtpAffair affair, Long destFolderId, String type) throws BusinessException {
		boolean hasAttachment = summary.isHasAttachments();
		StringBuilder result = new StringBuilder();
		User user = AppContext.getCurrentUser();
		List<Boolean> hasAttachments = new ArrayList<Boolean>();
		List<Long> collIdLongs = new ArrayList<Long>();
		collIdLongs.add(affair.getId());
		hasAttachments.add(hasAttachment);

		List<Long> results = docApi.pigeonhole(AppContext.currentUserId(), ApplicationCategoryEnum.edoc.key(), collIdLongs, hasAttachments, null, destFolderId,
				PigeonholeType.edoc_dept.ordinal());
		// 推送到门户，本期不做 TODO
		// if(results.size()>0){
		// pushToOuterspace(summary, destFolderId, results.get(0));
		// }
		if (results != null && results.size() == 1) {
			summary.setHasArchive(true);
			//只有预归档才设置summary的archiveId
			//summary.setArchiveId(results.get(0));
			DocResourceBO res = docApi.getDocResource(results.get(0));
			affair.setArchiveId(res.getParentFrId());
			String forderName = docApi.getDocResourceName(res.getParentFrId());
			govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_PINGHOLE.key(), user.getName(), res.getFrName(), forderName);
			affairManager.updateAffair(affair);
		}
		return result.toString();
	}

	@Override
	public void savePigeonhole(GovdocNewVO info, SendType sendType) throws BusinessException {
		User currUser = info.getCurrentUser();
		long currUserId = -1;
		if(currUser != null){
			currUserId = currUser.getId();
		}
		CtpTemplate template = info.getTemplate();
		EdocSummary summary = info.getSummary();
		CtpAffair sendAffair = info.getSenderAffair();
		if (template != null) {
			// 预归档真实路径
			String realFromInputVlaue = "";
			String archiveFolder = "";
			String archiveIsCreate = "";
			String archiveFieldName = "";
			EdocSummary templateSummary = (EdocSummary) XMLCoder.decoder(template.getSummary());
			if (Strings.isNotBlank(summary.getAdvancePigeonhole())) {
				try {
					JSONObject jo = new JSONObject(templateSummary.getAdvancePigeonhole());
					archiveFolder = jo.optString(EdocConstant.COL_ARCHIVEFIELDID).toString();
					archiveFieldName = jo.optString(EdocConstant.COL_ARCHIVEFIELDNAME).toString();
					archiveIsCreate = jo.optString(EdocConstant.COL_ISCEREATENEW).toString();
					//做一个防护，有的文单模板没有设置高级归档advancePigeonhole还是有值
					if(Strings.isBlank(archiveFolder)){
						summary.setAdvancePigeonhole("");
					}else{
						realFromInputVlaue = govdocFormManager.getMasterFieldValue(summary.getFormAppid(), summary.getFormRecordid(), archiveFolder, true).toString();
						if (Strings.isBlank(realFromInputVlaue)) {
							realFromInputVlaue = "Temp";
						}
						String advancePigeonhole = EdocUtil.getAdvancePigeonhole(archiveFolder, archiveFieldName, realFromInputVlaue, archiveIsCreate, "");
						summary.setAdvancePigeonhole(advancePigeonhole);
					}	
				} catch (Exception e) {
					LOGGER.error("高级归档出错：", e);
				}
			}
		}
		if(Strings.isNotBlank(summary.getAdvancePigeonhole())){//高级归档
    		JSONObject jo;
    		try {
    			jo = new JSONObject(summary.getAdvancePigeonhole());
    			String isCereateNew = jo.optString(EdocConstant.COL_ISCEREATENEW).toString();
    			String archiveFolder = jo.optString(ColConstant.COL_ARCHIVEFIELDID,"");//文单控件字段
    			//String archiveFieldValue = jo.optString(EdocConstant.COL_ARCHIVEFIELDVALUE).toString();
    			boolean isCreateFloder = "true".equals(isCereateNew);
    			Long archiveId = summary.getArchiveId();
    			//boolean hasAttachments = EdocUtil.isHasAttachments(summary);
    			String StrArchiveFolder =  "";
    			if(null != archiveId){
    				Long realFolderId = archiveId;
    				LOGGER.info("archiveFolder="+archiveFolder);
				    try {
				        StrArchiveFolder = capFormManager.getMasterFieldValue(summary.getFormAppid(),summary.getFormRecordid(),archiveFolder,true).toString();
                    } catch (SQLException e) {
                    	LOGGER.error("",e);
                    }
				    LOGGER.info("StrArchiveFolder="+StrArchiveFolder);
				    if(Strings.isNotBlank(StrArchiveFolder)){
				    	realFolderId = docApi.getPigeonholeFolder(archiveId, StrArchiveFolder, isCreateFloder);//真实归档的路径
				    	if(realFolderId == null){
				    		LOGGER.warn("公文高级归档，没有勾选表单不存在时自动创建目录, 需要创建的目录 : " + StrArchiveFolder);
                            StrArchiveFolder = null;
                        }
				    }
				    
				    if(Strings.isBlank(StrArchiveFolder)){
				        StrArchiveFolder = "Temp";
				    	//归档到Temp下面
				    	realFolderId = docApi.getPigeonholeFolder(archiveId, StrArchiveFolder, true);//真实归档的路径
				    }
				    if(null==realFolderId){
				    	LOGGER.error("归档路径为null,导致归档不成功!!!");
				    }else{
				    	summary.setArchiveId(realFolderId);//更新归档的实际目录
				    	docApi.pigeonholeWithoutAcl(currUserId,ApplicationCategoryEnum.edoc.getKey(),
								sendAffair.getId(), EdocUtil.isHasAttachments(summary), realFolderId,  
								PigeonholeType.edoc_account.ordinal(),null);
				    	try{
				    		//高级归档，需要修改summary的归档路径
				    		govdocSummaryManager.updateSummaryArchiveId(summary.getId(),realFolderId);	
				    	}catch(Exception e){
				    		LOGGER.error("高级归档，修改Summary归档路径失败!"+e);
				    	}
				    }
    				summary.setHasArchive(true);
    			}
    		} catch (Exception e) {
    			LOGGER.error("summary.getAdvancePigeonhole():"+summary.getAdvancePigeonhole(),e);
    		}
    	}else if(null != summary.getArchiveId()){
			if(Strings.isNotBlank(summary.getSubject())){
				DocResourceBO _exist = docApi.getDocResource(summary.getArchiveId());
				if(null != _exist){
					
					if(null != template && (sendType == EdocConstant.SendType.auto  || sendType == EdocConstant.SendType.child)){
						String summaryTem = template.getSummary();
						EdocSummary summaryTemBean = (EdocSummary) XMLCoder.decoder(summaryTem);
						boolean hasPrePath = null != summaryTemBean.getArchiveId() || Strings.isNotBlank(summaryTemBean.getAdvancePigeonhole());
						info.setTemplateHasPigeonholePath(hasPrePath);
					}
					
					docApi.pigeonholeWithoutAcl(currUserId,ApplicationCategoryEnum.edoc.getKey(),
							sendAffair.getId(), EdocUtil.isHasAttachments(summary), summary.getArchiveId(),  
							PigeonholeType.edoc_account.ordinal(),null);
					summary.setHasArchive(true);
				}
			} else {
				LOGGER.info("Id为"+summary.getId()+"的公文，由于标题为空不允许归档！");
			}
    	}
	
	}
	
	/**
	 * 修改归档数据
	 */
	public void updateDocMetadata(EdocSummary summary, int app) {
		
	}
	
	public void updateDocMetadata(Map<Long, Long> sourceIdMap) throws BusinessException {
		if(sourceIdMap!=null && sourceIdMap.size()>0) {
			for(Long sourceId : sourceIdMap.keySet()) {
				docApi.updatePigehole(sourceIdMap.get(sourceId), sourceId, ApplicationCategoryEnum.edoc.key());
			}
		}
	}
	
	
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setGovdocManager(GovdocManager govdocManager) {
		this.govdocManager = govdocManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }
}
