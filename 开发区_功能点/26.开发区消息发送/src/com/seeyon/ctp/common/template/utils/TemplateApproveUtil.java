package com.seeyon.ctp.common.template.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateHistory;
import com.seeyon.ctp.common.po.template.CtpTemplateOrg;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.form.po.CtpTemplateRelationAuth;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.Strings;

public class TemplateApproveUtil {
	

	private static final Log LOGGER = LogFactory.getLog(TemplateApproveUtil.class);
	
	public static  void saveRACI2ThsTemplate(CtpTemplate template, List<CtpTemplateOrg>  orgs,TemplateManager templateManager) throws BusinessException {
		
		templateManager.deleteCtpTemplateOrgByTemplateId(template.getId());
		
        if(Strings.isNotEmpty(orgs)){
        	List<CtpTemplateOrg> newOrgs = new ArrayList<CtpTemplateOrg>();
        	for(CtpTemplateOrg org : orgs){
        		try {
					CtpTemplateOrg newOrg = (CtpTemplateOrg) org.clone();
					newOrg.setTemplateId(template.getId());
					newOrg.setNewId();
	        		newOrgs.add(newOrg);
				} catch (CloneNotSupportedException e) {
					LOGGER.error("",e);
				}
        	}
        	templateManager.saveCtpTemplateOrgs(newOrgs);
        }
	}
	
	
	
	public static void deepClone(CtpTemplate template, CtpTemplateHistory history) {

		// 基本属性clone
		Long templateId = template.getId();
		Long orginalTemplateWorkFlowId = (Long) template.getExtraAttr("orginalTemplateWorkFlowId");
        if(orginalTemplateWorkFlowId == null || Long.valueOf(-1).equals(orginalTemplateWorkFlowId) ){ //为空再添加，确保一次完成的流程编辑保存操作，只添加一次，否则重复添加多次还是有问题。
        	template.putExtraAttr("orginalTemplateWorkFlowId", template.getWorkflowId() == null ? -1 : template.getWorkflowId());
        }
        
		BeanUtils.convert(template, history);
		template.putExtraAttr("orginalTemplateWorkFlowId", (Long)template.getExtraAttr("orginalTemplateWorkFlowId"));
		template.setId(templateId);

		// 复杂属性colne

		// 授权
		List<CtpTemplateAuth> templateAuths = (List<CtpTemplateAuth>) history.getExtraAttr("authList");
		List<CtpTemplateAuth> newAuths = cloneTemplateAuthListToThsTemplate(template.getId(), templateAuths);
		template.putExtraAttr("authList", newAuths);

		// 督办
		String superviseStr = (String) history.getExtraAttr("superviseStr");
		template.putExtraAttr("superviseStr", superviseStr);

		// 表单关联授权
		List<CtpTemplateRelationAuth> formAuths = (List<CtpTemplateRelationAuth>) history.getExtraAttr("relationAuthList");
		List<CtpTemplateRelationAuth> newFormRelationAuths = cloneFormRelationAuthToThsTemplate(template.getId(),formAuths);
		template.putExtraAttr("relationAuthList", newFormRelationAuths);
		
		//附件
		List<Attachment> attachments = (List<Attachment>) history.getExtraAttr("attachmentList");
		if(Strings.isNotEmpty(attachments)){
    		List<Attachment> atts = new ArrayList<Attachment>();
    		for(Attachment att : attachments){
    			Attachment a;
				try {
					a = (Attachment) att.clone();
					a.setNewId();
					a.setReference(template.getId());
					a.setSubReference(template.getId());
					atts.add(a);
				} catch (CloneNotSupportedException e) {
					LOGGER.error("", e);
				}
    		}
    		template.putExtraAttr("attachmentList",atts);
		}else{
			template.putExtraAttr("attachmentList",attachments);
		}
		

		
		//AI
		cloneTemplateHistoryAiPropertyToTemplate(template,history);	

	}

	public static void cloneTemplateHistoryAiPropertyToTemplate(CtpTemplate template,CtpTemplateHistory history){
		
		template.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_MONITOR_CAN_SEND_MSG,(String)history.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_MONITOR_CAN_SEND_MSG));
		template.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR,(String)history.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR));
		template.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_AI_PROCESSING_CONDITION,(String)history.getExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_AI_PROCESSING_CONDITION));
		
	}
	public static void cloneAndSaveAttachment(CtpTemplate target, List<Attachment> attachments,AttachmentManager attachmentManager)
			throws BusinessException {
		try {
        	attachmentManager.deleteOnlyAttByReference(target.getId());
        	if(Strings.isNotEmpty(attachments)){
        		List<Attachment> atts = new ArrayList<Attachment>();
        		for(Attachment att : attachments){
        			Attachment a = (Attachment) att.clone();
        			a.setNewId();
        			a.setReference(target.getId());
        			a.setSubReference(target.getId());
        			atts.add(a);
        		}
        		attachmentManager.create(atts);
        		TemplateUtil.setHasAttachments(target, true);
        	}
        } catch (Exception e) {
        	LOGGER.error("", e);
        }
	}
	
	
	private static List<CtpTemplateRelationAuth> cloneFormRelationAuthToThsTemplate(Long toTemplateId,List<CtpTemplateRelationAuth> formAuths) {
		List<CtpTemplateRelationAuth> newFormAuths = null;
		if(Strings.isNotEmpty(formAuths)){
			newFormAuths = new ArrayList<CtpTemplateRelationAuth>();
			for(CtpTemplateRelationAuth auth :formAuths ){
				
				try {
					CtpTemplateRelationAuth newAuth = (CtpTemplateRelationAuth) auth.clone();
					auth.setNewId();
					auth.setTemplateId(toTemplateId);
					newFormAuths.add(newAuth);
				} catch (CloneNotSupportedException e) {
					LOGGER.error("",e); 
				}
			}
		}
		return newFormAuths;
	}

	private static List<CtpTemplateAuth> cloneTemplateAuthListToThsTemplate(Long toTemplateId, List<CtpTemplateAuth> templateAuths) {
		List<CtpTemplateAuth> newAuths = null;
			
        if(Strings.isNotEmpty(templateAuths)){
        	newAuths = new ArrayList<CtpTemplateAuth>();
        	for(CtpTemplateAuth auth :templateAuths){
        		
        		CtpTemplateAuth newAuth = null;
				try {
					newAuth = (CtpTemplateAuth) auth.clone();
					newAuth.setModuleId(toTemplateId);
            		newAuth.setNewId();
				} catch (CloneNotSupportedException e) {
					LOGGER.error("",e);
				}
        		newAuths.add(newAuth);
        	}
        }
		return newAuths;
	}
	
	
}
