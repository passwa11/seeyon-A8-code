package com.seeyon.apps.govdoc.event;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.event.Event;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class GovDocURLEvent extends Event{
	private static final long serialVersionUID = 1L;
	
	public GovDocURLEvent(Object source) {
		super(source);
	}

	private Object formTypeKey;
	
	private String app = String.valueOf(ApplicationCategoryEnum.edoc.key());
	
	private String subApp;
	
	private Long affairId;

	private EdocSummary edocSummary;


	public EdocSummary getEdocSummary() {
		return edocSummary;
	}

	public void setEdocSummary(EdocSummary edocSummary) {
		this.edocSummary = edocSummary;
	}

	public Long getAffairId() {
		return affairId;
	}

	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}

	public Object getFormTypeKey() {
		return formTypeKey;
	}

	public void setFormTypeKey(Object formTypeKey) {
		this.formTypeKey = formTypeKey;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getSubApp() {
		return subApp;
	}

	public void setSubApp(String subApp) {
		this.subApp = subApp;
	}
	
	
}
