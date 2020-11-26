package com.seeyon.apps.collaboration.manager;

import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.doc.bo.DocPigeonholeBO;
import com.seeyon.apps.doc.manager.DocFilingEnable;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.Strings;

public class CollDocFilingEnable extends DocFilingEnable {
	private static Log LOG = CtpLogFactory.getLog(CollDocFilingEnable.class);
	private CAPFormManager     capFormManager;
	private	ColManager 		colManager;

	
    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }
    
    
    @Override
    public Integer getAppEnumKey() {
        return ApplicationCategoryEnum.collaboration.getKey();
    }

    @Override
    public DocPigeonholeBO getDocPigeonhole(Long arg0) {
        DocPigeonholeBO vo = new DocPigeonholeBO();
        try {
            CtpAffair affair = colManager.getAffairById(arg0);
            ColSummary colSummary = colManager.getColSummaryById(affair.getObjectId());
                vo.setName(affair.getSubject());
                vo.setObject(affair);
                if(Strings.isNotBlank(colSummary.getAdvancePigeonhole())){
                	String archiveKeyword = "";
                	String keyword = null;
    				try {
    					JSONObject jo = new JSONObject(colSummary.getAdvancePigeonhole());
    					archiveKeyword = jo.optString("archiveKeyword", "");
    					if (Strings.isNotBlank(archiveKeyword)) {
    						keyword = capFormManager.getCollSubjuet(colSummary.getFormAppid(), archiveKeyword, colSummary.getFormRecordid(), false);
    			            vo.setKeyWords(keyword);
    					}
					} catch (JSONException e) {
						LOG.error("", e);
					}
                }
        } catch (BusinessException e) {
        	LOG.error("", e);
        }
        return vo;
    }

    @Override
    public Boolean isExistPigeonholeSource(Long arg0) {
        Boolean result = false;
        try {
            CtpAffair affair = colManager.getAffairById(arg0);
            return ColUtil.isAfffairValid(affair);
        } catch (Exception e) {
        	LOG.error("", e);
        }
        return result;
    }

    /**
     * @param colManager the colManager to set
     */
    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

}
