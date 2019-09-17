package com.seeyon.apps.govdoc.manager.external;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.doc.bo.DocPigeonholeBO;
import com.seeyon.apps.doc.manager.DocFilingEnable;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 公文归档实现
 * @author tanggl
 *
 */
public class GovdocFilingEnableImpl extends DocFilingEnable {
	
	private final static Log LOGGER = LogFactory.getLog(GovdocFilingEnableImpl.class);
	
	private GovdocSummaryManager govdocSummaryManager;
	private  AffairManager   affairManager;
	
	@Override
	public Integer getAppEnumKey() {
		return ApplicationCategoryEnum.edoc.getKey();
	}

	@Override
	public Boolean isExistPigeonholeSource(Long sourceId) {
		try {
			boolean isExist = govdocSummaryManager.getSummaryById(sourceId) !=null;
			if(!isExist) {
				isExist=affairManager.get(sourceId) != null;
			}
			return isExist;
		} catch (BusinessException e) {
			LOGGER.error("查询affair异常", e);
		}
		return false;
	}

	@Override
	public DocPigeonholeBO getDocPigeonhole(Long sourceId) {
	    try {
			DocPigeonholeBO vo = new DocPigeonholeBO();
			
			EdocSummary edoc = govdocSummaryManager.getSummaryById(sourceId);
			if(edoc != null) {
				vo.setObject(edoc);
				vo.setName(edoc.getSubject());
				return vo;
			}
			
			CtpAffair affair = affairManager.get(sourceId);
			if (affair != null) {
			    Long edocId = affair.getObjectId();
			    EdocSummary e = govdocSummaryManager.getSummaryById(edocId);
			    
			    vo.setName(e.getSubject());
			    vo.setObject(e);
			    return vo;
			}
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
	    
		return null;
	}

	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
}
