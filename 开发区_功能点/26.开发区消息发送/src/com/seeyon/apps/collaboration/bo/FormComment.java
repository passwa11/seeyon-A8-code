/**
 * $Author$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.apps.collaboration.bo;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.ctp.common.po.BasePO;

/**
 * yangwulin 2012-12-24 Sprint5 处理回复意见
 * @author wulin
 *
 */
public class FormComment extends BasePO {
    
    private ColHandleType  colHandleType;
    
    /**
     * affairId
     */
    private Long affairId;
    
    /**
     * 处理意见
     */
    private String comment;
    
    /**
     * 落款
     */
    private String inscribed;

    /**
     * 
     */
    public FormComment() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param affairId
     * @param comment
     * @param inscribed
     */
    public FormComment(Long affairId, String comment, String inscribed) {
        super();
        this.affairId = affairId;
        this.comment = comment;
        this.inscribed = inscribed;
    }

    /**
     * @return the affairId
     */
    public Long getAffairId() {
        return affairId;
    }

    /**
     * @param affairId the affairId to set
     */
    public void setAffairId(Long affairId) {
        this.affairId = affairId;
    }

    /**
     * 处理意见
     */
    public String getComment() {
        return comment;
    }

    /**
     * 处理意见
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 落款
     */
    public String getInscribed() {
        return inscribed;
    }

    /**
     * 落款
     */
    public void setInscribed(String inscribed) {
        this.inscribed = inscribed;
    }

    /**
     * @return the colHandleType
     */
    public ColHandleType getColHandleType() {
        return colHandleType;
    }

    /**
     * @param colHandleType the colHandleType to set
     */
    public void setColHandleType(ColHandleType colHandleType) {
        this.colHandleType = colHandleType;
    }
}
