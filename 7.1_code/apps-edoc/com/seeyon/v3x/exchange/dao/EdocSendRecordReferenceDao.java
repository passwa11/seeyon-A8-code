package com.seeyon.v3x.exchange.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.exchange.domain.EdocSendRecordReference;

public class EdocSendRecordReferenceDao extends BaseHibernateDao<EdocSendRecordReference>{
	
    public EdocSendRecordReference findReferenceByNewId(long newId){
        String hql = "from EdocSendRecordReference r where r.newSendRecodId = :newSendRecodId";
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("newSendRecodId", newId);
        List<EdocSendRecordReference> refs = super.find(hql,-1,-1,parameterMap);
        if(refs.size() == 0)return null;
        return refs.get(0); 
    }
    
    public void saveEdocSendRecordReference(EdocSendRecordReference ref){
        super.save(ref);
    }
    
    public void deleteById(long id){
        super.delete(id);
    }
}
