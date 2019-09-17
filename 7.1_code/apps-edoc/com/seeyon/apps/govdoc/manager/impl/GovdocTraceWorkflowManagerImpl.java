package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocSignetManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.common.trace.manager.TraceWorkflowDataManager;
import com.seeyon.ctp.common.trace.po.WorkflowTracePO;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class GovdocTraceWorkflowManagerImpl extends TraceWorkflowDataManager {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocTraceWorkflowManagerImpl.class);
	
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocFormManager govdocFormManager;
	private GovdocSignetManager govdocSignetManager;
	private MainbodyManager ctpMainbodyManager;
	private AttachmentManager attachmentManager;
	private FileManager fileManager;
	private AffairManager affairManager;
	
	@Override
	public Map<String,Object> createRepealTraceData(Object summaryObj, CtpAffair currentAffair,List<CtpAffair> validAffairs, CtpTemplate template,String traceFlag)throws BusinessException {
		createRepealDataByType(summaryObj, currentAffair, validAffairs, template, StateEnum.col_cancel.key(),traceFlag);
		return null;
	}
	@Override
	public Map<String,Object> createStepBackTrackDataToBegin(Object summary,
			CtpAffair currentAffair, List<CtpAffair> validAffairs,
			CtpTemplate template,
			WorkflowTraceEnums.workflowTrackType trackType, String traceFlag) throws BusinessException {
		createRepealDataByType(summary,currentAffair,validAffairs,template,trackType.getKey(),traceFlag);
		DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_traceType", WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey());
		return null;
	}
	
	private  void  createRepealDataByType(Object summaryObj,CtpAffair currentAffair,List<CtpAffair> validAffairs,CtpTemplate template,int type,String isWFTrace) throws BusinessException{
		if(summaryObj == null || !(summaryObj instanceof EdocSummary) ){
			return;
		}
		EdocSummary summary = (EdocSummary)summaryObj;
		EdocSummary s = null;
		
		//勾选了追溯   或者是  追朔表里面有动态数据 直接产生静态数据
		List<Integer> typelist = new ArrayList<Integer>();
		typelist.add(WorkflowTraceEnums.workflowTrackType.step_back_normal.getKey());
		
		boolean hasDynamicDataFlag = false;//traceDao.hasDynamicData(summary.getId(), typelist);//是否有动态数据
		boolean isSelectedTraceFlag = false;//是否勾选流程追溯
		if(isSelectedTraceFlag || hasDynamicDataFlag) {
			try {
				s = (EdocSummary)summary.clone();
				s.setNewId(); 
				s.setState(EdocConstant.flowState.tracked.ordinal());//维护公文字段state，使用枚举flowState
				s.setCaseId(null);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			
			//AFFAIR数据
			CtpAffair cloneAffair  = null;
			try {
				cloneAffair  = (CtpAffair) currentAffair.clone();
				cloneAffair.setNewId();
				cloneAffair.setObjectId(s.getId());
				cloneAffair.setState(StateEnum.col_cancel.key());
				cloneAffair.setActivityId(null);
				DBAgent.save(cloneAffair);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			
			//clone正文：Ctp_Content_all 
			CtpContentAll cform = null;
			CtpContentAll content = null;
			Map<Long,String> formMap = new HashMap<Long,String>();
			List<CtpContentAll> contents = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.edoc, summary.getId());
			if(Strings.isNotEmpty(contents)) {
				try {
					cform =  (CtpContentAll)contents.get(0).clone();//文单
					cform.setNewId();
					cform.setModuleId(s.getId());
					LOGGER.info("撤销流程追溯生成表单数据：" + cform.getModuleId() + " : " + cform.getTitle() + " : " + cform.getContentType());
					cform.setContentType(MainbodyType.HTML.getKey());
					//83793 8月修复包：回退到发起人勾选流程追溯，在待发中查看，附件列表中显示表单正文中的附件显示重复.
					cform.setContentDataId(null);

					if(contents.size() > 0) {
						content = (CtpContentAll)contents.get(1).clone();//正文
						content.setNewId();
						content.setModuleId(s.getId());
						LOGGER.info("撤销流程追溯生成正文数据：" + content.getModuleId() + " : " + content.getTitle() + " : " + content.getContentType());
		                List<Integer> ctType = new ArrayList<Integer>();
		                ctType.add(Integer.valueOf(MainbodyType.OfficeWord.getKey()));
		                ctType.add(Integer.valueOf(MainbodyType.OfficeExcel.getKey()));
		                ctType.add(Integer.valueOf(MainbodyType.WpsWord.getKey()));
		                ctType.add(Integer.valueOf(MainbodyType.WpsExcel.getKey()));
		                ctType.add(Integer.valueOf(MainbodyType.Pdf.getKey()));
		                if(ctType.contains(content.getContentType()) && Strings.isNotBlank(content.getContent())) {
		                	V3XFile newFile = fileManager.clone(Long.parseLong(content.getContent()), true);
		                	content.setContent(String.valueOf(newFile.getId()));
		                }
					}
				}catch (Exception e) {
					LOGGER.error("", e);
				}
			}
			
			s.setBodyType(String.valueOf(MainbodyType.HTML.getKey()));
			s.setCanEdit(false); //转发的表单不能修改正文
			
			govdocSignetManager.saveSignature(summary.getId(), s.getId());
            if (Strings.isNotEmpty(validAffairs)) {
                for (CtpAffair aff : validAffairs) {
                    String formRightId = "-1";
                    if (aff != null) {
                        formRightId = govdocFormManager.findRightIdbyAffairIdOrTemplateId(aff, template, false,null);
                        //附件表单中的附件
                        //transForwardBody(newSummary.getId(),oldContentAll.getModuleId(), Long.valueOf(formRightId));
                        //将表单正文转换成HTML
                        String newContent = MainbodyService.getInstance().getContentHTML(ModuleType.edoc.getKey(), summary.getId(), formRightId);
                        formMap.put(aff.getId(), newContent);
                    }
                }
            }
			
			//意见
			Map<Long,Long> commentIdMap = new HashMap<Long,Long>();
			List<Comment> list = govdocCommentManager.getCommentAllByModuleId(ModuleType.edoc,summary.getId());
			List<CtpCommentAll> allList = new ArrayList<CtpCommentAll>();
			if(Strings.isNotEmpty(list)){
				for(Comment c : list){
					CtpCommentAll ctpCommentAll = c.toCommentAll();
					CtpCommentAll ctpCommentAllNew = null;
					try {
						ctpCommentAllNew = (CtpCommentAll)ctpCommentAll.clone();
						ctpCommentAllNew.setNewId();
						ctpCommentAllNew.setModuleId(s.getId());
						ctpCommentAllNew.setHidden(ctpCommentAll.isHidden());
						commentIdMap.put(ctpCommentAll.getId(), ctpCommentAllNew.getId());
						
					} catch (Exception e) {
						LOGGER.error("", e);
					}
					allList.add(ctpCommentAllNew);
				}
			}
			
			
			//克隆附件：
			List<Attachment> attachments = attachmentManager.getByReference(summary.getId());
			List<Attachment> newAttachments =  new ArrayList<Attachment>();
			//key ：意见ID  value : 附件json串
			Map<Long,List<Attachment>> jsonMap = new HashMap<Long,List<Attachment>>();
			if(Strings.isNotEmpty(attachments)){
				for(Attachment att : attachments){
					Attachment newAttachment = null;
					try {
						newAttachment = (Attachment) att.clone();
						newAttachment.setIdIfNew();
						newAttachment.setReference(s.getId());
					} catch (Exception e) {
						LOGGER.error("", e);
					}
					//正文附件
					boolean fromAttFlag = true;
					if(att.getReference().equals(att.getSubReference())){
						newAttachment.setSubReference(s.getId());
						fromAttFlag =false;
					}
					//意见附件
					if(commentIdMap.get(att.getSubReference()) != null){
						long commentId = commentIdMap.get(att.getSubReference());
						newAttachment.setSubReference(commentId);
						List<Attachment> al = new ArrayList<Attachment>();
						if(jsonMap.get(commentId)==null){
							al.add(newAttachment);
						}else{
							al = jsonMap.get(commentId);
							al.add(newAttachment);
						}
						jsonMap.put(commentId,al);
						fromAttFlag =false;
					}
					if(fromAttFlag){
					  newAttachment.setSubReference(100L);
					}
					newAttachments.add(newAttachment);
				}
				DBAgent.saveAll(newAttachments);
			}
			
			if(Strings.isNotEmpty(allList)){
				for(CtpCommentAll cca : allList){
					cca.setRelateInfo(JSONUtil.toJSONString(jsonMap.get(cca.getId())));
					if(cca.getPid()!=null && cca.getPid()!=0){
						cca.setPid(commentIdMap.get(cca.getPid()));
					}
				}
			}
			DBAgent.saveAll(allList);

			//追溯表的数据
			if(isSelectedTraceFlag) {
				if(Strings.isNotEmpty(validAffairs)) {
					List<WorkflowTracePO> wtlist = new ArrayList<WorkflowTracePO>();
					for(CtpAffair affair : validAffairs) {
						WorkflowTracePO po = createTraceWorkflowPO(cloneAffair, currentAffair.getMemberId() , summary.getStartUserId(), summary.getId());
						po.setAffairId(cloneAffair.getId());
						po.setTrackType(type);
						if(type == WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey()){//回到到首节点导致撤销的设置main状态为回退
							po.setMainWorkflowStatus(WorkflowTraceEnums.mainWorkflowStaus.stepback.ordinal());
						}
						po.setFormContent(formMap.get(affair.getId()));
						po.setActivityId(affair.getActivityId());
						po.setMemberId(affair.getMemberId());
						wtlist.add(po);
					}
					DBAgent.saveAll(wtlist);
				}
				List<Long> idList = new ArrayList<Long>();
				for(int a= 0 ;  a < validAffairs.size(); a++) {
					idList.add(validAffairs.get(a).getId());
				}
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_affair",idList);//产生了追溯数据的affair
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_summaryId",s.getId());//clone的静态数据的summaryId
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_affairId",cloneAffair.getId());//clone的静态数据affairId
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_traceType",Integer.valueOf(type));
			}
			
			/**生成新的colSummary,content数据 入库**/
			DBAgent.save(s);
			DBAgent.save(cform);
			DBAgent.save(content);

			if(hasDynamicDataFlag){
				Map _map = new HashMap();
				List<Long> idList = new ArrayList<Long>();
				List<WorkflowTracePO> dynamicData = null;//traceDao.getDynamicData(summary.getId(), typelist);
				Map<Long,Long> keyMap =new HashMap<Long,Long>();
				if(null != dynamicData && dynamicData.size() >0 ){
					for(int a =0; a < dynamicData.size(); a++){
						WorkflowTracePO workflowDyn = dynamicData.get(a);
						idList.add(workflowDyn.getAffairId());
						keyMap.put(workflowDyn.getAffairId(),workflowDyn.getId());
					}
					_map.put("id", idList);
					List<CtpAffair> byConditions = affairManager.getByConditions(null,_map);
					for(int a =0; a < byConditions.size(); a++){
							CtpAffair _affair = byConditions.get(a);
							String formRightId = "-1";
							if(_affair != null){
								formRightId = govdocFormManager.findRightIdbyAffairIdOrTemplateId(_affair,template,false,null);
							}
							//附件表单中的附件
							//transForwardBody(newSummary.getId(),oldContentAll.getModuleId(), Long.valueOf(formRightId));
							//将表单正文转换成HTML
							String _newContent = MainbodyService.getInstance().getContentHTML(ModuleType.edoc.getKey(), summary.getId(),formRightId);
							StringBuilder _hql =new StringBuilder("update "+WorkflowTracePO.class.getName()+" set ")
							.append(" formContent=:formContent where id=:id");
							Map<String,Object> _pMap = new HashMap<String, Object>();
							if(Strings.isNotBlank(_newContent) && null != keyMap.get(_affair != null ? _affair.getId() : null)){
								_pMap.put("formContent",_newContent);
								_pMap.put("id", keyMap.get(_affair != null ? _affair.getId() : null));
								DBAgent.bulkUpdate(_hql.toString(),_pMap);
							}
					}
				}
				super.updateWorkflowTraceDATA(summary.getId() , cloneAffair);
			}
		}
	}
	
	public WorkflowTracePO createTraceWorkflowPO(CtpAffair affair,Long operationMemberId,Long startMemberId,Long originalModuleId){
    	WorkflowTracePO wftpo = new WorkflowTracePO();
		
    	super.setProperties2WorklfowTracePO(affair, operationMemberId, startMemberId, wftpo);
		wftpo.setTrackType(StateEnum.col_cancel.key());//回退
		wftpo.setMainWorkflowStatus(WorkflowTraceEnums.mainWorkflowStaus.repeal.ordinal());
		wftpo.setOriginalModuleId(originalModuleId);
		return wftpo;
	}
	
	@Override
	public void deleteTraceWfRelativeData(Long summaryId, Long affairId) throws BusinessException {
		ctpMainbodyManager.deleteContentAllByModuleId(ModuleType.edoc,summaryId);
		
		govdocCommentManager.deleteCommentAllByModuleId(ModuleType.edoc,summaryId);
		
		attachmentManager.deleteByReference(summaryId);
		
		govdocSignetManager.deleteAllByDocumentId(summaryId);
		
		govdocSummaryManager.deleteEdocSummary(summaryId);
	}


	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setGovdocSignetManager(GovdocSignetManager govdocSignetManager) {
		this.govdocSignetManager = govdocSignetManager;
	}
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	
}
