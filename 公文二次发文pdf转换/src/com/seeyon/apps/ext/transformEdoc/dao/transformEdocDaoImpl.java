package com.seeyon.apps.ext.transformEdoc.dao;

import java.util.List;
import com.seeyon.ctp.util.DBAgent;

public class transformEdocDaoImpl implements transformEdocDao{

	@Override
	public List<Object> selectPerson() {
		String sql="from org_member";
		return DBAgent.find(sql);
	}
}