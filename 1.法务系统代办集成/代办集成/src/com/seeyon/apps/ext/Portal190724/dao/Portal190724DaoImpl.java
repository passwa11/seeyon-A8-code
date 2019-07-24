package com.seeyon.apps.ext.Portal190724.dao;

import java.util.List;
import com.seeyon.ctp.util.DBAgent;

public class Portal190724DaoImpl implements Portal190724Dao{

	@Override
	public List<Object> selectPerson() {
		String sql="from org_member";
		return DBAgent.find(sql);
	}
}