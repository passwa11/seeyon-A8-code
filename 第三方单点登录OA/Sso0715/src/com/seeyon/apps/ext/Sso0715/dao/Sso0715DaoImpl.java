package com.seeyon.apps.ext.Sso0715.dao;

import java.util.List;
import com.seeyon.ctp.util.DBAgent;

public class Sso0715DaoImpl implements Sso0715Dao{

	@Override
	public List<Object> selectPerson() {
		String sql="from org_member";
		return DBAgent.find(sql);
	}
}