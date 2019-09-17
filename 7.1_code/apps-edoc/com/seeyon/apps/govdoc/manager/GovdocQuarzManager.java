package com.seeyon.apps.govdoc.manager;

import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 新公文定时器接口
 * @author 唐桂林
 *
 */
public interface GovdocQuarzManager {

	/**
	 * 公文发送时-创建任务超期提醒和提前提醒定时器
	 * @param info
	 * @throws BusinessException
	 */
	void createSendQuartzJob(GovdocNewVO info) throws BusinessException;
	
}
