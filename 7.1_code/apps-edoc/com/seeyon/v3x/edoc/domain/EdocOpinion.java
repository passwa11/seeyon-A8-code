package com.seeyon.v3x.edoc.domain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.v3x.edoc.util.Constants;

/**
 * The persistent class for the edoc_opinion database table.
 * 
 * @author BEA Workshop Studio
 */
public class EdocOpinion extends BaseModel  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5604923935730253397L;
	//default serial version id, required for serializable classes.
	public static final String FEED_BACK = "feedback";
	public static final String REPORT = "report";
	
	private long affairId;
	private int attribute = Constants.EDOC_ATTITUDE_NULL;
	private String content;
	private java.sql.Timestamp createTime;
	private String strCreateTime;
	private long createUserId;
	private Boolean isHidden = false;
	private EdocSummary edocSummary;
	private Integer opinionType;
	private String policy;
	private String proxyName;
	private long nodeId;
	private int state;//lijl添加用来标记是否是删除的意见,0未删除,1已删除
	//当前意见是否上传了附件
	private boolean hasAtt;
	private Long subEdocId;//下级单位公文id
	private boolean isReportToSupAccount;//转收文使用，是否为下级单位公文向上级汇报意见
	private java.sql.Timestamp updateTime;//转收文使用，更新时间
	private Long subOpinionId; //上级单位显示的下级意见 关联的原始意见id
	private String departmentName; //处理人所在部门
	private String accountName; //处理人所在单位
	private Long departmentSortId; //处理人所在部门排序号
	private String needRepealRecord = "0";
	
	public String getNeedRepealRecord() {
		return needRepealRecord;
	}

	public void setNeedRepealRecord(String needRepealRecord) {
		this.needRepealRecord = needRepealRecord;
	}

	public Long getSubOpinionId() {
		return subOpinionId;
	}

	public void setSubOpinionId(Long subOpinionId) {
		this.subOpinionId = subOpinionId;
	}

	public java.sql.Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(java.sql.Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Long getSubEdocId() {
		return subEdocId;
	}

	public void setSubEdocId(Long subEdocId) {
		this.subEdocId = subEdocId;
	}

	public boolean getIsReportToSupAccount() {
		return isReportToSupAccount;
	}

	public void setIsReportToSupAccount(boolean isReportToSupAccount) {
		this.isReportToSupAccount = isReportToSupAccount;
	}
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	// 是否处理后跟踪
	// 不存储在EdocOpinion表中，只用于参数传递
	public boolean affairIsTrack = true;
	// 是否处理后立即删除
	// 不存储在数据库中，只用于参数传递
	public boolean isDeleteImmediate;
	
	//	是否处理后归档
	// 不存储在ColOpinion表中，只用于参数传递
	public boolean isPipeonhole = true;
	
	//交换类型
	public int exchangeType=-1;
	public long exchangeUnitId=-1;
	//意见对应附件
	List<Attachment> opinionAttachments=null;
	
	//Web传值
	private String deptName;
	private String unitName;//魏俊标添加
	private boolean isBound = true;//是否绑定到文单，只用于标记


	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	/**
	 * 意见类型，有新的类型往后追加，切勿改变顺序 枚举项顺序将被记录到数据库
	 */
	public enum OpinionType {
		senderOpinion, // 发起人意见
		signOpinion, // 处理意见
		provisionalOpinoin, // 暂存待办意见
		backOpinion,//回退意见
		draftOpinion, // 草稿意见
		stopOpinion, // 终止意见
		repealOpinion,//撤销意见
		sysAutoSignOpinion, //节点自动跳过意见
		reportOpinion,//上报意见
		transferOpinion//移交意见
	}
	
	
	public void setOpinionAttachments(List<Attachment> opinionAttachments)
	{
		this.opinionAttachments=opinionAttachments;
	}
	
	public List<Attachment> getOpinionAttachments()
	{
		return this.opinionAttachments;
	}
	public void setPolicy(String policy)
	{
		this.policy=policy;
	}
	public String getPolicy()
	{
		return this.policy;
	}
	
	public Integer getOpinionType() {
		return this.opinionType;
	}

	public void setOpinionType(Integer opinionType) {
		this.opinionType = opinionType;
	}

    public EdocOpinion() {
    }

	public long getAffairId() {
		return this.affairId;
	}
	public void setAffairId(long affairId) {
		this.affairId = affairId;
	}

	public int getAttribute() {
		return this.attribute;
	}
	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public String getContent() {
		return this.content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public java.sql.Timestamp getCreateTime() {
		return this.createTime;
	}
	public void setCreateTime(java.sql.Timestamp createTime) {
		this.createTime = createTime;
	}

	public long getCreateUserId() {
		return this.createUserId;
	}
	public void setCreateUserId(long createUserId) {
		this.createUserId = createUserId;
	}

	public Boolean getIsHidden() {
		return this.isHidden;
	}
	public void setIsHidden(Boolean isHidden) {
		this.isHidden = isHidden;
	}
	
	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	//bi-directional many-to-one association to EdocSummary
	public EdocSummary getEdocSummary() {
		return this.edocSummary;
	}
	public void setEdocSummary(EdocSummary edocSummary) {
		this.edocSummary = edocSummary;
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("id", getId())
			.toString();
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}
	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public boolean isHasAtt() {
		return hasAtt;
	}

	public void setHasAtt(boolean hasAtt) {
		this.hasAtt = hasAtt;
	}
	
	public EdocOpinion cloneEdocOpinion(){
		EdocOpinion supOp = new EdocOpinion();
		supOp.setIdIfNew();
		supOp.setAffairId(this.getAffairId());
		supOp.setCreateTime(this.getCreateTime());
		supOp.setCreateUserId(this.getCreateUserId());
		supOp.setDeptName(this.getDeptName());
		supOp.setUnitName(this.getUnitName());
		supOp.setContent(this.getContent());
		supOp.setHasAtt(this.isHasAtt());
		supOp.setAccountName(this.getAccountName());
		supOp.setDepartmentName(this.getDepartmentName());
		supOp.setDepartmentSortId(this.getDepartmentSortId());
		List<Attachment> att2 = new ArrayList<Attachment>();
		List<Attachment> att = this.getOpinionAttachments();
		if(att!=null){
			att2.addAll(att);
		}
		supOp.setOpinionAttachments(att2);
		supOp.setState(this.getState());
		supOp.setProxyName(this.getProxyName());
		return supOp;
	}

    public boolean isBound() {
        return isBound;
    }

    public void setBound(boolean isBound) {
        this.isBound = isBound;
    }

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public Long getDepartmentSortId() {
		return departmentSortId;
	}

	public void setDepartmentSortId(Long departmentSortId) {
		this.departmentSortId = departmentSortId;
	}

	public String getStrCreateTime() {
		return strCreateTime;
	}

	public void setStrCreateTime(String strCreateTime) {
		this.strCreateTime = strCreateTime;
	}

}




