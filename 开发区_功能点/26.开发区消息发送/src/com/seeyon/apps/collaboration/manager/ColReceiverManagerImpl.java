/**  
 * All rights Reserved, Designed By www.seeyon.com
 * @Title:  ColReceiveManagerImpl.java   
 * @Package com.seeyon.apps.collaboration.manager     
 * @date:   2019年4月26日  
 * @author: yaodj    
 * @version v7.1 sp1 
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.dao.ColReceiverDao;
import com.seeyon.apps.collaboration.po.ColReceiver;
import com.seeyon.apps.collaboration.vo.ColReceiverVO;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;

/**
 * <pre>
 * 协同接收信息Service层实现类
 * </pre>   
 * @ClassName:  ColReceiverManagerImpl      
 * @date:   2019年4月26日    
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
public class ColReceiverManagerImpl implements ColReceiverManager {

    private static Log LOG = CtpLogFactory.getLog(ColReceiverManagerImpl.class);
    private static final String RECEIVE_NODE_NAME        = "nodeName";                   // 接收名称
    private static final String RECEIVE_NODE_ID     = "nodeId";                     // 接收节点ID
    private static final String RECEIVE_ORG_TYPE        = "actorTypeId";                // 接收者类型
    private static final String RECEIVE_ORG_ID      = "actorPartyId";               // 接收者组织ID
    private static final String RECEIVE_ACCOUNT_ID  = "actorPartyAccountId";        // 参与者账户标识
    
    private ColReceiverDao colReceiverDao;
    
    public void setColReceiverDao(ColReceiverDao colReceiverDao) {
        this.colReceiverDao = colReceiverDao;
    }

    /**     
     * @see com.seeyon.apps.collaboration.manager.ColReceiverManager#saveColReceivers(java.lang.Long, java.util.List)   
     */
    @Override
    public void saveColReceivers(Long objectId, List<Map<String, String>> receiverInfos) {
        
        // 如果传入的列表信息为空，不需要保存，直接返回
        if(Strings.isEmpty(receiverInfos)) {
            return;
        }
        
        List<ColReceiver> receivers = buildColReceivers(objectId,receiverInfos);
        
        // 保存数据
        colReceiverDao.saveColReceivers(receivers);
    }

    /**     
     * @see com.seeyon.apps.collaboration.manager.ColReceiverManager#saveColReceiversByVO(java.lang.Long, java.util.List)   
     */  
    @Override
    public void saveColReceiversByVO(Long objectId, List<ColReceiverVO> receiverInfos) {
     // 如果传入的列表信息为空，不需要保存，直接返回
        if(Strings.isEmpty(receiverInfos)) {
            return;
        }
        
        List<ColReceiver> receivers = buildColReceiversByVO(objectId,receiverInfos);
        
        // 保存数据
        colReceiverDao.saveColReceivers(receivers);
    }

    /** 
     * <pre>
     * 根据receiveInfos信息，构建ColReceive实例列表
     *     
     * @param processId
     * @param receiverInfos
     * @return      
     * @return: List<ColReceive>  
     * @date:   2019年4月26日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    private List<ColReceiver> buildColReceivers(Long objectId, List<Map<String, String>> receiverInfos) {
        List<ColReceiver> receivers = new ArrayList<ColReceiver>();
        int sort = 1;
        for (Map<String, String> info : receiverInfos) {
            ColReceiver receive = new ColReceiver();
            
            receive.setNewId();
            receive.setObjectId(objectId);
            receive.setNodeName(info.get(RECEIVE_NODE_NAME));
            receive.setOrgType(info.get(RECEIVE_ORG_TYPE));
            receive.setNodeId(Long.valueOf(info.get(RECEIVE_NODE_ID)));
            Long orgId = null; // orgId 有可能为Sender
            if(Strings.isDigits(info.get(RECEIVE_ORG_ID))) {
                orgId = Long.valueOf(info.get(RECEIVE_ORG_ID));
            }
            receive.setOrgId(orgId); 
            receive.setAccountId(Strings.isNotBlank(info.get(RECEIVE_ACCOUNT_ID)) && Strings.isDigits(info.get(RECEIVE_ACCOUNT_ID)) ? Long.valueOf(info.get(RECEIVE_ACCOUNT_ID)) : null);
            receive.setSort(sort++);
            receive.setCreateTime(new Date());
            
            receivers.add(receive);
        }
        
        return receivers;
    }
    
    /** 
     * <pre>
     * 根据receiveInfos信息，构建ColReceive实例列表
     *     
     * @param objectId
     * @param receiverInfos
     * @return      
     * @return: List<ColReceive>  
     * @date:   2019年5月5日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    private List<ColReceiver> buildColReceiversByVO(Long objectId, List<ColReceiverVO> receiverInfos) {
        List<ColReceiver> receivers = new ArrayList<ColReceiver>();
        int sort = 1;
        for (ColReceiverVO info : receiverInfos) {
            ColReceiver receive = new ColReceiver();
            
            receive.setNewId();
            receive.setObjectId(objectId);
            receive.setNodeName(info.getNodeName());
            receive.setOrgType(info.getOrgType());
            receive.setNodeId(Long.valueOf(info.getNodeId()));
            Long orgId = null; // orgId 可能为 Sender
            if(Strings.isDigits(info.getOrgId())) {
                orgId = Long.valueOf(info.getOrgId());
            }
            receive.setOrgId(orgId);
            receive.setAccountId(Strings.isNotBlank(info.getAccountId()) && Strings.isDigits(info.getAccountId()) ? Long.valueOf(info.getAccountId()) : null);
            receive.setSort(sort++);
            receive.setCreateTime(new Date());
            
            receivers.add(receive);
        }
        
        return receivers;
    }

    /**     
     * @see com.seeyon.apps.collaboration.manager.ColReceiverManager#getColReceivers(java.lang.Long)   
     */
    @Override
    public List<ColReceiver> getColReceivers(Long objectId) {
        return colReceiverDao.queryColReceiverByObjectId(objectId);
    }

    /**     
     * @see com.seeyon.apps.collaboration.manager.ColReceiverManager#deleteColReceives(java.lang.Long)   
     */  
    @Override
    public int deleteColReceiversByObjectId(Long objectId) {
        return colReceiverDao.deleteByObjectId(objectId);
    }

    
    /**     
     * @see com.seeyon.apps.collaboration.manager.ColReceiverManager#deleteColReceiversBeforeDate(java.util.Date)   
     */  
    @Override
    public int deleteColReceiversBeforeDate(Date date) {
        int count =  colReceiverDao.deleteColReceiversBeforeDate(date);
        LOG.info("删除时间点  " + DateUtil.formatDateTime(date) + " 以前的接收者信息条数：" + count);
        return count;
    }

    /**     
     * @see com.seeyon.apps.collaboration.manager.ColReceiverManager#updateColReceivers(java.lang.Long, java.util.List)   
     */  
    @Override
    public void updateColReceivers(Long objectId, List<Map<String, String>> receiverInfos) {
        // 先删除旧数据
        this.deleteColReceiversByObjectId(objectId); 
        // 再新增现在的数据
        this.saveColReceivers(objectId, receiverInfos);
    }
}
