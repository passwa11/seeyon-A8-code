/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.manager;
import java.util.ArrayList;
import java.util.List;

import com.seeyon.apps.collaboration.dao.ISearchInterfaceColDao;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.isearch.manager.ISearchManager;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;
/**
 * @author mujun
 *
 */
public class ISearchInterfaceColImpl extends ISearchManager{
    private static final long serialVersionUID = 3149457864979857224L;
    
    @Override
    public Integer getAppEnumKey() {
        return ApplicationCategoryEnum.collaboration.getKey();
    }
    @Override
    public String getAppShowName() {
        return null;
    }
    @Override
    public int getSortId() {
        return this.getAppEnumKey();
    }
    @Override
    public List<ResultModel> iSearch(ConditionModel cModel) {
        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        ISearchInterfaceColDao iSearchInterfaceColDao = (ISearchInterfaceColDao) AppContext.getBean("iSearchInterfaceColDao");
        List<ResultModel> ret = new ArrayList<ResultModel>();
        // 1. 解析条件
        // 2. 分页查询
        List<CtpAffair> list = iSearchInterfaceColDao.transSearch(cModel);
        // 3. 组装数据，返回
        if(list != null){
            for(CtpAffair affair : list){
                Integer resentTime = affair.getResentTime();
                String forwardMember = affair.getForwardMember();
                String title = ColUtil.mergeSubjectWithForwardMembers(affair.getSubject(), 255, forwardMember, resentTime, null);
                V3xOrgMember member = null;
                try {
                    member = orgManager.getMemberById(affair.getSenderId());
                } catch (BusinessException e) {
                   throw new RuntimeException();
                }
                String fromUserName = member.getName();
                String locationPrefix = ResourceUtil.getString("collaboration.information.label");
                String locationSuffix = null;
                String link = "/collaboration/collaboration.do?method=summary&openFrom=glwd&affairId=" + affair.getId();
                if(affair.getState() == StateEnum.col_pending.key()){
                    locationSuffix = ResourceUtil.getString("collaboration.coltype.Pending.label");
                    link = "/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=" + affair.getId();
                }else if(affair.getState() == StateEnum.col_done.key()){
                    locationSuffix = ResourceUtil.getString("collaboration.coltype.Done.label");
                }else{
                    locationSuffix = ResourceUtil.getString("collaboration.coltype.Sent.label");
                }
                String location = locationPrefix + "-" + locationSuffix;
                String bodyType = affair.getBodyType();
                boolean hasAttachments = AffairUtil.isHasAttachments(affair);
                ResultModel rm = new ResultModel(title, fromUserName, affair.getCreateDate(), location, link,bodyType,hasAttachments);
                rm.setEntityId(affair.getId());
                rm.setObjectId(affair.getObjectId());
                ret.add(rm);
            }
        }
        return ret;
    }
    
}
