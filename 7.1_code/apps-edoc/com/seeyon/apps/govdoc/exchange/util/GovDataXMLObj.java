package com.seeyon.apps.govdoc.exchange.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "govdoc") 
public class GovDataXMLObj {
	private long id;
	private String subject;
	private Date createTime;
	private Long startUserId;
	private Date startTime;
	private String createPerson;
	private Long parentId;
	
	private List<GovDataFormTableXMLObj> table = new ArrayList<GovDataFormTableXMLObj>();
	
	@XmlAttribute(name = "id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@XmlAttribute(name = "subject")
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	@XmlAttribute(name = "createTime")
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@XmlAttribute(name = "startUserId")
	public Long getStartUserId() {
		return startUserId;
	}
	public void setStartUserId(Long startUserId) {
		this.startUserId = startUserId;
	}
	
	@XmlAttribute(name = "startTime")
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	@XmlAttribute(name = "createPerson")
	public String getCreatePerson() {
		return createPerson;
	}
	public void setCreatePerson(String createPerson) {
		this.createPerson = createPerson;
	}
	
	@XmlAttribute(name = "parentId")
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	public List<GovDataFormTableXMLObj> getTable() {
		return table;
	}
	public void setTable(List<GovDataFormTableXMLObj> table) {
		this.table = table;
	}
}
