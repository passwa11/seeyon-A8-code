/**
 * Author : xuqw
 *   Date : 2015年8月26日 下午1:30:12
 *
 * Copyright (C) 2015 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.util;

import com.seeyon.apps.edoc.bo.EdocBodyBO;
import com.seeyon.apps.edoc.bo.EdocElementBO;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.apps.edoc.bo.EdocSummaryComplexBO;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

/**
 * <p>Title       : 公文</p>
 * <p>Description : 公文数据类型转换</p>
 * <p>Copyright   : Copyright (c) 2015</p>
 * <p>Company     : seeyon.com</p>
 */
public class DataTransUtil {

    /**
     * 将EdocSummary元素转化成对外提供的VO对象
     * @Author      : xuqw
     * @Date        : 2015年8月26日下午2:18:13
     * @param summary
     * @return
     */
    public static EdocSummaryBO transEdocSummary2BO(EdocSummary summary){
        
        EdocSummaryBO eVO = null;
        if(summary != null){
            
            eVO = new EdocSummaryBO();
            
            eVO.setId(summary.getId());
            eVO.setArchiveId(summary.getArchiveId());
            eVO.setOverWorkTime(summary.getOverWorkTime());
            eVO.setRunWorkTime(summary.getRunWorkTime());
            eVO.setCaseId(summary.getCaseId());
            eVO.setProcessId(summary.getProcessId());
            eVO.setDeadline(summary.getDeadline());
            eVO.setDeadlineDatetime(summary.getDeadlineDatetime());
            eVO.setEdocType(summary.getEdocType());
            eVO.setCreateTime(summary.getCreateTime());
            eVO.setStartUserId(summary.getStartUserId());
            eVO.setState(summary.getState());
            eVO.setAdvanceRemind(summary.getAdvanceRemind());
            eVO.setOrgAccountId(summary.getOrgAccountId());
            eVO.setTemplateId(summary.getTempleteId());
            eVO.setHasAttachments(summary.isHasAttachments());
            eVO.setFormAppid(summary.getFormAppid());
            eVO.setFormRecordid(summary.getFormRecordid());
            //以下是文单元素
            eVO.setSubject(summary.getSubject());
            eVO.setUrgentLevel(summary.getUrgentLevel());
            eVO.setGovdocType(summary.getGovdocType());
            
            /*eVO.setIdentifier(summary.getIdentifier());
            eVO.setHasArchive(summary.getHasArchive());
            eVO.setOverTime(summary.getOverTime());
            eVO.setRunTime(summary.getRunTime());
            eVO.setIsunit(summary.getIsunit());
            eVO.setCanTrack(summary.getCanTrack());
            eVO.setComment(summary.getComment());
            eVO.setCompleteTime(summary.getCompleteTime());
            eVO.setFormId(summary.getFormId());
            eVO.setSendTime(summary.getSendTime());
            eVO.setSubEdocType(summary.getSubEdocType());
            eVO.setProcessType(summary.getProcessType());
            eVO.setIsQuickSend(summary.getIsQuickSend());
            eVO.setCoverTime(summary.getCoverTime());
            eVO.setFinished(summary.getFinished());
            eVO.setWorklfowTimeout(summary.getWorklfowTimeout());
            eVO.setUpdateTime(summary.getUpdateTime());
            eVO.setTempleteId(summary.getTempleteId());
            eVO.setWorkflowRule(summary.getWorkflowRule());
            eVO.setOrgDepartmentId(summary.getOrgDepartmentId());
            eVO.setAffairId(summary.getAffairId());
            eVO.setCurrentNodesInfo(summary.getCurrentNodesInfo());

            //以下是文单元素
            eVO.setImportantLevel(summary.getImportantLevel());
            eVO.setCopies(summary.getCopies());
            eVO.setCopies2(summary.getCopies2());
            eVO.setCopyTo(summary.getCopyTo());
            eVO.setCopyToId(summary.getCopyToId());
            eVO.setCopyTo2(summary.getCopyTo2());
            eVO.setCopyToId2(summary.getCopyToId2());
            eVO.setCreatePerson(summary.getCreatePerson());
            eVO.setPackTime(summary.getPackTime());
            eVO.setDocMark(summary.getDocMark());
            eVO.setDocMark2(summary.getDocMark2());
            eVO.setSerialNo(summary.getSerialNo());
            eVO.setIssuer(summary.getIssuer());
            eVO.setKeepPeriod(summary.getKeepPeriod());
            eVO.setKeywords(summary.getKeywords());
            eVO.setPrintUnit(summary.getPrintUnit());
            eVO.setPrintUnitId(summary.getPrintUnitId());
            eVO.setPrinter(summary.getPrinter());
            eVO.setSendTo(summary.getSendTo());
            eVO.setSendToId(summary.getSendToId());
            eVO.setSendTo2(summary.getSendTo2());
            eVO.setSendToId2(summary.getSendToId2());
            eVO.setReportTo(summary.getReportTo());
            eVO.setReportToId(summary.getReportToId());
            eVO.setReportTo2(summary.getReportTo2());
            eVO.setReportToId2(summary.getReportToId2());
            eVO.setSecretLevel(summary.getSecretLevel());
            eVO.setUnitLevel(summary.getUnitLevel());
            eVO.setSendType(summary.getSendType());
            eVO.setSendUnit(summary.getSendUnit());
            eVO.setSendUnit2(summary.getSendUnit2());
            eVO.setSendUnitId(summary.getSendUnitId());
            eVO.setSendUnitId2(summary.getSendUnitId2());
            eVO.setSendDepartment(summary.getSendDepartment());
            eVO.setSendDepartment2(summary.getSendDepartment2());
            eVO.setSendDepartmentId(summary.getSendDepartmentId());
            eVO.setSendDepartmentId2(summary.getSendDepartmentId2());
            eVO.setAttachments(summary.getAttachments());
            eVO.setSigningDate(summary.getSigningDate());
            eVO.setStartTime(summary.getStartTime());
            eVO.setFilesm(summary.getFilesm());
            eVO.setFilefz(summary.getFilefz());
            eVO.setPhone(summary.getPhone());
            eVO.setParty(summary.getParty());
            eVO.setAdministrative(summary.getAdministrative());
            eVO.setReceiptDate(summary.getReceiptDate());
            eVO.setRegistrationDate(summary.getRegistrationDate());
            eVO.setAuditor(summary.getAuditor());
            eVO.setReview(summary.getReview());
            eVO.setUndertaker(summary.getUndertaker());
            eVO.setUndertakenoffice(summary.getUndertakenoffice());
            eVO.setUndertakenofficeId(summary.getUndertakenofficeId());
            eVO.setUndertakerDep(summary.getUndertakerDep());
            eVO.setUndertakerAccount(summary.getUndertakerAccount());
            eVO.setDocType(summary.getDocType());*/
        }
        
        return eVO;
    }
    
    
    /**
     * 将公文body对象转化成VO对象
     * @Author      : xuqw
     * @Date        : 2015年8月26日下午3:46:00
     * @param body
     * @return
     */
    public static EdocBodyBO transEdocBody2BO(EdocBody body){
        
        EdocBodyBO v = null;
        if(body != null){
            v = new EdocBodyBO();
            v.setId(body.getId());
            v.setContent(body.getContent());
            v.setContentType(body.getContentType());
            v.setContentName(body.getContentName());
            v.setCreateTime(body.getCreateTime());
            v.setLastUpdate(body.getLastUpdate());
            v.setContentNo(body.getContentNo());
            v.setContentStatus(body.getContentStatus());
        }
        return v;
    }
    
    /**
     * 将EdocSummaryModel对象转化成复合的BO对象，对外提供
     * @Author      : xuqw
     * @Date        : 2015年8月27日下午11:54:44
     * @param summary
     * @return
     */
    public static EdocSummaryComplexBO transEdocSumary2CBO(EdocSummaryModel summary){
        EdocSummaryComplexBO b = null;
        
        if(summary != null){
            
            b = new EdocSummaryComplexBO();
            
            b.setDeadLineDisplayDate(summary.getDeadLineDisplayDate());
            b.setDeadLineDate(summary.getDeadLineDate());
            b.setAffairId(summary.getAffairId());
            b.setAffairState(summary.getState());//这里暂时只有时间线用到这个
            b.setAffairSubState(summary.getState());//因为EdocSummaryModel 的state有时候表示state，有时候表示substate
            b.setSubject(summary.getSubject());
            b.setEdocUnit(summary.getEdocUnit());
            b.setBodyType(summary.getBodyType());
            b.setHasAttachments(summary.isHasAttachments());
            b.setState(summary.getEdocStatus());
            
            EdocSummary eSummary = summary.getSummary();
            if(eSummary != null){
                b.setStartUserId(eSummary.getStartUserId());
                b.setUrgentLevel(eSummary.getUrgentLevel());
            }
        }
        return b;
    }
    
    /**
     * 将EdocElement转化成BO对象对外提供
     * @Author      : xuqw
     * @Date        : 2015年8月28日上午12:42:50
     * @param e
     * @return
     */
    public static EdocElementBO truansEdocElement2BO(EdocElement e){
        
        EdocElementBO eBO = null;
        if(e != null){
            eBO = new EdocElementBO();
            
            eBO.setId(e.getId());
            eBO.setElementId(e.getElementId());
            eBO.setFieldName(e.getFieldName());
            eBO.setName(e.getName());
            eBO.setInputMode(e.getInputMode());
            eBO.setType(e.getType());
            eBO.setMetadataId(e.getMetadataId());
            eBO.setSystem(e.getIsSystem());
            eBO.setStatus(e.getStatus());
            eBO.setDomainId(e.getDomainId());
            eBO.setPoFieldName(e.getPoFieldName());
            eBO.setPoName(e.getPoName());
        }
        return eBO;
    }

    /**
     * 将EdocSummary转换为BO对象(BO中的全字段)
     * @author rz
     * @param summary
     * @return
     */
    public static EdocSummaryBO transEdocSummary2BOAll(EdocSummary summary){
        
        EdocSummaryBO eVO = null;
        if(summary != null){
        	
        	eVO = new EdocSummaryBO();
        	
        	eVO.setId(summary.getId());
        	eVO.setProcessId(summary.getProcessId());
        	eVO.setCaseId(summary.getCaseId());
        	eVO.setArchiveId(summary.getArchiveId());
        	eVO.setDeadlineDatetime(summary.getDeadlineDatetime());
        	eVO.setAdvanceRemind(summary.getAdvanceRemind());
        	eVO.setCreateTime(summary.getCreateTime());
        	eVO.setStartUserId(summary.getStartUserId());
        	eVO.setEdocType(summary.getEdocType());
        	eVO.setState(summary.getState());
        	eVO.setOrgAccountId(summary.getOrgAccountId());
        	eVO.setRunWorkTime(summary.getRunWorkTime());
        	eVO.setOverWorkTime(summary.getOverWorkTime());
        	eVO.setTemplateId(summary.getTempleteId());
        	//eVO.setHasAttachments(summary.geth);
        	eVO.setStartMemberId(summary.getStartMemberId());
        	eVO.setTempleteId(summary.getTempleteId());
        	/*++++++++++++++++  文单元素 开始  ++++++++++++++++*/
        	eVO.setSubject(summary.getSubject());
        	eVO.setUrgentLevel(summary.getUrgentLevel());
        	/*++++++++++++++++ 文单元素 结束 ++++++++++++++++*/
        	eVO.setSerialNo(summary.getSerialNo());
        	eVO.setSendUnit(summary.getSendUnit());
        	eVO.setDocMark(summary.getDocMark());
        }
        
        return eVO;
    }
}
