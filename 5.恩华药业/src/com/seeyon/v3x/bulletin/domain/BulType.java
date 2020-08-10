package com.seeyon.v3x.bulletin.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seeyon.ctp.common.po.BasePO;
import com.seeyon.ctp.common.taglibs.functions.Functions;

public class BulType extends BasePO implements Serializable, Comparable<BulType> {

    private static final long    serialVersionUID = -9041181029457784269L;
    private String               typeName;
    private Integer              usedFlag;
    private String               description;
    private Byte                 topCount;
    private boolean              auditFlag;
    private Long                 auditUser;
    private Date                 createDate;
    private Long                 createUser;
    private Date                 updateDate;
    private Long                 updateUser;
    private Long                 accountId;
    private Integer              spaceType;
    private String               ext1;
    private String               ext2;
    private Integer              sortNum          = 999;
    private Boolean              isAuditorModify;                       //是否允许审核员修改
    private Boolean              printFlag        = true;
    private Boolean              printDefault     = false;
    private String               managerUserIds;
	private String               managerUserNames;
    private int                  totalItems;
	private Boolean 			 defaultPublish   = false;				//默认显示发布人勾选
    private Integer 		     finalPublish	  = 0;				    //最终显示发布人控制 0、发布人，1、发起人，2、审核人
    private Boolean 			 writePermit     = false;				//是否允许手动输入发布人
//zhou start
	private String sendArrangeId;
	private String sendArrangeName;

	public String getSendArrangeId() {
		return sendArrangeId;
	}

	public void setSendArrangeId(String sendArrangeId) {
		this.sendArrangeId = sendArrangeId;
	}

	public String getSendArrangeName() {
		return sendArrangeName;
	}

	public void setSendArrangeName(String sendArrangeName) {
		this.sendArrangeName = sendArrangeName;
	}
	//	zhou end

	public Boolean getWritePermit() {
        return writePermit;
    }

    public void setWritePermit(Boolean writePermit) {
        this.writePermit = writePermit;
    }

    public Integer getFinalPublish() {
		return finalPublish;
	}

	public void setFinalPublish(Integer finalPublish) {
		this.finalPublish = finalPublish;
	}

	public Boolean getDefaultPublish() {
		return defaultPublish;
	}

	public void setDefaultPublish(Boolean defaultPublish) {
		this.defaultPublish = defaultPublish;
	}

	//忽略json序列化
	@JsonIgnore
	private Set<BulTypeManagers> bulTypeManagers;

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getUsedFlag() {
		return usedFlag;
	}

	public void setUsedFlag(Integer usedFlag) {
		this.usedFlag = usedFlag;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Byte getTopCount() {
		return topCount;
	}

	public void setTopCount(Byte topCount) {
		this.topCount = topCount;
	}

	public boolean isAuditFlag() {
		return auditFlag;
	}

	public void setAuditFlag(boolean auditFlag) {
		this.auditFlag = auditFlag;
	}

	public Long getAuditUser() {
		return auditUser;
	}

	public void setAuditUser(Long auditUser) {
		this.auditUser = auditUser;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getCreateUser() {
		return createUser;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Long getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Integer getSpaceType() {
		return spaceType;
	}

	public void setSpaceType(Integer spaceType) {
		this.spaceType = spaceType;
	}

	public String getExt1() {
		return ext1;
	}

	public void setExt1(String ext1) {
		this.ext1 = ext1;
	}

	public String getExt2() {
		return ext2;
	}

	public void setExt2(String ext2) {
		this.ext2 = ext2;
	}

	public Integer getSortNum() {
		return sortNum;
	}

	public void setSortNum(Integer sortNum) {
		this.sortNum = sortNum;
	}

	public Boolean getIsAuditorModify() {
		if (isAuditorModify == null) {
			return false;
		}
		return isAuditorModify;
	}

	public void setIsAuditorModify(Boolean isAuditorModify) {
		this.isAuditorModify = isAuditorModify;
	}

	public Boolean getPrintFlag() {
		return printFlag;
	}

	public void setPrintFlag(Boolean printFlag) {
		this.printFlag = printFlag;
	}

	public Boolean getPrintDefault() {
		return printDefault;
	}

	public void setPrintDefault(Boolean printDefault) {
		this.printDefault = printDefault;
	}

	public String getManagerUserIds() {
		return managerUserIds;
	}

	public void setManagerUserIds(String managerUserIds) {
		this.managerUserIds = managerUserIds;
	}

	public String getManagerUserNames() {
		return managerUserNames;
	}

	public void setManagerUserNames(String managerUserNames) {
		this.managerUserNames = managerUserNames;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public Set<BulTypeManagers> getBulTypeManagers() {
		return bulTypeManagers;
	}

	public void setBulTypeManagers(Set<BulTypeManagers> bulTypeManagers) {
		this.bulTypeManagers = bulTypeManagers;
	}

	public String getAuditUserName() {
		String auditUserName = "";
		if (this.isAuditFlag()) {
			auditUserName = Functions.showMemberNameOnly(this.getAuditUser());
			if (auditUserName == null) {
				auditUserName = "";
			}
		}
		return auditUserName;
	}

	public int compareTo(BulType o) {
		if (this.getSortNum().intValue() < o.getSortNum().intValue()) {
			return -1;
		} else if (this.getSortNum().intValue() > o.getSortNum().intValue()) {
			return 1;
		} else {
			return (this.getCreateDate().compareTo(o.getCreateDate()));
		}
	}

}
