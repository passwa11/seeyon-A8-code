package com.seeyon.apps.synorg.manager;

import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * Description
 * <pre>AjaxManager，用户获取中间表数据</pre>
 * @author FanGaowei<br>
 * Date 2018年2月24日 下午4:16:28<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncDataManager {
	
	/**
     * Description:
     * <pre>获取部门、职务、岗位的同步数据</pre>
     * @param flipInfo 查询对象
     * @param query 查询条件
     * @return 前台展示的数据列表
     * @throws BusinessException
     */
    public FlipInfo getSyncData(FlipInfo flipInfo, Map<String, String> query) throws BusinessException;
	
    /**
     * Description:
     * <pre>获取人员的同步数据</pre>
     * @param flipInfo 查询对象
     * @param query 查询条件信息
     * @return 前台展示的数据列表
     * @throws BusinessException
     */
    public FlipInfo getSyncMemberData(FlipInfo flipInfo, Map<String, String> query) throws BusinessException;

}
