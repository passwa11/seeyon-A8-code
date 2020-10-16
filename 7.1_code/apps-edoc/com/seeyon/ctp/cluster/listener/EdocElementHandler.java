package com.seeyon.ctp.cluster.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.edoc.manager.EdocElementManager;

public class EdocElementHandler {
	protected static final Log logger = LogFactory.getLog(EdocElementHandler.class);
	private EdocElementManager edocElementManager;
	private OrgManager orgManager;
	
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setEdocElementManager(EdocElementManager edocElementManager) {
		this.edocElementManager = edocElementManager;
	}
}

