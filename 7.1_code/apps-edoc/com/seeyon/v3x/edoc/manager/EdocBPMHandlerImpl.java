/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler;

/**
 * 公文流程rest实现类
 * @author wangchw
 *
 */
public class EdocBPMHandlerImpl extends SeeyonBPMAppHandler {

	private static final Log LOGGER = CtpLogFactory.getLog(EdocBPMHandlerImpl.class);


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler#getApp()
	 */
	@Override
	public ApplicationCategoryEnum getApp() {
		return ApplicationCategoryEnum.edoc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler#startProcess(java.util.
	 * Map)
	 */
	@Override
	public Map<String, String> startProcess(Map<String, Object> param) throws BusinessException {
	    
	    return null;
	}
	
	/* (non-Javadoc)
     * @see com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler#transStepStop(java.util.Map)
     */
    @Override
    public Map<String, String> transStepStop(Map<String, Object> requestData) throws BusinessException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * @see SeeyonBPMAppHandler#preStartProcess
     */
    @Override
    public Map<String, String> preStartProcess(Map<String,Object> requestData) throws BusinessException {

        return null;
    }

    @Override
    public String[] transTackBack(Map<String, Object> appData) throws BusinessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] transStepBack(Map<String, Object> appData) throws BusinessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] transSpecifyBack(Map<String, Object> appData, String targetNodeId, String stepbackStyle)
            throws BusinessException {
        // TODO Auto-generated method stub
        return null;
    }
}
