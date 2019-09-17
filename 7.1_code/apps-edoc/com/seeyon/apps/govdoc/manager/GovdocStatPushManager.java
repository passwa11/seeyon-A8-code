package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;

public interface GovdocStatPushManager {	
	
	public List<EdocRegisterCondition> getRegisterConditionList(User user, Map<String, Object> paramMap);
	public EdocRegisterCondition getRegisterConditionById(Long conditionId) throws BusinessException;
	public void saveRegisterCondition(String listType, User user) throws BusinessException;
	
}

