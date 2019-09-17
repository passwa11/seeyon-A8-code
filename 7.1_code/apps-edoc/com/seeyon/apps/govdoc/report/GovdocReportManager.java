package com.seeyon.apps.govdoc.report;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;

/**
 * <p>Title: 公文报表相关接口</p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>Company: seeyon.com</p>
 * <p>auth  fucz </p>
 * <p>since V5 7.1 </p>
 */
public interface GovdocReportManager {
	
	/**
	 * <p>Title: 查询公文报表</p>
	 * <p>Company: seeyon.com</p>
	 * <p>author : fucz</p>
	 * <p>since V5 7.1 2019</p>
	 * @param fi
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public FlipInfo findReportDesigns(FlipInfo fi, Map<String,Object> params) throws BusinessException;
	/**
	 * <p>Title:获取公文节点权限 </p>
	 * <p>Company: seeyon.com</p>
	 * <p>author : fucz</p>
	 * <p>since V5 7.1 2019</p>
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
	public List<PermissionVO> getPermissions(Map<String,Object> params) throws BusinessException;
	/**
	 * <p>Title: 删除公文报表</p>
	 * <p>Company: seeyon.com</p>
	 * <p>author : fucz</p>
	 * <p>since V5 7.1 2019</p>
	 * @param recordIds
	 * @return
	 * @throws BusinessException
	 */
	@AjaxAccess
    public void removeReports(List<String> recordIds) throws BusinessException;
}
