package com.seeyon.apps.collaboration.util;

import java.util.HashMap;
import java.util.Map;

import com.seeyon.apps.collaboration.bo.ColOpenFromBean;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;

public class ColOpenFromUtil {

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
	
	private static Map<String, ColOpenFromBean> openFromMap = new HashMap<String, ColOpenFromBean>();
	
	static {
		openFromMap.put(ColOpenFrom.listPending.name(), new ColOpenFromBean().setListType(ColSummaryType.listPending.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(ColOpenFrom.listDone.name(), new ColOpenFromBean().setListType(ColSummaryType.listDone.name()).setCanComment(Boolean.TRUE));
		openFromMap.put(ColOpenFrom.listSent.name(), new ColOpenFromBean().setListType(ColSummaryType.listSent.name()).setCanComment(Boolean.TRUE));
		
		openFromMap.put(ColOpenFrom.listWaitSend.name(), new ColOpenFromBean().setListType(ColSummaryType.listWaitSend.name()));
		openFromMap.put(ColOpenFrom.supervise.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.F8Reprot.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.formStatistical.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.formQuery.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.formRelation.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.glwd.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.docLib.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.favorite.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.subFlow.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.stepBackRecord.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.repealRecord.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
		openFromMap.put(ColOpenFrom.task.name(), new ColOpenFromBean().setListType(ColSummaryType.onlyView.name()));
	}
	
	public static String getListType(String openFrom)  {
	    
	    String ret = null;
	    
	    ColOpenFromBean fromBean = openFromMap.get(openFrom);
	    
	    if(fromBean != null){
	        ret = fromBean.getListType();
	    }else{
	        ret = ColSummaryType.onlyView.name();
	    }
	    
		return ret;
	}
	
	public static Boolean isCanComment(String openFrom)  {
	    
	    boolean ret = false;
	    
	    ColOpenFromBean fromBean = openFromMap.get(openFrom);
        
        if(fromBean != null){
            ret = fromBean.isCanComment();
        }else{
            ret = false;
        }
	    
		return ret;
	}
	
}
