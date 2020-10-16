package com.seeyon.apps.ext.syncPost.po;

import com.seeyon.ctp.organization.po.OrgPost;

/**
 * 周刘成   2019-12-24
 */
public class MidPost {
    private Long id;
    private String postCode;
    private String postName;
    private String postDesc;
    private Long accountId;
    private Long operationType;
    private Long sortId;
    private Long oaType;
    private Long postId;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public MidPost() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getOperationType() {
        return operationType;
    }

    public void setOperationType(Long operationType) {
        this.operationType = operationType;
    }

    public Long getSortId() {
        return sortId;
    }

    public void setSortId(Long sortId) {
        this.sortId = sortId;
    }

    public Long getOaType() {
        return oaType;
    }

    public void setOaType(Long oaType) {
        this.oaType = oaType;
    }
}
