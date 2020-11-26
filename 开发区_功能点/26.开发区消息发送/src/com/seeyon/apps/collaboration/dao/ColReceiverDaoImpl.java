/**  
 * All rights Reserved, Designed By www.seeyon.com
 * @Title:  ColReceiveDaoImpl.java   
 * @Package com.seeyon.apps.collaboration.dao   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @date:   2019年4月26日 上午11:30:50 
 * @author: yaodj    
 * @version v7.1 sp1 
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
package com.seeyon.apps.collaboration.dao;

import com.seeyon.apps.collaboration.po.ColReceiver;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.util.DBAgent;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**   
 * @ClassName:  ColReceiverDaoImpl   
 * @Description:    
 * @date:   2019年4月26日 上午11:30:50   
 * @author: yaodj
 * @since：     v7.1 sp1    
 * @Copyright: 2019 www.seeyon.com Inc. All rights reserved. 
 */
public class ColReceiverDaoImpl extends BaseHibernateDao<ColReceiver> implements ColReceiverDao {

	@Override
	public void saveColReceiver(ColReceiver colReceiver) {

	}

	@Override
	public void saveColReceivers(List<ColReceiver> colReceivers) {

	}

	@Override
	public ColReceiver getColReceiverById(Long id) {
		return null;
	}

	@Override
	public List<ColReceiver> queryColReceiverByObjectId(Long objectId) {
		return null;
	}

	@Override
	public int deleteById(Long id) {
		return 0;
	}

	@Override
	public int deleteByObjectId(Long objectId) {
		return 0;
	}

	@Override
	public int deleteColReceiversBeforeDate(Date date) {
		return 0;
	}
}
