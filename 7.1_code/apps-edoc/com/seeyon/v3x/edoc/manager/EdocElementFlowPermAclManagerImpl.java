package com.seeyon.v3x.edoc.manager;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.v3x.edoc.dao.EdocElementFlowPermAclDao;
import com.seeyon.v3x.edoc.domain.EdocElementFlowPermAcl;

public class EdocElementFlowPermAclManagerImpl implements EdocElementFlowPermAclManager{
	
	private EdocElementFlowPermAclDao edocElementFlowPermAclDao;
	private static Log LOG = CtpLogFactory.getLog(EdocElementFlowPermAclManagerImpl.class);
	
	public EdocElementFlowPermAclDao getEdocElementFlowPermAclDao() {
		return edocElementFlowPermAclDao;
	}

	public void setEdocElementFlowPermAclDao(
			EdocElementFlowPermAclDao edocElementFlowPermAclDao) {
		this.edocElementFlowPermAclDao = edocElementFlowPermAclDao;
	}



	public void saveEdocElementFlowPermAcls(List<EdocElementFlowPermAcl> list){
		edocElementFlowPermAclDao.saveBatchEdocElementFlowPermAcl(list);
	}
	
	public void updateEdocElementFlowPermAcl(Long id,Integer access){
    	EdocElementFlowPermAcl acl = edocElementFlowPermAclDao.get(id);
    	acl.setAccess(access);

	}
	
	public List<EdocElementFlowPermAcl> getEdocElementFlowPermAcls(Long flowPermId){
		
		
		List<EdocElementFlowPermAcl> list =  edocElementFlowPermAclDao.getEdocElementFlowPermAcls(flowPermId,false);
		
		if(null!=list && list.size()>0){
			return list;
		}else{
			return null;
		}
	}
	
	public Hashtable<Long,EdocElementFlowPermAcl> getEdocElementFlowPermAclsHs(Long flowPermId)
	{		
		Hashtable<Long,EdocElementFlowPermAcl> hs=new Hashtable();
		//以前节点权限传递负数(一般传递-1)为默认知道权限;集团后后,节点权限可以出现负值了;
		if(flowPermId<0 && flowPermId>-100){return hs;}
		List ls=edocElementFlowPermAclDao.getEdocElementFlowPermAcls(flowPermId,true);
		EdocElementFlowPermAcl eac=null;
		for(Object obj:ls)
		{
			eac=(EdocElementFlowPermAcl)obj;
			//hs.put(eac.getEdocElement().getId(),eac);
			//多套公文元素错误数据导至错误
			try{
			hs.put(Long.parseLong(eac.getEdocElement().getElementId()),eac);
			}catch(Exception e)
			{
			    LOG.error("", e);
			}
		}
		return hs;
	}
	
	public EdocElementFlowPermAcl getEdocElementFlowPermAcl(Long id){
		return edocElementFlowPermAclDao.get(id);
	}
	public void deleteEdocElementFlowPermAcl(Long flowPermId){
		edocElementFlowPermAclDao.deleteByFlowPermId(flowPermId);
	}
}
