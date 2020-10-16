package com.seeyon.v3x.exchange.domain;

import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EdocSendRecord extends BaseModel implements Serializable{
    private static final long serialVersionUID = 618559615378721041L;
    public static final int Exchange_iStatus_Tosend = 0;
    public static final int Exchange_iStatus_Sent = 1;
    public static final int Exchange_iStatus_Send_StepBacked = 2;
    public static final int Exchange_iStatus_Send_New_StepBacked = 3;
    public static final int Exchange_iStatus_Send_New_Cancel = 4;
    public static final int Exchange_iStatus_Send_Delete = 5;
    public static final int Exchange_iStatus_ToSend_Delete = 6;
    public static final int Exchange_Send_iExchangeType_Dept = 0;
    public static final int Exchange_Send_iExchangeType_Org = 1;
    public static final int Exchange_Send_iExchangeType_ExternalOrg = 2;
    public static final int Exchange_Assign_To_All = 0;
    public static final int Exchange_Assign_To_Member = 1;
    public static final int Exchange_Base_NO = 0;
    public static final int Exchange_Base_YES = 1;
    private String subject;
    private String docType;
    private String docMark;
    private String secretLevel;
    private String urgentLevel;
    private String sendUnit;
    private String issuer;
    private Date issueDate;
    private Integer copies;
    private long edocId;
    private long sendUserId;
    private Integer assignType;
    private Integer isBase;
    private Timestamp sendTime;
    private long exchangeOrgId;
    private Long exchangeAccountId = null;
    private int exchangeType;
    private Integer exchangeMode = 0;
    private Timestamp createTime;
    private int status;
    private Set<EdocSendDetail> sendDetails;
    private String sendedTypeIds;
    private String stepBackInfo;
    private Integer isTurnRec;
    private String sendedNames;
    private List<EdocSendDetail> sendDetailList;
    private Integer contentNo;
    private String sendNames;
    private String sendUserNames;
    private String keywords;
    private String exchangeOrgName;
    private int xkjtSign;
    private int xkjtPresign;

    public EdocSendRecord() {
    }

    public String getSendedNames() {
        return this.sendedNames;
    }

    public void setSendedNames(String sendedNames) {
        this.sendedNames = sendedNames;
    }

    public Integer getIsTurnRec() {
        return this.isTurnRec;
    }

    public void setIsTurnRec(Integer isTurnRec) {
        this.isTurnRec = isTurnRec;
    }

    public String getStepBackInfo() {
        return this.stepBackInfo;
    }

    public void setStepBackInfo(String stepBackInfo) {
        this.stepBackInfo = stepBackInfo;
    }

    public Integer getIsBase() {
        return this.isBase;
    }

    public void setIsBase(Integer isBase) {
        this.isBase = isBase;
    }

    public Integer getContentNo() {
        return this.contentNo;
    }

    public void setContentNo(Integer contentNo) {
        this.contentNo = contentNo;
    }

    public String getSendEntityNames() {
        if (Strings.isNotBlank(this.sendedNames)) {
            return this.sendedNames;
        } else if (this.sendedTypeIds != null && !"".equals(this.sendedTypeIds)) {
            return Functions.showOrgEntities(this.sendedTypeIds, "、");
        } else {
            StringBuilder str = new StringBuilder("");

            EdocSendDetail ed;
            for(Iterator var3 = this.sendDetails.iterator(); var3.hasNext(); str.append(ed.getRecOrgName())) {
                ed = (EdocSendDetail)var3.next();
                if (str.length() > 0) {
                    str.append(",");
                }
            }

            return str.toString();
        }
    }

    public String getExchangeOrgName() {
        return this.exchangeOrgName;
    }

    public void setExchangeOrgName(String exchangeOrgName) {
        this.exchangeOrgName = exchangeOrgName;
    }

    public String getSendUserNames() {
        return this.sendUserNames;
    }

    public void setSendUserNames(String sendUserNames) {
        this.sendUserNames = sendUserNames;
    }

    public String getSendNames() {
        return this.sendNames;
    }

    public void setSendNames(String sendNames) {
        this.sendNames = sendNames;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDocType() {
        return this.docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocMark() {
        return this.docMark;
    }

    public void setDocMark(String docMark) {
        this.docMark = docMark;
    }

    public String getSecretLevel() {
        return this.secretLevel;
    }

    public void setSecretLevel(String secretLevel) {
        this.secretLevel = secretLevel;
    }

    public String getUrgentLevel() {
        return this.urgentLevel;
    }

    public void setUrgentLevel(String urgentLevel) {
        this.urgentLevel = urgentLevel;
    }

    public String getSendUnit() {
        return this.sendUnit;
    }

    public void setSendUnit(String sendUnit) {
        this.sendUnit = sendUnit;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Date getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Integer getCopies() {
        return this.copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public long getEdocId() {
        return this.edocId;
    }

    public void setEdocId(long edocId) {
        this.edocId = edocId;
    }

    public long getSendUserId() {
        return this.sendUserId;
    }

    public void setSendUserId(long sendUserId) {
        this.sendUserId = sendUserId;
    }

    public Timestamp getSendTime() {
        return this.sendTime;
    }

    public void setSendTime(Timestamp sendTime) {
        this.sendTime = sendTime;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getExchangeOrgId() {
        return this.exchangeOrgId;
    }

    public void setExchangeOrgId(long exchangeOrgId) {
        this.exchangeOrgId = exchangeOrgId;
    }

    public int getExchangeType() {
        return this.exchangeType;
    }

    public void setExchangeType(int exchangeType) {
        this.exchangeType = exchangeType;
    }

    public Set<EdocSendDetail> getSendDetails() {
        return this.sendDetails;
    }

    public void setSendDetails(Set<EdocSendDetail> sendDetails) {
        this.sendDetails = sendDetails;
    }

    public List<EdocSendDetail> getSendDetailList() {
        return this.sendDetailList;
    }

    public void setSendDetailList(List<EdocSendDetail> sendDetailList) {
        this.sendDetailList = sendDetailList;
    }

    public String getSendedTypeIds() {
        return this.sendedTypeIds;
    }

    public void setSendedTypeIds(String sendedTypeIds) {
        this.sendedTypeIds = sendedTypeIds;
    }

    public String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Integer getAssignType() {
        return this.assignType;
    }

    public void setAssignType(Integer assignType) {
        this.assignType = assignType;
    }

    public Integer getExchangeMode() {
        return this.exchangeMode;
    }

    public void setExchangeMode(Integer exchangeMode) {
        this.exchangeMode = exchangeMode;
    }

    public Long getExchangeAccountId() {
        return this.exchangeAccountId;
    }

    public void setExchangeAccountId(Long exchangeAccountId) {
        this.exchangeAccountId = exchangeAccountId;
    }

    public int getXkjtSign() {
        return this.xkjtSign;
    }

    public void setXkjtSign(int xkjtSign) {
        this.xkjtSign = xkjtSign;
    }

    public int getXkjtPresign() {
        return this.xkjtPresign;
    }

    public void setXkjtPresign(int xkjtPresign) {
        this.xkjtPresign = xkjtPresign;
    }
}
