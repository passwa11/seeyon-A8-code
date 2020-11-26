package com.seeyon.ctp.portal.portlet.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

public interface DeskCollaborationManager {
	/**
	 * 获取工作桌面待办数据
	 * @param size 需要获取的数据条数
	 * @return 工作桌面待办列表List<CollaborationInfo>
	 * @throws BusinessException
	 */
	public FlipInfo getCollaborationList(String size)throws BusinessException;
}
