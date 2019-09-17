package com.seeyon.apps.govdoc.manager.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.exchange.engine.service.ExchangeDataAbsManager;
import com.seeyon.apps.exchange.engine.service.ExchangeService;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocExchangeTypeEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocWorkflowTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocExchangeHelper;
import com.seeyon.apps.govdoc.listener.GovdocWorkflowEventListener;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetailLog;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.util.GovdocUtil;
//import com.seeyon.apps.ocip.exchange.edoc.IOCIPExchangeEdocManager;
import com.seeyon.apps.ocip.util.CommonUtil;
import com.seeyon.apps.ocip.util.OrgUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum;
import com.seeyon.ctp.form.modules.engin.base.formBase.GovdocTemplateDepAuthDao;
import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ocip.common.IConstant.AddressType;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.exchange.model.AttachmentFile;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.BIZExchangeData;
import com.seeyon.ocip.exchange.model.BIZMessage;
import com.seeyon.ocip.exchange.model.Organization;
import com.seeyon.ocip.exchange.model.PropertyValue;
import com.seeyon.ocip.exchange.model.edoc.EdocOperation;
import com.seeyon.ocip.exchange.model.edoc.OFCEdocObject;
import com.seeyon.ocip.exchange.model.edoc.RETEdocObject;
import com.seeyon.ocip.exchange.model.edoc.SeeyonEdoc;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.system.signet.manager.SignetManager;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMEnd;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMStart;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * 公文/协同交换单位接收实现
 */
public class RecieveGovdocObjManager extends ExchangeDataAbsManager {

	private GovdocTemplateDepAuthDao govdocTemplateDepAuthDao;
	private GovdocFormManager govdocFormManager;
	private OrgManager orgManager;
	private GovdocPubManager govdocPubManager;
	private GovdocExchangeManager govdocExchangeManager;
	private FileManager fileManager;
	private AttachmentManager attachmentManager;
    private MainbodyManager ctpMainbodyManager;
	private IOrganizationManager organizationManager;
	private List<Organization> failList;
//	private IOCIPExchangeEdocManager ocipExchangeEdocManager;
	private SignetManager signetManager;
	private GovdocCommentManager govdocCommentManager;
	private EnumManager enumManagerNew;
	private TemplateManager templateManager;
	private FormApi4Cap3 formApi4Cap3;
	
	@Override
	public String getType() {
		return "recieve";
	}

	@SuppressWarnings("static-access")
	@Override
	public Object send(Object sendData) throws BusinessException {
		long d1 = System.currentTimeMillis();
		Map<Long,String> ocipStatusMap = new HashMap<Long, String>();
		failList = new ArrayList<Organization>();
		BIZExchangeData data = (BIZExchangeData) sendData;
		BIZMessage bizMessage = data.getBussnissMessage();
		OFCEdocObject ofcEdocObject = (OFCEdocObject) bizMessage.getContent();
		int bodyType = 0;
		if(Strings.isNotBlank(ofcEdocObject.getContentType()) && MainbodyType.valueOf(ofcEdocObject.getContentType())!=null) {
			bodyType = MainbodyType.valueOf(ofcEdocObject.getContentType()).getKey();
		}
		 
		
		SeeyonEdoc seeyonEdoc = (SeeyonEdoc) ofcEdocObject.getExtendAttr();
		List<Organization> organizations = data.getRecivers();
		Map<Long, Long> orgIdAndDetailId = seeyonEdoc.getDetailIds();
		//Map<String, Object> mappingValue = (Map<String, Object>) extendAttr.get("mappingValue");
		Long currentUserId = Long.valueOf(data.getSender().getIdentification().getId());
		Long mainId = Long.valueOf(seeyonEdoc.getMainId());
		GovdocExchangeMain govdocExchangeMain = govdocExchangeManager.getGovdocExchangeMainById(mainId);
		List<GovdocExchangeDetail> waitSendDetails = govdocExchangeManager.getDetailByMainId(mainId);
		Map<Long,GovdocExchangeDetail> detailMap = new HashMap<Long,GovdocExchangeDetail>();
		if(waitSendDetails != null){
			for(GovdocExchangeDetail d:waitSendDetails){
				detailMap.put(d.getId(), d);
			}
		}
		LOGGER.info("查询main" + new Timestamp(System.currentTimeMillis()));
		Long summaryId = govdocExchangeMain.getSummaryId();
		List<GovdocExchangeDetail> details = new ArrayList<GovdocExchangeDetail>();
		List<GovdocExchangeDetailLog> logs = new ArrayList<GovdocExchangeDetailLog>();
		
		// 复制正文： 若有Ofd正文，则交换Ofd正文，若无Ofd正文，则交换Pdf正文，若无Pdf正文，则交换其它正文
		List<CtpContentAll> newContentList = new ArrayList<CtpContentAll>();
		
		CtpContentAll govdocContentObj = GovdocContentHelper.getTransBodyContentByModuleId(summaryId);
		
		bodyType = govdocContentObj.getContentType();
		
		
//		govdocContentObj.setTransId(null);
		CtpContentAll newGovdocContentObjTemp = new CtpContentAll();
		try {
			newGovdocContentObjTemp = (CtpContentAll) govdocContentObj.clone();
		} catch (CloneNotSupportedException e1) {
			LOGGER.error("正文复制失败",e1);
		}
		newGovdocContentObjTemp.setCreateDate(new Date());
		if (govdocContentObj.getContentType() != MainbodyType.HTML.getKey() && govdocExchangeMain.getType() != GovdocExchangeMain.EXCHANGE_TYPE_LIANHE) {
			if(ofcEdocObject.getContent()==null){//如果是外单位交换过来则不需要复制文件
				try {
					V3XFile v3xInFile = fileManager.getV3XFile(Long.valueOf(govdocContentObj.getContent()));
					V3XFile v3xOutFile = (V3XFile) v3xInFile.clone();
					v3xOutFile.setNewId();
					v3xOutFile.setCreateDate(newGovdocContentObjTemp.getCreateDate());
					v3xOutFile.setUpdateDate(newGovdocContentObjTemp.getCreateDate());
					v3xOutFile.setFilename(String.valueOf(v3xOutFile.getId()));
					fileManager.save(v3xOutFile);
					//设置附件文件为新复制的文件id
					newGovdocContentObjTemp.setContent(v3xOutFile.getId().toString());
					//upload目录
					String uploadFolder = fileManager.getFolder(newGovdocContentObjTemp.getCreateDate(), true);
					//原始文件
					File fileIn = fileManager.getFile(v3xInFile.getId(), v3xInFile.getCreateDate());
					//复制的文件
					File FileOut = new File(uploadFolder + File.separator + v3xOutFile.getId());
					GovdocExchangeHelper.copyFile(fileIn, FileOut);
					//复制正文成功后需要复制签章数据
					GovdocUtil.copySignet(fileManager, signetManager, Long.valueOf(govdocContentObj.getContent()), Long.valueOf(newGovdocContentObjTemp.getContent()));
					//复制签章end
				} catch (Exception e) {
					LOGGER.info("复制正文或签章错误:" + govdocContentObj.getTitle(), e);
				}
			}
		}
		
		//循环发文单所有附件之后，保存一份附件数据的复制到这个集合，
		List<Attachment> newAttachmentsTemp = new ArrayList<Attachment>();
		//循环单位触发流程之后， 为每一条流程循环以一次上面的复制附件的集合，为每一条流程保存跟发文单一致的附件
		List<Attachment> newAttachments = new ArrayList<Attachment>();
		if (CollectionUtils.isEmpty(bizMessage.getAttachments())) {
			List<Attachment> attachments = attachmentManager.getByReference(summaryId);
			List<Comment> comments = govdocCommentManager.getCommentAllByModuleId(ModuleType.edoc, summaryId, false);
			//循环所有附件
			for (Attachment attachment : attachments) {
				if (attachment.getType() != Constants.ATTACHMENT_TYPE.FILE.ordinal()) {
					continue;
				}
				if (attachment.getCategory() == ApplicationCategoryEnum.edoc.getKey() 
						&& attachment.getReference().longValue() != attachment.getSubReference().longValue()) {
					continue;
				}
				if(isFromOption(attachment, comments)){
					continue;
				}
				try {
					//复制附件对象
					Attachment newAttachment = (Attachment) attachment.clone();
					newAttachment.setNewId();
					newAttachment.setCreatedate(new Date());
					newAttachment.setCategory(ApplicationCategoryEnum.edoc.getKey());
					newAttachmentsTemp.add(newAttachment);

					//原始附件对应的文件对象
					V3XFile v3xInFile = fileManager.getV3XFile(attachment.getFileUrl());
					//复制的附件文件    发文单对应的附件文件， 每一个文件只复制一份，所有相同的附件指向同一个附件文件对象
					V3XFile v3xOutFile = (V3XFile) v3xInFile.clone();
					v3xOutFile.setNewId();
					v3xOutFile.setCreateDate(newAttachment.getCreatedate());
					v3xOutFile.setUpdateDate(newAttachment.getCreatedate());
					fileManager.save(v3xOutFile);
					//设置附件文件为新复制的文件id
					newAttachment.setFileUrl(v3xOutFile.getId());
					//upload目录
					String uploadFolder = fileManager.getFolder(newAttachment.getCreatedate(), true);
					//原始文件
					File fileIn = fileManager.getFile(v3xInFile.getId(), v3xInFile.getCreateDate());
					//复制的文件
					File FileOut = new File(uploadFolder + File.separator + v3xOutFile.getId());
					GovdocExchangeHelper.copyFile(fileIn, FileOut);
				} catch (Exception e) {
					LOGGER.info("附件复制错误:" + attachment.getFilename(), e);
				}
			} 
		}else{
			//attachmentFile.getIdentification() == ctpFile.getId()   attachment    ctpfile   两个数据都保存了一份模板
			List<AttachmentFile> attachmentFiles = bizMessage.getAttachments();
			for (AttachmentFile attachmentFile : attachmentFiles) {
				Attachment attachment = attachmentManager.getAttachmentByFileURL(Long.valueOf(attachmentFile.getIdentification()));
				newAttachmentsTemp.add(attachment);
			}
		}
		Address localAddress = null;
		Map<String,List<GovdocTemplateDepAuth>> temGTDA = new HashMap<String,List<GovdocTemplateDepAuth>>();
		Map<Long,CtpTemplate> temTemplate = new HashMap<Long,CtpTemplate>();
		Map<Long, Map<String, Object>> toFormMasterDataMap = new HashMap<Long,Map<String, Object>>();
		int c = 1; 
		for (Organization organization : organizations) {
			long d2 = System.currentTimeMillis();
			String ocipSendStatus = "0";
			boolean isOcipUnit = false;
			localAddress = new Address();
			Address add = organization.getIdentification();
			localAddress.setId(add.getId());
			localAddress.setName(add.getName());
			localAddress.setResource(add.getResource());
			localAddress.setType(add.getType());
			V3xOrgEntity entity = orgManager.getEntityOnlyById(Long.parseLong(add.getId()));
			Long orgId = -1l;
			if(entity!=null){
//				if(OrgUtil.isPlatformEntity(entity)){
//					String addid = organizationManager.getLocalObjectId(add);
//					if(addid==null){
//						continue;
//					}else{
//						isOcipUnit = true;
//					}
//					orgId = Long.parseLong(addid);
//					localAddress.setId(orgId.toString());
//				}else{
					orgId = entity.getId();
//				}
			}
			Long detailId = orgIdAndDetailId.get(Long.parseLong(add.getId()));
			GovdocExchangeDetailLog detailLog = new GovdocExchangeDetailLog();
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			detailLog.setTime(date);
			detailLog.setTimeMS(calendar.get(Calendar.MILLISECOND));
			detailLog.setNewId();
			detailLog.setDetailId(detailId);
			ocipStatusMap.put(detailId, ocipSendStatus);
			//查询交换流程时,只需要区分联合发文和交换
			int type = govdocExchangeMain.getType() == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE ? GovdocExchangeMain.EXCHANGE_TYPE_LIANHE
					: GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN;
			List<GovdocTemplateDepAuth> auths = findByOrgIdAndAccountId(orgId, type,temGTDA);
			CtpTemplate ctpTemplate = null;
			boolean isParentUnitAuth = false;
			if (auths.isEmpty()) {
				if (organization.getIdentification().getType().equals(AddressType.department.name())) {
					V3xOrgDepartment department = null;
					if(entity!=null && entity instanceof V3xOrgDepartment&&!OrgUtil.isPlatformEntity(entity)){
						department = (V3xOrgDepartment)entity;
					}else{
						department = orgManager.getDepartmentById(orgId);
					}
					if (department != null) {
						auths = findByOrgIdAndAccountId(department.getOrgAccountId(), type,temGTDA);
						isParentUnitAuth = true;
					}
				}
			}
			
			if (auths.isEmpty()) {
				detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
				if(type == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
					detailLog.setDescription("发送失败，收文单位未设置联合发文流程");
				}else {
					detailLog.setDescription("发送失败，收文单位未设置收文交换流程");
				}
				govdocExchangeManager.saveDetailLog(detailLog);
				ocipSendStatus = "1";
				ocipStatusMap.put(detailId, ocipSendStatus);
				failList.add(organization);
				continue;
			}
			GovdocTemplateDepAuth auth = auths.get(0);
			
			ctpTemplate = getCtpTemplate(auth.getTemplateId(),temTemplate);
			if (ctpTemplate == null) {
				detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
				detailLog.setDescription("发送失败，收文单位的交换流程被停用或者删除");
				govdocExchangeManager.saveDetailLog(detailLog);
				ocipSendStatus = "2";
				ocipStatusMap.put(detailId, ocipSendStatus);
				failList.add(organization);
				continue;
			}
			
			//如果当前模板为不可用状态
			if (ctpTemplate.getState() == TemplateEnum.State.invalidation.ordinal()) {
				if(isParentUnitAuth || organization.getIdentification().getType().equals(AddressType.account.name())){
					detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
					detailLog.setDescription("发送失败，收文单位的交换流程被停用或者删除");
					govdocExchangeManager.saveDetailLog(detailLog);
					ocipSendStatus = "2";
					ocipStatusMap.put(detailId, ocipSendStatus);
					failList.add(organization);
					continue;
				}else{
					auth = null;
					V3xOrgDepartment department = orgManager.getDepartmentById(orgId);
					if (department != null) {
						auth = govdocTemplateDepAuthDao.findExchangeByOrgId(department.getOrgAccountId());
					}
					if (auth == null) {
						detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
						detailLog.setDescription("发送失败，收文单位的交换流程被停用或者删除");
						govdocExchangeManager.saveDetailLog(detailLog);
						ocipSendStatus = "2";
						ocipStatusMap.put(detailId, ocipSendStatus);
						failList.add(organization);
						continue;
					}
					ctpTemplate = getCtpTemplate(auth.getTemplateId(),temTemplate);
					if (ctpTemplate == null || ctpTemplate.getState() == TemplateEnum.State.invalidation.ordinal()) {
						detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
						detailLog.setDescription("发送失败，收文单位的交换流程被停用或者删除");
						govdocExchangeManager.saveDetailLog(detailLog);
						ocipSendStatus = "2";
						ocipStatusMap.put(detailId, ocipSendStatus);
						failList.add(organization);
						continue;
					}
				}
			}
			GovdocExchangeDetail detail = detailMap.get(detailId);//govdocExchangeManager.getExchangeDetailById(detailId);
			V3xOrgMember orgMember = orgManager.getMemberById(currentUserId);
			if (orgMember != null) {
				User user = new User();
				user.setId(currentUserId);
				user.setLoginAccount(orgMember.getOrgAccountId());
				user.setAccountId(orgMember.getOrgAccountId());
				user.setDepartmentId(orgMember.getOrgDepartmentId());
				user.setName(orgMember.getName());
				CommonUtil.setCurrentUser(user);
				detailLog.setUserName(orgMember.getName());
				FormBean formBean = null;
				FormDataMasterBean dataMasterBean = null;
				try {
					formBean = govdocFormManager.getForm(ctpTemplate.getFormAppId());
					if (formBean != null) {
						dataMasterBean = new FormDataMasterBean(formBean.getNewFormAuthViewBean(), formBean.getMasterTableBean(), true);
						Map<String, Object> valueMap = toFormMasterDataMap.get(formBean.getId());
						if(valueMap == null){
							valueMap = new HashMap<String, Object>();
							toFormMasterDataMap.put(formBean.getId(), valueMap);
						}
						List<FormFieldBean> fieldBeans = formBean.getAllFieldBeans();
						initFormDataMasterBean(govdocExchangeMain.getType(), fieldBeans, seeyonEdoc, valueMap, GovdocExchangeHelper.getTypeAndIds(localAddress));
						deleteValueByLianHe(govdocExchangeMain, fieldBeans, valueMap);
						dataMasterBean.addFieldValue(valueMap);
						govdocFormManager.insertOrUpdateMasterData(dataMasterBean);
						govdocFormManager.putSessioMasterDataBean(formBean, dataMasterBean, false, false);
						
						List<V3xOrgMember> members = this.getPersonNextNode(formBean, dataMasterBean, ctpTemplate,orgMember);
						//System.out.println("--------校验人员："+(System.currentTimeMillis()-d3) +"MS");
						if(CollectionUtils.isNotEmpty(members)) {
							
							EdocSummary edocSummary = govdocPubManager.transSendColl(EdocConstant.SendType.exchange, auth.getTemplateId(),
									currentUserId, dataMasterBean.getId(), govdocExchangeMain.getSummaryId(), null, orgId,
									bodyType, CollectionUtils.isNotEmpty(newAttachmentsTemp),govdocExchangeMain.getType()).getSummary();
							//System.out.println("--------触发公文："+(System.currentTimeMillis()-d3) +"MS");
							try{
								govdocFormManager.removeSessionMasterDataBean(dataMasterBean.getId());
							}catch(Exception e){
								LOGGER.error("无法获取到session");
							}
							detail.setSummaryId(edocSummary.getId());
							if(type == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
								detail.setStatus(GovdocEnum.ExchangeDetailStatus.beingProcessed.getKey());
								detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.beingProcessed.getKey());
								detailLog.setDescription("收文单位已接收，进行中");
							}else{
								detail.setStatus(GovdocEnum.ExchangeDetailStatus.waitSign.getKey());
								detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSign.getKey());
								detailLog.setDescription("收文单位已接收，等待签收");
							}
							
							//复制正文
							CtpContentAll newCtpContentAll = null;
							//如果是联合发文 不用复制正文文件
							if(govdocExchangeMain.getType() != GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
								newCtpContentAll = (CtpContentAll) newGovdocContentObjTemp.clone();
							}else{
								newCtpContentAll = (CtpContentAll) govdocContentObj.clone();
							}
							newCtpContentAll.setNewId();
							newCtpContentAll.setCreateId(orgMember.getId());
							newCtpContentAll.setModuleId(edocSummary.getId());
							newCtpContentAll.setTitle(edocSummary.getSubject());
							if(newCtpContentAll.getSort() == 3){//转办的正文到收文后需要修改sort
								newCtpContentAll.setSort(1);
							}
							newContentList.add(newCtpContentAll);
							
							boolean hasAttachMentField = false;
							for (FormFieldBean field : fieldBeans) {
								if("attachments".equals(field.getMappingField())){
									hasAttachMentField = true;
									break;
								}
							}
							//复制附件
							for (Attachment attachment : newAttachmentsTemp) {
								Attachment newAttachment = (Attachment) attachment.clone();
								newAttachment.setNewId();
								newAttachment.setReference(edocSummary.getId());
								//签收单不包含附件字段 或者 签收单包含附件字段 但是附件是标题区域的
								if (!hasAttachMentField || (hasAttachMentField && newAttachment.getCategory() != ApplicationCategoryEnum.form.getKey())) {
									newAttachment.setCategory(ApplicationCategoryEnum.edoc.getKey());
									newAttachment.setSubReference(edocSummary.getId());
								}
								newAttachments.add(newAttachment);
							}
							
						}else{
							sendFailedMethod(detail, detailLog, null
									, organization, "发送失败，接收单位没有找到对应人员");
							ocipStatusMap.put(detailId, "3");
						}
					}else{
						sendFailedMethod(detail, detailLog, null
								, organization, "交换模板触发失败:设置签收模板的签收单发生错误");
						LOGGER.error("交换模板触发失败:设置签收模板的签收单发生错误");
						ocipStatusMap.put(detailId, "4");
						
					}
				} catch (Exception e) {
					sendFailedMethod(detail, detailLog, null
							, organization, "交换模板触发失败:"+e.getMessage());
					LOGGER.error("交换模板触发失败:",e);
					ocipStatusMap.put(detailId, "4");
				}
				if (detail.getStatus() == GovdocEnum.ExchangeDetailStatus.waitSend.getKey()) {
					if (formBean != null && dataMasterBean != null) {
						try {
						    formApi4Cap3.deleteFormData(dataMasterBean.getId(), formBean.getId());
						} catch (SQLException e) {
							LOGGER.error("交换模板触发失败，删除动态表数据发生错误",e);
						}
					}
				}
			}else{
				sendFailedMethod(detail, detailLog, data.getSender().getIdentification().getName()
						, organization, "发送失败，接收单位没有找到发起人");
				ocipStatusMap.put(detailId, "4");
			}
			details.add(detail);
			logs.add(detailLog);
			//System.out.println("--------第"+(c++)+"个单位《"+organization.getName()+"》："+(System.currentTimeMillis()-d2) +"MS");
		}
		govdocExchangeManager.updateDetailList(details);
		govdocExchangeManager.saveDetailLogList(logs);
		attachmentManager.saveAsAtt(newAttachments);
		ctpMainbodyManager.saveAll(newContentList);
		
		//ocip完成后回执消息
		callOcipReciept(organizations, bizMessage, details,ocipStatusMap);
		//System.out.println("--------完整的交换："+(System.currentTimeMillis()-d1) +"MS");
		return null;
	}

	private void sendFailedMethod(GovdocExchangeDetail detail,GovdocExchangeDetailLog detailLog,String userName,
			Organization organization,String failMsg){
		detail.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
		detailLog.setStatus(GovdocEnum.ExchangeDetailStatus.waitSend.getKey());
		if(userName!=null)
			detailLog.setUserName(userName);
		failList.add(organization);
		detailLog.setDescription(failMsg);
	}
	private void deleteValueByLianHe(GovdocExchangeMain govdocExchangeMain, List<FormFieldBean> fieldBeans, Map<String, Object> valueMap) {
		if (govdocExchangeMain.getType() == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE) {// 如果是联合发文,需要删除不必要的字段
			Set<String> lhFields = new HashSet<String>() {
				{
					add("subject");
					add("send_to");
					add("copy_to");
					add("report_to");
					add("send_unit");
					add("unit_level");
					add("doc_type");
					add("urgent_level");
					add("copies");
					add("createdate");
				}
			};
			for (FormFieldBean formFieldBean : fieldBeans) {
				if (!lhFields.contains(formFieldBean.getMappingField())) {// 不属于联合发文的字段需要去掉
					valueMap.remove(formFieldBean.getName());
				}
			}
		}
	}
	
	/**
	 * 缓存取值
	 * @param orgId
	 * @param type
	 * @param m
	 * @return
	 */
	private List<GovdocTemplateDepAuth> findByOrgIdAndAccountId(Long orgId,int type,Map<String,List<GovdocTemplateDepAuth>> m){
		List<GovdocTemplateDepAuth> result = m.get(""+type+ "_" +orgId.toString() );
		if(result != null){
			return result;
		}
		List<GovdocTemplateDepAuth> auths = govdocTemplateDepAuthDao.findByOrgIdAndAccountId(orgId, orgId, type);
		m.put(""+type+ "_" +orgId.toString() , auths);
		return auths;
	}
	
	/**
	 * @param id
	 * @param m
	 * @return
	 * @throws BusinessException 
	 */
	private CtpTemplate getCtpTemplate(Long id,Map<Long,CtpTemplate> m) throws BusinessException{
		CtpTemplate result = m.get(id);
		if(result !=null){
			return result;
		}
		result = templateManager.getCtpTemplate(id);
		m.put(id, result);
		return result;
	}
	
	private void initFormDataMasterBean(Integer exchangeType, List<FormFieldBean> fieldBeans, SeeyonEdoc seeyonEdoc,
			Map<String, Object> valueMap, String receiveUnit) throws Exception {
		Class clazz = seeyonEdoc.getClass();
		boolean needToSetData = valueMap.isEmpty()?true:false;
		Map<String, EnumNameEnum> enumMap = new HashMap<String, EnumNameEnum>() {
			{
				put("secret_level", EnumNameEnum.edoc_secret_level);
				put("urgent_level", EnumNameEnum.edoc_urgent_level);
				put("keep_period", EnumNameEnum.edoc_keep_period);
				put("unit_level", EnumNameEnum.edoc_unit_level);
				put("send_type", EnumNameEnum.edoc_send_type);
				put("doc_type", EnumNameEnum.edoc_doc_type);
			}
		};
		for (FormFieldBean formFieldBean : fieldBeans) {
			String elementName = formFieldBean.getMappingField();
			if (Strings.isBlank(elementName)) {
				continue;
			}
			//公文交换生成签收流程时，内部文号不带过去
			if(exchangeType == GovdocExchangeTypeEnum.jiaohuan.ordinal()
					|| exchangeType == GovdocExchangeTypeEnum.zhuansw.ordinal()
					|| exchangeType == GovdocExchangeTypeEnum.zhuanfw.ordinal()) {
				if ("serial_no".equals(elementName)) {
					continue;
				}
			}
			if ("receive_unit".equals(elementName)) {
				valueMap.put(formFieldBean.getName(), receiveUnit);
				continue;
			}
			if(!needToSetData){
				continue;
			}
			Object object = null;
			try {
				Method method = null;
				try {
					method = clazz.getMethod("get" + GovdocUtil.replaceUnderlineAndfirstToUpper("_" + elementName,"_"));
				} catch (NoSuchMethodException e) {
					continue;
				}
				object = method.invoke(seeyonEdoc);
				if (null != object) {
					if (object instanceof String) {
						//如果是枚举转化为枚举值
						if(Strings.isNotBlank(String.valueOf(object))){
							object = convertEnumIdIfExist(enumMap, elementName, object);
							String value = object.toString();
							if(formFieldBean.getInputTypeEnum()==FormFieldComEnum.EXTEND_ACCOUNT_DEPARTMENT){
								value+="hiddenValueundefined";
							}
							valueMap.put(formFieldBean.getName(), value);
						}
					} else if (object instanceof Organization) {
						Organization organization = (Organization) object;
						if(organization.getIdentification().getType().equals(AddressType.system.name())){
							valueMap.put(formFieldBean.getName(),organization.getIdentification().getName());
						}else{
							valueMap.put(formFieldBean.getName(),GovdocUtil.replaceUnderlineAndfirstToUpper("_" + organization.getIdentification().getType(),"_") + Address.split + organization.getIdentification().getId());
						}
					} else if (object instanceof PropertyValue) {
						PropertyValue propertyValue = (PropertyValue) object;
						valueMap.put(formFieldBean.getName(), propertyValue.getDisplay());
					} else if (object instanceof List) {
						List list = (List) object;
						StringBuilder value = new StringBuilder();
						for (Object object2 : list) {
							if (object2 instanceof Organization) {
								if (value.length() > 0) {
									value.append(",");
								}
								Organization organization = (Organization) object2;
								if(organization.getIdentification().getType().equals(AddressType.system.name())){
									value.append(organization.getIdentification().getName());
								}else{
									value.append(GovdocUtil.replaceUnderlineAndfirstToUpper("_" + organization.getIdentification().getType(),"_") + Address.split + organization.getIdentification().getId());
								}
							}
						}
						valueMap.put(formFieldBean.getName(), value.toString());
					} 
				}
			} catch (Exception e) {
				LOGGER.error("通过反射获取公文元素出错" + elementName,e);
			}
		}
	}
	
	private Object convertEnumIdIfExist(Map<String, EnumNameEnum> enumMap, String elementName, Object enumValue) throws BusinessException {
		if (enumMap.containsKey(elementName)) {
			List<CtpEnumItem> secretLevelList = enumManagerNew.getEnumItemByProCode(enumMap.get(elementName));
			for (CtpEnumItem ctpEnumItem : secretLevelList) {
				if (enumValue.toString().equals(ctpEnumItem.getShowvalue())) {
					return ctpEnumItem.getId();
				}
			}
			//只会返回枚举id，判断一下如果是id则返回，如果是字符返回-1
			try {
				if(enumValue!=null)
					Long.parseLong(enumValue.toString());
			} catch (NumberFormatException e) {
				return -1l;
			}
		}
		
		return enumValue;
	}
	
	private void callOcipReciept(List<Organization> organizations,BIZMessage bizMessage,List<GovdocExchangeDetail> details,Map<Long,String> ocipSendStatus) throws NumberFormatException, BusinessException{
//		if(AppContext.hasPlugin("ocip")&&CollectionUtils.isNotEmpty(organizations)){
//			Address add = organizations.get(0).getIdentification();
//		V3xOrgEntity entity = orgManager.getEntityOnlyById(Long.parseLong(add.getId()));
//			if(OrgUtil.isPlatformEntity(entity)&&bizMessage.getContentType()==BIZContentType.OFC){
//				for (GovdocExchangeDetail detail : details) {
//					try {
//						ocipExchangeEdocManager.receiptEdoc(-1l, detail.getId(),ocipSendStatus.get(detail.getId()));
//					} catch (Exception e) {
//						LOGGER.error(e);
//					}
//				}
//			}
//		}
	}
	
	@Override
	public void callBack(Object backData) throws BusinessException {
		if (CollectionUtils.isNotEmpty(failList)) {
			BIZExchangeData data = (BIZExchangeData) backData;
			BIZMessage bizMessage = data.getBussnissMessage();
			OFCEdocObject ofcEdocObject = (OFCEdocObject) bizMessage.getContent();
			StringBuilder info = new StringBuilder("");
			for (Organization organization : failList) {
				if (info.length() > 0) {
					info.append(",");
				}
				info.append(organization.getName());
				if (organization.getIdentification().getType().equals(AddressType.department.name())) {
					V3xOrgDepartment department = orgManager.getDepartmentById(Long.valueOf(organization.getIdentification().getId()));
					V3xOrgAccount account = orgManager.getAccountById(department.getOrgAccountId());
					info.append("("+account.getShortName()+")");
				}
			}
			Long currentUserId = Long.valueOf(data.getSender().getIdentification().getId());
			List<Organization> recivers = new ArrayList<Organization>();
			Address reciver = OrgUtil.getAddress(orgManager.getMemberById(currentUserId));
			recivers.add(Address.addressToOrgnation(reciver));
			BIZMessage backBizMessage = new BIZMessage();
			backBizMessage.setContentType(BIZContentType.RET);
			RETEdocObject object = new RETEdocObject();
			object.setOperation(EdocOperation.RECEIVED);
			object.setDatetime(new Date());
			object.setOpinion(info.toString());
			backBizMessage.setContent(object);
			BIZExchangeData backExchangeData = new BIZExchangeData();
			backExchangeData.setIdentifier(UUID.randomUUID().toString().replaceAll("-", ""));
			backExchangeData.setRecivers(recivers);
			backExchangeData.setSubject(ofcEdocObject.getTitle());
			backExchangeData.setBussnissMessage(backBizMessage);
			ExchangeService.sendObject(backExchangeData);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<V3xOrgMember> getPersonNextNode(FormBean formBean,FormDataMasterBean dataMasterBean, CtpTemplate ctpTemplate, V3xOrgMember member) throws BPMException{
		List<V3xOrgMember> listmember = new ArrayList<V3xOrgMember>();
		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setCurrentUserId(member.getId().toString());
		context.setCurrentAccountId(member.getOrgAccountId().toString());
   	 	context.setAppName(ModuleType.edoc.name());
   	 	context.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
        context.setDebugMode(false);
        context.setFormData(""+formBean.getId());
        context.setMastrid(""+dataMasterBean.getId());
        //设置正文内容，用来发送邮件的时候显示正文内容
        context.setStartUserId(member.getId().toString());
        context.setStartUserName(member.getName());
        context.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, member.getId());
        context.setStartAccountId(member.getOrgAccountId().toString());
        context.setStartAccountName("seeyon");
        context.setVersion("2.0");
        WorkflowApiManager wapi= (WorkflowApiManager)AppContext.getBean("wapi");
        wapi.addFormDataDisplayName(context, null);
        String xml1 = wapi.selectWrokFlowTemplateXml(ctpTemplate.getWorkflowId().toString());
        BPMProcess bpmProcess = BPMProcess.fromXML(xml1);
        context.setProcess(bpmProcess);
        List<BPMTransition> bpmTransitions = bpmProcess.getLinks();
        for (BPMTransition bpmTransition : bpmTransitions) {
	       	 BPMAbstractNode node = bpmTransition.getFrom();
	       	 if(node instanceof BPMStart){//如果是发起节点
	       		BPMAbstractNode nextNode = bpmTransition.getTo();
	       		if("split".equals(nextNode.getName())){
	        		List<BPMTransition> bpmTransitions2 = nextNode.getDownTransitions();
	        		for (BPMTransition bpmTransitio : bpmTransitions2) {
			       		try {
								listmember.addAll(wapi.getUserList("", (BPMHumenActivity)bpmTransitio.getTo(),context,true));
							} catch (Exception e) {
								LOGGER.error(e);
								//e.printStackTrace();
							}
			       	}
	        	}else{
		       		if(nextNode instanceof BPMHumenActivity || nextNode instanceof BPMEnd){//如果当前节点不是起始节点
		       			try {
							listmember.addAll(wapi.getUserList("", (BPMHumenActivity)nextNode,context,true));
						} catch (Exception e) {
							LOGGER.error(e);
							//e.printStackTrace();
						}
		       		}
	        	}
	        }
		}
        return listmember;
	}
	
	//判断是否来自意见区
	private boolean isFromOption(Attachment attachment, List<Comment> comments){
		Long commentId = attachment.getSubReference();
		Comment curComment = null;
		for (Comment comment : comments) {
			if (comment.getId().longValue() == commentId.longValue()) {
				curComment = comment;
				break;
			}
		}
		if (curComment != null) {
			// 来源
			Integer cType = curComment.getCtype();
			int ct = cType.intValue();
			if(cType != null && (ct == 1 || ct == 0)){//来自意见区
				return true;
			}
		}
		return false;
	}
//	public void setOcipExchangeEdocManager(IOCIPExchangeEdocManager ocipExchangeEdocManager) {
//		this.ocipExchangeEdocManager = ocipExchangeEdocManager;
//	}
	public void setOrganizationManager(IOrganizationManager organizationManager) {
		this.organizationManager = organizationManager;
	}
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public void setGovdocExchangeManager(GovdocExchangeManager govdocExchangeManager) {
		this.govdocExchangeManager = govdocExchangeManager;
	}
	public void setGovdocPubManager(GovdocPubManager govdocPubManager) {
		this.govdocPubManager = govdocPubManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setGovdocTemplateDepAuthDao(GovdocTemplateDepAuthDao govdocTemplateDepAuthDao) {
		this.govdocTemplateDepAuthDao = govdocTemplateDepAuthDao;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void setSignetManager(SignetManager signetManager) {
		this.signetManager = signetManager;
	}

	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}

	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
}
