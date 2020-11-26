/**  
 * All rights Reserved, Designed By www.seeyon.com
 * @Title:  ColReceiveDao.java   
 * @Package com.seeyon.apps.collaboration.dao   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @date:   2019年4月26日 上午11:25:39 
 * @author: yaodj    
 * @version v7.1 sp1 
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
package com.seeyon.apps.collaboration.dao;

import java.util.Date;
import java.util.List;

import com.seeyon.apps.collaboration.po.ColReceiver;

/**   
 * @ClassName:  ColReceiverDao   
 * @Description: 协同接收者DAO   
 * @date:   2019年4月26日 上午11:25:39   
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
public interface ColReceiverDao {

 
	/** 
	 * <pre>
	 * 保存
	 * </pre>  
	 * @param colReceiver  需要保存的数据
	 * @return: void  
	 * @date:   2019年4月26日 
	 * @author: yaodj
	 * @since   v7.1 sp1	       
	 */ 
	public void saveColReceiver(ColReceiver colReceiver);
	
	/** 
	 * <pre>
	 * 保存列表
	 * </pre>  
	 * @param colReceivers 需要保存的列表  
	 * @return: void  
	 * @date:   2019年4月26日 
	 * @author: yaodj
	 * @since   v7.1 sp1	       
	 */ 
	public void saveColReceivers(List<ColReceiver> colReceivers);
	
	/** 
	 * <pre>
	 * 根据ID获取协同接收者数据
	 * </pre>  
	 * @param id 主键ID
	 * @return      
	 * @return: ColReceiver 
	 * @date:   2019年4月26日 
	 * @author: yaodj
	 * @since   v7.1 sp1	
	 */ 
	public ColReceiver getColReceiverById(Long id);
	
	/**
	 * 
	 * <pre>
	 * 根据objectId获取协同接收者数据
	 * </pre>  
	 * @param objectId 流程事项id, 对应col_summary表的ID标识
	 * @return      
	 * @return: List<ColReceive>  
	 * @date:   2019年4月26日 
	 * @author: yaodj
	 * @since   v7.1 sp1
	 */
	public List<ColReceiver> queryColReceiverByObjectId(Long objectId);
	
	/** 
	 * <pre>
	 * 根据标识ID删除数据
	 * </pre>  
	 * @param id 主键ID  
	 * @return: void  
	 * @date:   2019年4月26日 
	 * @author: yaodj
	 * @since   v7.1 sp1	
	 */ 
	public int deleteById(Long id);
	
	/** 
	 * <pre>
	 * 根据objectId删除数据
	 * </pre>  
	 * @param objectId  流程事项id, 对应col_summary表的ID标识
	 * @return: void  
	 * @date:   2019年4月26日 
	 * @author: yaodj
	 * @since   v7.1 sp1	
	 */ 
	public int deleteByObjectId(Long objectId);
	
	/** 
	 * <pre>
	 *     删除指定时间以前的数据
	 *     
	 * @param date 指定时间
	 * @return: int   删除的数量
	 * @date:   2019年5月9日 
	 * @author: yaodj
	 * @since   v7.1 sp1	
	 * </pre> 
	 */ 
	public int deleteColReceiversBeforeDate(Date date);
}
