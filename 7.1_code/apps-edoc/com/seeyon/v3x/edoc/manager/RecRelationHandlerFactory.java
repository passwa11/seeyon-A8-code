package com.seeyon.v3x.edoc.manager;

import java.util.HashMap;
import java.util.Map;

public class RecRelationHandlerFactory {

	private static Map<Integer,RecRelationHandler> map = new HashMap<Integer,RecRelationHandler>();
	private static int A8_VERSION = 1;
	private static int G6_VERSION = 2;
	private RecRelationHandlerFactory(){}
	static{
		map.put(A8_VERSION, new RecRelationHandlerByA8());
		map.put(G6_VERSION, new RecRelationHandlerByG6());
	}
	
	public static RecRelationHandler getHandler(){
		if(EdocHelper.isG6Version()){
			return map.get(G6_VERSION);
		}else{
			return map.get(A8_VERSION);
		}
	}
}
