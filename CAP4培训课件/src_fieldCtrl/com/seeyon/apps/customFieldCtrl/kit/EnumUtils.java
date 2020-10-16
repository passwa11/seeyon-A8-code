package com.seeyon.apps.customFieldCtrl.kit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;

/**
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public class EnumUtils {
    
    private static final Log LOGGER = CtpLogFactory.getLog(EnumUtils.class);

    /** 枚举-枚举项 映射Map */
    private static Map<Long, List<CtpEnumItem>> enumMap = new HashMap<Long, List<CtpEnumItem>>();

    /** 单位所有枚举 */
    private static List<CtpEnumBean> ctpEnumList;

    /** 枚举管理 */
    private static EnumManager enumManager = (EnumManager)AppContext.getBean("enumManagerNew");
    
    /**
     * 根据枚举ID 获取枚举key
     * @param enumItemId 枚举ID
     * @return 枚举显示值
     */
    public static String getEnumItemKey(Long enumItemId) throws BusinessException {
    	CtpEnumItem item = enumManager.getEnumItem(enumItemId);
		if(null != item) {
			return item.getValue();
		}
		LOGGER.info("没有获取到枚举:" + enumItemId);
        return "";
    }
    
    public static CtpEnumItem getEnumItem(Long enumItemId) {
    	CtpEnumItem item = enumManager.getEnumItem(enumItemId);
		if(null != item) {
			return item;
		}
		LOGGER.info("没有获取到枚举:" + enumItemId);
        return null;
    }

    /**
     * 根据枚举ID 获取枚举显示值
     * @param enumItemId 枚举ID
     * @return 枚举显示值
     */
    public static String getEnumItemName(String enumItemId) {
        if(null == enumItemId || "".equals(enumItemId)) {
            return "";
        }
        String enumItemName = "";
        try {
            enumItemName = enumManager.getEItemNameById(Long.valueOf(enumItemId));
        } catch(BusinessException e) {
            LOGGER.error("获取枚举错误:", e);
        }
        return enumItemName;
    }

    /**
     * 根据枚举类型名及枚举显示值 获取枚举项的ID
     * @param enumTypeName 枚举类型名
     * @param enumItemName 枚举值
     * @return 枚举ID
     * @throws Exception 异常
     */
    public static Long getEnumItemId(String enumTypeName, String enumItemName) {
        try {
            // 查询单位所有枚举
            ctpEnumList = enumManager.getAllOrgCtpEnum();
            for(CtpEnumBean enumBean : ctpEnumList) {
                if(enumTypeName.equals(enumBean.getLabel())) {
                    if(!enumMap.containsKey(enumBean.getId())) {
                        enumMap.put(enumBean.getId(), enumManager.getEmumItemByEmumId(enumBean.getId()));
                    }
                    for(CtpEnumItem item : enumMap.get(enumBean.getId())) {
                        if(enumItemName.equals(item.getShowvalue())) {
                            return item.getId();
                        }
                    }
                }
            }
            String errorInfo = "枚举类型：" + enumTypeName + "下面没有枚举值为[" + enumItemName + "] 的选项";
            throw new Exception(errorInfo);
        } catch(Exception e) {
            LOGGER.error("获取枚举错误:", e);
        }
        // 先解决一个BUG，返回空的话查询出错
        return 0L;
    }
    
    public static String getEnumItemkey(String enumTypeName, String enumItemName) {
        try {
            // 查询单位所有枚举
            ctpEnumList = enumManager.getAllOrgCtpEnum();
            for(CtpEnumBean enumBean : ctpEnumList) {
                if(enumTypeName.equals(enumBean.getLabel())) {
                    if(!enumMap.containsKey(enumBean.getId())) {
                        enumMap.put(enumBean.getId(), enumManager.getEmumItemByEmumId(enumBean.getId()));
                    }
                    for(CtpEnumItem item : enumMap.get(enumBean.getId())) {
                        if(enumItemName.equals(item.getShowvalue())) {
                            return item.getValue();
                        }
                    }
                }
            }
            String errorInfo = "枚举类型：" + enumTypeName + "下面没有枚举值为[" + enumItemName + "] 的选项";
            throw new Exception(errorInfo);
        } catch(Exception e) {
            LOGGER.error("获取枚举错误:", e);
        }
        // 先解决一个BUG，返回空的话查询出错
        return "";
    }
    
    /**
     * 根据枚举类型名获取枚举
     * @param enumTypeName 枚举类型名
     * @return 枚举ID
     * @throws Exception 异常
     */
    public static List<CtpEnumItem> getEnumItemId(String enumTypeName) {
        try {
            // 查询单位所有枚举
            ctpEnumList = enumManager.getAllOrgCtpEnum();
            for(CtpEnumBean enumBean : ctpEnumList) {
                if(enumTypeName.equals(enumBean.getLabel())) {
                    return enumManager.getEmumItemByEmumId(enumBean.getId());
                }
            }
        } catch(Exception e) {
            LOGGER.error("获取枚举错误:", e);
        }
        // 先解决一个BUG，返回空的话查询出错
        return null;
    }
    
    
    /**
     * 根据枚举类型名及枚举值 获取枚举项的ID
     * @param enumTypeName 枚举类型名
     * @param enumItemName 枚举值
     * @return 枚举ID
     * @throws Exception 异常
     */
    public static String getEnumItemIdByKey(String enumTypeName, String enumItemKey) {
        try {
            // 查询单位所有枚举
            // if(ctpEnumList == null) {
            ctpEnumList = enumManager.getAllOrgCtpEnum();
            // }
            for(CtpEnumBean enumBean : ctpEnumList) {
                if(enumTypeName.equals(enumBean.getLabel())) {
                    if(!enumMap.containsKey(enumBean.getId())) {
                        enumMap.put(enumBean.getId(), enumManager.getEmumItemByEmumId(enumBean.getId()));
                    }
                    for(CtpEnumItem item : enumMap.get(enumBean.getId())) {
                        if(enumItemKey.equals(item.getValue())) {
                            return String.valueOf(item.getId());
                        }
                    }
                }
            }
            String errorInfo = "枚举类型：" + enumTypeName + "下面没有枚举值为[" + enumItemKey + "] 的选项";
            throw new Exception(errorInfo);
        } catch(Exception e) {
            LOGGER.error("获取枚举错误:", e);

        }
        return null;
    }
}
