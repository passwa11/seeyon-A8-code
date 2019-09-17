/**
 * Id: EdocManagerFacade.java, v1.0 2012-4-9 wangchw Exp
 * Copyright (c) 2011 Seeyon, Ltd. All rights reserved
 */
package com.seeyon.v3x.edoc.manager;

import java.util.Map;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * @Project/Product: 产品或项目名称（A8）
 * @Description: 类功能描述
 * @Copyright: Copyright (c) 2012 of Seeyon, Ltd.
 * @author: wangchw
 * @time: 2012-4-9 上午11:02:18
 * @version: v1.0
 */
public interface EdocManagerFacade {

	/**
	 * 启动一个流程(统一入口)
	 */
	public void runCaseFacade(EdocSummary edocSummary,EdocBody body,
			EdocOpinion senderOninion,EdocEnum.SendType sendType,
			Map<String, Object> options,String comm,Long agentToId,
			boolean track,String trackMembers,String trackRange,
			String title,String superviseTitle,String supervisorId,
			String supervisors,String awakeDate,String exchangeIdStr,
			boolean isCanBeRegisted,User user) throws Exception;
	
	/**
	 * 立即启动一个流程(统一入口)
	 */
	public boolean runCaseImmediateFacade(String _affairId,EdocSummary edocSummary) throws Exception;
	
/*	*//**
	 * 提交一个流程(统一入口)
	 *//*
	public String finishWorkItemFacade(String supervisorNames,String spMemberId,
			String superviseDate,String processId,String edocMangerID,
			EdocSummary summary,User user,Long affairId,EdocOpinion signOpinion,
			Map<String, String[]> map,Map<String,String> condition,
			String isDeleteSupervisior,String title) throws Exception;*/
	
}
