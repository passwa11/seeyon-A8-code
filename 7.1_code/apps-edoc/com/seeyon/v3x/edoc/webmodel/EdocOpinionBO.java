package com.seeyon.v3x.edoc.webmodel;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.Constants;

/**
 * The persistent class for the edoc_opinion database table.
 * 
 * @author BEA Workshop Studio
 */
public class EdocOpinionBO extends BaseModel  implements Serializable, Comparator<EdocOpinionBO> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8422408408115651498L;
	//default serial version id, required for serializable classes.
	private long affairId;
	private int attribute = Constants.EDOC_ATTITUDE_NULL;
	private String content;
	private java.sql.Timestamp createTime;
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

    public EdocOpinionBO() {
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
	
	@Override
	public int compare(EdocOpinionBO o1, EdocOpinionBO o2) {
		java.sql.Timestamp d1 = o1.getCreateTime();
		java.sql.Timestamp d2 = o2.getCreateTime();
		int res=0;
		if(d1!=null && d2!=null) {
			res = d1.compareTo(d2);
		} else if(d1==null && d2!=null) {
			res=-1;
		} else if(d1!=null && d2==null) {
			res=1;
		}
		int flag = res==0?0:res>0?-1:1;
		return res==0?0:res>0?-1:1;
	}
	
}