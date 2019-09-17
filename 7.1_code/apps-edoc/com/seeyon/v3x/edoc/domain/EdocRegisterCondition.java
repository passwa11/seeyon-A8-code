package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocRegisterCondition extends BaseModel implements Serializable {

	private static final long serialVersionUID = 3080619977553732426L;
	
	private String title;
    private String starttime;
    private String endtime;
    private String queryCol;    
    private Timestamp createTime;
    private long accountId;
    private long userId;
    private int type;   //发文或收文登记簿
    
    private String contentExt1;//小查询查询的条件 修改成查询条件
    private String contentExt2;//小查询值1 部门权限
    private String contentExt3;//小查询值2 废弃掉
	//发文登记簿——查询时间选择，原来的时间为交换日期，新增签发日期、拟文日期
    private Integer sendQueryTimeType;
    
    public Integer getSendQueryTimeType() {
		return sendQueryTimeType;
	}
	public void setSendQueryTimeType(Integer sendQueryTimeType) {
		this.sendQueryTimeType = sendQueryTimeType;
	}
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
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
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
    public String getQueryCol() {
        return queryCol;
    }
    public void setQueryCol(String queryCol) {
        this.queryCol = queryCol;
    }
    public Timestamp getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    public long getAccountId() {
        return accountId;
    }
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    
    
}
