package com.seeyon.v3x.exchange.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocSendDetail extends BaseModel implements Serializable, Comparable<EdocSendDetail>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 128784823633158538L;

	/** 公文交换状态：待签收  **/
	public final static int Exchange_iStatus_SendDetail_Torecieve = 0;
	
	/** 公文交换状态：已签收  **/
	public final static int Exchange_iStatus_SendDetail_Recieved = 1;
	
	/** 公文交换状态（发文记录）：已回退  */
	public final static int Exchange_iStatus_SendDetail_StepBacked = 2;
	
	/** 公文交换状态（发文记录）：撤销  */
	public final static int Exchange_iStatus_SendDetail_Cancel = 3;
	
	/** 交换帐号类型：手工添加的外部单位（无法交换） */
	public final static int Exchange_SendDetail_iAccountType_Default = 0;
	
	/** 交换帐号类型：单位帐号(recOrgType值域) */
	public final static int Exchange_SendDetail_iAccountType_Org = 1;
	
	/** 交换帐号类型：部门帐号(recOrgType值域) */
	public final static int Exchange_SendDetail_iAccountType_Dept = 2;
	
	/** 交换帐号类型：人员帐号(recOrgType值域) */
	public final static int Exchange_SendDetail_iAccountType_Person = 3;
	
	private Long sendRecordId;
	private String recOrgId; // 外部单位ID\内部单位ID|内部部门ID
	private int recOrgType; // 外部单位\单位|部门("department","org","external")
	private String recOrgName; //外部单位\单位\部门名称("用友财务,金融,工程")
	private int sendType; // "send","copy","report"	
	private String content; // 签收回执内容
	private String recNo; // 签收编号	
	private String recUserName; // 签收人	
	private Timestamp recTime; // 签收时间
	private int status;	// 状态：待签收，已签收（已回执）
	private Integer cuibanNum; //催办次数
	
	public Integer getCuibanNum() {
        return cuibanNum;
    }

    public void setCuibanNum(Integer cuibanNum) {
        if(cuibanNum == null){
            this.cuibanNum = 0;
        }else {
            this.cuibanNum = cuibanNum;
        }
    }

    public Long getSendRecordId() {
		return sendRecordId;
	}
	
	public void setSendRecordId(Long sendRecordId) {
		this.sendRecordId = sendRecordId;
	}
	
	public String getRecOrgId() {
		return recOrgId;
	}
	
	public void setRecOrgId(String recOrgId) {
		this.recOrgId = recOrgId;
	}
	
	public int getRecOrgType() {
		return recOrgType;
	}
	
	public void setRecOrgType(int recOrgType) {
		this.recOrgType = recOrgType;
	}
	
	public int getSendType() {
		return sendType;
	}
	
	public void setSendType(int sendType) {
		this.sendType = sendType;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}	
	
	public String getRecNo() {
		return recNo;
	}
	
	public void setRecNo(String recNo) {
		this.recNo = recNo;
	}
		
	public String getRecUserName() {
		return recUserName;
	}
	
	public void setRecUserName(String recUserName) {
		this.recUserName = recUserName;
	}
	
	public Timestamp getRecTime() {
		return recTime;
	}
	
	public void setRecTime(Timestamp recTime) {
		this.recTime = recTime;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public String getRecOrgName() {
		return recOrgName;
	}

	public void setRecOrgName(String recOrgName) {
		this.recOrgName = recOrgName;
	}

	public int compareTo(EdocSendDetail o) {
		if(this.status == Exchange_iStatus_SendDetail_Recieved){
			if(o.status == Exchange_iStatus_SendDetail_Recieved){
				return this.recTime.compareTo(o.recTime);
			}else{
				return -1;
			}
				
		}else{
			if(o.status == Exchange_iStatus_SendDetail_Recieved){
				return 1;
			}else{
				if(this.id <= o.id)
					return -1;
				else
					return 1;
			}
		}
	}	
	
}
