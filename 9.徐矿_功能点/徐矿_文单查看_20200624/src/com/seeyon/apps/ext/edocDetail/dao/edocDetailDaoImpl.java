package com.seeyon.apps.ext.edocDetail.dao;

import java.util.List;
import com.seeyon.ctp.util.DBAgent;

public class edocDetailDaoImpl implements edocDetailDao{

	@Override
	public List<Object> selectPerson() {
		String sql="from org_member";
		return DBAgent.find(sql);
	}
}