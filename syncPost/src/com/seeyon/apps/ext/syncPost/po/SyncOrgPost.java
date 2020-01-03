package com.seeyon.apps.ext.syncPost.po;

import java.math.BigDecimal;

/**
 * 周刘成   2019-12-23
 */
public class SyncOrgPost {

    private Long tId;
    private Long accountId;
    private Long sortId;
    private String postCode;
    private String postName;
    private String postDesc;
    private Long operationType;
    private Long oaType;
    private BigDecimal oaId;

    private Long postId;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public SyncOrgPost() {
    }

    public Long gettId() {
        return tId;
    }

    public void settId(Long tId) {
        this.tId = tId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getSortId() {
        return sortId;
    }

    public void setSortId(Long sortId) {
        this.sortId = sortId;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getPostDesc() {
        return postDesc;
    }

    public void setPostDesc(String postDesc) {
        this.postDesc = postDesc;
    }

    public Long getOperationType() {
        return operationType;
    }

    public void setOperationType(Long operationType) {
        this.operationType = operationType;
    }

    public Long getOaType() {
        return oaType;
    }

    public void setOaType(Long oaType) {
        this.oaType = oaType;
    }

    public BigDecimal getOaId() {
        return oaId;
    }

    public void setOaId(BigDecimal oaId) {
        this.oaId = oaId;
    }
}
