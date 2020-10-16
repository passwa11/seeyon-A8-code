package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.common.trace.manager.TraceWorkflowDataManager;
import com.seeyon.ctp.common.trace.po.WorkflowTracePO;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocBodyDao;
import com.seeyon.v3x.edoc.dao.EdocOpinionDao;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryRelation;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;


public class EdocTraceWorkflowManagerImpl extends TraceWorkflowDataManager {
	private static final Log log = LogFactory.getLog(EdocTraceWorkflowManagerImpl.class);
	private EdocBodyDao edocBodyDao;
	private FileManager fileManager;
	private EdocOpinionDao edocOpinionDao;
	private AttachmentManager attachmentManager;
	private AffairManager affairManager;
	private EdocSummaryDao edocSummaryDao;
	private EdocSummaryRelationManager edocSummaryRelationManager = null;;

	private V3xHtmDocumentSignatManager htmSignetManager;
	
	
	public void setEdocSummaryRelationManager(
            EdocSummaryRelationManager edocSummaryRelationManager) {
        this.edocSummaryRelationManager = edocSummaryRelationManager;
    }


	public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
        this.htmSignetManager = htmSignetManager;
    }


	
	
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
		this.edocSummaryDao = edocSummaryDao;
	}
	public void setEdocBodyDao(EdocBodyDao edocBodyDao) {
		this.edocBodyDao = edocBodyDao;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setEdocOpinionDao(EdocOpinionDao edocOpinionDao) {
		this.edocOpinionDao = edocOpinionDao;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void createStepBackTrackData(CtpAffair affair,Long startMemberId,Long operationMemberId, WorkflowTraceEnums.workflowTrackType trackType)throws BusinessException{
		super.createStepBackTrackData(affair, startMemberId, operationMemberId,trackType);
	}
	@Override
	public Map<String, Object> createRepealTraceData(Object summaryObj, CtpAffair currentAffair,List<CtpAffair> validAffairs, CtpTemplate template,String traceFlag)throws BusinessException {
		createRepealDataByType(summaryObj, currentAffair, validAffairs, template, StateEnum.col_cancel.key(),traceFlag);
		return null;
	}
    public WorkflowTracePO createTraceWorkflowPO(CtpAffair affair,Long operationMemberId,Long startMemberId,Long originalModuleId){
    	WorkflowTracePO wftpo = new WorkflowTracePO();
		
    	//super.setProperties2WorklfowTracePO(affair, operationMemberId, startMemberId, wftpo);
    	wftpo.setIdIfNew();
		//wftpo.setMemberId(affair.getMemberId());
		wftpo.setModuleId(affair.getObjectId());
		wftpo.setModuleType(affair.getApp());
		wftpo.setAffairId(affair.getId());
		
		
		wftpo.setOperationDate(new Date());
		wftpo.setOperationUserId(operationMemberId);
		if(!operationMemberId.equals(AppContext.currentUserId())){
			wftpo.setTransactorId(AppContext.currentUserId());
		}
		wftpo.setSubject(affair.getSubject());
		wftpo.setStartMemeberId(startMemberId);
		
		wftpo.setStartDate(affair.getCreateDate());
		wftpo.setActivityId(affair.getActivityId());
		wftpo.setTrackType(StateEnum.col_cancel.key());//回退
		wftpo.setMainWorkflowStatus(WorkflowTraceEnums.mainWorkflowStaus.repeal.ordinal());
		wftpo.setOriginalModuleId(originalModuleId);
		return wftpo;
    }
	@Override
	public void deleteTraceWfRelativeData(Long summaryId,Long affairId)throws BusinessException {
		edocSummaryDao.delete(summaryId);
		
		attachmentManager.deleteByReference(summaryId);
		
		
		//TODO 删除file
		
		affairManager.deleteAffair(affairId);
		
	}
	@Override
	public Map<String, Object> createStepBackTrackDataToBegin(Object summary,
			CtpAffair currentAffair, List<CtpAffair> validAffairs,
			CtpTemplate template,WorkflowTraceEnums.workflowTrackType trackType,String traceFlag) throws BusinessException {
		
		createRepealDataByType(summary,currentAffair,validAffairs,template,trackType.getKey(),traceFlag);
		DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_traceType", WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey());
		return null;
	}
	
	private  void  createRepealDataByType(Object summaryObj,CtpAffair currentAffair,List<CtpAffair> validAffairs,CtpTemplate template,int type,String traceFlag) throws BusinessException{

		
		if(summaryObj == null || !(summaryObj instanceof EdocSummary) ){
			return;
		}
		EdocSummary summary = (EdocSummary)summaryObj;
		EdocSummary s = null;
		
		/****
		 * 1：勾选了追溯   或者是  追朔表里面有动态数据 直接产生静态数据
		 */
		List<Integer> typelist = new ArrayList<Integer>();
		typelist.add(WorkflowTraceEnums.workflowTrackType.step_back_normal.getKey());
		
		boolean hasDynamicDataFlag = false;// traceDao.hasDynamicData(summary.getId(), typelist);//是否有动态数据
		boolean isSelectedTraceFlag = false;//是否勾选流程追溯 (7.1无流程追溯，暂时写死)
		if(isSelectedTraceFlag || hasDynamicDataFlag){
			try {
				s = (EdocSummary)summary.clone();
				s.setNewId(); 
				s.setCaseId(null);
				s.setEdocSendRecords(null);
			} catch (Exception e) {
				log.error("", e);
			}
			List<Long> fileIds = new ArrayList<Long>();
			/**
			 * 公文这边clone的数据包括了
			 * 1 EdocSummay        概要表
			 * 	   两个对象属性   正文和意见
			 * 2 list<EdocBody>    正文  
			 * 3 list<EodOpinion>  意见
			 * 4 List<Attachment>  附件
			 * 5  签批
			 * 6 workflowtracepo   流程追溯的数据
			 * 7 图片签批信息
			 */
			Set<EdocBody> oldBodies = s.getEdocBodies();//正文
			Set<EdocBody> newBodies = new HashSet<EdocBody>();
			List<EdocBody> newBodys = new ArrayList<EdocBody>();
			if(null != oldBodies){
				Iterator<EdocBody> it = oldBodies.iterator();
				while(it.hasNext()){
					EdocBody next = it.next();
					try {
						EdocBody  ebody =null;
						ebody = (EdocBody)next.clone();
						ebody.setIdIfNew();
						ebody.setEdocId(s.getId());
						//edocBodyDao.saveOrUpdate(ebody);
						
						if("OfficeWord".equals(ebody.getContentType())
								|| "OfficeExcel".equals(ebody.getContentType())
								||"WpsWord".equals(ebody.getContentType())
								||"WpsExcel".equals(ebody.getContentType())
								||"Pdf".equals(ebody.getContentType())){
							fileIds.add(Long.valueOf(next.getContent()));
						}
						
						newBodies.add(ebody);
						newBodys.add(ebody);
					} catch (CloneNotSupportedException e) {
					    log.error("", e);
					}
				}
				s.setEdocBodies(newBodies);
			}
			Map<Long,Long> commentIdMap = new HashMap<Long,Long>();
			Set<EdocOpinion> oldOpinions = s.getEdocOpinions();//意见
			Set<EdocOpinion> newOpinions = new HashSet<EdocOpinion>();
			if(null != oldOpinions){
				List<EdocOpinion> allList = new ArrayList<EdocOpinion>();
				Iterator<EdocOpinion> it = oldOpinions.iterator();
				while(it.hasNext()){
					EdocOpinion next = it.next();
					try {
						EdocOpinion  eopin =null;
						eopin = (EdocOpinion)next.clone();
						eopin.setIdIfNew();
						eopin.setEdocSummary(s);
						commentIdMap.put(next.getId(), eopin.getId());
						newOpinions.add(eopin);
						allList.add(eopin);
					} catch (CloneNotSupportedException e) {
					    log.error("", e);
					}
				}
				s.setEdocOpinions(newOpinions);
				DBAgent.saveAll(allList);//保存意见
			}
			DBAgent.save(s);
			
			//克隆关联收文信息
			EdocSummaryRelation edocSummaryRelationR = this.edocSummaryRelationManager.findRecEdoc(summary.getId(), 0);
			if(edocSummaryRelationR != null){
			    EdocSummaryRelation newEdocSummaryRelation = cloneEdocSummaryRelation(edocSummaryRelationR);
			    newEdocSummaryRelation.setSummaryId(s.getId());
			    DBAgent.save(newEdocSummaryRelation);
			}
			
			//克隆发文数据
			List<EdocSummaryRelation> edocSummaryRelations = this.edocSummaryRelationManager.findRelationsBySummaryId(summary.getId(), 1);
			if(Strings.isNotEmpty(edocSummaryRelations)){
			    List<EdocSummaryRelation> newEdocSummaryRelations = new ArrayList<EdocSummaryRelation>();
			    for(EdocSummaryRelation e : edocSummaryRelations){
			        EdocSummaryRelation newEdocSummaryRelation = cloneEdocSummaryRelation(e);
	                newEdocSummaryRelation.setSummaryId(s.getId());
	                newEdocSummaryRelations.add(newEdocSummaryRelation);
			    }
			    DBAgent.saveAll(newEdocSummaryRelations);
			}
			
			
			//克隆附件：
			List<Attachment> attachments = attachmentManager.getByReference(summary.getId());
			List<Attachment> newAttachments =  new ArrayList<Attachment>();
			Map<Long,V3XFile> fileMap  = new HashMap<Long,V3XFile>();
			if(Strings.isNotEmpty(attachments)){
				for(Attachment att : attachments){
					
					Attachment newAttachment = null;
					try {
						newAttachment = (Attachment) att.clone();
						newAttachment.setIdIfNew();
						newAttachment.setReference(s.getId());
					} catch (Exception e) {
						log.error("", e);
					}
					//正文附件
					if(att.getReference().equals(att.getSubReference())){
						newAttachment.setSubReference(s.getId());
					}
					//意见附件
					if(commentIdMap.get(att.getSubReference()) != null){
						newAttachment.setSubReference(commentIdMap.get(att.getSubReference()));
					}
					newAttachments.add(newAttachment);
					fileIds.add(att.getFileUrl());
				}
			}
			
			
			List<V3XFile> files = fileManager.getV3XFile(fileIds.toArray(new Long[fileIds.size()]));
			if(Strings.isNotEmpty(files)){
				for(V3XFile f:files){
					try {
						V3XFile v3xFile = fileManager.clone(f.getId());
						fileMap.put(f.getId(), v3xFile);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			}
			for(Attachment att : newAttachments){
				V3XFile newFile = fileMap.get(att.getFileUrl());
				if(null != newFile){
					att.setFileUrl(newFile.getId());
				}
			}
			DBAgent.saveAll(newAttachments);
			
			for(EdocBody eb : newBodys){
				if("OfficeWord".equals(eb.getContentType())
						|| "OfficeExcel".equals(eb.getContentType())
						||"WpsWord".equals(eb.getContentType())
						||"WpsExcel".equals(eb.getContentType())
						||"Pdf".equals(eb.getContentType())){
					
					V3XFile newFile = fileMap.get(Long.valueOf(eb.getContent()));
					if(null !=  newFile){
						eb.setContent(String.valueOf(newFile.getId()));
					}
				}
			}
			
			//更新edocBody数据
			if(Strings.isNotEmpty(newBodys)) {
				DBAgent.saveAll(newBodys);
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
				log.error("", e);
			}
			
			
			//
			
			
			try {
				List<V3xHtmDocumentSignature> listNew = new ArrayList<V3xHtmDocumentSignature>();
				List<V3xHtmDocumentSignature> listOld = htmSignetManager.findBySummaryIdAndType(summary.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
				for(int a =0 ; a < listOld.size();a ++){
					V3xHtmDocumentSignature v3xSign = listOld.get(a);
					V3xHtmDocumentSignature clone = (V3xHtmDocumentSignature)v3xSign.clone();
					clone.setSummaryId(s.getId());
					clone.setAffairId(cloneAffair.getId());
					clone.setIdIfNew();
					listNew.add(clone);
				}
				
				//签名图片信息
				List<V3xHtmDocumentSignature> signPicList = htmSignetManager.findBySummaryIdAndType(summary.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());
				if(Strings.isNotEmpty(signPicList)){
				    for(V3xHtmDocumentSignature v3xSign : signPicList){
				        V3xHtmDocumentSignature clone = (V3xHtmDocumentSignature)v3xSign.clone();
	                    clone.setSummaryId(s.getId());
	                    clone.setIdIfNew();
	                    listNew.add(clone);
				    }
				}
				
				DBAgent.saveAll(listNew);
			} catch (Exception e) {
				log.error("", e);
			}
			
			//追溯表的数据
			if(isSelectedTraceFlag){
				//7.1无流程追溯
//				if(Strings.isNotEmpty(validAffairs)){
//					List<WorkflowTracePO> wtlist = new ArrayList<WorkflowTracePO>();
//					for(CtpAffair affair : validAffairs){
//						WorkflowTracePO po = createTraceWorkflowPO(cloneAffair, currentAffair.getMemberId() , summary.getStartUserId(), summary.getId());
//						po.setAffairId(cloneAffair.getId());
//						po.setTrackType(type);
//						if(type == WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey()){//回到到首节点导致撤销的设置main状态为回退
//							po.setMainWorkflowStatus(WorkflowTraceEnums.mainWorkflowStaus.stepback.ordinal());
//						}
//						po.setActivityId(affair.getActivityId());
//						po.setMemberId(affair.getMemberId());
//						wtlist.add(po);
//					}
//					DBAgent.saveAll(wtlist);
//				}
				List<Long> idList = new ArrayList<Long>();
				for(int a= 0 ;  a < validAffairs.size(); a++){
					idList.add(validAffairs.get(a).getId());
				}
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_affair",idList);//产生了追溯数据的affair
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_summaryId",s.getId());//clone的静态数据的summaryId
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_affairId",cloneAffair.getId());//clone的静态数据affairId
				DateSharedWithWorkflowEngineThreadLocal.addToTraceDataMap("traceData_traceType",Integer.valueOf(type));
			}
			if(hasDynamicDataFlag){
				super.updateWorkflowTraceDATA(summary.getId(), cloneAffair);
			}
		}
		
	}
	
	/**
	 * 克隆关联数据
	 * @Author      : xuqiangwei
	 * @Date        : 2014年8月25日下午4:54:23
	 * @param edocSummaryRelation
	 * @return
	 */
	private EdocSummaryRelation cloneEdocSummaryRelation(EdocSummaryRelation edocSummaryRelation){
	    
	    if(edocSummaryRelation == null){
	        return null;
	    }
	    
	    EdocSummaryRelation newEdocSummaryRelation = new EdocSummaryRelation();
        newEdocSummaryRelation.setIdIfNew();
        newEdocSummaryRelation.setEdocType(edocSummaryRelation.getEdocType());
        newEdocSummaryRelation.setMemberId(edocSummaryRelation.getMemberId());
        newEdocSummaryRelation.setRecAffairId(edocSummaryRelation.getRecAffairId());
        newEdocSummaryRelation.setRelationEdocId(edocSummaryRelation.getRelationEdocId());
        newEdocSummaryRelation.setSummaryId(edocSummaryRelation.getSummaryId());
        newEdocSummaryRelation.setType(edocSummaryRelation.getType());
        return newEdocSummaryRelation;
	}
}
