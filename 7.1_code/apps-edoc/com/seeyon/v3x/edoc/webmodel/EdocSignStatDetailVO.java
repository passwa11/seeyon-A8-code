package com.seeyon.v3x.edoc.webmodel;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.seeyon.ctp.util.DateUtil;
/**
 * 签收统计穿透VO
 * @author zhangdong
 *
 */
public class EdocSignStatDetailVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long summaryId;//公文ID
	private String subject;//公文标题
	private String docMark;//公文文号
	private String issuer;//签发人
	private String sendUnit;//来文单位
	private String sendDepartment;//来文部门
	private Timestamp startTime;//来文时间
	private String startTimeView;//来文时间展示
	private Timestamp recTime;//签收时间
	private String recTimeView;//签收时间展示
	private Timestamp backTime;//退文时间
	private String backTimeView; //退文时间展示
	private String backOpinion;//退文原因
	private String recUserName;//签收人
	private Long exchangeSummaryId;
	public void setListValue(Object[] object) {
		int i = 0;
	    Object sId = object[i++];
		long summaryId = (sId==null?null:Long.parseLong((String)sId));
		this.setSummaryId(summaryId);		
		this.setRecTime((Timestamp)object[i++]);
		this.setBackTime((Timestamp)object[i++]);
		this.setBackOpinion((String)object[i++]);
		this.setRecUserName((String)object[i++]);
		Date recDate = this.getRecTime();
		Date backDate = this.getBackTime();
		if(recDate == null){
		    this.setRecTimeView("");
		}else {
		    this.setRecTimeView(DateUtil.format(recDate, "yyyy-MM-dd"));
        }
		if(backDate == null){
		    this.setBackTimeView("");
		}else {
		    this.setBackTimeView(DateUtil.format(backDate, "yyyy-MM-dd"));
        }
	}
	
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getDocMark() {
		return docMark;
	}
	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public String getSendUnit() {
		return sendUnit;
	}
	public void setSendUnit(String sendUnit) {
		this.sendUnit = sendUnit;
	}
	public String getSendDepartment() {
		return sendDepartment;
	}
	public void setSendDepartment(String sendDepartment) {
		this.sendDepartment = sendDepartment;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public String getStartTimeView() {
		return startTimeView;
	}
	public void setStartTimeView(String startTimeView) {
		this.startTimeView = startTimeView;
	}
	public Timestamp getRecTime() {
		return recTime;
	}
	public void setRecTime(Timestamp recTime) {
		this.recTime = recTime;
	}
	public String getRecTimeView() {
		return recTimeView;
	}
	public void setRecTimeView(String recTimeView) {
		this.recTimeView = recTimeView;
	}
	public Timestamp getBackTime() {
		return backTime;
	}
	public void setBackTime(Timestamp backTime) {
		this.backTime = backTime;
	}
	public String getBackTimeView() {
		return backTimeView;
	}
	public void setBackTimeView(String backTimeView) {
		this.backTimeView = backTimeView;
	}
	public String getBackOpinion() {
		return backOpinion;
	}
	public void setBackOpinion(String backOpinion) {
		this.backOpinion = backOpinion;
	}
	public String getRecUserName() {
		return recUserName;
	}
	public void setRecUserName(String recUserName) {
		this.recUserName = recUserName;
	}
	public Long getExchangeSummaryId() {
		return exchangeSummaryId;
	}
	public void setExchangeSummaryId(Long exchangeSummaryId) {
		this.exchangeSummaryId = exchangeSummaryId;
	}
}
