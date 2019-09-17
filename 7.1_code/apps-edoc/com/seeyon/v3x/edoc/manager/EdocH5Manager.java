package com.seeyon.v3x.edoc.manager;

import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocManagerModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryBO;

/**
 * <p>Title: 公文H5Manager</p>
 * <p>Description: 公文H5Manager</p>
 * <p>Copyright: Copyright (C) 2015 Seeyon, Inc. All rights reserved.</p>
 * <p> Company: 北京致远协创软件有限公司 </p>
 * 
 * @author muyx
 */
public interface EdocH5Manager {
    
    public EdocSummaryBO getEdocSummaryBO(Long summaryId, Long affairId, String openFrom, Map<String,Object> params) throws BusinessException;

    public String getEdocBodyContentRoot(Long affairId) throws BusinessException;

    public String transDealEdoc(EdocManagerModel edocManagerModel, Map<String, String> wfParamMap) throws BusinessException;

    public boolean transDoZCDB(EdocManagerModel edocManagerModel, User user, Map<String, String> wfParamMap) throws BusinessException;

    public String transStepback(EdocManagerModel edocManagerModel,Map<String, String> params) throws BusinessException;

    public String transRepeal(EdocManagerModel edocManagerModel,Map<String, String> params) throws BusinessException;
    
    public boolean isValidAffair(Long affairId,String pageNodePolicy, Map<String, String> ret);
    
    public boolean checkEdocMarkisUsed(String markStr, String edocId, String summaryOrgAccountId);
    /**
     * rest调用公文移交
     */
    public String transEdocTransfer(EdocManagerModel edocManagerModel,Long transferMemberId,Map<String, String> params) throws BusinessException;
    
}