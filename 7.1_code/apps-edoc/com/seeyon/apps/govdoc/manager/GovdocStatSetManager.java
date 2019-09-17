package com.seeyon.apps.govdoc.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.EdocStatSet;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * 新公文统计设置接口
 * @author 唐桂林
 *
 */
public interface GovdocStatSetManager {

	public void checkStatInitData() throws BusinessException;
	
	public List<EdocStatSet> findEdocStatSetByAccount(long loginAccount,String statType) throws BusinessException;

	public FlipInfo findEdocStatListByAccount(FlipInfo fi, Map<String,String> params) throws BusinessException;

	public EdocStatSet getEdocStatSet(Long statId) throws BusinessException;
	
	public void updateEdocStatSet(EdocStatSet po) throws BusinessException;

	public void saveEdocStatSet(EdocStatSet po) throws BusinessException;

	public List<EdocStatSet> findEdocStatTreeList(String statType) throws BusinessException;


    /**
     * 创建统计配置信息
     * @param map
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("rawtypes")
	public void createState(Map map) throws BusinessException;
    /**
     * 修改统计配置信息
     * @param map
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("rawtypes")
	public void updateState(Map map) throws BusinessException;
    /**
     * 根据ID查询统计配置信息
     * @param id
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("rawtypes")
	public HashMap viewOne(Long id) throws BusinessException;
    /**
     * 根据IDs删除配置信息数据
     * @param ids
     * @return
     * @throws BusinessException
     */
    public Boolean deleteStat(Long[] ids) throws BusinessException;
    /**
     * 根据IDs检查配置信息数据
     * @param ids
     * @return
     * @throws BusinessException
     */
    public Boolean checkStat(Long[] ids) throws BusinessException;
    
    /**
     * @author rz
     * 代领导批示待办
     * @param flipInfo
     * @param params
     * @throws BusinessException
     */
    @SuppressWarnings("rawtypes")
	public FlipInfo pendLeaderPishi(FlipInfo flipInfo,Map params) throws BusinessException;

    /**
     * @author rz
     * 代领导批示已办
     * @param flipInfo
     * @param params
     * @throws BusinessException
     */
    @SuppressWarnings("rawtypes")
	public FlipInfo doneLeaderPishi(FlipInfo flipInfo,Map params) throws BusinessException;
}
