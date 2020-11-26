package com.seeyon.ctp.rest.resources.vo;

import com.seeyon.apps.xiaoz.bo.card.XiaozGotoParams;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;

/**
 * 
 * <pre>
 *   描述 应用穿透参数，协同继承此对象，对需要穿透的参数进行扩展
 *   
 * @ClassName:  CollGotoParamsVO      
 * @date:   2019年5月29日    
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved.
 * </pre>
 */
public class CollGotoParamsVO extends XiaozGotoParams {

    private static final long serialVersionUID = 8754439084912892313L;
    
    /**
     *      协同ID
     */
    private String summaryId;     
    
    /**
     *      来源（参考ColOpenFrom.java,例如来自文档中心此处传递"docLib"）
     */
    private String openFrom;
    
    /**
     *      表单操作权限
     */
    private String operationId;
    

    public String getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(String summaryId) {
        this.summaryId = summaryId;
    }

    public String getOpenFrom() {
        return openFrom;
    }

    public void setOpenFrom(String openFrom) {
        this.openFrom = openFrom;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    public CollGotoParamsVO() {
		setAppId(String.valueOf(ApplicationCategoryEnum.collaboration.key()));
	}
}
