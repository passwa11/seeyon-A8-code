package com.seeyon.v3x.exchange.dao;

import java.util.List;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;

public class EdocExchangeTurnRecDao extends BaseHibernateDao<EdocExchangeTurnRec>{

	public EdocExchangeTurnRec findEdocExchangeTurnRecByEdocId(long edocId){
		String hsql = "from EdocExchangeTurnRec as a where a.edocId=?";
		List<EdocExchangeTurnRec> list = super.findVarargs(hsql, edocId);
        if(null != list && list.size() > 0)
            return list.get(0);
        else
            return null;
	}
	
	/**
	 * 下级单位文单获得 对应的上级单位文单
	 */
	public Long findSupEdocId(long distributeEdocId){
		Long supEdocId = null;
		String hql = " select b.edocId from EdocRegister a, EdocRecieveRecord b,EdocExchangeTurnRec c "+
					" where a.recieveId = b.id and a.distributeEdocId = ? and b.edocId = c.edocId "+
					" and b.isTurnRec = ?";
		
		List<Long> list = super.findVarargs(hql, distributeEdocId, 1);
		if(null != list && list.size() > 0)
            supEdocId = list.get(0);
        return supEdocId;
	}

	public void delTurnRecByEdocId(long edocId) {
		String hql = "delete from EdocExchangeTurnRec where edocId = ? ";
		super.bulkUpdate(hql, null,edocId);
		
	}
}
