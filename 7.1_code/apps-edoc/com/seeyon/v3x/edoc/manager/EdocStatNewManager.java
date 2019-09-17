package com.seeyon.v3x.edoc.manager;

import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

public interface EdocStatNewManager {

	/**
	 * 查询统计的预制数据是否生成，没有生成则自动生成
	 * @throws Exception
	 */
    public void checkStatInitData()throws Exception;
	/**
	 * 获取公文统计数据
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocStatVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
	/**
	 * 获取公文统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
	/**
	 * 公文统计界面-选择枚举值
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocEnumitemList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
}
