/**
 * EdocElementDao.java
 * Created on 2007-4-19
 */
package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.v3x.edoc.domain.EdocElement;

/**
 *
 * @author <a href="mailto:handy@seeyon.com">Han Dongyou</a>
 *
 */
public class EdocElementDao extends BaseHibernateDao<EdocElement>
{     
    
    public List<EdocElement> getAllEdocElements()
    {
        String hsql = "from EdocElement as a order by a.isSystem desc, a.type, a.elementId ";
        //return super.find(hsql);
        List ls=null;
        try{
        ls=super.findVarargs(hsql);
        }catch(Exception e)
        {
			logger.error(e.getMessage(), e);
        }
        return ls;
    }
    
    public EdocElement getEdocElementsById(long id){
    	return super.get(id);
    }
    
    public void deleteEdocElementsByDomainId(Long domainId){
    	 String hsql = "delete from EdocElement where domainId = ? ";
    	 super.bulkUpdate(hsql, null, new Object[]{domainId});
    }
    
    @SuppressWarnings("unchecked")
	public List<EdocElement> getEdocElementListByDomainId(Long domainId) {
    	Map<String, Object> paramterMap = new HashMap<String, Object>();
    	paramterMap.put("domainId", domainId);
    	return (List<EdocElement>)super.find("from EdocElement where domainId = :domainId order by status desc, isSystem desc, elementId asc", -1, -1, paramterMap);
    }
    public int countEdocElementsFromDB(Long domainId) {
    	Map<String, Object> paramterMap = new HashMap<String, Object>();
    	paramterMap.put("domainId", domainId);
    	return DBAgent.count("from EdocElement where domainId = :domainId order by status desc, isSystem desc, elementId asc" ,paramterMap);
    }
}