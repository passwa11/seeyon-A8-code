/**  
 * All rights Reserved, Designed By www.seeyon.com
 * @Title:  ColReceive.java   
 * @Package com.seeyon.apps.collaboration.po   
 * @Description:    协同接收者PO类   
 * @date:   2019年4月26日 上午10:52:17 
 * @author: yaodj    
 * @version v7.1 sp1 
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
package com.seeyon.apps.collaboration.po;

import java.util.Date;

import com.seeyon.ctp.common.po.BasePO;

/**   
 * @ClassName:  ColReceive   
 * @Description: 协同接收者PO类 ,table=COL_RECEIVE
 * @date:   2019年4月26日 上午10:52:17   
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
public class ColReceiver extends BasePO {

	private static final long serialVersionUID = 9080822092020410563L;
	
	/**   
	 * @Fields name : 节点名称   
	 */  
	private String nodeName;
	
	/**   
	 * @Fields orgId : 组织机构标识   
	 */  
	private Long orgId;
	
	/**   
	 * @Fields type : 组织机构类型  
	 */  
	private String orgType;
	
	/**   
	 * @Fields nodeId : 接收节点标识   
	 */  
	private Long nodeId;
	
	/**   
	 * @Fields accountId : 单位ID   
	 */  
	private Long accountId;
	
	/**   
	 * @Fields objectId : summaryID   
	 */  
	private Long objectId;
	
	/**   
	 * @Fields orderNum : 排序号，同一个objectId的数据接收者具有顺序特征   
	 */  
	private Integer sort;
	
	/**   
	 * @Fields createTime : 创建时间   
	 */  
	private Date createTime;


    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
