package com.seeyon.v3x.edoc.webmodel;

import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocFormModel extends com.seeyon.ctp.util.ObjectToXMLBase{
	private Long edocSummaryId;
	private EdocBody edocBody=null;
	private String xslt="";
	private String xml="";
	private Long edocFormId=0L;
	private Long deadline=0L;
	private Long advanceRemind = 0L;
	private EdocOpinion senderOpinion;
	private EdocSummary edocSummary;
	private String deadline4temp;//流程期限，用来回显调用模板的时候显示期限
	
	public void setEdocSummary(EdocSummary edocSummary)
	{
		this.edocSummary=edocSummary;
	}
	public EdocSummary getEdocSummary()
	{
		return this.edocSummary;
	}
	
	public Long getEdocSummaryId()
	{
		return this.edocSummaryId;
	}
	public void setEdocSummaryId(Long edocSummaryId)
	{
		this.edocSummaryId=edocSummaryId;
	}
	public EdocOpinion getSenderOpinion()
	{
		return this.senderOpinion;
	}
	
	public void setSenderOpinion(EdocOpinion senderOpinion)
	{
		this.senderOpinion=senderOpinion;
	}
	
	public Long getEdocFormId()
	{
		return this.edocFormId;
	}
	
	public void setEdocFormId(Long edocFormId)
	{
		this.edocFormId=edocFormId;
	}
	
	public Long getAdvanceRemind() {
		return advanceRemind;
	}

	public void setAdvanceRemind(Long advanceRemind) {
		this.advanceRemind = advanceRemind;
	}
	
	public Long getDeadline() {
		return this.deadline;
	}

	public void setDeadline(Long deadline) {
		this.deadline = deadline;
	}
	
	public void setEdocBody(EdocBody edocBody)
	{
		this.edocBody=edocBody;
	}
	public EdocBody getEdocBody()
	{
		return this.edocBody;
	}
	public void setXml(String xml)
	{
		this.xml=xml;
	}
	public String getXml()
	{
		return this.xml;
	}
	
	public void setXslt(String xslt)
	{
		this.xslt=xslt;
	}
	public String getXslt()
	{
		return this.xslt;
	}
	public void setDeadline4temp(String deadline4temp) {
		this.deadline4temp = deadline4temp;
	}
	public String getDeadline4temp() {
		return deadline4temp;
	}

}
