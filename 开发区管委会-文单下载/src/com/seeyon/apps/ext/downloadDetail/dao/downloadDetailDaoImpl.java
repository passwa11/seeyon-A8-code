package com.seeyon.apps.ext.downloadDetail.dao;

import java.util.List;
import com.seeyon.ctp.util.DBAgent;

public class downloadDetailDaoImpl implements downloadDetailDao{

	@Override
	public List<Object> selectPerson() {
		String sql="from org_member";
		return DBAgent.find(sql);
	}
}