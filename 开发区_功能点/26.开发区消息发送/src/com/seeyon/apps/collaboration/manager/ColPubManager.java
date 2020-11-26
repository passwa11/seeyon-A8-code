/**
 * $Author: 翟锋$
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.manager;

import java.util.Map;

import com.seeyon.apps.collaboration.bo.ColInfo;
import com.seeyon.apps.collaboration.bo.SendCollResult;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;

/**
 * @author zhaifeng
 *
 */
public interface ColPubManager {

    /**
     * startNewflowCaseFromHasFlow
     * 发起子（新）流程方法(来自有流程表单触发)
     * @param templateId 子(新)流程所属模板Id
     * @param senderId 发起者Id
     * @param formId 表单Id值
     * @param formMasterId 表单数据记录主键Id值
     * @param parentSummaryId 所属父协同Id
     * @param parentNodeId 所属父协同节点Id
     * @param parentAffairId 所属福协同待办事项Id
     * @throws NoSuchTemplateException
     * @throws BusinessException
     */
    public abstract ColSummary transStartNewflowCaseFromHasFlow(Long templateId, Long senderId, Long formMasterId,
            Long parentSummaryId, String parentNodeId, Long parentAffairId, boolean isRelated) throws BusinessException;

    /**
     * startNewflowCaseFromNoFlow
     * 发起子（新）流程方法(来自无流程表单触发)
     * @param templateId 子(新)流程所属模板Id
     * @param senderId 发起者Id
     * @param formId 表单Id值
     * @param formMasterId 表单数据记录主键Id值
     * @param parentFormId 所属父无流程表单Id
     * @param parentFormMasterId 所属父无流程表单主键记录Id
     * @param formType: 只能传2和3：2表示基础数据，3表示信息管理
     * @throws NoSuchTemplateException
     * @throws BusinessException
     */
    public abstract ColSummary transStartNewflowCaseFromNoFlow(Long templateId, Long senderId, Long formMasterId,
            Long parentFormId, Long parentFormMasterId, int formType, boolean isRelated) throws BusinessException;
   
    /**
     * 发起模板协同
     * 
     * @param sendType
     * @param templateId 新协同对应的模板ID
     * @param senderId 新协同的发起者
     * @param formMasterId 如果是表单\Office正文的话, 请提前保存好表单\Office， 把对应的ID传递过来
     * @param fromSummaryId 主流程summaryId
     * @param extParams 扩展参数
     * @return 
     * @throws BusinessException
     */
    public SendCollResult transSendColl(ColConstant.SendType sendType, Long templateId, Long senderId,
            Long formMasterId, Long parentSummaryId, Map<String,Object> extParams) throws BusinessException;  
    
    /**
     * 发起模板协同
     * 
     * @param sendType
     * @param templateId 新协同对应的模板ID
     * @param senderId 新协同的发起者
     * @param formMasterId 如果是表单\Office正文的话, 请提前保存好表单\Office， 把对应的ID传递过来
     * @param fromSummaryId 主流程summaryId
     * 
     * @return 
     * @throws BusinessException
     */
    public SendCollResult transSendColl(ColConstant.SendType sendType, Long templateId, Long senderId, Long formMasterId,Long fromSummaryId) throws BusinessException;
    
    
    
    /**
     * 发起模板协同
     * 
     * @param sendType
     * @param templateId 新协同对应的模板ID
     * @param senderId 新协同的发起者
     * @param formMasterId 如果是表单\Office正文的话, 请提前保存好表单\Office， 把对应的ID传递过来
     * @param fromSummaryId 主流程summaryId
     * @param newSumamryId 新发起的子流程的id
     * @return 
     * @throws BusinessException
     */
    public SendCollResult transSendColl(
        	ColConstant.SendType sendType, 
        	Long templateId, Long senderId, 
        	Long formMasterId, 
        	Long parentSummaryId,
        	Long newSumamryId) throws BusinessException;
   
   
    /**
     * 兼容其它工程调用，重载
     * @param sendType
     * @param templateId
     * @param senderId
     * @param formMasterId
     * @return
     * @throws BusinessException
     */
    public SendCollResult transSendColl(ColConstant.SendType sendType, Long templateId, Long senderId, Long formMasterId) throws BusinessException;
    
    /**
     * 发起协同接口
     * 
     * @param sendtype
     * @param info  <pre>
     *              1.需要指定summary\senderUser\comment\contentBody
     *              2.设置协同的formAppid\formid
     *              </pre>
     * @return
     * @throws BusinessException
     */
    public SendCollResult transSendColl(ColConstant.SendType sendtype, ColInfo info) throws BusinessException;
    
    /**
     * 
     * @param summary
     */
    public void updateSpecialBackedAffair(ColSummary summary);
    
    /**
     * 
     * @Title: transPigeonhole   
     * @Description: 发起后进行归档 
     * @param summary
     * @param affair
     * @param template
     * @throws BusinessException      
     * @return: void  
     * @date:   2019年5月21日 下午12:13:05
     * @author: xusx
     * @since   V7.1SP1	       
     * @throws
     */
    public void transPigeonhole(ColConstant.SendType sendtype,ColSummary summary,CtpAffair affair) throws BusinessException;

}