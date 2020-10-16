package com.seeyon.apps.synorg.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.synorg.vo.SyncDataListVO;
import com.seeyon.apps.synorg.vo.SyncMemberListVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * Description
 * <pre></pre>
 * @author FanGaowei<br>
 * Date 2018年2月24日 下午4:54:43<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncDataDao {
	
    /**
     * Description:
     * <pre>查询部门、职务、岗位中间表的数据</pre>
     * @param flipInfo	前台grid数据
     * @param condition	查询条件
     * @return List<SyncDataListVO> 前台展示的数据列表
     * @throws BusinessException
     */
    public List<SyncDataListVO> queryData(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

    /**
     * Description:
     * <pre>查询人员中间表的数据</pre>
     * @param flipInfo	前台grid数据
     * @param condition	查询条件
     * @return List<SyncMemberListVO> 前台展示的数据列表
     * @throws BusinessException
     */
    public List<SyncMemberListVO> queryMemberData(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

}
