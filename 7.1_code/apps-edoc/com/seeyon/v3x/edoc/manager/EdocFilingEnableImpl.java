package com.seeyon.v3x.edoc.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.doc.bo.DocPigeonholeBO;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocFilingEnableImpl {
	
	private final static Log log = LogFactory.getLog(EdocFilingEnableImpl.class);
	
	private EdocSummaryManager edocSummaryManager;
	private  AffairManager   affairManager;
	
	public Boolean isExistPigeonholeSource(Long sourceId) {
		boolean isExist=edocSummaryManager.findById(sourceId) !=null;
		if(!isExist){
			try {
				isExist=affairManager.get(sourceId) !=null;
			} catch (BusinessException e) {
				log.error("查询affair异常", e);
			}
		}
		return isExist;
	}

	public DocPigeonholeBO getDocPigeonhole(Long sourceId) {
	    DocPigeonholeBO vo = new DocPigeonholeBO();
		EdocSummary edoc=edocSummaryManager.findById(sourceId);
		if(edoc!=null){
			vo.setObject(edoc);
			vo.setName(edoc.getSubject());
			return vo;
		}
		try {
			CtpAffair affair = affairManager.get(sourceId);
			if (affair != null) {
			    
			    Long edocId = affair.getObjectId();
			    EdocSummary e = edocSummaryManager.findById(edocId);
			    
			    vo.setName(e.getSubject());
			    vo.setObject(e);
			    return vo;
			}
		} catch (BusinessException e) {
			log.error("", e);
		}
		return null;
	}

	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
}
