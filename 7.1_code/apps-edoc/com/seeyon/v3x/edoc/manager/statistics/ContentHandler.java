package com.seeyon.v3x.edoc.manager.statistics;

import java.util.List;
import java.util.Map;

public interface ContentHandler {
	
	/**
	 * 内容展现，在页面中弹出窗口显示列表数据
	 */
	public List contentDisplay(long loginAccount);
	
	
	/**
	 * 统计时间维度
	 * @return
	 */
	public Map<Object,List<Object>> statisticsTimeAfterFind(StatParamVO statParam,List contents);
	
}
