package com.seeyon.v3x.edoc.manager.statistics;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 统计接口
 * 抽象出统计方法，按时间维度进行统计和按组织维度进行统计
 *
 */
public interface StrategyInter {
	
	/**
	 * 统计查询出最后显示的统计表(不包括左边维度和上边的内容显示名称)
	 * 左边和上边的可以直接在contoller层中获得，然后在页面中分开显示出来
	 * 
	 * @param dimensionList 时间维度或者组织维度List<Object>
	 * 			 时间为List<String>,组织为List<Long>
	 * @param organizationType 组织类型，可以为全单位，部门，职务，职级
	 * 
	 * @param otherSingleObject  单一时间或者单一组织
	 * 			单一的时间为String类型
	 * 			单一的组织为Long类型
	 * @param 	statisticsContentList	List<Object> 需要统计显示的内容
	 * 			流程节点  从页面过来如 shenpi 英文编码
	 * 			办理情况  (阅文、办文等) 从页面传过来的应该是  编号
	 * 
	 * @return Map中存放的key为行号，value为一List,表示每一行中的统计数据
	 */
	public Map<Object,List<Object>> statistics(HttpServletRequest request, StatParamVO statParam
			,List<ContentData> statisticsContentList) throws Exception; 
	
}














