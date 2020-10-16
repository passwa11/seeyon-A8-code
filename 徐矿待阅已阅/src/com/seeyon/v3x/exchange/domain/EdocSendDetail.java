package com.seeyon.v3x.exchange.domain;

import com.seeyon.v3x.common.domain.BaseModel;
import java.io.Serializable;
import java.sql.Timestamp;

public class EdocSendDetail extends BaseModel implements Serializable, Comparable<EdocSendDetail>{
    private static final long serialVersionUID = 128784823633158538L;
    public static final int Exchange_iStatus_SendDetail_Torecieve = 0;
    public static final int Exchange_iStatus_SendDetail_Recieved = 1;
    public static final int Exchange_iStatus_SendDetail_StepBacked = 2;
    public static final int Exchange_iStatus_SendDetail_Cancel = 3;
    public static final int Exchange_SendDetail_iAccountType_Default = 0;
    public static final int Exchange_SendDetail_iAccountType_Org = 1;
    public static final int Exchange_SendDetail_iAccountType_Dept = 2;
    public static final int Exchange_SendDetail_iAccountType_Person = 3;
    private Long sendRecordId;
    private String recOrgId;
    private int recOrgType;
    private String recOrgName;
    private int sendType;
    private String content;
    private String recNo;
    private String recUserName;
    private Timestamp recTime;
    private int status;
    private Integer cuibanNum;
    private Long leaderId;
    private Long daiyueId;

    public EdocSendDetail() {
    }

    public Long getLeaderId() {
        return this.leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public Long getDaiyueId() {
        return this.daiyueId;
    }

    public void setDaiyueId(Long daiyueId) {
        this.daiyueId = daiyueId;
    }

    public Integer getCuibanNum() {
        return this.cuibanNum;
    }

    public void setCuibanNum(Integer cuibanNum) {
        if (cuibanNum == null) {
            this.cuibanNum = 0;
        } else {
            this.cuibanNum = cuibanNum;
        }

    }

    public Long getSendRecordId() {
        return this.sendRecordId;
    }

    public void setSendRecordId(Long sendRecordId) {
        this.sendRecordId = sendRecordId;
    }

    public String getRecOrgId() {
        return this.recOrgId;
    }

    public void setRecOrgId(String recOrgId) {
        this.recOrgId = recOrgId;
    }

    public int getRecOrgType() {
        return this.recOrgType;
    }

    public void setRecOrgType(int recOrgType) {
        this.recOrgType = recOrgType;
    }

    public int getSendType() {
        return this.sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRecNo() {
        return this.recNo;
    }

    public void setRecNo(String recNo) {
        this.recNo = recNo;
    }

    public String getRecUserName() {
        return this.recUserName;
    }

    public void setRecUserName(String recUserName) {
        this.recUserName = recUserName;
    }

    public Timestamp getRecTime() {
        return this.recTime;
    }

    public void setRecTime(Timestamp recTime) {
        this.recTime = recTime;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRecOrgName() {
        return this.recOrgName;
    }

    public void setRecOrgName(String recOrgName) {
        this.recOrgName = recOrgName;
    }

    @Override
    public int compareTo(EdocSendDetail o) {
        if (this.status == 1) {
            return o.status == 1 ? this.recTime.compareTo(o.recTime) : -1;
        } else if (o.status == 1) {
            return 1;
        } else {
            return this.id <= o.id ? -1 : 1;
        }
    }
}
