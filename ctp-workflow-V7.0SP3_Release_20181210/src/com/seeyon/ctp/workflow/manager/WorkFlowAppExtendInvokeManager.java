/**
 * Author: wangchw
 * Rev:WorkFlowAppExtendInvokeManager.java
 * Date: 20122012-11-6下午06:12:19
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.wapi.IWorkflowNodeFormSelectItems;
import com.seeyon.ctp.workflow.wapi.SelectItemVo;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager;

/**
 * <p>Title: T4工作流</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * <p>Author: wangchw</p>
 * @since CTP2.0
*/
public class WorkFlowAppExtendInvokeManager {
    
    /**
     * 日志记录
     */
    public static final Logger logger = Logger.getLogger(WorkFlowAppExtendInvokeManager.class);
    
    /**
     * 初始化标记
     */
    private static boolean isInit= false;
    
    /**
     * 调用外部催办manager的map属性。
     */
    private static Map<String, WorkFlowAppExtendManager> hastenMap;
    
    private static Map<String, IWorkflowNodeFormSelectItems> itemManagerMap;
    
    /**
     * 应用催办管理器初始化。
     */
    public static void init(){
        if(isInit){
            return;
        }
        isInit= true;
        final Map<String, WorkFlowAppExtendManager> initsMap = AppContext.getBeansOfType(WorkFlowAppExtendManager.class);
        if(initsMap==null || initsMap.isEmpty()){
            return;
        }
        hastenMap = new HashMap<String, WorkFlowAppExtendManager>();
        for(Map.Entry<String, WorkFlowAppExtendManager> entry:initsMap.entrySet()){
            if(entry==null){
                continue;
            }
            String name = entry.getKey();
            WorkFlowAppExtendManager hastenManager = entry.getValue();
            if(hastenManager!=null && hastenManager.getAppName()!=null){
                hastenMap.put(hastenManager.getAppName().name(), hastenManager);
            } else {
                logger.info("id或name="+name+"的WorkFlowAppExtendInvokeManager初始化失败，getAppName的返回值=null");
            }
        }
        final Map<String, IWorkflowNodeFormSelectItems> initsItemMap = AppContext.getBeansOfType(IWorkflowNodeFormSelectItems.class);
        if(initsItemMap==null || initsItemMap.isEmpty()){
        	return;
        }
        itemManagerMap = new HashMap<String, IWorkflowNodeFormSelectItems>(initsItemMap.size());
        for(Map.Entry<String, IWorkflowNodeFormSelectItems> entry : initsItemMap.entrySet()){
        	if(entry==null){
        		continue;
        	}
        	String name = entry.getKey();
        	IWorkflowNodeFormSelectItems itemManager = entry.getValue();
        	if(itemManager!=null && itemManager.getType()!=null){
        		itemManagerMap.put(itemManager.getType(), itemManager);
        	}else{
        		logger.info("id或name="+name+"的IWorkflowNodeFormSelectItems初始化失败，getType的返回值=null");
        	}
        }
    }
    
    /**
     * 获得WorkFlowAppExtendManager
     * @param appName
     * @return
     */
    public static WorkFlowAppExtendManager getAppManager(String appName){
        if("form".equals(appName)){
            appName= "collaboration";
        }
        if(ApplicationCategoryEnum.edocRec.name().equals(appName)
                || ApplicationCategoryEnum.edocSend.name().equals(appName)
                || ApplicationCategoryEnum.edocSign.name().equals(appName)
                || "sendEdoc".equals(appName) 
                || "recEdoc".equals(appName)
                || "signReport".equals(appName)){//公文种类较多，兼容处理下
            appName= "edoc";
        }
        if("sendInfo".equals(appName)){
            appName= "info";
        }
        WorkFlowAppExtendManager hastenManager = null;
        if(hastenMap!=null){
            hastenManager = hastenMap.get(appName);
        }
        return hastenManager;
    }
    
    public static IWorkflowNodeFormSelectItems getSelectItemManager(String type){
    	IWorkflowNodeFormSelectItems result = null;
    	if(itemManagerMap!=null){
    		result = itemManagerMap.get(type);
    	}
    	if(result==null){
    		final Map<String, IWorkflowNodeFormSelectItems> initsItemMap = AppContext.getBeansOfType(IWorkflowNodeFormSelectItems.class);
            if(initsItemMap==null || initsItemMap.isEmpty()){
            	return result;
            }
            if(itemManagerMap==null){
	            itemManagerMap = new HashMap<String, IWorkflowNodeFormSelectItems>(initsItemMap.size());
            }
            for(Map.Entry<String, IWorkflowNodeFormSelectItems> entry : initsItemMap.entrySet()){
            	if(entry==null){
            		continue;
            	}
            	IWorkflowNodeFormSelectItems itemManager = entry.getValue();
            	if(itemManager!=null && type.equals(itemManager.getType())){
            		result = itemManager;
            		itemManagerMap.put(itemManager.getType(), itemManager);
            		break;
            	}
            }
    	}
    	return result;
    }
    
    /**
     * 根据类型查找查询或统计的文本
     * @param type
     * @param formApp
     * @param ids
     * @return
     */
    public static String getWorkflowNodePropertyBindText(String type, Long formApp, Long[] ids){
    	String result = "";
    	if(ids!=null && ids.length>0){
    		StringBuilder sb = new StringBuilder("");
	        IWorkflowNodeFormSelectItems itemManager = WorkFlowAppExtendInvokeManager.getSelectItemManager(type);
			if(itemManager!=null){
				List<SelectItemVo> items = itemManager.getDataList(formApp);
				if(items!=null && !items.isEmpty()){
					Map<Long, SelectItemVo> itemMap = new HashMap<Long, SelectItemVo>();
		    		for(SelectItemVo item : items){
		    			itemMap.put(item.getId(), item);
		    		}
		    	//	List<SelectItemVo> selectedItems = new ArrayList<SelectItemVo>();
					int index = 0;
		    		for(Long id : ids){
	    				Long key = id;
		    			SelectItemVo item = itemMap.get(key);
		    			if(item!=null){
		    			//	selectedItems.add(item);
		    				if(index>0){
								sb.append("、");
							}
							sb.append(item.getName());
							index++;
		    			}
		    		}
		    		String temp = sb.toString().trim();
		    		if(temp!=null && !"".equals(temp)){
						result = temp;
					}
				}
			}
    	}
		return result;
    }
    
    public static boolean validateFormQueryStatistics(String type, Long newFormApp, String ids){
        boolean result = true;
        if(ids!=null && !"".equals(ids.trim())){
            Long[] idLs = WorkflowUtil.stringToLongArray(ids, ",");
            IWorkflowNodeFormSelectItems itemManager = WorkFlowAppExtendInvokeManager.getSelectItemManager(type);
            if(itemManager!=null){
                boolean isNewFormBean = WorkflowFormDataMapInvokeManager.getAppManager("form").isNewFormBean(newFormApp);
                //新的查询和统计
                List<SelectItemVo> newItems = null;
                if(isNewFormBean){
                    newItems = itemManager.getDataList(null);
                }else{
                    newItems = itemManager.getDataList(newFormApp);
                }
                //不为空时，才来校验
                if(newItems!=null && !newItems.isEmpty()){
                    Map<Long, SelectItemVo> newItemMap = new HashMap<Long, SelectItemVo>();
                    for(SelectItemVo item : newItems){
                        newItemMap.put(item.getId(), item);
                    }
                    for(Long id : idLs){
                        Long key = id;
                        //根据Id找到查询或统计，没找到直接报错
                        SelectItemVo item = newItemMap.get(key);
                        if(item==null){
                            result = false;
                            break;
                        }
                    }
                }else{
                    result = false;
                }
            }
        }
        return result;
    }
    
    public static String fromOldIdToNewId(String type, Long oldFormApp, Long newFormApp, String ids){
        String result = "";
        if(ids!=null && !"".equals(ids.trim())){
            Long[] idLs = WorkflowUtil.stringToLongArray(ids, ",");
            StringBuilder sb = new StringBuilder("");
            IWorkflowNodeFormSelectItems itemManager = WorkFlowAppExtendInvokeManager.getSelectItemManager(type);
            if(itemManager!=null){
                boolean isNewFormBean = WorkflowFormDataMapInvokeManager.getAppManager("form").isNewFormBean(newFormApp);
                //老的查询和统计
                List<SelectItemVo> oldItems = itemManager.getDataList(oldFormApp);
                //新的查询和统计
                List<SelectItemVo> newItems = null;
                if(isNewFormBean){
                    newItems = itemManager.getDataList(null);
                }else{
                    newItems = itemManager.getDataList(newFormApp);
                }
                //都不为空时，才来替换
                if(oldItems!=null && !oldItems.isEmpty() && newItems!=null && !newItems.isEmpty()){
                    Map<Long, SelectItemVo> oldItemMap = new HashMap<Long, SelectItemVo>();
                    Map<String, SelectItemVo> newItemMap = new HashMap<String, SelectItemVo>();
                    for(SelectItemVo item : oldItems){
                        oldItemMap.put(item.getId(), item);
                    }
                    for(SelectItemVo item : newItems){
                        newItemMap.put(item.getName(), item);
                    }
                    int index = 0;
                    for(Long id : idLs){
                        Long key = id;
                        //根据Id找到老的查询或统计，再根据老的查询或统计的name找到新的查询或统计
                        SelectItemVo item = oldItemMap.get(key);
                        if(item!=null){
                            //如果老的查询或统计存在，并且新的查询或统计也存在，那么新的查询或统计就是我要找的结果。
                            //多个Id之间用,隔开
                            SelectItemVo newItem = newItemMap.get(item.getName());
                            if(newItem!=null){
                                if(index>0){
                                    sb.append(",");
                                }
                                sb.append(newItem.getId().toString());
                                index++;
                            }
                        }
                    }
                    String temp = sb.toString().trim();
                    if(temp!=null && !"".equals(temp)){
                        result = temp;
                    }
                }
            }
        }
        return result;
    }

}
