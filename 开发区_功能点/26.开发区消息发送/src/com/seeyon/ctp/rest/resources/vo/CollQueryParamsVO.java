package com.seeyon.ctp.rest.resources.vo;

import java.util.Map;

import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.xiaoz.bo.card.XiaozQueryParams;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.util.ParamUtil;

/**
 * <pre>
 *  协同应用查询
 *   
 * @ClassName:  CollQueryParamsVO      
 * @date:   2019年5月31日    
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved.
 * </pre>  
 */
public class CollQueryParamsVO extends XiaozQueryParams {

    private static final long serialVersionUID = 8479727349403843251L;

    /**
     * 协同发起人姓名
     */
    private String startMemberName;
    
    /**
     * 协同发起人ID
     */
    private String startMemberId;
    
    /**
     * 协同标题
     */
    private String subject;
    
    
    /**   
     * 创建时间   
     */  
    private String createDate;
    
    /**   
     * 接收时间  
     */  
    private String receiveDate;
    
    /**   
     * 完成时间  
     */  
    private String completeDate;
    
    /**   
     * 从哪个页面打开的,期望跳入哪个列表
     */  
    private String openFrom;
  
    /**   
     * 协同状态，参考 StateEnum   
     */  
    private String status;
    
    public String getStartMemberName() {
        return startMemberName;
    }
    
    public void setStartMemberName(String startMemberName) {
        this.startMemberName = startMemberName;
    }

    public String getStartMemberId() {
        return startMemberId;
    }

    public void setStartMemberId(String startMemberId) {
        this.startMemberId = startMemberId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(String completeDate) {
        this.completeDate = completeDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getOpenFrom() {
        return openFrom;
    }

    public void setOpenFrom(String openFrom) {
        this.openFrom = openFrom;
    }

    /** 
     * <pre>
     * 将Map中的数据转换成实体类
     *     
     * @param queryParams
     * @return      
     * @return: CollQueryParamsVO  
     * @date:   2019年5月31日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    public static CollQueryParamsVO convert(Map<String,String> queryParams) {
        CollQueryParamsVO param = new CollQueryParamsVO();
        
        param.setStartMemberId(ParamUtil.getString(queryParams, ColQueryCondition.startMemberId.name(),""));
        param.setStartMemberName(ParamUtil.getString(queryParams, ColQueryCondition.startMemberName.name(),""));
        param.setSubject(ParamUtil.getString(queryParams, ColQueryCondition.subject.name(),""));
        param.setCreateDate(ParamUtil.getString(queryParams, ColQueryCondition.createDate.name(),""));
        param.setReceiveDate(ParamUtil.getString(queryParams, ColQueryCondition.receiveDate.name(),""));
        param.setCompleteDate(ParamUtil.getString(queryParams, ColQueryCondition.completeDate.name(),""));
        
        return param;
    }
    public CollQueryParamsVO() {
        setAppId(String.valueOf(ApplicationCategoryEnum.collaboration.key()));
    }
}
