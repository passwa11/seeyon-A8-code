package com.seeyon.apps.ext.oauthLogin.dao;

import java.util.List;
import com.seeyon.ctp.util.DBAgent;

public class oauthLoginDaoImpl implements oauthLoginDao{

	@Override
	public List<Object> selectPerson() {
		String sql="from org_member";
		return DBAgent.find(sql);
	}
}