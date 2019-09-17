package com.seeyon.v3x.edoc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;

public class SharedWithThreadLocal {

	//公文发送中用到的
	private static ThreadLocal<List<EdocFormElement>> sharedWithElements = new ThreadLocal<List<EdocFormElement>>();
	private static ThreadLocal<Map<String,EdocMarkDefinition>> sharedWithMarkDefinitions = new ThreadLocal<Map<String,EdocMarkDefinition>>();
	private static ThreadLocal<Boolean> isCanUse = new ThreadLocal<Boolean>();
	private static ThreadLocal<Map<String, Boolean>> sharedWithMarks = new ThreadLocal<Map<String, Boolean>>();
	
	//公文查看中用到的
	private static ThreadLocal<Hashtable<String, String>> locHs = new ThreadLocal<Hashtable<String, String>>();

	
	public static void setCanUse(){
		isCanUse.set(true);
	}
	
	public static boolean getIsCanUse(){
		if(isCanUse.get()==null){
			return false;
		}
		return isCanUse.get();
	}

	public static void setLocHs(Hashtable<String, String> hs){
		Hashtable<String, String> chs = locHs.get();
    	if(chs == null){
    		chs = new Hashtable<String, String>();
    		locHs.set(chs);
    	}
    	chs.putAll(hs);
	}
	
	
	
	public static Hashtable<String, String> getLocHs(){
		return locHs.get();
	}
	
	
	
	public static void setEdocFormElements(List<EdocFormElement> list){
		List<EdocFormElement> elementList = sharedWithElements.get();
    	if(elementList == null){
    		elementList = new ArrayList<EdocFormElement>();
    		sharedWithElements.set(elementList);
    	}
    	elementList.addAll(list);
	}
	
	public static List<EdocFormElement> getEdocFormElements(){
		List<EdocFormElement> list = sharedWithElements.get();
		return  list== null ? new ArrayList<EdocFormElement>(0) : list ;
	}
	
	public static void setMarks(List<String> marks) {
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for(int i=0;i<marks.size();i++) {
			map.put(String.valueOf(marks.get(i)), Boolean.TRUE);
		}
		
		sharedWithMarks.set(map);
	}
	
	public static Boolean getMarks(String mark) {
		Map<String, Boolean> map = sharedWithMarks.get();
		return  map != null ? map.get(mark): false;
	}
	
	public static void setMarkDefinition(List definitions){
		Map<String,EdocMarkDefinition> map = new HashMap<String,EdocMarkDefinition>();
		for(int i=0;i<definitions.size();i++){
			Object[] obj = (Object[])definitions.get(i);
			EdocMarkDefinition def = (EdocMarkDefinition)obj[0];
			EdocMarkCategory edocMarkCategory = (EdocMarkCategory)obj[1];
			def.setEdocMarkCategory(edocMarkCategory);
			map.put(String.valueOf(def.getId()), def);
		}
		
		sharedWithMarkDefinitions.set(map);
	}
	
	public static EdocMarkDefinition getMarkDefinition(long definitionId){
		Map<String,EdocMarkDefinition> map = sharedWithMarkDefinitions.get();
		return  map != null ? map.get(String.valueOf(definitionId)) : null;
	}
	
	public static void remove() {
		sharedWithElements.remove();
		sharedWithMarkDefinitions.remove();
		isCanUse.remove();
	}
	
	public static void removeView() {
		locHs.remove();
	}
}
