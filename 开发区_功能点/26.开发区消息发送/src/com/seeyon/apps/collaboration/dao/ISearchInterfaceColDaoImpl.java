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
package com.seeyon.apps.collaboration.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.isearch.model.ConditionModel;

/**
 * @author mujun
 *
 */
public class ISearchInterfaceColDaoImpl extends BaseHibernateDao<ColSummary> implements ISearchInterfaceColDao {
    @Override
    public List<CtpAffair> transSearch(ConditionModel cModel){
        User user = AppContext.getCurrentUser();
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder sb = new StringBuilder();
        
        String title = cModel.getTitle();
        final java.util.Date beginDate = cModel.getBeginDate();
        final java.util.Date endDate = cModel.getEndDate();
        Long fromUserId = cModel.getFromUserId();
        Long archiveId = cModel.getDocLibId(); ////归档ID
        List<Integer> stateList = new ArrayList<Integer>();
        sb.append("select affair from "+ CtpAffair.class.getName() +" as affair");
        if(cModel.getPigeonholedFlag() && archiveId != null){
            sb.append(",DocResourcePO as doc");
        }
        parameterMap.put("APP", ApplicationCategoryEnum.collaboration.key());
        
        sb.append(" where ");
        boolean hasSenderId = false;
        if(fromUserId != null && !fromUserId.equals(user.getId())){
            //指定认发给我的
            sb.append(" affair.memberId=:userId2 ");
            parameterMap.put("userId2", user.getId());
            stateList.add(StateEnum.col_pending.key());
            stateList.add(StateEnum.col_done.key());
            hasSenderId = true;
        }else if(fromUserId != null){
            //我发送到
            sb.append(" affair.memberId=:userId1 ");
            parameterMap.put("userId1", fromUserId);
            stateList.add(StateEnum.col_sent.key());
        }else{
            //别人发给我的
            sb.append(" affair.memberId=:userId3");
            parameterMap.put("userId3", user.getId());          
            stateList.add(StateEnum.col_pending.key());
            stateList.add(StateEnum.col_done.key());
        }
        sb.append(" and affair.state in(:stateList) and affair.app=:APP and affair.delete=false ");
        if(cModel.getPigeonholedFlag() && archiveId != null){
            sb.append(" and doc.docLibId =:archiveId and doc.id=affair.archiveId");
            parameterMap.put("archiveId", archiveId);
        }
        else{
            sb.append(" and affair.archiveId is null");                    
        }
        if(hasSenderId){
            sb.append(" and affair.senderId=:userId1 ");
            parameterMap.put("userId1", fromUserId);
        }
        
        parameterMap.put("stateList", stateList);
        
        if(Strings.isNotBlank(title)){
            sb.append(" and affair.subject like :subject ");
            parameterMap.put("subject", "%" + title + "%");
        }
        if(beginDate != null){
            sb.append(" and affair.createDate >= :begin");
            parameterMap.put("begin", beginDate);
        }
        if(endDate != null){
            sb.append(" and affair.createDate <= :end");
            parameterMap.put("end", endDate);
        }
        sb.append(" order by affair.createDate desc");
        final String hsql = sb.toString();
        
        List<CtpAffair> result = super.find(hsql, parameterMap);
        return result;
    }
}
