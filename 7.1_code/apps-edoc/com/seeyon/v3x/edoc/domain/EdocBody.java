package com.seeyon.v3x.edoc.domain;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.seeyon.v3x.common.domain.BaseModel;

/**
 * The persistent class for the edoc_body database table.
 * 
 * @author BEA Workshop Studio
 */
public class EdocBody extends BaseModel  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5571256602135152700L;
	private Long edocId = null;
	private String content;
	private String contentType;
	private String contentName;
	private java.sql.Timestamp createTime;
	private java.sql.Timestamp lastUpdate;
	private EdocSummary edocSummary;
	private Integer contentStatus;
	private Integer contentNo=0;
	
	public final static int EDOC_BOBY_NORMAL = 0; /** 联合发文标识 ： 保留的正文 */
	public final static int EDOC_BODY_FIRST = 1; /** 联合发文标识 : 一单位的正文 */
	public final static int EDOC_BODY_SECOND = 2; /** 联合发文标识 : 二单位的正文 */
	public final static int EDOC_BODY_PDF_ONE = 3; /**公文交换的时候，WORD转PDF正文时候的第一套PDF正文 */
	public final static int EDOC_BODY_PDF_TWO = 4; /**如果联合发文公文交换的时候，WORD转PDF正文时候的第二套PDF正文 */
	
	
	public Integer getContentNo()
	{
		if(this.contentNo==null){this.contentNo=0;}
		return this.contentNo;
	}
	public void setContentNo(Integer contentNo)
	{
		if(contentNo==null){return;}
		this.contentNo=contentNo;
		
	}
	
	public Integer getContentStatus()
	{
		return this.contentStatus;
	}
	public void setContentStatus(Integer contentStatus)
	{
		this.contentStatus=contentStatus;
	}

    public EdocBody() {
    }
    
	public String getContent() {
		return this.content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return this.contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentName() {
		return this.contentName;
	}
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}

	public java.sql.Timestamp getCreateTime() {
		return this.createTime;
	}
	public void setCreateTime(java.sql.Timestamp createTime) {
		this.createTime = createTime;
	}

	public java.sql.Timestamp getLastUpdate() {
		return this.lastUpdate;
	}
	public void setLastUpdate(java.sql.Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
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
	public Long getEdocId() {
		return edocId;
	}
	public void setEdocId(Long edocId) {
		this.edocId = edocId;
	}
}