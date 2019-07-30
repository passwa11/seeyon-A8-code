package com.seeyon.apps.synorg.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.synorg.dao.SyncDataDao;
import com.seeyon.apps.synorg.vo.SyncDataListVO;
import com.seeyon.apps.synorg.vo.SyncMemberListVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * Description
 * <pre></pre>
 * @author FanGaowei<br>
 * Date 2018年2月24日 下午4:19:19<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncDataManagerImpl implements SyncDataManager{
	
	private SyncDataDao syncDataDao;

	@Override
	public FlipInfo getSyncMemberData(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
		List<SyncMemberListVO> result = syncDataDao.queryMemberData(flipInfo, query);
        if(flipInfo == null) {
        	flipInfo = new FlipInfo();
        }
        flipInfo.setData(result);
        return flipInfo;
	}

	@Override
	public FlipInfo getSyncData(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
		List<SyncDataListVO> result = syncDataDao.queryData(flipInfo, query);
        if(flipInfo == null) {
        	flipInfo = new FlipInfo();
        }
        flipInfo.setData(result);
        return flipInfo;
	}

	public void setSyncDataDao(SyncDataDao syncDataDao) {
		this.syncDataDao = syncDataDao;
	}
}
