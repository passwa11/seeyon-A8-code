package com.seeyon.apps.synorg.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.apps.synorg.vo.SynLogListVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * 日志管理
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:23:17
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncLogDao {

    /**
     * 创建日志数据
     * @param postList 岗位列表
     */
    public void createAll(List<SynLog> Log);

    /**
     * 查找全部日志
     */
    public List<SynLogListVO> queryByCondition(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

    /**
     * 根据纪录ID删除日志
     * @param ids ids
     */
    public void deleteSyncLogByIds(String ids);

    /**
     * 清空日志数据
     */
    public void deleteAll();
}
