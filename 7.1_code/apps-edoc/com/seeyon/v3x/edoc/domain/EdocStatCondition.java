package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocStatCondition  extends BaseModel implements Serializable{

    private String title;
    private int statisticsDimension;//同现在的displayType 升级前1时间 2人员  升级后1部门 2人员 3时间 (升级时将1->3)
    private String organizationId;
    private int timeType;//同现在的displayTimeType
    private String starttime;//同现在的startRangeTime
    private String endtime;//同现在的endRangeTime
    private String sendContentId;//过滤条件：发文种类，已去掉
    private String workflowNodeId;//过滤条件：流程节点，已去掉
    private String processSituationId;//过滤条件：办理条件，已去掉
    private Timestamp createTime;//推送时间
    private String sendNodeCode;//已去掉
    private String recNodeCode;//已去掉
    private long accountId;//推送单位
    private long userId;//推送人
    
    private Boolean isOld =  Boolean.FALSE;
    private Integer edocType;
    private String sendType;
    private String unitLevel;
    private String operationType;
    private String operationTypeIds;
    private Integer pushRole;
    private Long pushFrom;
    
    private String contentExt1;
    private String contentExt2;
    private String contentExt3;
    
    public String getContentExt1() {
        return contentExt1;
    }
    public void setContentExt1(String contentExt1) {
        this.contentExt1 = contentExt1;
    }
    public String getContentExt2() {
        return contentExt2;
    }
    public void setContentExt2(String contentExt2) {
        this.contentExt2 = contentExt2;
    }
    public String getContentExt3() {
        return contentExt3;
    }
    public void setContentExt3(String contentExt3) {
        this.contentExt3 = contentExt3;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public long getAccountId() {
        return accountId;
    }
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
    public String getSendNodeCode() {
        return sendNodeCode;
    }
    public void setSendNodeCode(String sendNodeCode) {
        this.sendNodeCode = sendNodeCode;
    }
    public String getRecNodeCode() {
        return recNodeCode;
    }
    public void setRecNodeCode(String recNodeCode) {
        this.recNodeCode = recNodeCode;
    }
    public Timestamp getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    public String getStarttime() {
        return starttime;
    }
    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }
    public String getEndtime() {
        return endtime;
    }
    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getStatisticsDimension() {
        return statisticsDimension;
    }
    public void setStatisticsDimension(int statisticsDimension) {
        this.statisticsDimension = statisticsDimension;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    public int getTimeType() {
        return timeType;
    }
    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }
    public String getSendContentId() {
        return sendContentId;
    }
    public void setSendContentId(String sendContentId) {
        this.sendContentId = sendContentId;
    }
    public String getWorkflowNodeId() {
        return workflowNodeId;
    }
    public void setWorkflowNodeId(String workflowNodeId) {
        this.workflowNodeId = workflowNodeId;
    }
    public String getProcessSituationId() {
        return processSituationId;
    }
    public void setProcessSituationId(String processSituationId) {
        this.processSituationId = processSituationId;
    }
    public Boolean getIsOld() {
        return isOld;
    }
    public void setIsOld(Boolean isOld) {
        this.isOld = isOld;
    }
    public Integer getEdocType() {
        return edocType;
    }
    public void setEdocType(Integer edocType) {
        this.edocType = edocType;
    }
    public String getSendType() {
        return sendType;
    }
    public void setSendType(String sendType) {
        this.sendType = sendType;
    }
    public String getUnitLevel() {
        return unitLevel;
    }
    public void setUnitLevel(String unitLevel) {
        this.unitLevel = unitLevel;
    }
    public String getOperationType() {
        return operationType;
    }
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    public String getOperationTypeIds() {
        return operationTypeIds;
    }
    public void setOperationTypeIds(String operationTypeIds) {
        this.operationTypeIds = operationTypeIds;
    }
    public Integer getPushRole() {
        return pushRole;
    }
    public void setPushRole(Integer pushRole) {
        this.pushRole = pushRole;
    }
    public Long getPushFrom() {
        return pushFrom;
    }
    public void setPushFrom(Long pushFrom) {
        this.pushFrom = pushFrom;
    }
    
}
