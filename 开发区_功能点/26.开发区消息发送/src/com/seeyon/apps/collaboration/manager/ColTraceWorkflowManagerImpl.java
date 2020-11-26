package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.dao.ColDao;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.trace.api.TraceWorkflowDataManager;
import com.seeyon.ctp.common.trace.enums.WFTraceConstants;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.common.trace.po.WorkflowTracePO;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;

public class ColTraceWorkflowManagerImpl extends TraceWorkflowDataManager{
	private static Log LOG = CtpLogFactory.getLog(ColTraceWorkflowManagerImpl.class);
	private MainbodyManager ctpMainbodyManager;
	private ISignatureHtmlManager iSignatureHtmlManager;
	private AffairManager affairManager;
	private AttachmentManager attachmentManager;
	private CommentManager ctpCommentManager; 
	private ColDao colDao;
	private FileManager fileManager;
	private CAPFormManager capFormManager;
	public FileManager getFileManager() {
		return fileManager;
	}


	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}


	public CAPFormManager getCapFormManager() {
        return capFormManager;
    }


    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }



	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}


	public void setColDao(ColDao colDao) {
		this.colDao = colDao;
	}


	public void setiSignatureHtmlManager(ISignatureHtmlManager iSignatureHtmlManager) {
		this.iSignatureHtmlManager = iSignatureHtmlManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}



	public void setCtpCommentManager(CommentManager ctpCommentManager) {
		this.ctpCommentManager = ctpCommentManager;
	}
	/**
	 * 产生回退数据
	 */
	public void createStepBackTrackData(CtpAffair affair,Long startMemberId,Long operationMemberId,WorkflowTraceEnums.workflowTrackType trackType)throws BusinessException{
		super.createStepBackTrackData(affair, startMemberId, operationMemberId,trackType);
	}
	
	@Override
	public Map<String,Object> createRepealTraceData(Object summaryObj,CtpAffair currentAffair,List<CtpAffair> validAffairs,CtpTemplate template,String trackWorkflowType) throws BusinessException {
		return createRepealDataByType(summaryObj,currentAffair,validAffairs,template,StateEnum.col_cancel.key(),trackWorkflowType);
	}
    public WorkflowTracePO createTraceWorkflowPO(CtpAffair affair,Long operationMemberId,Long startMemberId,Long originalModuleId){
    	WorkflowTracePO wftpo = new WorkflowTracePO();
		
    	super.setProperties2WorklfowTracePO(affair, operationMemberId, startMemberId, wftpo);
		wftpo.setTrackType(StateEnum.col_cancel.key());//回退
		wftpo.setMainWorkflowStatus(WorkflowTraceEnums.mainWorkflowStaus.repeal.ordinal());
		wftpo.setOriginalModuleId(originalModuleId);
		return wftpo;
    }

    /***
     * 回退到首节点导致撤销的数据
     */
	@Override
	public Map<String,Object>  createStepBackTrackDataToBegin(Object summaryObj,CtpAffair currentAffair,List<CtpAffair> validAffairs,CtpTemplate template,WorkflowTraceEnums.workflowTrackType trackType,String _trackWorkflowType) throws BusinessException {
	    
	    Map<String,Object> retMap = createRepealDataByType(summaryObj,currentAffair,validAffairs,template,trackType.getKey(),_trackWorkflowType);
	    retMap.put(WFTraceConstants.WFTRACE_TYPE, WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey());
	    
	    return retMap;
	}

	private  Map<String,Object>  createRepealDataByType(Object summaryObj,CtpAffair currentAffair,List<CtpAffair> validAffairs,CtpTemplate template,int type,String _trackWorkflowType) throws BusinessException{

	    Map<String,Object> retMap = new HashMap<String,Object>();
		if(summaryObj == null || !(summaryObj instanceof ColSummary) ){
			return retMap;
		}
		ColSummary summary = (ColSummary)summaryObj;
		
		ColSummary s = null;
		/****
		 * 1：勾选了追溯   或者是  追朔表里面有动态数据 直接产生静态数据
		 * 2: if(追溯){增加新的追溯数据记录，并且和静态数据关联}
		 * 3: if(有动态数据){更新动态数据为静态数据}
		 */
		List<Integer> typelist = WorkflowTraceEnums.workflowTrackType.getDynamicTrackTypeKeys();
		
		boolean hasDynamicDataFlag = false; //TODO MUJ_  traceDao.hasDynamicData(summary.getId(), typelist);//是否有动态数据
		boolean isSelectedTraceFlag = String.valueOf(TemplateEnum.TrackWorkFlowType.track.ordinal()).equals(_trackWorkflowType);//是否勾选流程追溯
		
		if(isSelectedTraceFlag || hasDynamicDataFlag){
			try {
				s = (ColSummary)summary.clone();
				s.setNewId();
				s.setState(CollaborationEnum.flowState.cancel.ordinal());  //设置撤销状态，避免管理等其他地方能看到这个数据
				ColUtil.setWorkflowTrace(summary, true);
			} catch (Exception e) {
				LOG.error("", e);
			}
			
			//clone正文：Ctp_Content_all 
			CtpContentAll content = null;
			Map<Long,String> formMap = new HashMap<Long,String>();
			List<CtpContentAll> contents = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId());
			if(Strings.isNotEmpty(contents)){
				CtpContentAll origialcontent = contents.get(0);
				try {
					content =  (CtpContentAll)origialcontent.clone();
					//83793 8月修复包：回退到发起人勾选流程追溯，在待发中查看，附件列表中显示表单正文中的附件显示重复.
					//content.setContentDataId(null);
					content.setNewId();
	                content.setModuleId(s.getId());
	                List<Integer> ctType = new ArrayList<Integer>();
	                ctType.add(Integer.valueOf(MainbodyType.OfficeWord.getKey()));
	                ctType.add(Integer.valueOf(MainbodyType.OfficeExcel.getKey()));
	                ctType.add(Integer.valueOf(MainbodyType.WpsWord.getKey()));
	                ctType.add(Integer.valueOf(MainbodyType.WpsExcel.getKey()));
	                ctType.add(Integer.valueOf(MainbodyType.Pdf.getKey()));
	                if(ctType.contains(content.getContentType()) && null != content.getContentDataId()){
	                	//V3XFile	oldFile = fileManager.getV3XFile(content.getContentDataId());
	                	//Date createDate = oldFile.getCreateDate();
	                	V3XFile newFile = fileManager.clone(content.getContentDataId(), true);
	                	content.setContentDataId(newFile.getId());
	                }else{
	                	content.setContentDataId(null);
	                }
				}catch (Exception e) {
					LOG.error("", e);
				}
			}
			if(ColUtil.isForm(s.getBodyType())) {
				content.setContentType(MainbodyType.HTML.getKey());
				s.setBodyType(String.valueOf(MainbodyType.HTML.getKey()));
				s.setCanEdit(false); //转发的表单不能修改正文
				//s.setParentformSummaryid(summary.getId());
				iSignatureHtmlManager.save(summary.getId(), s.getId());
                if (Strings.isNotEmpty(validAffairs)) {
                    for (CtpAffair aff : validAffairs) {
                        String formRightId = "-1";
                        if (aff != null) {
                            formRightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(aff, template, false,null);
                            //附件表单中的附件
                            //transForwardBody(newSummary.getId(),oldContentAll.getModuleId(), Long.valueOf(formRightId));
                            //将表单正文转换成HTML
                            //String newContent = MainbodyService.getInstance().getContentHTML(ModuleType.collaboration.getKey(), summary.getId(), formRightId);
                            Map<String,Object> paramMap = new HashMap<String,Object>();
		                    
		                    paramMap.put("formId",content.getContentTemplateId());
		                    paramMap.put("moduleType",ModuleType.collaboration.getKey());
		                    paramMap.put("moduleId", summary.getId());
		                    paramMap.put("rightId", formRightId);
		                    paramMap.put("newSummaryId", s.getId());
		                    
							String newContent = capFormManager.getFormDataContentForForward(paramMap);
                            formMap.put(aff.getId(), newContent);
                        }
                    }
                }
			}
			
			//意见
			Map<Long,Long> commentIdMap = new HashMap<Long,Long>();
			List<Comment> list = ctpCommentManager.getCommentAllByModuleId(ModuleType.collaboration,summary.getId());
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
						LOG.error("", e);
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
						LOG.error("", e);
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
				LOG.error("", e);
			}
			
			
			//追溯表的数据
			if(isSelectedTraceFlag){
				if(Strings.isNotEmpty(validAffairs)){
					List<WorkflowTracePO> wtlist = new ArrayList<WorkflowTracePO>();
					for(CtpAffair affair : validAffairs){
						WorkflowTracePO po = createTraceWorkflowPO(cloneAffair, currentAffair.getMemberId() , summary.getStartMemberId(), summary.getId());
						if(ColUtil.isForm(summary.getBodyType())){
							po.setFormContent(formMap.get(affair.getId()));
						}
						po.setTrackType(type);
						po.setAffairId(cloneAffair.getId());
						po.setMemberId(affair.getMemberId());
						po.setActivityId(affair.getActivityId());
						if(type == WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey()){//回到到首节点导致撤销的设置main状态为回退
							po.setMainWorkflowStatus(WorkflowTraceEnums.mainWorkflowStaus.stepback.ordinal());
						}
						wtlist.add(po);
					}
					DBAgent.saveAll(wtlist);
				}
				List<Long> idList = new ArrayList<Long>();
				for(int a= 0 ;  a < validAffairs.size(); a++){
					idList.add(validAffairs.get(a).getId());
				}
				
				retMap.put(WFTraceConstants.WFTRACE_AFFAIRIDS, idList);//产生了追溯数据的affair
				retMap.put(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID,cloneAffair.getId());//clone的静态数据的affairId
				retMap.put(WFTraceConstants.WFTRACE_CLONENEW_SUMMARYID,s.getId());//clone的静态数据summaryId
				retMap.put(WFTraceConstants.WFTRACE_TYPE, Integer.valueOf(type));
				
			}
			/**生成新的colSummary,content数据 入库**/
			DBAgent.save(s);
			DBAgent.save(content);
			if(hasDynamicDataFlag){
				if(ColUtil.isForm(summary.getBodyType())) {//OA-62511
					Map _map = new HashMap();
					List<Long> idList = new ArrayList<Long>();
					List<WorkflowTracePO> dynamicData = null ; //TODO MUJ_ traceDao.getDynamicData(summary.getId(), typelist);
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
									formRightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(_affair,template,false,null);
								}
								//附件表单中的附件
								//transForwardBody(newSummary.getId(),oldContentAll.getModuleId(), Long.valueOf(formRightId));
								//将表单正文转换成HTML
								//String _newContent = MainbodyService.getInstance().getContentHTML(ModuleType.collaboration.getKey(), summary.getId(),formRightId);
								Map<String,Object> paramMap = new HashMap<String,Object>();
				                    
			                    paramMap.put("formId",content.getContentTemplateId());
			                    paramMap.put("moduleType",ModuleType.collaboration.getKey());
			                    paramMap.put("moduleId", summary.getId());
			                    paramMap.put("rightId", formRightId);
			                    paramMap.put("newSummaryId", s.getId());
			                    
								String _newContent = capFormManager.getFormDataContentForForward(paramMap);
								StringBuilder _hql =new StringBuilder("update "+WorkflowTracePO.class.getName()+" set ")
								.append(" formContent=:formContent where id=:id");
								Map<String,Object> _pMap = new HashMap<String, Object>();
								if(Strings.isNotBlank(_newContent) && _affair != null && null != keyMap.get(_affair.getId())){
									_pMap.put("formContent",_newContent);
									_pMap.put("id", keyMap.get(_affair.getId()));
									DBAgent.bulkUpdate(_hql.toString(),_pMap);
								}
						}
					}
				}
				super.updateWorkflowTraceDATA(summary.getId() , cloneAffair);
			}
		}
		return retMap;
	}

	@Override
	public void deleteTraceWfRelativeData(Long summaryId,Long affairId)throws BusinessException {
		
		ctpMainbodyManager.deleteContentAllByModuleId(ModuleType.collaboration,summaryId);
		
		ctpCommentManager.deleteCommentAllByModuleId(ModuleType.collaboration,summaryId);
		
		attachmentManager.deleteByReference(summaryId);
		
		iSignatureHtmlManager.deleteAllByDocumentId(summaryId);
		
		colDao.deleteColSummaryById(summaryId);
	}

	
	
}
