package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.domain.BaseModel;

public class RegisterBody extends BaseModel  implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3362326716210742845L;
	private String content;
	private String contentType;
	private EdocRegister edocRegister;
	private Integer contentNo=0;
	private java.sql.Timestamp createTime;
	
	public void bind(HttpServletRequest request) {
		this.setId(Strings.isBlank(request.getParameter("bodyId"))? -1L : Long.parseLong(request.getParameter("bodyId")));
		this.setContent(request.getParameter("content"));
		this.setContentNo(request.getParameter("contentNo")==null? 0 : Integer.parseInt(request.getParameter("contentNo")));
		this.setContentType(request.getParameter("bodyType")==null? "HTML" : request.getParameter("bodyType"));
		String bodyCreateTime = request.getParameter("bodyCreateDate");
		java.util.Date date1 = new java.util.Date();
		java.util.Date date2 = Datetimes.parseDatetime(bodyCreateTime);
        this.setCreateTime(bodyCreateTime==null || "".equals(bodyCreateTime) ? new java.sql.Timestamp(date1.getTime()) : new java.sql.Timestamp(date2.getTime()));
	}
	
	public java.sql.Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(java.sql.Timestamp createTime) {
		this.createTime = createTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public EdocRegister getEdocRegister() {
		return edocRegister;
	}
	public void setEdocRegister(EdocRegister edocRegister) {
		this.edocRegister = edocRegister;
	}
	public Integer getContentNo() {
		return contentNo;
	}
	public void setContentNo(Integer contentNo) {
		this.contentNo = contentNo;
	}

}
