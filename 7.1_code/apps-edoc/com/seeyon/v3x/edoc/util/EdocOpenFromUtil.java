package com.seeyon.v3x.edoc.util;

import java.util.HashMap;
import java.util.Map;

import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.webmodel.EdocOpenFromBean;

public class EdocOpenFromUtil {

	/**
	 * 列表listType
	 * @author tanggl
	 *
	 */
	public enum EdocSummaryType {
		listWaitSend, //待发
	    listSent,     //已发
	    listPending,  //待办,时间线也能打开处理，传入的参数也是这个，只有OPENFROM是这个参数的时候才会打开处理页面
	    listDone ,    //已办
	    onlyView //仅查看
	}
	
	private static Map<String, EdocOpenFromBean> openFromMap = new HashMap<String, EdocOpenFromBean>();
	
	static {
		openFromMap.put(EdocOpenFrom.listPending.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listPending.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listZcdb.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listPending.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listPendingAll.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listPending.name()).setCanComment(Boolean.TRUE));		
		openFromMap.put(EdocOpenFrom.listDone.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listDone.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listDoneAll.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listDone.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(EdocOpenFrom.listSent.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listSent.name()).setCanComment(Boolean.TRUE));
		
		openFromMap.put(EdocOpenFrom.listWaitSend.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listWaitSend.name()));
		openFromMap.put(EdocOpenFrom.listFinish.name(), new EdocOpenFromBean().setListType(EdocSummaryType.listDone.name()));
		openFromMap.put(EdocOpenFrom.supervise.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.supervise.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.F8Reprot.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.glwd.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.docLib.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.favorite.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.subFlow.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.stepBackRecord.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.repealRecord.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
		openFromMap.put(EdocOpenFrom.lenPotent.name(), new EdocOpenFromBean().setListType(EdocSummaryType.onlyView.name()));
	}
	
	public static String getListType(String openFrom)  {
		return openFromMap.get(openFrom).getListType();
	}
	
	public static Boolean isCanComment(String openFrom)  {
		return openFromMap.get(openFrom).isCanComment();
	}
	
}
