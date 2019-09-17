package com.seeyon.v3x.edoc.manager;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.v3x.edoc.domain.RegisterBody;

public interface RegisterBodyManager {

	public StringBuffer getRegisterContent(HttpServletRequest request, RegisterBody registerBody) throws Exception;
	
	public void updateReigsterBody(RegisterBody registerBody);
	
}
