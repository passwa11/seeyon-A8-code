package com.seeyon.v3x.edoc.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;

import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocElementFlowPermAcl;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocElementFlowPermAclDao extends BaseHibernateDao<EdocElementFlowPermAcl>{

	private static final Log log = LogFactory.getLog(EdocElementFlowPermAclDao.class);
	
	public void saveEdocElementFlowPermAclDao(EdocElementFlowPermAcl edocElementFlowPermAcl){
		log.debug("saving V3xEdocElementFlowPermAcl instance");
		try{
			super.save(edocElementFlowPermAcl);
			log.debug("save sucesslly");
		}catch(RuntimeException re){
			log.error("save failed", re);
			throw re;
		}
	}
	
	public void saveBatchEdocElementFlowPermAcl(List<EdocElementFlowPermAcl> list){
		log.debug("saving V3xEdocElementFlowPermAcl list instance");
		try{
			DBAgent.saveAll(list);
			log.debug("save sucesslly");
		}catch(RuntimeException re){
			log.error("save failed", re);
			throw re;
		}
	}
	
	
	public List getEdocElementFlowPermAcls(Long flowPermId,boolean loadElement){
		try{
		String queryString = "from EdocElementFlowPermAcl as flowPermAcl" + (loadElement?" inner join fetch flowPermAcl.edocElement":"") + " where flowPermAcl.flowPermId = ? ";
		Object[] values = {flowPermId};
		return super.findVarargs(queryString, values);		
		}catch(RuntimeException re){
			log.debug("find by flowPermId failed",re);
			throw re;
		}
	}
	
	public void updateEdocElementFlowPermAcl(EdocElementFlowPermAcl acl){
		try{
			super.update(acl);
			log.debug("update sucesslly");
		}catch(RuntimeException re){
			log.error("update failed", re);
			throw re;
		}
	}
	public EdocElementFlowPermAcl getEdocElementFlowPermAcl(Long id){
		try{
			String queryString = "from EdocElementFlowPermAcl as flowPermAcl where flowPermAcl.id = ? ";
			Object[] values = {id};
			List<EdocElementFlowPermAcl> list  = super.findVarargs(queryString, values);
			if(list.size()==0){
				 return null;
			}
			else{
				return list.get(0);
			}
			}catch(RuntimeException re){
				log.debug("find by flowPermId failed",re);
				throw re;
			}	
	}
	public void deleteByFlowPermId(Long flowPermId){
		try{
			String queryString = "delete from EdocElementFlowPermAcl as flowPermAcl where flowPermAcl.flowPermId = ? ";
			super.bulkUpdate(queryString, null, flowPermId);
		}catch(RuntimeException re){
			log.debug("delete by flowPermId failed",re);
			throw re;
		}
	}
}
