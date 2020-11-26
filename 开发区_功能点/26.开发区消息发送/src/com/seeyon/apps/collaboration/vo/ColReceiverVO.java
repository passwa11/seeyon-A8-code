package com.seeyon.apps.collaboration.vo;

/**
 * 
 * <pre>
 *      接收者信息VO,和PO数据结构差不多，但是分层一下，以免产生不必要的影响
 *   
 * @ClassName:  ColReceiveVO      
 * @date:   2019年5月5日    
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved.
 * </pre>
 */
public class ColReceiverVO {

    public static final String NODE_ID  = "i";
    public static final String NODE_NAME = "n";
    public static final String ACTOR_PARTY_ID = "p";
    public static final String ACTOR_TYPE_ID = "t" ;

    private String showName;    // 显示的名称
    private String nodeName;    // 接收节点名称
    private String nodeId;      // 节点标识
    private String orgId;       // 组织机构标识
    private String orgType;     // 组织机构类型
    private String accountId;   // 单位ID 
    
    
    public String getShowName() {
        return showName;
    }
    public void setShowName(String showName) {
        this.showName = showName;
    }
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    public String getOrgId() {
        return orgId;
    }
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
    public String getOrgType() {
        return orgType;
    }
    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
  
}
