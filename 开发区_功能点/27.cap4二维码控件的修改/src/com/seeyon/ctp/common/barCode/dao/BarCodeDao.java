package com.seeyon.ctp.common.barCode.dao;

import com.seeyon.ctp.common.barCode.domain.BarCodeInfo;
import com.seeyon.ctp.common.dao.BaseDao;

public class BarCodeDao extends BaseDao<BarCodeInfo> {

	public BarCodeInfo getByObjectId(Long objectId) {
		return (BarCodeInfo)super.findUnique("from " + BarCodeInfo.class.getName() + " as b where b.objectId=?", null, objectId);
	}
}
