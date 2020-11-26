package com.seeyon.apps.collaboration.manager;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.index.bo.IndexInfo;
import com.seeyon.apps.index.bo.IndexInfo.FieldIndex_Type;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.form.modules.index.FormIndexModuleType;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class FormIndexEnableImpl extends ColIndexEnable implements FormIndexModuleType {
    private CollaborationApi collaborationApi;
    
    private ColIndexFormContentManager colIndexFormContentManager;

	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}
	
    public void setColIndexFormContentManager(
			ColIndexFormContentManager colIndexFormContentManager) {
		this.colIndexFormContentManager = colIndexFormContentManager;
	}

    /**
     * 获取表单类型：ModuleType
     * 由于在ctpContentAll中有流程表单的moduleType是协同，所以这里返回协同
     * @return
     */
    @Override
    public Integer getModuleType() {
        return ApplicationCategoryEnum.collaboration.getKey();
    }

    @Override
    public Integer findIndexResumeCount(Date beginDate, Date endDate) throws BusinessException {
        return this.collaborationApi.getColSummaryCount(beginDate, endDate,true);
    }
    

    @Override
    public List<Long> findIndexResumeIDList(Date starDate, Date endDate, Integer firstRow, Integer pageSize) throws BusinessException {
        return this.collaborationApi.findColSummaryIdList(starDate, endDate, firstRow, pageSize,true);
    }

    @Override
    public Map<String, Object> findSourceInfo(Long summaryId) throws BusinessException {
        Map<String,Object> resultMap = super.findSourceInfo(summaryId);
        if(resultMap != null){
            resultMap.put("moduleType",ModuleType.form);
        }
        return resultMap;
    }


    /**@Override
    public Integer getAppEnumKey() {
        return ApplicationCategoryEnum.form.getKey();
    }*/

  
    @Override
    public IndexInfo getIndexInfo(Long id, ModuleType type) throws BusinessException {
    	IndexInfo info = super.getIndexInfo(id);

    	ColSummary colSummary = (ColSummary)AppContext.getThreadContext("IndexInfo_summary");
    	String content = colIndexFormContentManager.getFormContent(colSummary);
    	if(info != null){
            info.setContent(content);
            info.addExtendProperties("formType", "flow", FieldIndex_Type.IndexNo.ordinal());
        }
        return info;
    }

    @Override
    public boolean isShowIndexSummary() throws BusinessException {
        return true;
    }

}
