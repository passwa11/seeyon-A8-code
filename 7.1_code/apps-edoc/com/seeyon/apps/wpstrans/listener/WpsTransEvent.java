package com.seeyon.apps.wpstrans.listener;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.Event;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class WpsTransEvent extends Event {

	private static final long serialVersionUID = 1L;
	
	private CtpAffair affair;
	private EdocSummary summary;
	
	public WpsTransEvent(Object source) {
		super(source);
	}

	public EdocSummary getSummary() {
		return summary;
	}

	public void setSummary(EdocSummary summary) {
		this.summary = summary;
	}

	public CtpAffair getAffair() {
		return affair;
	}

	public void setAffair(CtpAffair affair) {
		this.affair = affair;
	}
	
}
