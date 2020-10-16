/**
 * Author : xuqw
 *   Date : 2017年3月7日 下午1:21:34
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.rest.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 * <p>@Since A8-V5 6.1</p>
 */
public class EdocSummaryListVO {

    private final EdocSummaryModel model;
    
    public EdocSummaryListVO(EdocSummaryModel model) {
        if(model == null){
            throw new NullPointerException("EdocSummaryModel" + ResourceUtil.getString("edoc.error.vo.not.empty"));// 为空， 不能初始化VO对象
        }
        this.model = model;
    }
    
    /**
     * 将EdocSummaryModel列表转换成SummaryListVO列表
     * 
     * @param models
     * @return
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年3月7日下午2:12:25
     *
     */
    public static List<EdocSummaryListVO> valueOf(List<EdocSummaryModel> models){
        
        if(models == null || models.isEmpty()){
            return Collections.emptyList();
        }
        
        List<EdocSummaryListVO> results = new ArrayList<EdocSummaryListVO>(models.size());
        for(EdocSummaryModel m : models){
            results.add(new EdocSummaryListVO(m));
        }
        return results;
    }
    
    public Map<String, Object> getSummary(){
        Map<String, Object> summary = new HashMap<String, Object>();
        summary.put("summaryId", model.getSummary().getId());
        summary.put("urgentLevel", model.getSummary().getUrgentLevel());
        summary.put("hasAttachments", model.getSummary().isHasAttachments());
        summary.put("startUserId", model.getSummary().getStartUserId());
        summary.put("subject", model.getSummary().getSubject());
        summary.put("docMark", model.getSummary().getDocMark());
        summary.put("state", model.getSummary().getState());
        return summary;
    }
    
    public Map<String, Object> getAffair(){
        Map<String, Object> affair = new HashMap<String, Object>();
        
        affair.put("subState", model.getAffair().getSubState());
        affair.put("state", model.getAffair().getState());
        affair.put("app", model.getAffair().getApp());
        affair.put("subApp", model.getAffair().getSubApp());
        return affair;
    }
    
    public boolean isFinshed(){
        return model.isFinshed();
    }
    
    
    public Long getAffairId(){
        return model.getAffairId();
    }
    public String getSecretLevelName(){
        return model.getSecretLevelName();
    }
    
    public String getCreatePerson(){
        return model.getCreatePerson();
    }
    public String getCreateDate(){
        return model.getCreateDate();
    }
    public String getEdocType(){
        return model.getEdocType();
    }
    public String getBackFromName(){
        return model.getBackFromName();
    }
    
    public int[] getSurplusTime(){
        return model.getSurplusTime();
    }
    public String getFromName(){
        return model.getFromName();
    }
    public Integer getAffairState(){
        return model.getAffair().getState();
    }
  
}
