package com.seeyon.v3x.edoc.dao;

import java.util.List;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocFormFlowPermBound;

public class EdocFormFlowPermBoundDao extends BaseHibernateDao<EdocFormFlowPermBound>{

	
	public void deleteFormFlowPermBoundByFormId(Long formId)
	{
		String hsql = "delete from EdocFormFlowPermBound as a where a.edocFormId = ? ";
		Object[] values = new Object[]{formId};
		super.bulkUpdate(hsql, null, values);
	}
	public void deleteFormFlowPermBoundByFormId(Long formId,long accountId)
	{
		String hsql = "delete from EdocFormFlowPermBound as a where a.edocFormId = ? and  domainId=?";
		Object[] values = new Object[]{formId,accountId};
		super.bulkUpdate(hsql, null, values);
	}
	/**
	 * 性能优化：查询文单中哪些 意见框绑定了意见
	 * @param formId
	 * @param accountId
	 * @return
	 */
	public List<EdocFormFlowPermBound> findBoundProcessByFormId(Long formId,long accountId)
    {
        String hsql = "from EdocFormFlowPermBound as a where a.edocFormId = ? and  domainId=?";
        Object[] values = new Object[]{formId,accountId};
        return super.find(hsql, -1,-1,null, values);
    }
}
