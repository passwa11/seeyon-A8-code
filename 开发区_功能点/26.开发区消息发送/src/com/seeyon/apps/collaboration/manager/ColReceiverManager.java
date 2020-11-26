/**  
 * All rights Reserved, Designed By www.seeyon.com
 * @Title:  ColReceviceManager.java   
 * @Package com.seeyon.apps.collaboration.manager     
 * @date:   2019年4月26日  
 * @author: yaodj    
 * @version v7.1 sp1 
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
package com.seeyon.apps.collaboration.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.po.ColReceiver;
import com.seeyon.apps.collaboration.vo.ColReceiverVO;

/**
 * <pre>
 *  协同接收信息Service层接口
 * </pre>   
 * @ClassName:  ColRecevicerManager      
 * @date:   2019年4月26日    
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
public interface ColReceiverManager {

    /** 
     * <pre>
     * 保存接收者信息
     * @param objectId summary ID
     * @param receiverInfos 接收者信息列表 
     *        map 里面字段：
     *          nodeId 接收节点ID、
     *          nodeName 接收节点名称、
     *          actorTypeId 接收者类型、
     *          actorPartyId 接收者标识、
     *          actorPartyAccountId 参与者账户标识
     * @return: void  
     * @date:   2019年4月26日 
     * @author: yaodj
     * @since   v7.1 sp1
     * </pre>  	
     */ 
    public void saveColReceivers(Long objectId,List<Map<String,String>> receiverInfos);
    
    /** 
     * <pre>
     *  保存接收者信息
     *     
     * @param objectId  summary ID
     * @param receiverInfos 接收者信息列表      
     * @return: void  
     * @date:   2019年5月5日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    public void saveColReceiversByVO(Long objectId,List<ColReceiverVO> receiverInfos);
    
    /** 
     * <pre>
     * 根据流程ID获取接收者信息
     * </pre>    
     * @param processId 流程ID
     * @return      
     * @return: List<ColReceive>  
     * @date:   2019年4月26日 
     * @author: yaodj
     * @since   v7.1 sp1	
     */ 
    public List<ColReceiver> getColReceivers(Long objectId);
    
    /**
     * 
     * <pre>
     * 根据流程ID删除接收者信息
     *     
     * @param processId 流程ID
     * @return      
     * @return: int  
     * @date:   2019年4月29日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre>
     */
    public int deleteColReceiversByObjectId(Long objectId);
    
    /** 
     * <pre>
     *              删除某个日期以前的数据
     *     
     * @param date    指定日期  
     * @return: int   删除的数量
     * @date:   2019年5月9日 
     * @author: yaodj
     * @since   v7.1 sp1    
     * </pre> 
     */ 
    public int deleteColReceiversBeforeDate(Date date);
    
    /** 
     * <pre>
     * 更新接收者信息，实际采用的是先删除后插入保存顺序和信息一致
     * @param processId 流程ID
     * @param receiverInfos 接收者信息列表 
     *        map 里面字段：
     *          nodeId 接收节点ID、
     *          nodeName 接收节点名称、
     *          actorTypeId 接收者类型、
     *          actorPartyId 接收者标识、
     *          actorPartyAccountId 参与者账户标识
     *     
     * @param objectId  summary Id
     * @param receiverInfos   接收者信息 
     * @return: void  
     * @date:   2019年4月29日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    public void updateColReceivers(Long objectId, List<Map<String,String>> receiverInfos);
    
}
