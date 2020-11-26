package com.seeyon.ctp.common.template.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateHistory;
import com.seeyon.ctp.common.template.event.FormTemplateSaveAllParam;

import java.util.Map;

public interface FormTemplateDesignManager {
	
	public Map saveTemplate2Cache(Map jsonDomain)throws BusinessException;
	
	public void saveFlowTemplate2DB(FormTemplateSaveAllParam saveAllParams) throws BusinessException ;
	
	public void newFormBindQuartzJob(CtpTemplate template) throws BusinessException ;


	/**
	 * 将模板的历史中的数据更新到模板表中
	 * @param template  模板
	 * @param history   模板历史表
	 * @param isWholeTemplateNew  是否是整个模板新建
	 * @param isTemplateEditState 是否是模板编辑页面保存，编辑页面保存某些数据直接从页面获取即可，否则如定时任务需要从数据库中获取
	 * @throws BusinessException
	 */
    public void cloneAndSaveTemplateHistoryToTemplate(CtpTemplate template, CtpTemplateHistory history,
                                                      boolean isWholeTemplateNew, boolean isTemplateEditState) throws BusinessException;
    
    
	/**
	 * 根据Approve Id 撤销approve 审批数据
	 */
	public void cancleTempleteApprove(Long approveId)throws BusinessException;
}
