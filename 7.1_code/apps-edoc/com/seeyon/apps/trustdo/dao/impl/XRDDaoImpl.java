package com.seeyon.apps.trustdo.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.trustdo.dao.XRDDao;
import com.seeyon.apps.trustdo.po.XRDUserPO;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

@SuppressWarnings("unchecked")
public class XRDDaoImpl extends BaseHibernateDao<XRDUserPO> implements XRDDao{
	
	private static final Log LOGGER = CtpLogFactory.getLog(XRDDaoImpl.class);

	@Override
	public void save(com.seeyon.apps.trustdo.po.XRDUserPO xrdPo) {
		 DBAgent.saveOrUpdate(xrdPo);
	}

	@Override
	public void del(com.seeyon.apps.trustdo.po.XRDUserPO xrdPo) {
		DBAgent.delete(xrdPo);
	}

	@Override
	public com.seeyon.apps.trustdo.po.XRDUserPO getOneByid(Long id) {
		com.seeyon.apps.trustdo.po.XRDUserPO xo = DBAgent.get(com.seeyon.apps.trustdo.po.XRDUserPO.class, id);
		return xo;
	}

	@Override
	public List<com.seeyon.apps.trustdo.po.XRDUserPO> getAll(Map<String, Object> conditions, FlipInfo fi) {
		String seeyonLoginNameCondition = (String)conditions.get("seeyonLoginName");
		String seeyonUserDeptCondition = (String)conditions.get("seeyonUserDept");
		String seeyonUserNameCondition = (String)conditions.get("seeyonUserName");
		StringBuilder hql = new StringBuilder("from XRDUserPO as xup WHERE 1=1");
		if (Strings.isNotBlank(seeyonLoginNameCondition)) {
			hql.append(" and xup.seeyonLoginName like '%"+seeyonLoginNameCondition+"%' ");
		}
		if (Strings.isNotBlank(seeyonUserDeptCondition)) {
			hql.append(" and xup.seeyonUserDept like '%"+seeyonUserDeptCondition+"%' ");
		}
		if (Strings.isNotBlank(seeyonUserNameCondition)) {
			hql.append(" and xup.seeyonUserName like '%"+seeyonUserNameCondition+"%' ");
		}
		hql.append(" ORDER BY xup.bindTime desc ");
		Map<String,Object> map = new HashMap<String,Object>();
		List<com.seeyon.apps.trustdo.po.XRDUserPO> result = DBAgent.find(hql.toString(), map, fi);
		return result;
	}
	
	@Override
	public List<com.seeyon.apps.trustdo.po.XRDUserPO> getOneByTrustdoAccount(String account) {
		Map<String,Object> param = new HashMap<String,Object>();
		StringBuilder hql = new StringBuilder("from XRDUserPO as xup WHERE 1=1");
		hql.append(" AND xup.trustdoAccount=:account");
		param.put("account", account);
		LOGGER.debug(hql.toString());
	    List<com.seeyon.apps.trustdo.po.XRDUserPO> result = DBAgent.find(hql.toString(),param);
		return result;
	}
}
