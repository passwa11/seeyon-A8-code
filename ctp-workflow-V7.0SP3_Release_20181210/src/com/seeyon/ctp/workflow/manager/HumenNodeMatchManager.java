package com.seeyon.ctp.workflow.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.workflow.wapi.HumenNodeMatchInterface;

public class HumenNodeMatchManager {

    public static final Logger logger = Logger.getLogger(HumenNodeMatchManager.class);
    
    private static boolean isInit= false;
    
    private static Map<String, HumenNodeMatchInterface> humenNodeMatchMap;
    
    public static void init(){
        if(isInit){
            return;
        }
        isInit= true;
        final Map<String, HumenNodeMatchInterface> initsMap = AppContext.getBeansOfType(HumenNodeMatchInterface.class);
        if(initsMap==null || initsMap.isEmpty()){
            return;
        }
        humenNodeMatchMap = new HashMap<String, HumenNodeMatchInterface>();
        for(Map.Entry<String, HumenNodeMatchInterface> entry:initsMap.entrySet()){
            if(entry==null){
                continue;
            }
            HumenNodeMatchInterface hastenManager = entry.getValue();
            String typeIdStr= hastenManager.getTypeId();
            logger.info(typeIdStr+";"+hastenManager);
            String[] typeIdArr= typeIdStr.split(",");
            for (String typeId : typeIdArr) {
            	humenNodeMatchMap.put(typeId, hastenManager);
			}
        }
    }
    
    public static HumenNodeMatchInterface getHumenNodeMatchInterface(String partyId){
    	if(humenNodeMatchMap != null){
    		return humenNodeMatchMap.get(partyId);
    	}
    	return null;
    }
}
