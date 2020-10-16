package com.seeyon.apps.govdoc.util;
import java.util.HashMap;
import java.util.Map;

import com.seeyon.apps.govdoc.bo.GovDocOpenFromBean;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;

public class GovDocOpenFromUtil {

	/**
	 * 列表listType
	 * @author tanggl
	 *
	 */
	public enum ColSummaryType {
		listWaitSend, //待发
	    listSent,     //已发
	    listPending,  //待办,时间线也能打开处理，传入的参数也是这个，只有OPENFROM是这个参数的时候才会打开处理页面
	    listDone ,    //已办
	    onlyView //仅查看
	}
	
	private static Map<String, GovDocOpenFromBean> openFromMap = new HashMap<String, GovDocOpenFromBean>();
	
	static {
		openFromMap.put(EdocOpenFrom.listPending.name(), new GovDocOpenFromBean().setListType(ColSummaryType.listPending.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listPendingAll.name(), new GovDocOpenFromBean().setListType(EdocOpenFrom.listPendingAll.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listDone.name(), new GovDocOpenFromBean().setListType(ColSummaryType.listDone.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listDoneAll.name(), new GovDocOpenFromBean().setListType(EdocOpenFrom.listDoneAll.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listSent.name(), new GovDocOpenFromBean().setListType(ColSummaryType.listSent.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listZcdb.name(), new GovDocOpenFromBean().setListType(EdocOpenFrom.listZcdb.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listWaitSend.name(), new GovDocOpenFromBean().setListType(ColSummaryType.listWaitSend.name()));
		openFromMap.put(EdocOpenFrom.supervise.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.F8Reprot.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.formStatistical.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.formQuery.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.formRelation.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.glwd.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.docLib.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.favorite.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.subFlow.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.stepBackRecord.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.repealRecord.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.task.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.edocStatistics.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.lenPotent.name(), new GovDocOpenFromBean().setListType(ColSummaryType.onlyView.name()));
	}
	 
	public static String getListType(String openFrom)  {
		return openFromMap.get(openFrom).getListType();
	}
	
	public static Boolean isCanComment(String openFrom)  {
		return openFromMap.get(openFrom).isCanComment();
	}
	
}
