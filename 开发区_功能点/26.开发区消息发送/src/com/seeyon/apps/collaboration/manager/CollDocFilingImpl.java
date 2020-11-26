package com.seeyon.apps.collaboration.manager;
import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.doc.bo.DocPigeonholeBO;
import com.seeyon.apps.doc.manager.DocFilingEnable;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;

public class CollDocFilingImpl extends DocFilingEnable {

    private static Log LOG = CtpLogFactory.getLog(CollDocFilingImpl.class);
	private  ColManager  colManager;
	private  AffairManager   affairManager;
	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	@Override
	public Integer getAppEnumKey() {
		// TODO Auto-generated method stub
		return ModuleType.collaboration.ordinal();
	}

	@Override
	public DocPigeonholeBO getDocPigeonhole(Long affairId) {
		String name = "";
		DocPigeonholeBO vo = new DocPigeonholeBO();
		try {
			CtpAffair affair = affairManager.get(affairId);
			name = ColUtil.mergeSubjectWithForwardMembers(affair.getSubject(),affair.getForwardMember() ,affair.getResentTime(),null,-1);
			vo.setObject(affair);
			vo.setName(name);
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
	            return ColUtil.isAfffairValid(affair,false);
	        } catch (Exception e) {
	            LOG.error("", e);
	        }
	        return result;
	    }

}
